package com.example.ui.runtime.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
