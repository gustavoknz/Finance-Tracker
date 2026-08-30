package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.MetadataDao
import dev.gustavo.finance.data.local.MetadataEntity

class FakeMetadataDao : MetadataDao {
    private val metadata = mutableMapOf<String, Long>()
    var shouldThrow = false

    override suspend fun getLastUpdatedTimestamp(key: String): Long? {
        if (shouldThrow) throw RuntimeException("DB Error")
        return metadata[key]
    }

    override suspend fun insertMetadata(metadata: MetadataEntity) {
        if (shouldThrow) throw RuntimeException("DB Error")
        this@FakeMetadataDao.metadata[metadata.key] = metadata.lastUpdatedTimestamp
    }
}
