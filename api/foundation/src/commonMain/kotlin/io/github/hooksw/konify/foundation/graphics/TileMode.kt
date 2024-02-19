package io.github.hooksw.konify.foundation.graphics

@kotlin.jvm.JvmInline
value class TileMode internal constructor(@Suppress("unused") private val name: String) {
    companion object {
        /**
         * Edge is clamped to the final color.
         *
         * The gradient will paint the all the regions outside the inner area with
         * the color of the point closest to that region.
         * ![TileMode.Clamp](https://developer.android.com/static/images/jetpack/compose/graphics/brush/tile_mode_clamp.png)
         */
        val Clamp = TileMode("Clamp")

        /**
         * Edge is repeated from first color to last.
         *
         * This is as if the stop points from 0.0 to 1.0 were then repeated from 1.0
         * to 2.0, 2.0 to 3.0, and so forth (and for linear gradients, similarly from
         * -1.0 to 0.0, -2.0 to -1.0, etc).
         * ![TileMode.Repeated](https://developer.android.com/static/images/jetpack/compose/graphics/brush/tile_mode_repeated.png)
         */
        val Repeated = TileMode("Repeated")
    }

    override fun toString() = name
}