package dev.gustavo.finance.util

import platform.Foundation.NSBundle

class IosAppConfig : AppConfig {
    override val baseUrl: String = "https://api.frankfurter.dev/v1/"
    override val isDebug: Boolean = NSBundle.mainBundle.infoDictionary?.get("DEBUG") as? Boolean ?: false
}
