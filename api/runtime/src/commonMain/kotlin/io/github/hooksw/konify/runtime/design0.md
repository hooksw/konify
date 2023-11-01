### define a component

```kotlin
@View
fun Component(style: Style, event: Event, int: Int, list: List<Int>) {
    var state by signal(0)
    LaunchEffect{
        print(int+state())
    }
    Child(state(), event, int+1, list.map { "" }){
        Text(state.toString)
    }
}
//trans to
class ComponentProp(private val getInt:()->Int,private val getList:()->List<Int>){
    val int:Int
        get() =getInt()
    val list:List<Int>
        get() =getList()
}
@View
fun Component(style: Style, event: Event,prop: ComponentProp,node:ViewNode) {
    var state by signal(0)
    LaunchEffect {
        delay(200)
        print(prop.int+state())
    }
    
    Child(state, event,{props.int+1}, {props.list.map{it+1}})
}


@View
fun Parent() {
    var clickable by mutableStateOf(true)
    var a by mutableStateOf(0)
    val int by compute {
        a + 1
    }
    Component(
        Style {
            background=Color.RED
            border[bottom]{
                width=3.dp
            }
        },
        Event.clickable(clickable){ a++ },
        int,
        emptyList()
    )
}

```