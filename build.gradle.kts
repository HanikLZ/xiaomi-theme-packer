plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.3.10"
    id("org.openjfx.javafxplugin") version "0.0.5"
    id("com.google.osdetector") version "1.6.0"
}

application {
    mainClassName = "org.mdvsc.tools.xiaomi.skin/org.mdvsc.tools.xiaomi.skin.Main"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        destinationDir = compileJava.get().destinationDir
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.3"
            languageVersion = "1.3"
        }
    }
    create<Exec>("jlink") {
        dependsOn(clean, jar)
        workingDir("build")
        val javaHome = try { property("org.gradle.java.home").toString() } catch (_: Exception) { System.getenv("JAVA_HOME") }
        val fxJmods = try { property("path.to.fx.mods").toString() } catch (_: Exception) { System.getenv("PATH_TO_FX_MODS") } 
        commandLine(
                "$javaHome/bin/jlink",
                "--module-path",
                "libs${File.pathSeparatorChar}$fxJmods",
                "--add-modules",
                moduleName,
                "--output",
                moduleName,
                "--strip-debug",
                "--compress",
                "2",
                "--no-header-files",
                "--no-man-pages")
    }
}

javafx {
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml")
}

repositories {
    jcenter()
    mavenCentral()
    maven("http://dl.bintray.com/kotlin/kotlin-dev")
}

dependencies {
    val javafxVersion = "12-ea+2"
    val platformClassifier = when(osdetector.os) {
        "osx" -> "mac"
        "windows" -> "win"
        else -> osdetector.os ?: ""
    }
    implementation("org.openjfx", "javafx-base", javafxVersion, classifier = platformClassifier)
    implementation("org.openjfx", "javafx-graphics", javafxVersion, classifier = platformClassifier)
    implementation("org.openjfx", "javafx-fxml", javafxVersion, classifier = platformClassifier)
    implementation("org.openjfx", "javafx-controls", javafxVersion, classifier = platformClassifier)
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", "1.3.20-dev-2214", classifier = "modular")
    // implementation("org.apache.commons", "commons-lang3", "3.8.1")
    // implementation("io.reactivex.rxjava2", "rxjavafx", "2.2.2")
    // implementation("io.reactivex.rxjava2", "rxjava", "2.2.4")
    testImplementation("org.testng", "testng", "7.0.0-beta1")
}