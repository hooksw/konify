package com.example.ui

fun px2dp(float: Float): Int {
    val scale: Float = AndroidContext.resources.displayMetrics.density
    return (float / scale + 0.5f).toInt() // 四舍五入取整
}

fun px2sp(float: Float): Int {
    val scale: Float = AndroidContext.resources.displayMetrics.scaledDensity;
    return (float / scale + 0.5f).toInt()
}
fun dp2px(float: Float): Int {
    val scale: Float = AndroidContext.resources.displayMetrics.density
    return (float * scale + 0.5f).toInt() // 四舍五入取整
}

fun sp2px(float: Float): Int {
    val scale: Float = AndroidContext.resources.displayMetrics.scaledDensity;
    return (float * scale + 0.5f).toInt()
}