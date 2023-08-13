package com.example.ui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform