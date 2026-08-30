package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.PinDao
import dev.gustavo.finance.data.local.PinEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePinDao : PinDao {
    private val pins = MutableStateFlow<Set<String>>(emptySet())
    private var shouldThrow = false

    override fun getAllPinnedCodes(): Flow<List<String>> {
        if (shouldThrow) throw RuntimeException("DB Error")
        return pins.map { it.toList() }
    }

    override suspend fun insertPin(pin: PinEntity) {
        if (shouldThrow) throw RuntimeException("DB Error")
        pins.value += pin.currencyCode
    }

    override suspend fun deletePin(pin: PinEntity) {
        if (shouldThrow) throw RuntimeException("DB Error")
        pins.value -= pin.currencyCode
    }

    override suspend fun isPinned(code: String): Boolean {
        if (shouldThrow) throw RuntimeException("DB Error")
        return pins.value.contains(code)
    }
}
