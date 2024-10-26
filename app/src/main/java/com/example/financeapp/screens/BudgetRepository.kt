package com.example.financeapp.screens

import Budget
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val budgetsCollection = db.collection("budgets")
    private var listenerRegistration: ListenerRegistration? = null

    // Adding a new budget with error handling
    fun addBudget(budget: Budget): Task<Void> {
        val docRef = budgetsCollection.document() // Generate a new document reference
        budget.id = docRef.id // Set the generated ID in the Budget object

        // Return the Task from the set operation so that the caller can handle success/failure
        return docRef.set(budget)
    }

    // Fetch all budgets with real-time updates and error handling
    fun getBudgets(): LiveData<List<Budget>> {
        val liveData = MutableLiveData<List<Budget>>()
        listenerRegistration = budgetsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val budgets = snapshot.toObjects(Budget::class.java)
                liveData.value = budgets
            }
        }
        return liveData
    }

    // Update an existing budget by its ID with error handling
    fun updateBudget(budget: Budget) {
        budget.id?.let {
            budgetsCollection.document(it).set(budget)
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }
    }

    // Delete a budget by its ID with error handling
    fun deleteBudget(budgetId: String) {
        budgetsCollection.document(budgetId).delete()
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    // Optional: remove listener to avoid memory leaks
    fun removeListener() {
        listenerRegistration?.remove()
    }
}
