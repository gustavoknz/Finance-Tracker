package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.MetadataDao
import dev.gustavo.finance.data.local.MetadataEntity

class FakeMetadataDao : MetadataDao {
    private val metadata = mutableMapOf<String, Long>()

    override suspend fun getLastUpdatedTimestamp(key: String): Long? = metadata[key]

    override suspend fun insertMetadata(metadata: MetadataEntity) {
        this@FakeMetadataDao.metadata[metadata.key] = metadata.lastUpdatedTimestamp
    }
}
