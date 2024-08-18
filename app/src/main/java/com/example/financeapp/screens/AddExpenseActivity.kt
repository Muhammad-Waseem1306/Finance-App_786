package com.example.financeapp.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityAddExpenseBinding
import com.example.financeapp.models.Expense
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var expenseId: String? = null
    private lateinit var db: FirebaseFirestore
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Setup the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup the category spinner
        val categories = arrayOf("Income", "Expense")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        // Setup the date picker dialog for date input
        binding.dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                binding.dateEditText.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        // Get the data from the intent if this is an update operation
        intent?.let {
            expenseId = it.getStringExtra("expenseId")
            category = it.getStringExtra("category") ?: ""
            if (expenseId != null) {
                // Load existing data
                loadExpenseData(expenseId!!)
            }
        }

        binding.saveExpenseButton.setOnClickListener {
            val amountText = binding.amountEditText.text.toString()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDouble()
            val selectedCategory = binding.categorySpinner.selectedItem.toString()
            val date = binding.dateEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (expenseId == null) {
                // Create a new expense
                createExpense(amount, selectedCategory, date, description)
            } else {
                // Update an existing expense
                updateExpense(expenseId!!, selectedCategory, amount, date, description)
            }
        }

        binding.deleteExpenseButton.setOnClickListener {
            expenseId?.let {
                deleteExpense(it)
            }
        }
    }

    private fun loadExpenseData(expenseId: String) {
        val collectionName = if (category == "Income") "incomes" else "expenses"
        db.collection(collectionName).document(expenseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val expense = document.toObject(Expense::class.java)
                    expense?.let {
                        binding.amountEditText.setText(it.amount.toString())
                        val adapter = binding.categorySpinner.adapter as ArrayAdapter<*>
//                        val position = adapter.getPosition(it.category==)
//                        binding.categorySpinner.setSelection(position)
                        binding.dateEditText.setText(it.date)
                        binding.descriptionEditText.setText(it.description)
                    }
                } else {
                    Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load expense: ${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }

    private fun createExpense(amount: Double, category: String, date: String, description: String) {
       showProgressBar()
        val expenseData = hashMapOf(
            "amount" to amount,
            "category" to category,
            "date" to date,
            "description" to description
        )

        val collectionName = if (category == "Income") "incomes" else "expenses"

        db.collection(collectionName)
            .add(expenseData)
            .addOnSuccessListener {
                Toast.makeText(this, "$category added successfully", Toast.LENGTH_SHORT).show()
                saveDataToSharedPreferences(amount.toString(), category, date, description)
                clearFields()
                navigateToTransactionScreen()
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                Toast.makeText(this, "Failed to add $category: ${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }

    private fun updateExpense(expenseId: String, category: String, amount: Double, date: String, description: String) {
        val collectionName = if (category == "Income") "incomes" else "expenses"

        val expenseData = hashMapOf(
            "amount" to amount,
            "category" to category,
            "date" to date,
            "description" to description
        )

        db.collection(collectionName).document(expenseId)
            .set(expenseData)
            .addOnSuccessListener {
                Toast.makeText(this, "$category updated successfully", Toast.LENGTH_SHORT).show()
                saveDataToSharedPreferences(amount.toString(), category, date, description)
                clearFields()
                navigateToTransactionScreen()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update $category: ${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }

    private fun deleteExpense(expenseId: String) {
        showProgressBar()
        val collectionName = if (category == "Income") "incomes" else "expenses"

        db.collection(collectionName).document(expenseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                navigateToTransactionScreen()
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                Toast.makeText(this, "Failed to delete expense: ${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }

    private fun saveDataToSharedPreferences(
        amount: String,
        category: String,
        date: String,
        description: String
    ) {
        val sharedPreferences = getSharedPreferences("transaction_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("amount", amount)
        editor.putString("category", category)
        editor.putString("date", date)
        editor.putString("description", description)
        editor.apply()
    }

    private fun clearFields() {
        // Reset Spinner to the first item
        binding.categorySpinner.setSelection(0)
        // Clear EditTexts
        binding.amountEditText.text.clear()
        binding.dateEditText.text.clear()
        binding.descriptionEditText.text.clear()
    }

    private fun navigateToTransactionScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}
