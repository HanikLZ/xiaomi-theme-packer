pluginManagement {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.osdetector") {
                useModule("com.google.gradle:osdetector-gradle-plugin:1.6.0")
            }
        }
    }
}
rootProject.name = "xiaomi-skin"

