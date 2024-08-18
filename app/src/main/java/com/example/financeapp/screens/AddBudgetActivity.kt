package com.example.financeapp.screens

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.models.Budget

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var budgetRepository: BudgetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        budgetRepository = BudgetRepository()

        // Setup the category spinner
        val categorySpinner: Spinner = findViewById(R.id.categorySpinnerBudget)
        val categories = arrayOf("Shopping", "Outing", "Food", "Travel", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val saveBudgetButton: Button = findViewById(R.id.saveBudgetButton)
        saveBudgetButton.setOnClickListener {
            val amount = findViewById<EditText>(R.id.budgetAmountEditText).text.toString().toDouble()
            val category = findViewById<EditText>(R.id.categorySpinner).text.toString()
            val startDate = findViewById<EditText>(R.id.startDateEditText).text.toString()

            val budget = Budget(
                id = "",
                amount = amount,
                category = category,
                startDate = startDate,
            )

            budgetRepository.addBudget(budget)
            finish()
        }
    }
}