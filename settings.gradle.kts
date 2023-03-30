pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.github.com/Qawaz/kte") {
            credentials {
                username = (System.getenv("GPR_USER")).toString()
                password = (System.getenv("GPR_API_KEY")).toString()
            }
        }
    }
    plugins {
        val kotlinVersion = extra["kotlin_version"] as String
        kotlin("android").version(kotlinVersion).apply(false)
        id("com.android.application").version(extra["agp_version"] as String).apply(false)
        id("com.android.library").version(extra["agp_version"] as String).apply(false)
        id("org.jetbrains.compose").version(extra["compose.jb.version"] as String).apply(false)
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "KATE-DEV"
include(":desktop")
include(":web")
include(":kate")