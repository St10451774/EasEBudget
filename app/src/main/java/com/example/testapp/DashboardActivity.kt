package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var userDao: UserDao
    private lateinit var gamificationDao: GamificationDao
    private lateinit var transactionAdapter: TransactionAdapter
    private var currentUserId: Int = -1

    // UI components
    private lateinit var totalBalanceTextView: TextView
    private lateinit var monthlyIncomeTextView: TextView
    private lateinit var monthlyExpensesTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var transactionsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        transactionDao = database.transactionDao()
        categoryDao = database.categoryDao()
        userDao = database.userDao()
        gamificationDao = database.gamificationDao()

        initializeViews()
        setupRecyclerView()
        setupFab()
        setupBottomNavigation()
        loadDashboardData()
        initializeDefaultCategories()
    }

    private fun initializeViews() {
        totalBalanceTextView = findViewById(R.id.totalBalanceTextView)
        monthlyIncomeTextView = findViewById(R.id.monthlyIncomeTextView)
        monthlyExpensesTextView = findViewById(R.id.monthlyExpensesTextView)
        pointsTextView = findViewById(R.id.pointsTextView)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click - edit or delete
            showTransactionOptions(transaction)
        }
        transactionsRecyclerView.adapter = transactionAdapter
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupFab() {
        val fab = findViewById<FloatingActionButton>(R.id.addTransactionFab)
        fab.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val dashboardBtn = findViewById<Button>(R.id.dashboardBtn)
        val reportsBtn = findViewById<Button>(R.id.reportsBtn)
        val categoriesBtn = findViewById<Button>(R.id.categoriesBtn)
        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        dashboardBtn.setOnClickListener {
            // Already on dashboard
        }

        reportsBtn.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        categoriesBtn.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            // Load transactions
            transactionDao.getUserTransactions(currentUserId).collect { transactions ->
                transactionAdapter.submitList(transactions.take(10)) // Show last 10 transactions
                updateFinancialSummary(transactions)
            }
        }

        lifecycleScope.launch {
            // Load user points
            val userPoints = gamificationDao.getUserPoints(currentUserId)
            if (userPoints != null) {
                pointsTextView.text = "Points: ${userPoints.totalPoints}"
            } else {
                // Initialize user points if not exists
                val initialPoints = UserPoints(userId = currentUserId)
                gamificationDao.insertUserPoints(initialPoints)
                pointsTextView.text = "Points: 0"
            }
        }
    }

    private fun updateFinancialSummary(transactions: List<Transaction>) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.timeInMillis

        val monthlyIncome = transactions
            .filter { it.type == "income" && it.date >= monthStart }
            .sumOf { it.amount }

        val monthlyExpenses = transactions
            .filter { it.type == "expense" && it.date >= monthStart }
            .sumOf { it.amount }

        val totalBalance = transactions
            .sumOf { if (it.type == "income") it.amount else -it.amount }

        monthlyIncomeTextView.text = "Income: $${String.format("%.2f", monthlyIncome)}"
        monthlyExpensesTextView.text = "Expenses: $${String.format("%.2f", monthlyExpenses)}"
        totalBalanceTextView.text = "Balance: $${String.format("%.2f", totalBalance)}"
    }

    private fun showTransactionOptions(transaction: Transaction) {
        // Show dialog with options to edit or delete transaction
        // For now, just show a toast
        val message = "Transaction: ${transaction.description} - $${transaction.amount}"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun initializeDefaultCategories() {
        val existingCategories = categoryDao.getDefaultCategories()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "Food & Dining", color = "#FF6200EE", icon = "food", budgetLimit = 500.0, isDefault = true),
                Category(name = "Transportation", color = "#FF03DAC5", icon = "transport", budgetLimit = 200.0, isDefault = true),
                Category(name = "Shopping", color = "#FFCF6679", icon = "shopping", budgetLimit = 300.0, isDefault = true),
                Category(name = "Entertainment", color = "#FF3700B3", icon = "entertainment", budgetLimit = 150.0, isDefault = true),
                Category(name = "Bills & Utilities", color = "#FF018786", icon = "bills", budgetLimit = 800.0, isDefault = true),
                Category(name = "Healthcare", color = "#FFBB86FC", icon = "health", budgetLimit = 200.0, isDefault = true),
                Category(name = "Education", color = "#FF6200EE", icon = "education", budgetLimit = 100.0, isDefault = true),
                Category(name = "Savings", color = "#FF03DAC5", icon = "savings", budgetLimit = 0.0, isDefault = true),
                Category(name = "Other", color = "#FFCF6679", icon = "other", budgetLimit = 100.0, isDefault = true)
            )

            defaultCategories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }
    }
}
