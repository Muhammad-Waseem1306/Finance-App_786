package com.example.financeapp.models

data class Expense(
    var id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    var subcategory: String? = null,
    var customText: String?= null,
    val date: String = "",
    val description: String = ""
)
