# Konify

> The name comes from kotlin, construct, notify

Konify is a fast, compact, and highly scalable library inspired by [Solid] and [Compose] for building reactive Android, web and iOS applications using Kotlin.

**It is currently in the design phase.**

### Differences from Compose

[Compose] is amazing, but there are still the following problems:

* there are performance issues in some cases, mostly related to unnecessary recompositions.
* need to mark stable everywhere.
* Compose UI cannot be used in web dom.

so we plan to do something like:

* writes like Compose, works like native view elements and avoid recompositions.
* keep scalability so that we can adapt it to other scenes (e.g. use skia as behind-the-scenes implementation,or implement other tree-structured responsive systems)

## Determined parts

### overview

the component should code like:

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

### The responsive mechanism behind(pseudocode)

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

like compose there two kinds of State:  `Signal` (like `state` in Compose), `Memo`(like `derivedState` in Compose)

### Effect

like compose, there are 3 side effect related functions:
`SideEffect`,`LaunchEffect`,`DisposeEffect`
The main difference from Compose is, when you don't pass any key, it works like pass `Unit/true/...` in Compose

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

### Control Flow

#### Switch

in Compose, we can do something like:

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

since Konify only invoke these functions once, we cannot simply use kotlin control flow keywords, but we can use `If` and `Else` function inside `Switch` block:

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

In the code above, when one of stateA,stateB,stateC changes, `Switch`will check the state in order of declaration, the first callback that state value is `true` when will execute.

#### For

```kotlin
For(list=listState,key={it.id}){item->
    Text(text=item.toString())
}
```

## Undetermined Parts

### The Node System

This part is very important, as it involves a lot of parts: basic architecture,the UI node tree, expandability, debug information, and more.

We have had the following ideas:

1. **Use global variables to manage the current Node(like what SolidJS do)**

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
2. **Inject the Node parameter**

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
3. **Inject the NodeTree parameter(like what Compose do)**

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

There is still debate. If you have any opinions or other solutions, please report here [https://github.com/hooksw/konify/issues/1](https://github.com/hooksw/konify/issues/1).

And our goal is to keep maintainability, scalability, and high performance.

### ContextLocal

If you are familiar with Compose, you must know `CompositionLocal`.in Konify ,it's `ContextLocal`.
Its use is temporarily designed as follows:

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

We don't use Modifier system,and we use css-like style system.

The current design is as follows

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

The callback block in the `Style` function only supports value assignment and function calls

And it can be extended by extension functions.

**Todo:Do we need to support operations such as `val style=style1+Style{//...}` like inline styles and class styles in CSS?**

## Supported Platform

We plan to support Android and Web dom first (the author currently cannot afford a mac)

[compose]: https://developer.android.com/jetpack/compose
[kmm]: https://kotlinlang.org/lp/mobile/
[Solid]: https://www.solidjs.com/
