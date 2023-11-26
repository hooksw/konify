# Konify

> 名字来源于以下词语： Kotlin、construct、notify

Konify 是一个高性能、紧凑且高度可扩展的库，受到 [Solid] 和 [Compose] 的启发，用于使用 Kotlin 构建响应式的 Android、Web 和 iOS 应用程序。

你可以将 Konify 的函数视为构造函数，它们只执行一次，**没有重组**。

**目前正处于设计阶段。**

---

#### 与 Compose 的区别

[Compose] 很好，但仍然存在以下问题：

* 在某些情况下存在性能问题，主要与不必要的重组有关。
* 需要在所有地方标记为 stable。
* Compose UI 不能在 Web DOM 中使用。

因此，我们计划实现以下目标：

* 写起来像 Compose 一样，工作起来像Native元素一样，不需要重组。
* 保持可扩展性，以便我们可以将其适应其他场景（例如，使用 Skia 作为后台实现，或者实现其他树状结构的响应式系统）。


## 已确定的部分

### 概览

一个Component的写法应该如下：

```kotlin
@Component
fun Counter() {
    var count by signalOf(1)
    val greaterThan10 = memo{count>10}
    LaunchEffect(greaterThan10) {
        print("the count is greater than 10")
    }
    Row {
        Text(count)
        Switch {
            If(greaterThan10) {
                Button(text = "Reset", onClick = { count = 0 })
            }
            Else {
                Button(text = "+", onClick = { count += 1 })
            }
        }
    }
}
```
### 响应式机制背后（伪代码）
以下代码展示了目前的响应式系统是如何工作的（在不需要重组的情况下）。

```kotlin
//Computation代表当所依赖的任一state发生变化时，需要执行的函数（也可以叫做计算，也就是名称的由来）
class Computation(
   private val fn:()->Unit
){
    val listener={
        //每次执行Computation前先解绑所有的signal，以防止重复添加
        //至于为什么不用Set数据结构，因为这样可以实现自动追踪
        //比如一个computation的回调是textView.text=if(boolSignal) signal1 else signal2，
        //当boolSignal为true时，由于条件分支执行，只有boolSignal和signal1会和这个computation进行绑定
        //当boolSignal变为false，先解绑再重新绑定后,signal1不会再追踪这个computation，而是boolSignal和signal2会追踪它
        signals.forEach{
            it.observers.remove(this)
        }
        //12. 重新绑定，注意此时fn为textView.text=str()，因此会在设置好currentComputation时重新调用signal.getValue
        withComputation(this,fn)
    }
    val signals:MutableList<Signal> =mutableListOf()
}

class Signal(private var backValue:Any){
    var value:Any
        get(){
            //9.此时computation为null，直接获取值
            if(currentComputation!=null){
                //6.此时进行双绑
                //13.重新绑定，以便下次signal变化的时候再次调用绑定的computation
                currentComputation.states.add(this)
                observers.add(currentComputation)
            }
            return backValue
        }
        set(value){
            backValue=value
            //11.遍历执行computation
            observers.forEach{
                it.listener()
            }
        }
    val observers:MutableList<Computation> =mutableListOf()
}
var currentComputation:Computation?=null

//2.进入bind内部
fun bind(block:()->Unit){
    withComputation(Computation(block),block)
}

fun withComputation(computation: Computation, run: () -> Unit){
    //保存当前Computation，在没有memo（compose中的derivedState）的情况下，它一般为空
    val lasComputation=currentComputation
    //3. 设置当前Computation，准备进行双绑
    currentComputation=computation
    //4. 执行第一次绑定
     run()
    //恢复值，当前示例下，恒为null
    currentComputation=lasComputation
}

//使用示例
fun counter(){
    val count = Signal(0)
    Button(()->count.value.toString()){
        //10.调用signal.getValue
        count.value=
        //8. 进入signal.getValue
            count.value+1
    }
}
//为了实现响应式，我们必须通过编译器插件把除了lambda类型以外的类型转为lazy获取的形式，以便signal.value能准确获取到computation并和其进行互相绑定
fun Button(str:()->String,onClick:()->Unit){
    val textView=TextView()
    //7.绑定完成后当调用onClick，也就是更改count.value时
    textView.onClick=onClick
    //1. 此时进入bind实现state和computation的双向绑定，而不是传统的以便signal.bind{}的单向绑定
    bind{
        //5. 在回调中通过()->T的形式能够在此时获取到对以便signal.value，也就是count.value，并开始和其绑定，然后执行一次回调
        textView.text=str()
    }
}

//ViewModel：
//在脱离computation的单线程场景中，computation恒为null,因此此时signal.getValue没有响应式特性
//因此在viewModel中使用flow替代是最好的

```
### State

与 Compose 类似，有两种类型的 State：`Signal`（类似于 Compose 中的 `state`），`Memo`（类似于 Compose 中的 `derivedState`）

### Effect

与 Compose 类似，有三个与副作用相关的函数：
`SideEffect`，`LaunchEffect`，`DisposeEffect`
与 Compose 的主要区别在于，当你不传递任何键时，它的行为类似于在 Compose 的LaunchEffect中传递 `Unit/true/...`

```kotlin
//in konify
LaunchEffect{
  //todo
}
//like in compose
LaunchEffect(Unit){
  //todo
}
```
### 控制流

#### Switch

在 Compose 中，我们可以这样做：

```kotlin
@Compose
fun A(bool:Boolean){
    if(bool){
        B()
    }else{
        C()
    }
}
```
由于 Konify 只调用这些函数一次，我们不能简单地使用 Kotlin 的控制流关键字，但我们可以在 `Switch` 块内部使用 `If` 和 `Else` 函数：

```kotlin
Switch {
  If(stateA) {
    ComponentA()
  }
  If(stateB) {
    ComponentB()
  }
  If(stateC) {
    ComponentC()
  }
  Else {//optional
    ComponentD()
  }
}
```

在上述代码中，当 stateA、stateB、stateC 中的一个发生变化时，`Switch` 将按声明的顺序检查状态，第一个状态值为 `true` 的回调将被执行。

#### For

```kotlin
For(list=listState,key={it.id}){item->
    Text(text=item.toString())
}
```



## 未确定的部分

### 节点系统

这部分非常重要，因为涉及到许多部分：基本架构、UI 节点树、可扩展性、调试信息等等。

我们有以下想法：

1. **使用全局变量来管理当前节点（类似于 SolidJS 的做法）**

   ```kotlin
   internal var currentNode:Node?=null
   fun ComponentA(){
     val parent=currentNode!!
     currentNode=createNode(parent)
     //function body
     LaunchEffect{
       //...
     }
     //restore
     currentNode=parent
   }
   fun LaunchEffect(block:()->Unit){
     val node=currentNode!!
     node.registerOnPrepare{//...}
     node.registerOnDispose{//...}
   }
   ```
2. **注入节点参数**

   ```kotlin
   fun ComponentA(node:Node){
     val currentNode=createNode(node)
     //function body
     LaunchEffect(currentNode){
       //...
     }
   }
   fun LaunchEffect(node:Node,block:()->Unit){
     node.registerOnPrepare{//...}
     node.registerOnDispose{//...}
   }
   ```
3. **注入节点树参数（类似于 Compose 的做法）**

   ```kotlin
   fun ComponentA(tree:NodeTree){
     val currentNode=tree.createNode()
     //function body
     LaunchEffect(currentNode){
       //...
     }
   }
   fun LaunchEffect(node:Node,block:()->Unit){
     node.registerOnPrepare{//...}
     node.registerOnDispose{//...}
   }
   ```
目前仍然存在一些争论。如果您有任何意见或其他解决方案，请在这里报告 [https://github.com/hooksw/konify/issues/1](https://github.com/hooksw/konify/issues/1)。

我们的目标是保持可维护性、可扩展性和高性能。

### ContextLocal

如果您熟悉 Compose，您一定了解 `CompositionLocal`。在 Konify 中，它是 `ContextLocal`。

它的使用方式临时设计如下：


```kotlin
val ContextLocalCount = ContextLocalOf(1)

@Component
fun A() {
  var counter bv signalOf(0)
  LaunchEffect{
      while (true){
          delay(1000)
          counter+=1
      }
  }
  ContextLocalProvider(ContextLocalCount provides counter){
      B()
  }
}

@Component
fun B(){
  val localCount by useContext(ContextLocalCount)
  //get a signal whose init value is 0 
  val countState by signalOf(localCount)
  LaunchEffect(localCount){
     print(localCount.toString())
  }
}
```

### Style

我们不使用Modifier系统，而是使用类似 CSS 的样式系统。

当前的设计如下：


```kotlin
fun Style(callback:StyleNode.()->Unit){
  //...
}
fun Sample(){
  Text(
    style=Style{
      width=100.dp
      height=50.dp
      border[Left,Right]{
         color=Color.Red
      }
    }
    ,"text"
  )
}
```
`Style` 函数中的回调块仅支持值赋值和函数调用，并且可以通过扩展函数进行扩展。

**Q：我们是否需要支持类似于内联样式和 CSS 中的类样式的操作，比如 `val style = style1 + Style{//...}`？**

## 待办事项列表
1. 确定整体架构，改进核心代码，并基于其修改编译器插件。
2. 编写相关测试。
3. 确定要实现的style属性，构建其平台实现，并设计其 DSL。
4. 设计并实现事件系统，如手势事件。
5. 实现基本的 UI 组件：Text、Image、TextInput、FlexLayout、FrameLayout、Buttons。
6. 设计并实现动画系统。
7. 实现高级 UI 组件：LazyLayout、LazyList、Pager、AsyncImage。
8. （可选）提供实现自定义布局和视图的机制。
9. 设计并实现路由机制。
10. （可选）设计并实现增强开发的 IDE 插件。
11. （可选）支持热重载。

## 支持的平台

我们计划首先支持 Android 和 Web DOM（作者买不起 Mac）。

[compose]: https://developer.android.com/jetpack/compose
[kmm]: https://kotlinlang.org/lp/mobile/
[Solid]: https://www.solidjs.com/
