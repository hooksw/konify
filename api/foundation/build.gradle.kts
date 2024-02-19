plugins {
    id("build.KmpPureLogic")
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
