package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.transactionAmount)
        private val categoryTextView: TextView = itemView.findViewById(R.id.transactionCategory)
        private val dateTextView: TextView = itemView.findViewById(R.id.transactionDate)

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.description
            categoryTextView.text = "Category: ${transaction.categoryId}" // In real app, fetch category name
            
            // Format amount with proper sign and color
            val formattedAmount = if (transaction.type == "income") {
                "+$${String.format("%.2f", transaction.amount)}"
            } else {
                "-$${String.format("%.2f", transaction.amount)}"
            }
            amountTextView.text = formattedAmount
            
            // Set color based on transaction type
            amountTextView.setTextColor(
                if (transaction.type == "income") {
                    android.graphics.Color.parseColor("#4CAF50") // Green for income
                } else {
                    android.graphics.Color.parseColor("#F44336") // Red for expense
                }
            )

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(Date(transaction.date))

            itemView.setOnClickListener {
                onTransactionClick(transaction)
            }
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}
