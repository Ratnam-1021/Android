package com.example.bankapp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val type: String,
    val amount: Int,
    val date: String
)

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}