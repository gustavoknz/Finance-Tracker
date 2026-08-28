package dev.gustavo.finance.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE ExchangeRateEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE CurrencyEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE PinEntity ADD COLUMN localTimestamp INTEGER NOT NULL DEFAULT 0")
    }
}
