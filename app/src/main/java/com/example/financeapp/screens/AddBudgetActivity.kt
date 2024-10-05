package com.example.financeapp.screens

import Budget
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financeapp.R
import com.google.firebase.firestore.FirebaseFirestore

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var budgetRepository: BudgetRepository
    private var budgetToEdit: Budget? = null // To handle budget editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        // Initialize the toolbar for navigation
        val toolbar: Toolbar = findViewById(R.id.toolbarBudget)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back navigation
        }

        budgetRepository = BudgetRepository()

        // Setup the category spinner
        val categorySpinner: Spinner = findViewById(R.id.categorySpinnerBudget)
        val categories = arrayOf("Shopping", "Outing", "Food", "Travel", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Check if we're editing a budget
//        budgetToEdit = intent.getParcelableExtra("budget")

        // If editing, populate fields
        budgetToEdit?.let { budget ->
            findViewById<EditText>(R.id.budgetAmountEditText).setText(budget.amount.toString())
            findViewById<EditText>(R.id.startDateEditText).setText(budget.startDate)
            findViewById<EditText>(R.id.endDateEditText).setText(budget.endDate)
            val spinnerPosition = adapter.getPosition(budget.category)
            categorySpinner.setSelection(spinnerPosition)
        }

        val saveBudgetButton: Button = findViewById(R.id.saveBudgetButton)
        saveBudgetButton.setOnClickListener {
            val amountText = findViewById<EditText>(R.id.budgetAmountEditText).text.toString()
            val category = categorySpinner.selectedItem.toString()
            val startDate = findViewById<EditText>(R.id.startDateEditText).text.toString()
            val endDate = findViewById<EditText>(R.id.endDateEditText).text.toString()

            // Input validation
            if (amountText.isBlank() || startDate.isBlank() || endDate.isBlank()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDouble()

            if (budgetToEdit != null) {
                // Update existing budget
                val updatedBudget = budgetToEdit!!.copy(
                    amount = amount,
                    category = category,
                    startDate = startDate,
                    endDate = endDate
                )
                budgetRepository.updateBudget(updatedBudget)
            } else {
                // Add new budget
                val newBudget = Budget(
                    id = FirebaseFirestore.getInstance().collection("budgets").document().id,
                    amount = amount,
                    category = category,
                    startDate = startDate,
                    endDate = endDate
                )
                budgetRepository.addBudget(newBudget)
            }

            finish()
        }
    }

    // Handle the back navigation for the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
