package com.example.financeapp.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.models.Budget

class BudgetAdapter(private val budgets: List<Budget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        holder.amountTextView.text = budget.amount.toString()
        holder.categoryTextView.text = budget.category
        holder.startDateTextView.text = budget.startDate
        holder.endDateTextView.text = budget.endDate
    }

    override fun getItemCount(): Int {
        return budgets.size
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val startDateTextView: TextView = itemView.findViewById(R.id.startDateTextView)
        val endDateTextView: TextView = itemView.findViewById(R.id.endDateTextView)
    }
}