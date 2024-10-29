package com.example.financeapp.screens

import Budget
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.financeapp.R
import com.example.financeapp.adapters.BudgetAdapter
import com.example.financeapp.databinding.ActivityBudgetBinding
import com.example.financeapp.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class BudgetActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BudgetAdapter
    private lateinit var db: FirebaseFirestore
    private val allBudget = mutableListOf<Budget>()
    private lateinit var binding: ActivityBudgetBinding
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    // Notification Channel ID
    private val CHANNEL_ID = "budget_notifications"
    private val NOTIFICATION_ID = 101
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize notification channel
        createNotificationChannel()

        // Initialize the RecyclerView
        recyclerView = binding.budgetsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadAndDisplayData()
        }

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbarBudget)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarBudget.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Add budget button listener
        binding.addBudgetButton.setOnClickListener {
            startActivity(Intent(this, AddBudgetActivity::class.java))
        }

        // Load and display budget data initially and listen for updates
        setupSnapshotListener()
    }

    private fun setupSnapshotListener() {
        db = FirebaseFirestore.getInstance()
        allBudget.clear()  // Clear existing data to avoid duplication
        showProgressBar()

        // Set up a real-time listener for changes in the "budgets" collection
        db.collection("budgets").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                hideProgressBar()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                allBudget.clear()
                for (document in snapshot) {
                    // Parse the document into a Budget object and check the date
                    val budget = document.toObject(Budget::class.java).apply {
                        id = document.id
                    }
                    allBudget.add(budget)

                    // Calculate the spent amount for each budget item
                    calculateSpentAmount(budget)
                }
                displayData()
            }
            hideProgressBar()
        }
    }

    private fun displayData() {
        allBudget.sortByDescending { it.date }  // Sort by date for latest budgets
        adapter = BudgetAdapter(this, allBudget, ::onBudgetClick, ::onBudgetUpdate, ::onBudgetDelete)
        recyclerView.adapter = adapter
    }

    private fun calculateSpentAmount(budget: Budget) {
        db.collection("expenses")
            .whereEqualTo("category", budget.category)
            .whereGreaterThanOrEqualTo("date", budget.date)
            .get()
            .addOnSuccessListener { expenses ->
                var spentAmount = 0.0
                for (document in expenses) {
                    val expense = document.toObject(Expense::class.java)
                    spentAmount += expense.amount
                }

                // Calculate remaining amount
                val remainingAmount = budget.limit - spentAmount

                // Update budget in Firestore
                val updatedBudget = budget.copy(spent = spentAmount.toString(), remaining = remainingAmount.toString())
                db.collection("budgets").document(budget.id).set(updatedBudget)
                    .addOnSuccessListener {
                        updateBudgetInList(updatedBudget)
                        // Trigger notification if the remaining amount is negative (exceeded)
                        if (remainingAmount < 0) {
                            sendBudgetExceededNotification(budget)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update budget: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBudgetInList(updatedBudget: Budget) {
        val index = allBudget.indexOfFirst { it.id == updatedBudget.id }
        if (index >= 0) {
            allBudget[index] = updatedBudget
            adapter.notifyItemChanged(index)
        }
    }

    private fun showProgressBar() {
        swipeRefreshLayout.isRefreshing = true
    }

    private fun hideProgressBar() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.budget_nav -> {
                startActivity(Intent(this, BudgetActivity::class.java))
                true
            }
            R.id.nav_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.chart_nav->{
                startActivity(Intent(this,GraphicallyData::class.java))
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
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()  // Finish the current activity
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this awesome finance app: [We are working on it thanks for your patience]")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun rateUs() {
        val appPackageName = "com.example.financeapp"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun onBudgetClick(budget: Budget) {
        val intent = Intent(this, AddBudgetActivity::class.java)
        intent.putExtra("BUDGET_ID", budget.id)
        startActivity(intent)
    }

    private fun onBudgetUpdate(updatedBudget: Budget) {
        db.collection("budgets").document(updatedBudget.id).set(updatedBudget)
            .addOnSuccessListener {
                loadAndDisplayData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onBudgetDelete(budget: Budget) {
        db.collection("budgets").document(budget.id).delete()
            .addOnSuccessListener {
                loadAndDisplayData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAndDisplayData() {
        allBudget.clear()
        showProgressBar()

        // Fetch all budgets from Firebase and display them
        db.collection("budgets").get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot) {
                    val budget = document.toObject(Budget::class.java).apply {
                        id = document.id
                    }
                    allBudget.add(budget)
                }
                displayData()
                hideProgressBar()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                hideProgressBar()
            }
    }

    // Function to create a notification channel (for Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Notifications"
            val descriptionText = "Notifications for budget limit exceedance"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to send a notification when the budget is exceeded
    private fun sendBudgetExceededNotification(budget: Budget) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Replace with your app's icon
            .setContentTitle("Budget Limit Exceeded")
            .setContentText("Your budget for ${budget.category} has exceeded the limit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(this@BudgetActivity)) {
            if (ActivityCompat.checkSelfPermission(
                    this@BudgetActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request notification permission if it's not granted
                ActivityCompat.requestPermissions(
                    this@BudgetActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                return
            }
            // Permission granted, show the notification
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }
}
