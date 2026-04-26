package com.example.testapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private var currentUserId: Int = -1
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        categoryDao = database.categoryDao()
        transactionDao = database.transactionDao()

        initializeViews()
        setupRecyclerView()
        loadCategories()
    }

    private fun initializeViews() {
        val addCategoryBtn = findViewById<Button>(R.id.addCategoryBtn)
        val categoryNameInput = findViewById<EditText>(R.id.categoryNameInput)
        val budgetLimitInput = findViewById<EditText>(R.id.budgetLimitInput)

        addCategoryBtn.setOnClickListener {
            val categoryName = categoryNameInput.text.toString().trim()
            val budgetLimitStr = budgetLimitInput.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budgetLimit = budgetLimitStr.toDoubleOrNull() ?: 0.0

            createCategory(categoryName, budgetLimit)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        categoriesAdapter = CategoriesAdapter { category ->
            showCategoryOptions(category)
        }
        recyclerView.adapter = categoriesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            categoryDao.getAllCategories().collect { categories ->
                withContext(Dispatchers.Main) {
                    categoriesAdapter.submitList(categories)
                }
            }
        }
    }

    private fun createCategory(name: String, budgetLimit: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newCategory = Category(
                    name = name,
                    budgetLimit = budgetLimit,
                    isDefault = false
                )
                categoryDao.insertCategory(newCategory)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoriesActivity, "Category created successfully", Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.categoryNameInput).text.clear()
                    findViewById<EditText>(R.id.budgetLimitInput).text.clear()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoriesActivity, "Failed to create category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCategoryOptions(category: Category) {
        // Show dialog with options to edit budget or delete category
        val options = arrayOf("Edit Budget", "View Spending", "Delete Category")
        
        AlertDialog.Builder(this)
            .setTitle("Category Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editCategoryBudget(category)
                    1 -> viewCategorySpending(category)
                    2 -> deleteCategory(category)
                }
            }
            .show()
    }

    private fun editCategoryBudget(category: Category) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "New budget limit"
        input.setText(category.budgetLimit.toString())
        
        builder.setTitle("Edit Budget Limit")
        builder.setView(input)
        builder.setPositiveButton("Save") { _, _ ->
            val newLimit = input.text.toString().toDoubleOrNull()
            if (newLimit != null && newLimit >= 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    categoryDao.updateCategoryBudgetLimit(category.id, newLimit)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoriesActivity, "Budget limit updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun viewCategorySpending(category: Category) {
        CoroutineScope(Dispatchers.IO).launch {
            val expenses = transactionDao.getUserTransactionsByCategory(currentUserId, category.id)
            val totalSpent = expenses.filter { it.type == "expense" }.sumOf { it.amount }
            
            withContext(Dispatchers.Main) {
                val message = "Total spent in ${category.name}: $${String.format("%.2f", totalSpent)}\n" +
                    "Budget limit: $${String.format("%.2f", category.budgetLimit)}\n" +
                    "Remaining: $${String.format("%.2f", category.budgetLimit - totalSpent)}"
                
                AlertDialog.Builder(this@CategoriesActivity)
                    .setTitle("Category Spending")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun deleteCategory(category: Category) {
        if (category.isDefault) {
            Toast.makeText(this, "Cannot delete default categories", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete ${category.name}?")
            .setPositiveButton("Delete") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        categoryDao.deleteCategory(category)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CategoriesActivity, "Cannot delete category with existing transactions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
