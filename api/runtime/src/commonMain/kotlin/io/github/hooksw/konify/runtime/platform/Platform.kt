package io.github.hooksw.konify.runtime.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
