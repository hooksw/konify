plugins {
    kotlin("jvm") version "1.9.10"
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.10")

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("dev.zacsweers.kctfork:core:0.3.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
//
//kotlin {
//    jvmToolchain(8)
//}