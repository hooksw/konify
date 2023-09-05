plugins {
    kotlin("jvm")

    id("com.google.devtools.ksp")

//    id("com.vanniktech.maven.publish")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
}
