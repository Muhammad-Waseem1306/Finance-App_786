package com.example.financeapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        fun bind(expense: Expense) {
            amountTextView.text = "Amount  : ${expense.amount.toString()}"

            // Set text color based on the category
            if (expense.category == "Income") {
                amountTextView.setTextColor(Color.GREEN)
            } else if (expense.category == "Expense") {
                amountTextView.setTextColor(Color.RED)
            }
            categoryTextView.text = "Category  : ${expense.category}"
            dateTextView.text = "Date : ${expense.date}"
            descriptionTextView.text = "Description  : ${expense.description}"

            itemView.setOnClickListener {
                onItemClick?.invoke(expense)
            }

            deleteButton.setOnClickListener {
                onItemDeleteClick?.invoke(expense)
            }
        }
    }
}
