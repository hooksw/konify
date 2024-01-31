plugins {
    kotlin("multiplatform") version "2.0.0-Beta2" apply false
    kotlin("jvm") version "2.0.0-Beta2" apply false

    kotlin("android") version "2.0.0-Beta2" apply false

    id("com.android.library") version "8.0.2" apply false
    id("com.android.application") version "8.0.2" apply false

//    id("com.google.devtools.ksp") version "2.0.0-Beta2-1.0.13" apply false
//    id("org.jetbrains.dokka") version "1.8.20" apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0-Beta2")
    }
}