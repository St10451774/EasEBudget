package com.example.testapp

import android.os.Bundle
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedAccountsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var sharedAccountDao: SharedAccountDao
    private lateinit var userDao: UserDao
    private var currentUserId: Int = -1
    private lateinit var sharedAccountsAdapter: SharedAccountsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_accounts)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        sharedAccountDao = database.sharedAccountDao()
        userDao = database.userDao()

        initializeViews()
        setupRecyclerView()
        loadSharedAccounts()
    }

    private fun initializeViews() {
        val createAccountBtn = findViewById<Button>(R.id.createSharedAccountBtn)
        val accountNameInput = findViewById<EditText>(R.id.accountNameInput)
        val accountTypeSpinner = findViewById<Spinner>(R.id.accountTypeSpinner)

        // Setup account type spinner
        val accountTypes = arrayOf("Family", "Couple", "Roommates")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountTypeSpinner.adapter = adapter

        createAccountBtn.setOnClickListener {
            val accountName = accountNameInput.text.toString().trim()
            val accountType = accountTypeSpinner.selectedItem.toString().lowercase()

            if (accountName.isEmpty()) {
                Toast.makeText(this, "Please enter account name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createSharedAccount(accountName, accountType)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.sharedAccountsRecyclerView)
        sharedAccountsAdapter = SharedAccountsAdapter { account ->
            showAccountDetails(account)
        }
        recyclerView.adapter = sharedAccountsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSharedAccounts() {
        CoroutineScope(Dispatchers.IO).launch {
            sharedAccountDao.getUserSharedAccounts(currentUserId).collect { accounts ->
                withContext(Dispatchers.Main) {
                    sharedAccountsAdapter.submitList(accounts)
                }
            }
        }
    }

    private fun createSharedAccount(name: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newAccount = SharedAccount(
                    name = name,
                    createdBy = currentUserId,
                    type = type
                )
                val accountId = sharedAccountDao.insertSharedAccount(newAccount)

                // Add creator as owner
                val ownerMember = SharedAccountMember(
                    sharedAccountId = accountId.toInt(),
                    userId = currentUserId,
                    role = "owner",
                    canAddTransactions = true,
                    canApproveTransactions = true
                )
                sharedAccountDao.addMember(ownerMember)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SharedAccountsActivity, "Shared account created successfully", Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.accountNameInput).text.clear()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SharedAccountsActivity, "Failed to create account: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAccountDetails(account: SharedAccount) {
        // Show account details dialog
        // TODO: Create SharedAccountDetailsActivity
        // For now, just show a toast
        Toast.makeText(this, "Account details: ${account.name}", Toast.LENGTH_SHORT).show()
    }
}
