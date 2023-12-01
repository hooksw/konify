# Konify

[![zh](https://img.shields.io/badge/%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87%E7%89%88%E6%9C%AC-6495ED)](https://github.com/hooksw/konify/blob/master/README-ZH.md)

> The name comes from the following words: Kotlin, construct, notify

konify is a cross-platform UI library inspired by [Solid] and [Compose]  that targets high performance, small size, and strong scalability for building responsive Android, Web DOM and iOS applications using Kotlin .

You can think of functions of Konify as constructors, they are executed only once, without recomposition.

**Currently, in the design stage.**

---

## Determined part

### Overview

A Component should be written as follows:

```kotlin
@Component
fun Counter() {
   var count by signalOf(1)
   val greaterThan10 = memo{ count>10 }
   LaunchEffect(greaterThan10) {
      print("the count is greater than 10")
   }
   Row {
      Text(count.toString())
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
### Behind the responsive mechanism 
The following code shows how the current reactive system works basically without the need for recomposition or virtual-dom.
It relays on the single thread and the two-way binding.

```kotlin

class Signal(private var backValue: Any) {
    var value: Any
        get() {
            if (currentListener != null) {
                observers.add(currentListener)
            }
            return backValue
        }
        set(value) {
            backValue = value
            observers.forEach {
                it.listener()
            }
        }
    val observers = setOf<() -> Unit>()
}

var currentListener: (()->Unit)? = null

fun bind(block: () -> Unit) {
    val listener=currentListener
    currentListener=block
    block()
    currentListener=listener
}


//Usage example
fun counter() {
    val count = Signal(0)
    Button(()->count.value.toString()){
        count.value ++
    }
}

fun Button(str: () -> String, onClick: () -> Unit) {
    val textView = TextView()
    textView.onClick = onClick
    bind {
        textView.text = str()
    }
}


```
### Function parameters
In order for State to accurately capture the observer that should be bound currently,
the function parameters should be converted into the form of ()->T.

At the same time, in order to avoid unboxing, a special concrete type is provided for the primitive type, and an annotation is provided to generate the corresponding supplier for the value class

```kotlin
@RefiedSupplier
value class Dp(val value:Long)

@Component
fun Parent() {
    Child("", 0, 0.dp) {}
}

@Component
fun Child(string: String, int: Int, dp: Dp, call: () -> Unit) {

}

//will be transformed by compiler plugin
@Component
fun Parent() {
    Child({ "" }, IntSupplier{0},Dp.RefiedSupplier{0.dp}) {}
}

@Component
fun Child(string: ()->String, int: IntSupplier, dp: Dp.RefiedSupplier, call: () -> Unit) {

}

```


### State

There are two types of State: `Signal` (similar to `state` in Compose), `Memo` (similar to `derivedState` in Compose)

### Effect

Similar to Compose, there are three functions related to side effects:
`SideEffect`, `LaunchEffect`, `DisposeEffect`
The main difference with Compose is that when you don't pass any keys, it behaves like passing `Unit/true/...` in  `LaunchEffect` of Compose

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
### Control flow

#### Switch

In Compose we can do this:

```kotlin
@Composable
fun A(bool:Boolean){
   if(bool){
      B()
   }else{
      C()
   }
}
```
Since Konify only calls these functions once, we can't simply use Kotlin's control flow keywords, but we can use the `If` and `Else` functions inside the `Switch` block:

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

In the above code, when one of stateA, stateB, and stateC changes, `Switch` will check the states in the order of declaration, and the first callback with a state value of `true` will be executed.

#### For

```kotlin
For(list=listState,key={it.id}){item->
   Text(text=item.toString())
}
```

### Style

We use a CSS-like style system.

The current design is as follows:


```kotlin
fun Style(callback:StyleNode.()->Unit){
   //...
}
val commonStyle=Style {
    margin:10.dp
}
fun Sample(){
   Text(
      style=commonStyle+Style{
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
Callback blocks in `Style` functions only support value assignment and function calls, and can be extended through extension functions.

### wrap UI Elements

```kotlin
@Component
expect fun Text(text:String,style:Style)

@Component
actual fun Text(text:String,style:Style){
    val textView=textViewFactory()
    createNativeNode(textView){
        bind{
            it.text=text
        }
        bind{
            it.style=fontStyle
        }
    }
}
```

## Undetermined part

### Multi-thread state reading and writing

In order to ensure the development experience, we should make the state readable and written by multiple threads, just like compose does. At present, a snapshot isolation mechanism similar to mvcc seems to be the best choice. But the specific design has not yet been determined.

### Node system

We will treat special elements(Switch,For,Native UI Elements,Routing…) as special nodes, and all these nodes will construct a node tree.

When the last plan is complete, we will start working on this.

### ContextLocal

If you are familiar with Compose, you must know about `CompositionLocal`. In Konify, it is `ContextLocal`.

Its usage is temporarily designed as follows:


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

## To-do list
1. Determine the overall architecture, improve the core code.
2. Modify the compiler plug-in based on it 
3. Write relevant tests.
4. Determine the style attributes to be implemented, build its platform implementation, and design its DSL.
5. Design and implement event systems, such as gesture events.
6. Implement basic UI components: Text, Image, TextInput, FlexLayout, FrameLayout, Buttons.
7. Design and implement animation system.
8. Implement advanced UI components: LazyLayout, LazyList, Pager, AsyncImage.
9. (Optional) Provide mechanisms for implementing custom layouts and views.
10. Design and implement routing mechanism.
11. (Optional) Design and implement IDE plug-ins to enhance development.
12. (Optional) Support hot reload.

## Supported platforms

We plan to support Android and Web DOM first, if you are interested in it, welcome to contribute for any other platform.

[compose]: https://developer.android.com/jetpack/compose
[kmm]: https://kotlinlang.org/lp/mobile/
[Solid]: https://www.solidjs.com/
