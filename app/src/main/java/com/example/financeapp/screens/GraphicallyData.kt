package com.example.financeapp.screens

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.financeapp.R
import com.example.financeapp.models.Expense
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore

class GraphicallyData : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var db: FirebaseFirestore
    private val allExpenses = mutableListOf<Expense>()
    private lateinit var graphTypeSpinner: Spinner
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphically_data)

        lineChart = findViewById(R.id.lineChart)
        pieChart = findViewById(R.id.piChart)
        db = FirebaseFirestore.getInstance()


        // Initialize the toolbar for navigation
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back navigation
        }

        // Initialize graphTypeSpinner
        graphTypeSpinner = findViewById(R.id.graphTypeSpinner)
        val graphTypes = arrayOf("Pie Chart","Line Chart")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, graphTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        graphTypeSpinner.adapter = adapter

        // Load and display data
        loadAndDisplayData()

        graphTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateChart(graphTypes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun loadAndDisplayData() {
        // Clear previous data
        allExpenses.clear()

        // Load expenses and incomes from Firestore
        db.collection("expenses").get().addOnCompleteListener { expenseTask ->
            if (expenseTask.isSuccessful) {
                for (document in expenseTask.result) {
                    val expense = document.toObject(Expense::class.java).apply { id = document.id }
                    allExpenses.add(expense)
                }
                updateCharts()
            }
        }
    }

    private fun updateCharts() {
        // Only update charts if data is available
        if (allExpenses.isEmpty()) return

        setupLineChart()
        setupPieChart()
    }

    private fun setupLineChart() {
        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        var incomeIndex = 0
        var expenseIndex = 0

        // Group data by category for line chart
        val incomeMap = mutableMapOf<String, Float>()
        val expenseMap = mutableMapOf<String, Float>()

        for (entry in allExpenses) {
            val amount = entry.amount.toString().toFloatOrNull() ?: 0f
            val category = entry.category ?: "Uncategorized"
            if (entry.category == "Income") {
                incomeMap[category] = incomeMap.getOrDefault(category, 0f) + amount
            } else if ( entry.category == "Shopping"||entry.category == "Outing" ||entry.category == "Food"||entry.category ==  "Travel"||entry.category == "Rent"||entry.category == "Other") {
                expenseMap[category] = expenseMap.getOrDefault(category, 0f) + amount
            }
        }

        // Convert to line chart entries
        incomeMap.forEach { (_, amount) -> incomeEntries.add(Entry(incomeIndex++.toFloat(), amount)) }
        expenseMap.forEach { (_, amount) -> expenseEntries.add(Entry(expenseIndex++.toFloat(), amount)) }

        // Create LineDataSets
        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = resources.getColor(R.color.green)
            lineWidth = 2f
            valueTextSize = 10f
        }
        val expenseDataSet = LineDataSet(expenseEntries, "Expense").apply {
            color = resources.getColor(R.color.Red)
            lineWidth = 2f
            valueTextSize = 10f
        }

        // Set data to the line chart
        lineChart.data = LineData(incomeDataSet, expenseDataSet)
        lineChart.invalidate() // Refresh the chart
    }

    private fun setupPieChart() {
        val pieEntries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Group data by category for pie chart
        val categoryMap = allExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Populate pie entries and colors
        categoryMap.forEach { (category, amount) ->
            val label = category ?: "Uncategorized"  // Use a default label if null
            pieEntries.add(PieEntry(amount.toFloat(), label))  // Convert amount to Float
            colors.add(getColorForCategory(label))
        }

        // Set up the PieDataSet
        val pieDataSet = PieDataSet(pieEntries, "Expense Categories").apply {
            this.colors = colors
            valueTextSize = 12f
        }

        // Set up PieData and PieChart
        pieChart.apply {
            data = PieData(pieDataSet)
            description.isEnabled = false
            isRotationEnabled = true
            centerText = "Expenses by Category"
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            invalidate() // Refresh the chart
        }
    }

    private fun getColorForCategory(category: String?): Int {
        return when (category) {
            "Shopping" -> Color.BLUE
            "Food" -> Color.GREEN
            "Travel" -> Color.MAGENTA
            "Outing" -> Color.YELLOW
            "Rent" -> Color.CYAN
            else -> Color.RED
        }
    }

    private fun updateChart(type: String) {
        // Hide both charts initially
        lineChart.visibility = View.GONE
        pieChart.visibility = View.GONE

        // Show the selected chart
        when (type) {
            "Line Chart" -> {
                lineChart.visibility = View.VISIBLE
            }
            "Pie Chart" -> {
                pieChart.visibility = View.VISIBLE
            }
        }
    }
}
