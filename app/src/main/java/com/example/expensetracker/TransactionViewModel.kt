package com.example.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.TransactionEntity
import com.example.expensetracker.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(AppDatabase.get(application).transactionDao())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(amount: Double, type: String, category: String, payment: String, note: String) {
        viewModelScope.launch {
            repository.add(TransactionEntity(amount = amount, type = type, category = category,
                dateMillis = System.currentTimeMillis(), paymentMethod = payment, note = note))
        }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }
}
