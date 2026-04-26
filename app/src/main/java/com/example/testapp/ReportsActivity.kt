package com.example.testapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var gamificationDao: GamificationDao
    private var currentUserId: Int = -1

    // UI components
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var netWorthTextView: TextView
    private lateinit var monthlyIncomeTextView: TextView
    private lateinit var monthlyExpensesTextView: TextView
    private lateinit var savingsRateTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var periodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        transactionDao = database.transactionDao()
        categoryDao = database.categoryDao()
        gamificationDao = database.gamificationDao()

        initializeViews()
        setupPeriodSpinner()
        loadReportsData()
    }

    private fun initializeViews() {
        pieChart = findViewById(R.id.pieChart)
        lineChart = findViewById(R.id.lineChart)
        netWorthTextView = findViewById(R.id.netWorthTextView)
        monthlyIncomeTextView = findViewById(R.id.monthlyIncomeTextView)
        monthlyExpensesTextView = findViewById(R.id.monthlyExpensesTextView)
        savingsRateTextView = findViewById(R.id.savingsRateTextView)
        pointsTextView = findViewById(R.id.pointsTextView)
        periodSpinner = findViewById(R.id.periodSpinner)

        // Setup charts
        setupPieChart()
        setupLineChart()

        // Export button
        val exportBtn = findViewById<Button>(R.id.exportBtn)
        exportBtn.setOnClickListener {
            showExportOptions()
        }
    }

    private fun setupPeriodSpinner() {
        val periods = arrayOf("This Month", "Last Month", "Last 3 Months", "Last 6 Months", "This Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodSpinner.adapter = adapter

        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadReportsData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
    }

    private fun loadReportsData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transactions = transactionDao.getUserTransactions(currentUserId).collect { transactionList ->
                    withContext(Dispatchers.Main) {
                        updateCharts(transactionList)
                        updateSummaryStats(transactionList)
                    }
                }

                // Load user points
                val userPoints = gamificationDao.getUserPoints(currentUserId)
                withContext(Dispatchers.Main) {
                    pointsTextView.text = "Total Points: ${userPoints?.totalPoints ?: 0}"
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportsActivity, "Error loading reports: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCharts(transactions: List<Transaction>) {
        val period = getSelectedPeriod()
        val filteredTransactions = filterTransactionsByPeriod(transactions, period)

        // Update Pie Chart - Spending by Category
        updatePieChart(filteredTransactions)

        // Update Line Chart - Spending Trends
        updateLineChart(filteredTransactions)
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        val expenses = transactions.filter { it.type == "expense" }
        val categoryTotals = mutableMapOf<Int, Double>()

        expenses.forEach { transaction ->
            categoryTotals[transaction.categoryId] = categoryTotals.getOrDefault(transaction.categoryId, 0.0) + transaction.amount
        }

        val pieEntries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        CoroutineScope(Dispatchers.IO).launch {
            categoryTotals.forEach { (categoryId, amount) ->
                val category = categoryDao.getCategoryById(categoryId)
                withContext(Dispatchers.Main) {
                    category?.let {
                        pieEntries.add(PieEntry(amount.toFloat(), it.name))
                        colors.add(android.graphics.Color.parseColor(it.color))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val pieDataSet = PieDataSet(pieEntries, "Spending by Category")
                pieDataSet.colors = colors
                pieDataSet.valueTextSize = 12f
                pieDataSet.valueTextColor = android.graphics.Color.BLACK

                val pieData = PieData(pieDataSet)
                pieChart.data = pieData
                pieChart.invalidate()
            }
        }
    }

    private fun updateLineChart(transactions: List<Transaction>) {
        val dailyTotals = mutableMapOf<Long, Double>()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        transactions.forEach { transaction ->
            val dayKey = getDayKey(transaction.date)
            val amount = if (transaction.type == "expense") -transaction.amount else transaction.amount
            dailyTotals[dayKey] = dailyTotals.getOrDefault(dayKey, 0.0) + amount
        }

        val sortedDates = dailyTotals.keys.sorted()
        val entries = sortedDates.map { date ->
            Entry(date.toFloat(), dailyTotals[date]?.toFloat() ?: 0f)
        }

        val lineDataSet = LineDataSet(entries, "Daily Balance Trend")
        lineDataSet.color = getColor(R.color.primary_color)
        lineDataSet.setCircleColor(getColor(R.color.primary_color))
        lineDataSet.lineWidth = 2f
        lineDataSet.circleRadius = 4f
        lineDataSet.valueTextSize = 10f

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun updateSummaryStats(transactions: List<Transaction>) {
        val period = getSelectedPeriod()
        val filteredTransactions = filterTransactionsByPeriod(transactions, period)

        val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpenses = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        val netWorth = totalIncome - totalExpenses
        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpenses) / totalIncome * 100) else 0.0

        netWorthTextView.text = "Net Worth: $${String.format("%.2f", netWorth)}"
        monthlyIncomeTextView.text = "Income: $${String.format("%.2f", totalIncome)}"
        monthlyExpensesTextView.text = "Expenses: $${String.format("%.2f", totalExpenses)}"
        savingsRateTextView.text = "Savings Rate: ${String.format("%.1f", savingsRate)}%"

        // Set color based on savings rate
        savingsRateTextView.setTextColor(
            when {
                savingsRate >= 20 -> getColor(R.color.success_color)
                savingsRate >= 10 -> getColor(R.color.warning_color)
                else -> getColor(R.color.error_color)
            }
        )
    }

    private fun getSelectedPeriod(): String {
        return when (periodSpinner.selectedItemPosition) {
            0 -> "this_month"
            1 -> "last_month"
            2 -> "last_3_months"
            3 -> "last_6_months"
            4 -> "this_year"
            else -> "this_month"
        }
    }

    private fun filterTransactionsByPeriod(transactions: List<Transaction>, period: String): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        return when (period) {
            "this_month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                transactions.filter { it.date >= calendar.timeInMillis }
            }
            "last_month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val lastMonthStart = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val thisMonthStart = calendar.timeInMillis
                transactions.filter { it.date >= lastMonthStart && it.date < thisMonthStart }
            }
            "last_3_months" -> {
                calendar.add(Calendar.MONTH, -3)
                transactions.filter { it.date >= calendar.timeInMillis }
            }
            "last_6_months" -> {
                calendar.add(Calendar.MONTH, -6)
                transactions.filter { it.date >= calendar.timeInMillis }
            }
            "this_year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                transactions.filter { it.date >= calendar.timeInMillis }
            }
            else -> transactions
        }
    }

    private fun getDayKey(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showExportOptions() {
        val options = arrayOf("Export as CSV", "Export as PDF", "Share Report")
        
        AlertDialog.Builder(this)
            .setTitle("Export Report")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> exportAsCSV()
                    1 -> exportAsPDF()
                    2 -> shareReport()
                }
            }
            .show()
    }

    private fun exportAsCSV() {
        Toast.makeText(this, "CSV export feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsPDF() {
        Toast.makeText(this, "PDF export feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport() {
        Toast.makeText(this, "Share report feature coming soon", Toast.LENGTH_SHORT).show()
    }
}
