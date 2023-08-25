plugins {
    kotlin("multiplatform") version "1.9.0" apply false
    kotlin("jvm") version "1.9.0" apply false
    kotlin("android") version "1.9.0" apply false

    id("org.jetbrains.dokka") version "1.8.20" apply false

    id("com.android.library") version "8.0.2" apply false
    id("com.android.application") version "8.0.2" apply false

    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}
