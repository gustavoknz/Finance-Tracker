package dev.gustavo.finance.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration from version 1 -> 2: add localTimestamp columns with default 0
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ExchangeRateEntity: primaryKeys = ["baseCode", "targetCode"]
        db.execSQL("ALTER TABLE ExchangeRateEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
        // CurrencyEntity
        db.execSQL("ALTER TABLE CurrencyEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
        // PinEntity
        db.execSQL("ALTER TABLE PinEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
    }
}
