plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    compileOnly(project(":api:runtime"))
    compileOnly(libs.kotlin.compilerEmbeddable)

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation(libs.kcp.test)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}
//
//kotlin {
//    jvmToolchain(8)
//}