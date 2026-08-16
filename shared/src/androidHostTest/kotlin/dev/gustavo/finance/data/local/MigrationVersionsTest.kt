package dev.gustavo.finance.data.local

import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationVersionsTest {
    @Test
    fun migrationVersionsAreCorrect() {
        // Basic sanity check for migration object availability and versions
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
    }
}
