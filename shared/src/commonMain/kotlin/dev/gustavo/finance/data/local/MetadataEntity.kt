package dev.gustavo.finance.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MetadataEntity(
    @PrimaryKey val key: String,
    val lastUpdatedTimestamp: Long
)
