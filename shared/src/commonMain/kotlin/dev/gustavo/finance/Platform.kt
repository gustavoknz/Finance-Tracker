package dev.gustavo.finance

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
