package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTaskActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        taskDao = database.taskDao()

        val titleInput = findViewById<EditText>(R.id.taskTitleInput)
        val descriptionInput = findViewById<EditText>(R.id.taskDescriptionInput)
        val saveBtn = findViewById<Button>(R.id.saveTaskBtn)
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTask = Task(
                title = title,
                description = description,
                userId = currentUserId
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    taskDao.insertTask(newTask)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddTaskActivity, "Task added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddTaskActivity, "Failed to add task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }
}
