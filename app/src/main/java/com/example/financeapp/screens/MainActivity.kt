package com.example.financeapp.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.adapters.ExpenseAdapter
import com.example.financeapp.databinding.ActivityMainBinding
import com.example.financeapp.models.Expense
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var db: FirebaseFirestore
    private val allExpenses = mutableListOf<Expense>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Finish MainActivity so the user can't go back to it
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarM)
        setSupportActionBar(toolbar)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.expensesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load and display income and expense data
        loadAndDisplayData()

        // Set up the FloatingActionButton to add new expenses
        val addExpenseButton: FloatingActionButton = findViewById(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
    }

    private fun loadAndDisplayData() {
        db = FirebaseFirestore.getInstance()

        // Show progress bar while loading data
        showProgressBar()
        // Clear previous data
        allExpenses.clear()

        // Load expenses
        db.collection("expenses").get().addOnCompleteListener { expenseTask ->
            if (expenseTask.isSuccessful) {
                val expenseDocuments = expenseTask.result
                for (document in expenseDocuments) {
                    val expense = document.toObject(Expense::class.java).apply {
                        id = document.id
                    }
                    allExpenses.add(expense)
                }

                // Load incomes
                db.collection("incomes").get().addOnCompleteListener { incomeTask ->
                    if (incomeTask.isSuccessful) {
                        val incomeDocuments = incomeTask.result
                        for (document in incomeDocuments) {
                            val income = document.toObject(Expense::class.java).apply {
                                id = document.id
                            }
                            allExpenses.add(income)
                        }

                        // Sort by date
                        allExpenses.sortByDescending { it.date }

                        // Set adapter with combined list
                        adapter = ExpenseAdapter(allExpenses).apply {
                            onItemClick = { expense ->
                                val intent = Intent(
                                    this@MainActivity,
                                    AddExpenseActivity::class.java
                                ).apply {
                                    putExtra("expenseId", expense.id)
                                    putExtra("amount", expense.amount)
                                    putExtra("category", expense.category)
                                    putExtra("date", expense.date)
                                    putExtra("description", expense.description)
                                }
                                startActivity(intent)
                            }
                            onItemDeleteClick = { expense ->
                                deleteExpense(expense)
                            }
                        }
                        recyclerView.adapter = adapter

                        // Update the total income and expense in the UI
                        updateTotalIncomeAndExpense()

                    } else {
                        Toast.makeText(
                            this,
                            "Failed to load incomes: ${incomeTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    hideProgressBar() // Hide progress bar whether success or failure
                }

            } else {
                Toast.makeText(
                    this,
                    "Failed to load expenses: ${expenseTask.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressBar() // Hide progress bar if expenses fail to load
            }
        }
    }

    private fun updateTotalIncomeAndExpense() {
        // Show progress bar while updating totals
        showProgressBar()

        var totalIncome = 0.0
        var totalExpense = 0.0

        for (expense in allExpenses) {
            if (expense.category == "Income") {
                totalIncome += expense.amount
            } else if (expense.category == "Expense") {
                totalExpense += expense.amount
            }
        }

        findViewById<TextView>(R.id.totalIncomeTextView).text = "+ Rs. $totalIncome"
        findViewById<TextView>(R.id.totalExpenseTextView).text = "- Rs. $totalExpense"
        findViewById<TextView>(R.id.totalBalanceTextView).text =
            (totalIncome - totalExpense).toString()
        // Hide progress bar after updating totals
        hideProgressBar()
    }

    private fun deleteExpense(expense: Expense) {
        // Show progress bar while deleting expense
        showProgressBar()

        val collectionName = if (expense.category == "Income") "incomes" else "expenses"
        db.collection(collectionName).document(expense.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${expense.category} deleted successfully", Toast.LENGTH_SHORT)
                    .show()
                loadAndDisplayData() // Reload data to update the UI

                // Hide progress bar after deletion and data reload
                hideProgressBar()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to delete ${expense.category}: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()

                // Hide progress bar on failure
                hideProgressBar()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu for the toolbar
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle toolbar menu item clicks
        return when (item.itemId) {
            R.id.nav_home -> {
                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

            R.id.nav_login -> {
                // Navigate to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }

            R.id.nav_share -> {
                shareApp()
                true
            }

            R.id.nav_logout -> {
                logoutUser()
                true
            }

            R.id.nav_rateUs -> {
                rateUs()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        // Sign out the user
        FirebaseAuth.getInstance().signOut()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()  // Finish the current activity so the user can't return to it
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this awesome finance app: [We are working on it thanks for your patience]"
            )
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun rateUs() {
        val appPackageName = "com.example.financeapp" // Replace with your app's package name
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}
