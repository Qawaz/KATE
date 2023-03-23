pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion).apply(false)
        kotlin("android").version(kotlinVersion).apply(false)
        kotlin("multiplatform").version(kotlinVersion).apply(false)
        kotlin("jvm").version(kotlinVersion).apply(false)
        id("com.android.application").version(extra["agp.version"] as String).apply(false)
        id("com.android.library").version(extra["agp.version"] as String).apply(false)
        id("org.jetbrains.compose").version(extra["compose.version"] as String).apply(false)
    }
}
rootProject.name = "KTE"

include(":demo:android")
include(":demo:desktop")
include(":demo:common")
include(":demo:web")
include(":kte")