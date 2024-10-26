package com.example.financeapp.screens

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.financeapp.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class GraphicallyData : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graphically_data)

        // Adjusting window insets for edge-to-edge experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the LineChart
        lineChart = findViewById(R.id.lineChart)

        // Generate sample data for the chart
        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 200f))  // Example data: x=1, y=200
        entries.add(Entry(2f, 250f))
        entries.add(Entry(3f, 180f))
        entries.add(Entry(4f, 300f))
        entries.add(Entry(5f, 240f))

        // Create a dataset from the entries
        val dataSet = LineDataSet(entries, "Finance Data")
        dataSet.color = resources.getColor(R.color.green)
        dataSet.valueTextColor = resources.getColor(R.color.black)

        // Create LineData with the dataset
        val lineData = LineData(dataSet)

        // Attach the data to the chart
        lineChart.data = lineData

        // Customize the chart
        lineChart.description.text = "Financial Overview"
        lineChart.animateXY(1000, 1000)

        // Format X-axis values (optional, you can customize as needed)
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())
            }
        }

        // Customize the legend
        val legend: Legend = lineChart.legend
        legend.isEnabled = true

        // Refresh the chart
        lineChart.invalidate()
    }
}
