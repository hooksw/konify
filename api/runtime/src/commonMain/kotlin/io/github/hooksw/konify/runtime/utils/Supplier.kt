package io.github.hooksw.konify.runtime.utils

fun interface IntSupplier {
   operator fun invoke():Int
}
fun interface FloatSupplier {
   operator fun invoke():Float
}
fun interface LongSupplier {
   operator fun invoke():Long
}
fun interface DoubleSupplier {
   operator fun invoke():Double
}
