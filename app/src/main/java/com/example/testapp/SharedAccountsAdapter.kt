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

class SharedAccountsAdapter(
    private val onAccountClick: (SharedAccount) -> Unit
) : ListAdapter<SharedAccount, SharedAccountsAdapter.SharedAccountViewHolder>(SharedAccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedAccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_account, parent, false)
        return SharedAccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: SharedAccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SharedAccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.accountNameTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.accountTypeTextView)
        private val createdDateTextView: TextView = itemView.findViewById(R.id.createdDateTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(account: SharedAccount) {
            nameTextView.text = account.name
            typeTextView.text = "Type: ${account.type.replaceFirstChar { it.uppercase() }}"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            createdDateTextView.text = "Created: ${dateFormat.format(Date(account.createdAt))}"
            
            statusTextView.text = if (account.isActive) "Active" else "Inactive"
            statusTextView.setTextColor(
                if (account.isActive) {
                    android.graphics.Color.parseColor("#4CAF50") // Green
                } else {
                    android.graphics.Color.parseColor("#F44336") // Red
                }
            )

            itemView.setOnClickListener {
                onAccountClick(account)
            }
        }
    }
}

class SharedAccountDiffCallback : DiffUtil.ItemCallback<SharedAccount>() {
    override fun areItemsTheSame(oldItem: SharedAccount, newItem: SharedAccount): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SharedAccount, newItem: SharedAccount): Boolean {
        return oldItem == newItem
    }
}
