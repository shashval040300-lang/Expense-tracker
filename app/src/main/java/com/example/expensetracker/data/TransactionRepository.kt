package com.example.expensetracker.data

class TransactionRepository(private val dao: TransactionDao) {
    val transactions = dao.observeAll()
    suspend fun add(transaction: TransactionEntity) = dao.insert(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
}
