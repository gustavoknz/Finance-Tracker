package dev.gustavo.finance.util

class AndroidAppConfig : AppConfig {
    override val baseUrl: String = "https://api.frankfurter.dev/v1/"
    override val isDebug: Boolean = true // Should be linked to BuildType
}
