package dev.gustavo.finance.data.local

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

actual fun createEncryptedSettings(): Settings {
    return KeychainSettings(service = "dev.gustavo.finance.secure")
}
