package com.example.financeapp.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityAddExpenseBinding
import com.example.financeapp.models.Expense
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var expenseId: String? = null
    private lateinit var db: FirebaseFirestore
    private var category: String? = null
    private lateinit var categorySpinner: Spinner
    private lateinit var expenseSubcategorySpinner: Spinner
    private lateinit var customCategoryLayout: TextInputLayout
    private lateinit var customCategoryEditText: TextInputEditText

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

        // Initialize views
        categorySpinner = findViewById(R.id.categorySpinner)
        expenseSubcategorySpinner = findViewById(R.id.expenseSubcategorySpinner)
        customCategoryLayout = findViewById(R.id.customCategoryLayout)
        customCategoryEditText = findViewById(R.id.customCategoryEditText)

        setupCategorySpinner()
        setupSubcategorySpinner()
        setupDateInput()

        // Get the data from the intent if this is an update operation
        intent?.let {
            expenseId = it.getStringExtra("expenseId")
            category = it.getStringExtra("category") ?: ""
            if (expenseId != null) {
                // Load existing data
                loadExpenseData(expenseId!!)
                binding.deleteExpenseButton.visibility = View.VISIBLE // Show delete button for updates
            }
        }

        binding.saveExpenseButton.setOnClickListener {
            val amountText = binding.amountEditText.text.toString()
            val category = binding.categorySpinner.selectedItem.toString()

            if (amountText.isEmpty() && category.isEmpty()) {
                showSnackbar("Amount and Category fields are empty")
                return@setOnClickListener
            }

            val amount = amountText.toDouble()
            val selectedCategory = binding.categorySpinner.selectedItem.toString()
            val subCategory = binding.expenseSubcategorySpinner.selectedItem.toString()
            val customText = if (subCategory == "Other") {
                binding.customCategoryEditText.text.toString()
            } else {
                ""
            }
            val date = binding.dateEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (expenseId == null) {
                createExpense(amount, selectedCategory, subCategory, customText, date, description)
            } else {
                updateExpense(expenseId!!, selectedCategory, amount, subCategory, customText, date, description)
            }
        }

        binding.deleteExpenseButton.setOnClickListener {
            expenseId?.let { deleteExpense(it) }
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Income", "Expense")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (categories[position]) {
                    "Income" -> {
                        expenseSubcategorySpinner.visibility = View.GONE
                        customCategoryLayout.visibility = View.GONE
                    }
                    "Expense" -> {
                        expenseSubcategorySpinner.visibility = View.VISIBLE
                        customCategoryLayout.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }
    }

    private fun setupSubcategorySpinner() {
        val subcategories = arrayOf("--Select--", "Shopping", "Outing", "Food", "Travel", "Rent", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subcategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        expenseSubcategorySpinner.adapter = adapter

        expenseSubcategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (subcategories[position] == "Other") {
                    customCategoryLayout.visibility = View.VISIBLE
                } else {
                    customCategoryLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }
    }

    private fun setupDateInput() {
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
    }

    private fun loadExpenseData(expenseId: String) {
        val collectionName = if (category == "Income") "incomes" else "expenses"
        db.collection(collectionName).document(expenseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val expense = document.toObject(Expense::class.java)
                    expense?.let {
                        binding.amountEditText.setText(it.amount.toString())
                        binding.dateEditText.setText(it.date)
                        binding.descriptionEditText.setText(it.description)

                        if (category == "Income") {
                            expenseSubcategorySpinner.visibility = View.GONE
                            customCategoryLayout.visibility = View.GONE
                        } else if (category == "Expense") {
                            expenseSubcategorySpinner.visibility = View.VISIBLE
                            val subCategoryAdapter = binding.expenseSubcategorySpinner.adapter as ArrayAdapter<String>
                            val subCategoryPosition = subCategoryAdapter.getPosition(expense.subcategory)
                            binding.expenseSubcategorySpinner.setSelection(subCategoryPosition)

                            if (expense.subcategory == "Other") {
                                customCategoryLayout.visibility = View.VISIBLE
                                customCategoryEditText.setText(expense.customText)
                            } else {
                                customCategoryLayout.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    showSnackbar("Expense not found")
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                showSnackbar("Failed to load expense: ${exception.message}")
                exception.printStackTrace()
            }
    }

    private fun createExpense(amount: Double, category: String, subcategory: String, customText: String, date: String, description: String) {
        showProgressBar()
        val expenseData = hashMapOf(
            "amount" to amount,
            "category" to category,
            "subcategory" to subcategory,
            "customText" to customText,
            "date" to date,
            "description" to description
        )

        val collectionName = if (category == "Income") "incomes" else "expenses"

        db.collection(collectionName)
            .add(expenseData)
            .addOnSuccessListener {
                hideProgressBar()
                showSnackbar("$category added successfully")
                saveDataToSharedPreferences(amount.toString(), category, subcategory, customText, date, description)
                clearFields()
                navigateToTransactionScreen()
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                showSnackbar("Failed to add $category: ${exception.message}")
                exception.printStackTrace()
            }
    }

    private fun updateExpense(expenseId: String, category: String, amount: Double, subcategory: String, customText: String, date: String, description: String) {
        showProgressBar()
        val collectionName = if (category == "Income") "incomes" else "expenses"

        val expenseData = hashMapOf(
            "amount" to amount,
            "category" to category,
            "subcategory" to subcategory,
            "customText" to customText,
            "date" to date,
            "description" to description
        )

        db.collection(collectionName).document(expenseId)
            .set(expenseData)
            .addOnSuccessListener {
                hideProgressBar()
                showSnackbar("$category updated successfully")
                saveDataToSharedPreferences(amount.toString(), category, subcategory, customText, date, description)
                clearFields()
                navigateToTransactionScreen()
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                showSnackbar("Failed to update $category: ${exception.message}")
                exception.printStackTrace()
            }
    }

    private fun deleteExpense(expenseId: String) {
        val collectionName = if (category == "Income") "incomes" else "expenses"
        db.collection(collectionName).document(expenseId)
            .delete()
            .addOnSuccessListener {
                showSnackbar("$category deleted successfully")
                finish()
            }
            .addOnFailureListener { exception ->
                showSnackbar("Failed to delete $category: ${exception.message}")
                exception.printStackTrace()
            }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun clearFields() {
        binding.amountEditText.text?.clear()
        binding.dateEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.expenseSubcategorySpinner.setSelection(0)
        customCategoryLayout.visibility = View.GONE
        customCategoryEditText.text?.clear()
    }

    private fun navigateToTransactionScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun saveDataToSharedPreferences(amount: String, category: String, subcategory: String, customText: String, date: String, description: String) {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("amount", amount)
        editor.putString("category", category)
        editor.putString("subcategory", subcategory)
        editor.putString("customText", customText)
        editor.putString("date", date)
        editor.putString("description", description)
        editor.apply()
    }
}
