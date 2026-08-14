package dev.gustavo.finance.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM ExchangeRateEntity WHERE baseCode = :baseCode")
    fun getRatesByBase(baseCode: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM ExchangeRateEntity WHERE baseCode = :baseCode")
    suspend fun getRatesByBaseOnce(baseCode: String): List<ExchangeRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
}
