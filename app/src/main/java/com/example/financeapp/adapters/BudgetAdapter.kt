package com.example.financeapp.adapters

import Budget
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.google.firebase.firestore.FirebaseFirestore

class BudgetAdapter(
    private val context: Context,
    private val budgets: List<Budget>,
    private val onBudgetClick: (Budget) -> Unit,
    private val onBudgetUpdate: (Budget) -> Unit,
    private val onBudgetDelete: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val spentTextView: TextView = itemView.findViewById(R.id.spentTextView)
        private val remainingTextView: TextView = itemView.findViewById(R.id.remainingTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.startDateTextView)
        private val add_Button_ex: AppCompatImageButton = itemView.findViewById(R.id.add_Buttonex)
        private val updateButton: AppCompatImageButton = itemView.findViewById(R.id.updateButton2)
        private val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.deleteButton)

        // Bind data to views
        fun bind(budget: Budget) {
            dateTextView.text = "Date: ${budget.date}"
            categoryTextView.text = "Category: ${budget.category}"
            amountTextView.text = "Limit: ${budget.limit}"
            amountTextView.setTextColor(Color.GREEN)
            spentTextView.text = "Spent: ${budget.spent}"
            spentTextView.setTextColor(Color.RED)
            remainingTextView.text = "Remaining: ${budget.remaining}"

            // Handle card view click
            itemView.setOnClickListener { onBudgetClick(budget) }

            // Handle "Add Expense" button click
            add_Button_ex.setOnClickListener {
                showAddExpenseDialog(budget)
            }

            // Handle update and delete button clicks
            updateButton.setOnClickListener { onBudgetUpdate(budget) }
            deleteButton.setOnClickListener { onBudgetDelete(budget) }
        }

        // Show dialog for adding an expense amount
        private fun showAddExpenseDialog(budget: Budget) {
            val builder = AlertDialog.Builder(context)
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setTitle("Enter Expense Amount")
            builder.setView(input)

            builder.setPositiveButton("Add") { dialog, _ ->
                val expenseAmount = input.text.toString().toDoubleOrNull()
                if (expenseAmount != null) {
                    addExpenseToBudget(budget, expenseAmount)
                } else {
                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        // Add expense to Firestore and calculate remaining budget
        private fun addExpenseToBudget(budget: Budget, expenseAmount: Double) {
            val budgetDocRef = FirebaseFirestore.getInstance().collection("budgets").document(budget.id)

            budgetDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentSpent = document.getDouble("spent") ?: 0.0
                    val updatedSpent = currentSpent + expenseAmount
                    val updatedRemaining = budget.limit - updatedSpent

                    // Update Firestore with new spent and remaining values
                    val updates = mapOf(
                        "spent" to updatedSpent,
                        "remaining" to updatedRemaining
                    )

                    budgetDocRef.update(updates).addOnSuccessListener {
                        Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged() // Refresh the item display to show updated values
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount() = budgets.size
}
