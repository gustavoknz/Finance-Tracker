package dev.gustavo.finance.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        // Create earliest version of the database.
        var db = helper.createDatabase(TEST_DB, 1)

        // Insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.execSQL("INSERT INTO CurrencyEntity (code, name) VALUES ('USD', 'US Dollar')")
        db.execSQL("INSERT INTO ExchangeRateEntity (baseCode, targetCode, rate, date) VALUES ('USD', 'EUR', 0.91, '2024-05-19')")
        db.execSQL("INSERT INTO PinEntity (currencyCode) VALUES ('USD')")

        // Prepare for the next version.
        db.close()

        // Re-open the database with version 2 and provide MIGRATION_1_2 as the migration object.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // MigrationTestHelper automatically verifies the schema changes, but we can also verify data.
        val cursor = db.query("SELECT * FROM CurrencyEntity WHERE code = 'USD'")
        cursor.moveToFirst()
        
        // Check if the new column localTimestamp exists and has the default value 0
        val timestampIndex = cursor.getColumnIndex("localTimestamp")
        assertEquals(0L, cursor.getLong(timestampIndex))
        
        assertEquals("USD", cursor.getString(cursor.getColumnIndex("code")))
        assertEquals("US Dollar", cursor.getString(cursor.getColumnIndex("name")))
        
        cursor.close()
    }
}
