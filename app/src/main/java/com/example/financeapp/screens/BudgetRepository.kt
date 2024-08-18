package com.example.financeapp.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financeapp.models.Budget
import com.google.firebase.firestore.FirebaseFirestore

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val budgetsCollection = db.collection("budgets")

    fun addBudget(budget: Budget) {
        budgetsCollection.add(budget)
    }

    fun getBudgets(): LiveData<List<Budget>> {
        val liveData = MutableLiveData<List<Budget>>()
        budgetsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val budgets = snapshot.toObjects(Budget::class.java)
                liveData.value = budgets
            }
        }
        return liveData
    }

    fun updateBudget(budget: Budget) {
        budgetsCollection.document(budget.id).set(budget)
    }

    fun deleteBudget(budgetId: String) {
        budgetsCollection.document(budgetId).delete()
    }
}
