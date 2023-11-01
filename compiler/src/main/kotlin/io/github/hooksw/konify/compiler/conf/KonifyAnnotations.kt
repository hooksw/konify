package io.github.hooksw.konify.compiler.conf


object KonifyAnnotations {
    val Component = annotation("Component")
    val Stateless = annotation("Stateless")
    val ReadOnly= annotation("ReadOnly")
    val StyleBuilder = annotation("StyleBuilder")
    val DisallowKonifyCalls = annotation("DisallowKonifyCalls")
    private fun annotation(string: String) = classIdFor("annotation", string)
}

object Classes {
    val ViewNode = classIdFor("node", "ViewNode")
    val Signal = classIdFor("signal", "Signal")
    val IntSignal = classIdFor("signal", "IntSignal")
    val LongSignal = classIdFor("signal", "LongSignal")
    val FloatSignal = classIdFor("signal", "FloatSignal")
    val DoubleSignal = classIdFor("signal", "DoubleSignal")
    val SignalMarker = classIdFor("signal", "SignalMarker")
}
object Funs{
    val constant=callableIdFor("signal",null,"constant")
}