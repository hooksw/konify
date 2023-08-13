package com.example.ui

import kotlinx.browser.window


class JSPlatform : Platform {
    override val name: String = "JS ${window.name}"
}

actual fun getPlatform(): Platform = JSPlatform()