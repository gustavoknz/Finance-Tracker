package dev.gustavo.finance.data.local

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import java.nio.charset.StandardCharsets

actual fun createEncryptedSettings(): Settings {
    error("Android requires context for TinkEncryptedSettings")
}

fun createEncryptedSettings(context: Context): Settings {
    return TinkEncryptedSettings(context)
}

class TinkEncryptedSettings(context: Context) : Settings {
    init {
        AeadConfig.register()
    }

    private val keysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(context, "master_keyset", "master_key_pref_file")
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri("android-keystore://master_key")
        .build()
        .keysetHandle

    private val aead: Aead = keysetHandle.getPrimitive(Aead::class.java)

    private val delegate: Settings = SharedPreferencesSettings(
        context.getSharedPreferences("secure_finance_prefs", Context.MODE_PRIVATE)
    )

    private fun encrypt(value: String): String {
        val encrypted = aead.encrypt(value.toByteArray(StandardCharsets.UTF_8), null)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        return try {
            val decoded = Base64.decode(value, Base64.NO_WRAP)
            val decrypted = aead.decrypt(decoded, null)
            String(decrypted, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            value
        }
    }

    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size

    override fun clear() = delegate.clear()
    override fun remove(key: String) = delegate.remove(key)
    override fun hasKey(key: String): Boolean = delegate.hasKey(key)

    override fun putInt(key: String, value: Int) = delegate.putString(key, encrypt(value.toString()))
    override fun getInt(key: String, defaultValue: Int): Int {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored).toIntOrNull() ?: defaultValue
    }
    override fun getIntOrNull(key: String): Int? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored).toIntOrNull()
    }

    override fun putLong(key: String, value: Long) = delegate.putString(key, encrypt(value.toString()))
    override fun getLong(key: String, defaultValue: Long): Long {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored).toLongOrNull() ?: defaultValue
    }
    override fun getLongOrNull(key: String): Long? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored).toLongOrNull()
    }

    override fun putString(key: String, value: String) = delegate.putString(key, encrypt(value))
    override fun getString(key: String, defaultValue: String): String {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored)
    }
    override fun getStringOrNull(key: String): String? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored)
    }

    override fun putFloat(key: String, value: Float) = delegate.putString(key, encrypt(value.toString()))
    override fun getFloat(key: String, defaultValue: Float): Float {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored).toFloatOrNull() ?: defaultValue
    }
    override fun getFloatOrNull(key: String): Float? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored).toFloatOrNull()
    }

    override fun putBoolean(key: String, value: Boolean) = delegate.putString(key, encrypt(value.toString()))
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored).toBooleanStrictOrNull() ?: defaultValue
    }
    override fun getBooleanOrNull(key: String): Boolean? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored).toBooleanStrictOrNull()
    }

    override fun putDouble(key: String, value: Double) = delegate.putString(key, encrypt(value.toString()))
    override fun getDouble(key: String, defaultValue: Double): Double {
        val stored = delegate.getString(key, "")
        return if (stored.isEmpty()) defaultValue else decrypt(stored).toDoubleOrNull() ?: defaultValue
    }
    override fun getDoubleOrNull(key: String): Double? {
        val stored = delegate.getStringOrNull(key) ?: return null
        return if (stored.isEmpty()) null else decrypt(stored).toDoubleOrNull()
    }
}
