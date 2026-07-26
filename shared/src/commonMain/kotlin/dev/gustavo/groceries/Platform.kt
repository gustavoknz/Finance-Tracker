package dev.gustavo.groceries

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
