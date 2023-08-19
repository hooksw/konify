package com.example.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

enum class LifecycleState {
    Created, Mounted, Release
}

class Lifecycle {
    internal var state: LifecycleState = LifecycleState.Created
        set(value) {
            field = value
            when (value) {
                LifecycleState.Created -> throw IllegalStateException("Lifecycle cannot be recreated.")
                LifecycleState.Mounted -> onMountList.forEach {
                    scope.launch {
                        it.invoke()
                    }
                }

                LifecycleState.Release -> {
                    scope.cancel()
                    onMountList.clear()
                    cleanupList.forEach {
                        it.invoke()
                    }
                    cleanupList.clear()
                }
            }
        }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val onMountList: MutableList<() -> Unit> = ArrayList(5)
    private val cleanupList: MutableList<() -> Unit> = ArrayList(5)
    fun addOnMount(call: () -> Unit) {
        onMountList.add(call)
    }

    fun addOnCleanup(call: () -> Unit) {
        cleanupList.add(call)
    }
}