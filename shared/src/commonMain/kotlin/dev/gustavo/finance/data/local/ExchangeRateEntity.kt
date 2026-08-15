package dev.gustavo.finance.data.local

import androidx.room.Entity

@Entity(primaryKeys = ["baseCode", "targetCode"])
data class ExchangeRateEntity(
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val date: String,
    val localTimestamp: Long = 0L
)
