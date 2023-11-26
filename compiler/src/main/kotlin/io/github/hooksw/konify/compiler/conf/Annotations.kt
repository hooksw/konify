package io.github.hooksw.konify.compiler.conf

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name


object Annotations {
    val Component = annotation("Component")
    val ReifiedSupplier = annotation("ReifiedSupplier")
    val Stateless = annotation("Stateless")
    val ReadOnly= annotation("ReadOnly")
    val StyleBuilder = annotation("StyleBuilder")
    val DisallowKonifyCalls = annotation("DisallowKonifyCalls")
    private fun annotation(string: String) = classIdFor("annotation", string)
}

object Classes {
    val Signal = classIdFor("signal", "Signal")
    val IntSupplier= classIdFor("utils","IntSupplier")
    val LongSupplier= classIdFor("utils","LongSupplier")
    val FloatSupplier= classIdFor("utils","FloatSupplier")
    val DoubleSupplier= classIdFor("utils","DoubleSupplier")
}
object Funs{
    val VarargMap= callableIdFor("utils",null,"invokeMap")
}
object Names{
    val invoke= Name.identifier("invoke")
}