package com.example.financeapp.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.adapters.BudgetAdapter
import com.example.financeapp.models.Budget
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetRepository: BudgetRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BudgetAdapter

    companion object {
        private const val REQUEST_CODE_POST_NOTIFICATIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        createNotificationChannel()

        budgetRepository = BudgetRepository()

        recyclerView = findViewById(R.id.budgetsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        budgetRepository.getBudgets().observe(this, Observer { budgets ->
            adapter = BudgetAdapter(budgets)
            recyclerView.adapter = adapter

            checkBudgets(budgets)
        })

        findViewById<FloatingActionButton>(R.id.addBudgetButton).setOnClickListener {
            startActivity(Intent(this, AddBudgetActivity::class.java))
        }

        // Check and request notification permission if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun checkBudgets(budgets: List<Budget>) {
        budgets.forEach { budget ->
            // Here you should implement the logic to check if the budget is exceeded
            // For simplicity, let's assume a fixed amount is checked
            val totalSpent = 500.0 // Replace with actual calculation
            if (totalSpent >= budget.amount) {
                sendNotification("Budget Alert", "You have exceeded your budget for ${budget.category}")
            }
        }
    }

    private fun sendNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, "BUDGET_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return
        }
        notificationManager.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Channel for budget alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("BUDGET_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can send notifications
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}
