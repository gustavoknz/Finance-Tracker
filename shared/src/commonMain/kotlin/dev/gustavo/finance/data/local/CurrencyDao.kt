package dev.gustavo.finance.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM CurrencyEntity")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM CurrencyEntity")
    suspend fun getAllCurrenciesOnce(): List<CurrencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Query("DELETE FROM CurrencyEntity WHERE localTimestamp < :timestamp")
    suspend fun deleteOldCurrencies(timestamp: Long)
}
