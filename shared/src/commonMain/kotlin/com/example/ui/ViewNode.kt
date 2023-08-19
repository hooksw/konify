package com.example.ui

/*
注解为private来保证通过父节点的newNode方法完成子节点的创建和初始化，
避免因为手动new一个实例和添加到父节点的顺序不一致导致的问题
*/
class ViewNode private constructor() {
    companion object {
        fun empty() = ViewNode()
    }

    val lifecycle = Lifecycle()
    private var parent: ViewNode? = null
    private var nativeView: PlatformView? = null
    private var children: MutableList<ViewNode>? = null
    private val localMap = hashMapOf<ContextLocal<*>, State<*>>()


    fun addContextLocal(contextProvide: ContextProvide<*>) {
        localMap[contextProvide.context] = contextProvide.local
    }

    fun <T> getContextLocal(contextLocal: ContextLocal<T>): State<T>? {
        return (localMap[contextLocal] ?: parent?.getContextLocal(contextLocal)) as State<T>?
    }

    fun newNode(): ViewNode {
        val childNode = ViewNode()
        if (children == null) children = mutableListOf()
        children!!.add(childNode)
        childNode.parent = this
        childNode.setup()
        return childNode
    }

    fun removeChild(viewNode: ViewNode) {
        viewNode.release()
        children!!.remove(viewNode)
    }

    //注册原生view组件
    //查找到最近的含有nativeView的父节点，并挂载到上面，因为ViewNode本身可以不包含UI组件，只包含逻辑
    fun <T : PlatformView> registerNativeView(view: T) {
        nativeView = view
        val parentNativeView = findParentNativeView()
        parentNativeView?.addChild(nativeView as T)

    }

    fun removeAllChildren() {
        //用于switch判断
        children!!.forEach {
            it.release()
        }
        children!!.clear()
    }

    private fun setup() {
        lifecycle.state = LifecycleState.Mounted
    }

    private fun release() {
        localMap.clear()
        parent = null
        nativeView?.let { findParentNativeView()?.removeChild(it) }
        lifecycle.state = LifecycleState.Release
    }
    private fun findParentNativeView(): PlatformView? {
        return when (parent) {
            null -> parent?.findParentNativeView()
            else -> parent?.nativeView
        }
    }
}


//由于目前不支持编译注解，所以get方法无法获取到ViewNode，因此会调用失败
val currentViewNode: ViewNode
    @ReadOnlyViewNode get() = throw NotImplementedError("Implemented as an intrinsic")
