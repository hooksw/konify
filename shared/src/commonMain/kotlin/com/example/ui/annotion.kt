package com.example.ui

//标记当前函数为一个视图组件，会向函数参数注入ViewNode，并生成新的ViewNode来控制当前函数状态，目前只起标记左右，因为编译部分未实现
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class ViewMarker

//标记为当前函数传入ViewNode，除此之外不会有其他作用，用于控制所属ViewNode的状态
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class ReadOnlyViewNode

//TODO
annotation class Observe