package com.example.testapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var userDao: UserDao
    private lateinit var gamificationDao: GamificationDao
    private var currentUserId: Int = -1
    private var selectedCategoryId: Int = 1

    // UI components
    private lateinit var titleInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var categorySpinner: Spinner
    private lateinit var receiptButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var receiptImagePath: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Handle image selection - save to app storage and get path
            receiptImagePath = it.toString()
            receiptButton.text = "Receipt Selected"
            Toast.makeText(this, "Receipt attached", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

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
        setupCategoriesSpinner()
        setupClickListeners()
    }

    private fun initializeViews() {
        titleInput = findViewById(R.id.transactionTitleInput)
        amountInput = findViewById(R.id.transactionAmountInput)
        descriptionInput = findViewById(R.id.transactionDescriptionInput)
        typeRadioGroup = findViewById(R.id.transactionTypeRadioGroup)
        categorySpinner = findViewById(R.id.categorySpinner)
        receiptButton = findViewById(R.id.attachReceiptButton)
        saveButton = findViewById(R.id.saveTransactionBtn)
        cancelButton = findViewById(R.id.cancelBtn)
    }

    private fun setupCategoriesSpinner() {
        CoroutineScope(Dispatchers.IO).launch {
            val categories = categoryDao.getAllCategories()
            withContext(Dispatchers.Main) {
                val categoryNames = categories.map { category -> category.name }
                val adapter = ArrayAdapter(this@AddTransactionActivity, android.R.layout.simple_spinner_dropdown_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
        }
    }

    private fun setupClickListeners() {
        receiptButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            saveTransaction()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveTransaction() {
        val title = titleInput.text.toString().trim()
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in title and amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedTypeId = typeRadioGroup.checkedRadioButtonId
        val transactionType = if (selectedTypeId == R.id.incomeRadioButton) "income" else "expense"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get selected category
                val categories = categoryDao.getAllCategories()
                val selectedCategory = if (categorySpinner.selectedItemPosition < categories.size) {
                    categories[categorySpinner.selectedItemPosition]
                } else {
                    null
                }
                val categoryId = selectedCategory?.id ?: 1

                val newTransaction = Transaction(
                    userId = currentUserId,
                    categoryId = categoryId,
                    amount = amount,
                    description = description,
                    type = transactionType,
                    receiptPath = receiptImagePath
                )

                transactionDao.insertTransaction(newTransaction)
                
                // Update gamification points
                updateGamificationPoints(transactionType, amount)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Failed to add transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun updateGamificationPoints(transactionType: String, amount: Double) {
        val userPoints = gamificationDao.getUserPoints(currentUserId)
        if (userPoints != null) {
            val pointsToAdd = when {
                transactionType == "income" -> (amount / 10).toInt() // 1 point per $10 income
                transactionType == "expense" -> (amount / 5).toInt() // 1 point per $5 expense tracked
                else -> 0
            }
            
            gamificationDao.addPoints(currentUserId, pointsToAdd, System.currentTimeMillis())
            
            // Check for milestones
            checkAndAwardMilestones(userPoints.totalPoints + pointsToAdd)
        }
    }

    private suspend fun checkAndAwardMilestones(totalPoints: Int) {
        val milestones = listOf(
            Triple(100, "First Steps", "Track your first 100 points!"),
            Triple(500, "Budget Master", "Reach 500 points!"),
            Triple(1000, "Financial Guru", "Achieve 1000 points!"),
            Triple(5000, "Expert Tracker", "Become a 5000 point expert!")
        )

        milestones.forEach { (points, title, description) ->
            if (totalPoints >= points) {
                val existingMilestones = gamificationDao.getActiveMilestones(currentUserId)
                val milestoneExists = existingMilestones.any { it.title == title }
                
                if (!milestoneExists) {
                    val milestone = Milestone(
                        userId = currentUserId,
                        title = title,
                        description = description,
                        type = "points_milestone",
                        targetValue = points.toDouble(),
                        currentValue = totalPoints.toDouble(),
                        isCompleted = true,
                        rewardPoints = 50,
                        completedAt = System.currentTimeMillis()
                    )
                    gamificationDao.insertMilestone(milestone)
                    gamificationDao.addPoints(currentUserId, 50, System.currentTimeMillis())
                }
            }
        }
    }
}
