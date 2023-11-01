const fs = require("node:fs")
const primitives = ["Int", "Long", "Float", "Double"]

const primitiveSignal = {
    dest: "../api/runtime/src/commonMain/kotlin/io/github/hooksw/konify/runtime/signal/PrimitiveSignal.kt",
    head: "package io.github.hooksw.konify.runtime.signal\n\n" +
        "import kotlin.reflect.KProperty",
    template: `
interface <>Signal:SignalMarker {
    val value: <>
 
    operator fun getValue(thisRef: Any?, property: KProperty<*>): <> {
        return value
    }
}
interface Mutable<>Signal :<>Signal {
    override var value: <>
  
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: <>) {
        this.value = value
    }
}
    `,
    generate() {
        fs.writeFile(this.dest, this.head + primitives.map((e) =>
            this.template.replaceAll("<>", e)
        ).join("\n"), e => {
            console.log(e)
        })
    }
}
const observedSignal = {
    dest: "../api/runtime/src/commonMain/kotlin/io/github/hooksw/konify/runtime/signal/ObservedPrimitiveSignal.kt",
    head: `
package io.github.hooksw.konify.runtime.signal
    
import androidx.collection.MutableScatterSet
import androidx.collection.mutableScatterSetOf
import io.github.hooksw.konify.runtime.signal.Effect
import io.github.hooksw.konify.runtime.signal.EffectDisposeList
import io.github.hooksw.konify.runtime.utils.UnitCallBack
import kotlin.jvm.JvmField`,
    template: `

internal class Observed<>Signal(
    initialValue: <>
) : Mutable<>Signal {
    @JvmField
    internal val observers: MutableScatterSet<UnitCallBack> = mutableScatterSetOf()

    override var value: <> = initialValue
        get() {
            val effect = Effect
            if (effect != null) {
                observers.add(effect)
                EffectDisposeList.add {
                    observers.remove(effect)
                }
            }
            return field
        }
        set(value) {
            if (field==value) {
                return
            }
            field = value
            dispatchUpdate()

        }

    private fun dispatchUpdate() {
        observers.forEach { observer ->
            observer()
        }
    }
}

fun  <1>SignalOf(
    initialValue:<> 
): Mutable<>Signal {
    return Observed<>Signal(
        initialValue = initialValue
    )
}

    `,
    generate() {
        fs.writeFile(this.dest, this.head + primitives.map((e) =>
            this.template.replaceAll("<>", e).replaceAll("<1>",e.toLowerCase())
        ).join("\n"), e => {
            console.log(e)
        })
    }
}
const derivedSignal = {
    dest: "../api/runtime/src/commonMain/kotlin/io/github/hooksw/konify/runtime/signal/DerivedPrimitiveSignal.kt",
    head: "package io.github.hooksw.konify.runtime.signal\n" + primitives.map(e => `import io.github.hooksw.konify.runtime.utils.${e}CallBack`).join("\n"),
    template: `
    
class Derived<>Signal(
    private  val getValue: <>CallBack
) : <>Signal {
    override val value: <>
        get() = getValue()
}

fun  derived<>(function: <>CallBack ): Derived<>Signal {
    return Derived<>Signal(function)
}
    `,
    generate() {
        fs.writeFile(this.dest, this.head + primitives.map((e) =>
            this.template.replaceAll("<>", e)
        ).join("\n"), e => {
            console.log(e)
        })
    }
}
// const primitiveDelegate = {
//     dest: "../api/runtime/src/commonMain/kotlin/io/github/hooksw/konify/runtime/signal/PrimitiveDelegate.kt",
//     head: "package io.github.hooksw.konify.runtime.signal\n" +
//         "import kotlin.reflect.KProperty",
//     template: `
//
// operator fun  <>Signal.getValue(thisRef: Any?, property: KProperty<*>):<>  {
//     return value
// }
//
// operator fun  Mutable<>Signal.setValue(thisRef: Any?, property: KProperty<*>, value:<> ) {
//     this.value = value
// }
//     `,
//     generate() {
//         fs.writeFile(this.dest, this.head + primitives.map((e) =>
//             this.template.replaceAll("<>", e)
//         ).join("\n"), e => {
//             console.log(e)
//         })
//     }
// }
primitiveSignal.generate()
observedSignal.generate()
derivedSignal.generate()
// primitiveDelegate.generate()