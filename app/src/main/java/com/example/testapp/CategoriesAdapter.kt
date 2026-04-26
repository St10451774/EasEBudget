package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val budgetLimitTextView: TextView = itemView.findViewById(R.id.budgetLimitTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.categoryTypeTextView)

        fun bind(category: Category) {
            nameTextView.text = category.name
            budgetLimitTextView.text = "Budget: $${String.format("%.2f", category.budgetLimit)}"
            
            if (category.isDefault) {
                typeTextView.text = "Default"
                typeTextView.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            } else {
                typeTextView.text = "Custom"
                typeTextView.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
            }

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}
