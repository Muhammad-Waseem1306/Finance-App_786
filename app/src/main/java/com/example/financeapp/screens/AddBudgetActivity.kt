package com.example.financeapp.screens

import Budget
import android.app.DatePickerDialog
import java.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityAddBudgetBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var budgetRepository: BudgetRepository
    private var budgetToEdit: Budget? = null // To handle budget editing
    private lateinit var binding: ActivityAddBudgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up DatePicker on startDateEditText
        binding.startDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

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
        val categories = arrayOf("--Select--", "Shopping", "Outing", "Food", "Travel", "Rent", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter


        // Check if we're editing a budget
        budgetToEdit = intent.getParcelableExtra<Budget>("budget") // Safe cast to Budget

        // If editing, populate fields
        budgetToEdit?.let { budget ->
            findViewById<EditText>(R.id.budgetAmountEditText).setText(budget.limit.toString())
            findViewById<EditText>(R.id.startDateEditText).setText(budget.date)
            val spinnerPosition = adapter.getPosition(budget.category)
            categorySpinner.setSelection(spinnerPosition)

            // Show the delete button if editing
            findViewById<Button>(R.id.deleteBudgetButton).visibility = Button.VISIBLE
        }

        // Save Budget button click handler
        findViewById<Button>(R.id.saveBudgetButton).setOnClickListener {
            saveOrUpdateBudget()
        }

        // Delete Budget button click handler
        findViewById<Button>(R.id.deleteBudgetButton).setOnClickListener {
            deleteBudget()
        }
    }

    // Save or update the budget
    private fun saveOrUpdateBudget() {
        val amountText = findViewById<EditText>(R.id.budgetAmountEditText).text.toString()
        val category = findViewById<Spinner>(R.id.categorySpinnerBudget).selectedItem.toString()
        val startDate = findViewById<EditText>(R.id.startDateEditText).text.toString()
//        val spent = findViewById<TextView>(R.id.spentTextView).text.toString()
//        val remaining = findViewById<EditText>(R.id.remainingEtv).text.toString()

        if (amountText.isBlank() || startDate.isBlank()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()

        if (budgetToEdit != null) {
            // Update existing budget
            val updatedBudget = budgetToEdit!!.copy(
                limit = amount,
                category = category,
                date = startDate,
//                spent = spent,
//                remaining = remaining
            )
            budgetRepository.updateBudget(updatedBudget)
        } else {
            // Add new budget
            val newBudget = Budget(
                id = FirebaseFirestore.getInstance().collection("budgets").document().id,
                limit = amount,
                category = category,
                date = startDate,
//                spent = spent,
//                remaining = remaining
            )
            budgetRepository.addBudget(newBudget)
        }

        finish()
    }

    // Delete the budget
    private fun deleteBudget() {
        budgetToEdit?.let {
            budgetRepository.deleteBudget(it.id)
            finish() // Close the activity after deletion
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.startDateEditText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
