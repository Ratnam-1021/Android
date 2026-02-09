package com.example.bankapp


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BankAccount(
    val username: String,
    initialBalance: Int
) {
    // Uses mutableIntStateOf for automatic UI updates
    var balance by mutableIntStateOf(initialBalance)
    val transactions = mutableStateListOf<Transaction>()
}


