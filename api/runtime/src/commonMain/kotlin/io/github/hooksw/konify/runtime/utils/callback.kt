package io.github.hooksw.konify.runtime.utils

fun interface UnitCallBack {
   operator fun invoke()
}
fun interface IntCallBack {
   operator fun invoke():Int
}
fun interface FloatCallBack {
   operator fun invoke():Float
}
fun interface LongCallBack {
   operator fun invoke():Long
}
fun interface DoubleCallBack {
   operator fun invoke():Double
}