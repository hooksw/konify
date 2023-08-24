# Konify

Konify is a library inspired by [Solid] and [Compose] for building reactive Android, iOS, and web
UIs using Kotlin.

**Konify is  not ready for production.**

### Differences from Compose

[Compose] is amazing, but there are still the following problems:

* there are performance issues in some cases, mostly related to some unnecessary recomposition.
* Compose UI cannot be used in web dom, and kotlin/wasm is new and experimental which not ready for
  production.

so we plan to do somethings like:

* builds the UI tree at once, no need to diff with different ui states.
* wraps native UI element, just like what react native do to avoid implement the complicated render
  part and keeps UI native.
* writes like Compose to simplify development

### Documentation

#### overview

the component should code like:

```kotlin
@View
fun Counter() {
    val count = mutableStateOf(1)
    val greaterThan10 = count.map { it > 10 }
    LaunchEffect(greaterThan10) {
        print("greaterThan10:${greaterThan10.value}")
    }
    Row {
        Icon()
        Text(count)
        Switch {
            If(greaterThan10) {
                Button(text = stateOf("Reset"), onClick = { count.value = 0 })
            }
            Else {
                Button(text = stateOf("+"), onClick = { count.value += 1 })
            }
        }
    }
}
```

#### How it works?

```kotlin
@View
fun B() {
}

@View
fun A(params: State<Int>) {
    B()
}

//will be transformed to
fun A(params: State<Int>, $node: ViewNode) {
    val $cnode =$node.createChild()
    B($cnode)
    $node.prepare()
}
```
`ViewNode` is a wrapper for native UI elements(in Android, it wraps `View`, in Web,it wraps `HtmlElement`)
You can see a annotated function as a ViewNode tree builder.

#### State

As we only build the UI once, there's no recomposition.
So we have to declare all state that may change with `State<XXX>`,including state and function parameter
`mutableStateOf` create a mutable State like,
`stateOf` create a state that is readonly, it just is used to match the State type

#### Effect
we provide 2 functions to manage effects
`LaunchEffect` receive a list of states(can be empty) and a suspend callback, when one of the states changes, the callback is invoked and previous job will be cancel
```kotlin
LaunchEffect(state){ 
  delay(2000)
  print(state.value.toString())
}
```
`DisposableEffect` receive a list of states(can be empty) and a onClean callback, when one of the states changes, the callback is invoked and previous cleanUp callback will be called
```kotlin
DisposableEffect(state){
  register()
  onDispose{
    unregister()
  }
}
```

#### Control Flow

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

since Konify only invoke there functions once, we cannot simply use kotlin control flow keywords, but we can use `If` and `Else` function inside `Switch` block:
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
`If` receive a State<Boolean> state and a` @View ()->Unit ` callback.
in the code above, when one of stateA,stateB,stateC changes, `Switch` will check the state in order of declaration, the first callback that state value is `true when will execute.

#### ViewLocal
If you are familiar with Compose, you must know `CompositionLocal`.in Konify ,it's `ViewLocal`.
The main difference of usage is that the value of ViewLocal should be a State type, even if it won't be changed, it's a design compromise.
You can use it like:
```kotlin
val LocalCount = ViewLocalOf(1)

@View
fun A() {
  val counter = mutableStateOf(0)
  LaunchEffect{
      while (true){
          counter.value+=1
      }
  }
  ViewLocalProvider(LocalCount provides counter){
      B()
  }
  ViewLocalProvider(LocalCount provides 3){
      C()
  }
}

@View
fun B(){
  //get a mutableState whose init value is 0 
  val countState:State<Int> = LocalCount.current
  
}
@View
fun C(){
  //get a state whose value is 3 and will never be changed
  val countState:State<Int> = LocalCount.current
  
}
```
#### How to Wrap Native View

first we declare a expect function, for Example,Text()
```kotlin
expect fun Text(text:State<String>)
```
then we can implement in different platform:
```kotlin
//Android

@View
actual fun Text(
  text: State<String>
) {
  val textView = createTextView(
    text = text.value
  )
  registerPlatformView(textView)
  text.observe{
      textView.text=it
  }
}
```

#### Modifier
We haven't decide how to design the Modifier system.

### Supported Platform
We plan to support Android and Web dom first.

### About IOS?
The author doesn't have a job and is too poor to buy a Mac, if you are interested in this project, welcome to contribute.

[compose]: https://developer.android.com/jetpack/compose

[kmm]: https://kotlinlang.org/lp/mobile/

[Solid]: https://www.solidjs.com/