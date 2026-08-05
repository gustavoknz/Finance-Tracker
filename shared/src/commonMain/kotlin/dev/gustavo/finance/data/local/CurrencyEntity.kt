package dev.gustavo.finance.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CurrencyEntity(
    @PrimaryKey val code: String,
    val name: String
)
