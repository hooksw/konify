# Konify

[![zh](https://img.shields.io/badge/%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87%E7%89%88%E6%9C%AC-6495ED)](https://github.com/hooksw/konify/blob/master/README-ZH.md)

> The name comes from the following words: Kotlin, construct, notify

Konify is a high-performance, compact and highly scalable library inspired by [Solid] and [Compose] for building responsive Android, Web and iOS applications using Kotlin.

You can think of functions of Konify as constructors, they are executed only once, without recomposition.

**Currently, in the design stage.**

---

#### Differences from Compose

[Compose] is good, but still has the following issues:

* There are performance issues in some cases, mostly related to unnecessary recompositions.
* Needs to be marked stable everywhere.
* Compose UI cannot be used in the Web DOM.

Therefore, we plan to achieve the following goals:

* Written like Compose, works like Native elements, no need to recompose.
* Keep it extensible so that we can adapt it to other scenarios (e.g. using Skia as a backend implementation, or implementing other tree-structured reactive systems).



## Determined part

### Overview

A Component should be written as follows:

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
### Behind the responsive mechanism (pseudocode)
The following code shows how the current reactive system works (without the need for recomposition).

```kotlin
//Computation represents the function that needs to be executed when any of the dependent states changes (it can also be called calculation, which is where the name comes from)
classComputation(
   private val fn:()->Unit
){
   val listener={
      //Unbind all signals before each execution of Computation to prevent repeated additions
      //As for why the Set data structure is not used, because this can achieve automatic tracking.
      //For example, the callback of a computation is textView.text=if(boolSignal) signal1 else signal2,
      //When boolSignal is true, due to conditional branch execution, only boolSignal and signal1 will be bound to this computation.
      //When boolSignal becomes false, after first unbinding and then rebinding, signal1 will no longer track this computation, but boolSignal and signal2 will track it.
      signals.forEach{
         it.observers.remove(this)
      }
      //12. Rebind. Note that fn is textView.text=str() at this time, so signal.getValue will be called again when currentComputation is set.
      withComputation(this,fn)
   }
   val signals:MutableList<Signal> =mutableListOf()
}

class Signal(private var backValue:Any){
   var value:Any
      get(){
         //9. At this time computation is null, get the value directly
         if(currentComputation!=null){
            //6. Double-binding is performed at this time
            //13. Rebind so that the bound computation will be called again the next time the signal changes.
            currentComputation.states.add(this)
            observers.add(currentComputation)
         }
         return backValue
      }
      set(value){
         backValue=value
         //11. Traverse and execute computation
         observers.forEach{
            it.listener()
         }
      }
   val observers:MutableList<Computation> =mutableListOf()
}
var currentComputation:Computation?=null

//2. Enter inside bind
fun bind(block:()->Unit){
   withComputation(Computation(block),block)
}

fun withComputation(computation: Computation, run: () -> Unit){
   //Save the current Computation. If there is no memo (derivedState in compose), it is usually empty.
   vallasComputation=currentComputation
   //3. Set the current Computation and prepare for double binding
   currentComputation=computation
   //4. Perform the first binding
   run()
   //Restore value, in the current example, it is always null
   currentComputation=lasComputation
}

//Usage example
fun counter(){
   val count = Signal(0)
   Button(()->count.value.toString()){
      //10. Call signal.getValue
      count.value=
              //8. Enter signal.getValue
         count.value+1
   }
}
//In order to achieve responsiveness, we must use the compiler plug-in to convert types other than lambda types into lazy acquisition forms, so that signal.value can accurately obtain computation and bind it to each other.
fun Button(str:()->String,onClick:()->Unit){
   val textView=TextView()
   //7. After the binding is completed, onClick is called, that is, when count.value is changed.
   textView.onClick=onClick
   //1. At this time, enter bind to implement two-way binding of state and computation, instead of the traditional one-way binding of signal.bind{}
   bind{
      //5. In the callback, you can obtain the pair signal.value, which is count.value, in the form of ()->T at this time, and start binding to it, and then execute a callback
      textView.text=str()
   }
}

//ViewModel:
//In a single-threaded scenario without computation, computation is always null, so signal.getValue does not have responsive features at this time.
//Therefore it is best to use flow instead in viewModel

```
### State

Similar to Compose, there are two types of State: `Signal` (similar to `state` in Compose), `Memo` (similar to `derivedState` in Compose)

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
@Compose
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


## Undetermined part

### Node system

This part is very important because it involves many parts: basic architecture, UI node tree, extensibility, debugging information, etc.

We have the following ideas:

1. **Use global variables to manage the current node (similar to SolidJS’s approach)**

   ```kotlin
   internal var currentNode:Node?=null
   funComponentA(){
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
2. **Inject node parameters**

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
3. **Inject node tree parameters (similar to Compose’s approach)**

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
There is still some debate. If you have any comments or other solutions, please report them here [https://github.com/hooksw/konify/issues/1](https://github.com/hooksw/konify/issues/1).

Our goal is to maintain maintainability, scalability, and high performance.

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

### Style

We don't use a Modifier system, but a CSS-like style system.

The current design is as follows:


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
Callback blocks in `Style` functions only support value assignment and function calls, and can be extended through extension functions.

**Q: Do we need to support operations similar to inline styles and class styles in CSS, such as `val style = style1 + Style{//...}`? **

## To-do list
1. Determine the overall architecture, improve the core code, and modify the compiler plug-in based on it.
2. Write relevant tests.
3. Determine the style attributes to be implemented, build its platform implementation, and design its DSL.
4. Design and implement event systems, such as gesture events.
5. Implement basic UI components: Text, Image, TextInput, FlexLayout, FrameLayout, Buttons.
6. Design and implement animation system.
7. Implement advanced UI components: LazyLayout, LazyList, Pager, AsyncImage.
8. (Optional) Provide mechanisms for implementing custom layouts and views.
9. Design and implement routing mechanism.
10. (Optional) Design and implement IDE plug-ins to enhance development.
11. (Optional) Support hot reload.

## Supported platforms

We plan to support Android and Web DOM first (the author can't afford a Mac).

[compose]: https://developer.android.com/jetpack/compose
[kmm]: https://kotlinlang.org/lp/mobile/
[Solid]: https://www.solidjs.com/