package com.example.financeapp.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financeapp.models.Expense

import com.google.firebase.firestore.FirebaseFirestore

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("expenses")

    fun addExpense(expense: Expense) {
        expensesCollection.add(expense)
    }

    fun getExpenses(): LiveData<List<Expense>> {
        val liveData = MutableLiveData<List<Expense>>()
        expensesCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val expenses = snapshot.toObjects(Expense::class.java)
                liveData.value = expenses
            }
        }
        return liveData
    }

    fun updateExpense(expense: Expense) {
        expensesCollection.document(expense.id).set(expense)
    }

    fun deleteExpense(expenseId: String) {
        expensesCollection.document(expenseId).delete()
    }
}
