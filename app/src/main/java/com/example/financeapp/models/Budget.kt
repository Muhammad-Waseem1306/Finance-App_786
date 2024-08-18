package com.example.financeapp.models

data class Budget(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val startDate: String = "",
    val endDate: String = ""
)
