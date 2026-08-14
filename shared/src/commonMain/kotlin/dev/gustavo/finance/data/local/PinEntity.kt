package dev.gustavo.finance.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PinEntity(
    @PrimaryKey val currencyCode: String
)
