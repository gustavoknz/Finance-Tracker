package dev.gustavo.finance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {
    @Query("SELECT currencyCode FROM PinEntity")
    fun getAllPinnedCodes(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: PinEntity)

    @Delete
    suspend fun deletePin(pin: PinEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM PinEntity WHERE currencyCode = :code)")
    suspend fun isPinned(code: String): Boolean
}
