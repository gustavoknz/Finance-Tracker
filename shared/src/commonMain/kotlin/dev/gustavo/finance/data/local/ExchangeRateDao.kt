package dev.gustavo.finance.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM ExchangeRateEntity WHERE baseCode = :baseCode")
    fun getRatesByBase(baseCode: String): Flow<List<ExchangeRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
}
