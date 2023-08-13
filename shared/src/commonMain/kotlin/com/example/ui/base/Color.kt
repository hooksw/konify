package com.example.ui.base


@kotlin.jvm.JvmInline
value class Color(val hexColor: Long) {
    constructor(
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ) : this((((alpha and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)).toLong())


    val red: Int
        get() {
            return (hexColor shr 16 and 0xFF).toInt()
        }

    val green: Int
        get() {
            return (hexColor shr 8 and 0xFF).toInt()
        }


    val blue: Int
        get() {
            return (hexColor and 0xFF).toInt()
        }

    val alpha: Int
        get() {
            return (hexColor shr 24 and 0xFF).toInt()
        }


    operator fun component1(): Int = red


    operator fun component2(): Int = green


    operator fun component3(): Int = blue


    operator fun component4(): Int = alpha


    fun copy(
        alpha: Int = this.alpha,
        red: Int = this.red,
        green: Int = this.green,
        blue: Int = this.blue
    ): Color = Color(
        red = red,
        green = green,
        blue = blue,
        alpha = alpha
    )

    override fun toString(): String {
        return "Color($red, $green, $blue, $alpha)"
    }

    companion object {

        val Black = Color(0xFF000000)


        val DarkGray = Color(0xFF444444)


        val Gray = Color(0xFF888888)


        val LightGray = Color(0xFFCCCCCC)


        val White = Color(0xFFFFFFFF)


        val Red = Color(0xFFFF0000)


        val Green = Color(0xFF00FF00)


        val Blue = Color(0xFF0000FF)


        val Yellow = Color(0xFFFFFF00)


        val Cyan = Color(0xFF00FFFF)


        val Magenta = Color(0xFFFF00FF)


        val Transparent = Color(0x00000000)


    }
}
