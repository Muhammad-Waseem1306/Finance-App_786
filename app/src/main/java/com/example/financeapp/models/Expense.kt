package com.example.financeapp.models

data class Expense(
    var id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val description: String = ""
)
