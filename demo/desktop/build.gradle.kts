plugins {
    kotlin("jvm")
}

kotlin {
//    jvm {
//        compilations.all {
//            kotlinOptions.jvmTarget = "11"
//        }
//    }
//    sourceSets {
//        val jvmMain by getting {
//            dependencies {
//
//            }
//        }
//        val jvmTest by getting
//    }
}

dependencies {
    implementation(project(":demo:common"))
}

//compose.desktop {
//    application {
//        mainClass = "MainKt"
//        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
//            packageName = "jvm"
//            packageVersion = "1.0.0"
//        }
//    }
//}
