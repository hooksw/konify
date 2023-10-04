### define a component

```kotlin
@View
fun Component(style: Style, event: Event, int: Int, list: List<Int>) {
    var state by mutableStateOf(0)
    LaunchEffect(int, state) {
        print(int+state)
    }
    Child(state, event, int+1, list.map { "" }){
        Text(state.toString)
    }
}
//trans to
@View
fun Component(style: Style, event: State<Event>, int: State<Int>, list: State<List<Int>>,node:ViewNode) {
    var state by mutableStateOf(0)
    LaunchEffect(int, state) {
        print(int.value+state.value)
    }
    Child(state, event, int.map{it+1}, list.map{it.map{""}})
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