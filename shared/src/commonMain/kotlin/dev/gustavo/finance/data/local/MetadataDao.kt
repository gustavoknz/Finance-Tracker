package dev.gustavo.finance.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MetadataDao {
    @Query("SELECT lastUpdatedTimestamp FROM MetadataEntity WHERE `key` = :key")
    suspend fun getLastUpdatedTimestamp(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: MetadataEntity)
}
