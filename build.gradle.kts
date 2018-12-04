plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.3.10"
    // id("org.openjfx.javafxplugin") version "0.0.5"
    id("com.google.osdetector") version "1.6.0"
}

val moduleName = "org.mdvsc.tools.xiaomi.skin"
val modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "kotlin.stdlib")
val javaHome = System.getProperty("java.home")

application {
    mainClassName = "$moduleName.ApplicationStarter"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        inputs.property("moduleName", moduleName)
        doFirst {
            options.compilerArgs = listOf(
                    "--module-path", classpath.asPath,
                    "--patch-module", "$moduleName=${sourceSets["main"].output.asPath}"
            )
            classpath = files()
        }
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.3"
            languageVersion = "1.3"
        }
    }

    named<JavaExec>("run") {
        doFirst {
            jvmArgs = listOf(
                    "--module-path",
                    listOf(configurations.runtimeClasspath.get().asPath, jar.get().archivePath).joinToString(File.pathSeparator)
            ).plus(modules.flatMap { listOf("--add-modules", it) })
        }
    }

    create<Exec>("jlink") {
        dependsOn(jar)
        val outputDir by extra("$buildDir/jlink")
        doFirst {
            delete(outputDir)
            commandLine("$javaHome/bin/jlink",
                    "--module-path",
                    listOf("$javaHome/jmods/", configurations.runtimeClasspath.get().asPath, jar.get().archivePath).joinToString(File.pathSeparator),
                    "--add-modules",
                    moduleName,
                    "--output",
                    outputDir,
                    "--launcher",
                    "launch=$moduleName/${application.mainClassName}",
                    "--strip-debug",
                    "--compress",
                    "2",
                    "--no-header-files",
                    "--no-man-pages")
        }
    }
}

/*
javafx {
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml")
}
*/

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
    testImplementation("org.testng", "testng", "7.0.0-beta1")
}