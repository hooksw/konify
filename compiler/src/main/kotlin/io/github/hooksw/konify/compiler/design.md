``
1. 更改函数参数。
如果参数类型为SignalMarker或者类型有Stateless注解，跳过
否则按照类型将其修改为Signal<T>或者IntSignal等未包装类型
2. 形参注入ViewNode参数。
3. 增加代码。
如果有ReadyOnly注解，则不增加代码；否则在函数体最开始生成
``val $child=$viewNode.createChild()``代码，在return之前或者函数体结尾插入
``$child.prepare()``
4. 更新调用点
查询函数体内的对应函数，注入``$child``或者``$viewNode``,
跳过参数类型为SignalMarker或者类型有Stateless注解的参数，
然后判断形参是否有直接对signal类型的引用，有则用``derived``否则用``constant``包裹，如果是直接引用没有其他运算的话，直接传入原来的signal即可
``

1
```kotlin
@View
fun a(p:P){
    
}
```
```kotlin
@View
fun a(p:Signal<P>,$viewNode:ViewNode){
    
}
```
2
```kotlin
@View
fun a(p:Int){
    
}
```
```kotlin
@View
fun a(p:IntSignal,$viewNode:ViewNode){
    
}
```
3
```kotlin
@ReadOnly
@View
fun a(p:Int):Int{
    return p+1
}
@ReadOnly
@View
fun b(p:Int):Signal<Int>{
    return derivedIntSignalOf{p+1}
}
```
```kotlin
@ReadOnly
@View
fun a(p:Int):Int{
    return p+1
}
@ReadOnly
@View
fun b(p:Int):IntSignal{
    return derivedIntSignalOf{p+1}
}
```
4
```kotlin
@View
fun a(p:Int){
    var signal by mutableSignalOf("")
    LaunchEffect(signal){
        println(signal)
    }
    b(signal+1,signal,p,p+1)
}
```
```kotlin
@View
fun a(p:IntSignal,$viewNode:ViewNode){
    val $child=$ViewNode.createChild()
    var signal = mutableSignalOf("")
    LaunchEffect(signal){
        println(signal.value)
    }
    b(derivedSignalOf{signal+1},signal,p,derivedSignalOf{p+1})
}
```
5
```kotlin
@View
@ReadyOnly
internal inline fun trans2Origin(p:Int):IntSignal{
    return p
}
fun LaunchEffect(vararg any:Any){
    val b=trans2Origin(any[0])
}
```
```kotlin
@View
@ReadyOnly
internal inline fun trans2Origin(p:Int):IntSignal{
    return p
}
fun LaunchEffect(vararg any:Signal<Any>){
    val b=any[0]
}
```
5
```kotlin
@View
@Native
fun Image(style:Style=Style,event:Event=Event.Empty,src:String){
    val imageView=createImageView()
    regNative(imageView,style,event){
        bind{
            it.src=src
        }
    }
}
```