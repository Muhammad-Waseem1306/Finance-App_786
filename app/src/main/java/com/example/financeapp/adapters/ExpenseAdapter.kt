package com.example.financeapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.models.Expense

class ExpenseAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    var onItemClick: ((Expense) -> Unit)? = null
    var onItemDeleteClick: ((Expense) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val subCategoryTextView: TextView = itemView.findViewById(R.id.subcategoryTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)
        private val customTextView: TextView = itemView.findViewById(R.id.customText) // Rename to customCategoryTextView

        fun bind(expense: Expense) {
            // Set the amount text and color based on category
            amountTextView.text = "Amount: ${expense.amount}"
            if (expense.category == "Income") {
                amountTextView.setTextColor(Color.GREEN)
                subCategoryTextView.visibility = View.GONE
                customTextView.visibility = View.GONE
            } else if (expense.category == "Expense") {
                amountTextView.setTextColor(Color.RED)
                subCategoryTextView.visibility = View.VISIBLE
                subCategoryTextView.text = "SubCategory: ${expense.subcategory}"

                // Handle custom subcategory visibility
                if (expense.subcategory == "Other") {
                    customTextView.visibility = View.VISIBLE
                    customTextView.text = "Custom Category: ${expense.customText ?: "No custom category"}"
                } else {
                    customTextView.visibility = View.GONE
                }
            }

            // Set other fields
            categoryTextView.text = "Category: ${expense.category}"
            dateTextView.text = "Date: ${expense.date}"
            descriptionTextView.text = "Description: ${expense.description}"

            // Handle item clicks
            itemView.setOnClickListener {
                onItemClick?.invoke(expense)
            }

            // Handle delete button click
            deleteButton.setOnClickListener {
                onItemDeleteClick?.invoke(expense)
            }
        }
    }
}

