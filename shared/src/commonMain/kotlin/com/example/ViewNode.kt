package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.experimental.ExperimentalTypeInference

interface EffectResult {
    fun clean()
}

class EffectScope {
    inline fun onClean(crossinline call: () -> Unit) = object : EffectResult {
        override fun clean() {
            call()
        }

    }
}

interface State<T> {
    val value: T
}

class ReadonlyState<T>(override val value: T) : State<T>

class MutableState<T>(
    init: T
) : State<T> {
    override var value: T = init
        set(value) {
            if (value == field) return
            field = value
            observers.forEach { (observer, lifecycle) ->
                if (lifecycle.state == LifecycleState.Mounted) {
                    observer.accept(field)
                }
            }
        }
    private val observers = hashMapOf<Observer<T>, Lifecycle>()

    @UI
    fun bind(observer: Observer<T>) {
        observers[observer] = currentViewNode.lifecycle
        currentViewNode.lifecycle.addOnMount {
            observer.accept(value)
        }
        currentViewNode.lifecycle.addOnCleanup {
            observers.remove(observer)
        }
    }

}

fun interface Observer<T> {
    fun accept(call: T)
}

enum class LifecycleState {
    Created, Mounted, Pause, Release
}

class Lifecycle {
    var state: LifecycleState = LifecycleState.Created
        set(value) {
            field = value
            when (value) {
                LifecycleState.Created -> throw IllegalStateException("Lifecycle cannot be recreated.")
                LifecycleState.Mounted -> onMountList.forEach {
                    it.invoke()
                }

                LifecycleState.Pause -> {}
                LifecycleState.Release -> {
                    onMountList.clear()
                    cleanupList.forEach {
                        it.invoke()
                    }
                    cleanupList.clear()
                }
            }
        }
    val scope = SupervisorJob() + Dispatchers.Main.immediate
    private val onMountList: MutableList<() -> Unit> = ArrayList(5)
    private val cleanupList: MutableList<() -> Unit> = ArrayList(5)
    fun addOnMount(call: () -> Unit) {
        onMountList.add(call)
    }

    fun addOnCleanup(call: () -> Unit) {
        cleanupList.add(call)
    }
}

sealed class ViewNode {
    val lifecycle = Lifecycle()

    private var parent: ViewNode? = null
    private var nativeView: PlatformView? = null
    private var children: MutableList<ViewNode>? = null
    private val localMap = hashMapOf<ContextLocal<*>, State<*>>()
    fun addChild(viewNode: ViewNode) {
        if (children == null) children = mutableListOf()
        children!!.add(viewNode)
        viewNode.parent = this
        viewNode.setup()
    }

    fun removeChild(viewNode: ViewNode) {
        children!!.remove(viewNode)
        viewNode.release()
    }

    fun <T : PlatformView> registerNativeView(view: T, builder: T.() -> Unit) {
        nativeView = view.apply(builder)
    }

    private fun setup() {
        nativeView?.let { findParentNativeView().addChild(it) }
        lifecycle.state = LifecycleState.Mounted
    }

    private fun release() {
        parent = null
        nativeView?.let { findParentNativeView().removeChild(it) }
        lifecycle.state = LifecycleState.Mounted
    }

    private fun findParentNativeView(): PlatformView {
        return when {
            parent!!.nativeView == null -> parent!!.findParentNativeView()
            else -> parent!!.nativeView!!
        }
    }
}

expect class PlatformView {
    fun addChild(platformView: PlatformView)

    fun removeChild(platformView: PlatformView)
}

//自定义组件
abstract class Component : ViewNode() {
    abstract val body: ViewNode
}

//封装原生组件
abstract class NativeComponent : ViewNode() {
    abstract val nativeView: PlatformView
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UI

@UI
fun ui1(m: State<Int>) {
    val a = stateOf(0)
    val b = stateOf(9)
    val c = a.value + b.value

    createEffect(a) {

    }

}

annotation class Observe

fun Show(b: State<Boolean>, function: () -> Unit) {

}


fun <T> memo(vararg key: Any, function: () -> T): State<T> {
    return stateOf(function())
}

@UI
fun Switch(function: SwitchScope.() -> Unit) {

}

class SwitchScope {

}

fun SwitchScope.If(boolean: State<Boolean>, function: () -> Unit) {

}

fun SwitchScope.Else(function: () -> Unit) {

}


fun <T> stateOf(i: T): MutableState<T> {
    return MutableState(i)
}

fun <T> readonlyStateOf(i: T): ReadonlyState<T> {
    return ReadonlyState(i)
}


annotation class NotViewNode

@UI
@NotViewNode
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun <T> cleanableEffect(vararg key: State<T>, effect: EffectScope.() -> EffectResult) {
    val scope = EffectScope()
    combine(key) {
        scope.effect()
    }
    currentViewNode.lifecycle.addOnMount {
        val result=scope.effect()
        currentViewNode.lifecycle.addOnCleanup {
            result.clean()
        }
        //TODO
    }
}

@UI
@NotViewNode
fun <T> createEffect(vararg key: State<T>, effect: EffectScope.() -> Unit) {
    val scope = EffectScope()
    combine(key) {
        scope.effect()
    }
    currentViewNode.lifecycle.addOnMount { scope.effect() }
}


val currentViewNode: ViewNode
    @UI
    @NotViewNode get() = throw NotImplementedError("Implemented as an intrinsic")

class ContextLocal<T>(default: T) {
    val current: State<T> = stateOf(default)
}

@UI
fun ContextProvider(contextProvide: ContextProvide<*>, call: () -> Unit) {

}

infix fun <T> ContextLocal<T>.provides(
    local: State<T>
) = ContextProvide(this, local)

infix fun <T> ContextLocal<T>.provides(
    local: T
) = ContextProvide(this, ReadonlyState(local))

class ContextProvide<T>(
    val context: ContextLocal<T>,
    val local: State<T>
)

