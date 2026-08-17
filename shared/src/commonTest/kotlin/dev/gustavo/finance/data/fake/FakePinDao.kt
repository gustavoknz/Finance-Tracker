package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.PinDao
import dev.gustavo.finance.data.local.PinEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePinDao : PinDao {
    private val pins = MutableStateFlow<Set<String>>(emptySet())

    override fun getAllPinnedCodes(): Flow<List<String>> =
        pins.map { it.toList() }

    override suspend fun insertPin(pin: PinEntity) {
        pins.value += pin.currencyCode
    }

    override suspend fun deletePin(pin: PinEntity) {
        pins.value -= pin.currencyCode
    }

    override suspend fun isPinned(code: String): Boolean =
        pins.value.contains(code)
}
