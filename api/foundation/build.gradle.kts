plugins {
    id("build.KmpCommon")
}

kotlin {
    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":api:runtime"))
            }
        }
    }
}
