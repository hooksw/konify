plugins {
    kotlin("jvm") version "2.0.0-Beta2"
}

dependencies {
    compileOnly(project(":api:runtime"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.0-Beta2")

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("dev.zacsweers.kctfork:core:0.4.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
//
//kotlin {
//    jvmToolchain(8)
//}