import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    id("org.jetbrains.dokka") version "1.7.10"
    id("maven-publish")
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
        classpath("com.android.tools.build:gradle:7.2.2")
    }
}

allprojects {
    version = "1.0.5-alpha"
    group = "io.iohk.atala.prism.didcomm"

    repositories {
        google()
        mavenCentral()
        maven {
            this.url = uri("https://maven.pkg.github.com/input-output-hk/atala-prism-apollo")
            credentials {
                this.username = System.getenv("ATALA_GITHUB_ACTOR")
                this.password = System.getenv("ATALA_GITHUB_TOKEN")
            }
        }
    }

    apply(plugin = "org.gradle.maven-publish")

    publishing {
        repositories {
            maven {
                this.name = "GitHubPackages"
                this.url = uri("https://maven.pkg.github.com/input-output-hk/atala-prism-didcomm-kmm")
                credentials {
                    this.username = System.getenv("ATALA_GITHUB_ACTOR")
                    this.password = System.getenv("ATALA_GITHUB_TOKEN")
                }
            }
        }
    }
}

rootProject.plugins.withType(NodeJsRootPlugin::class.java) {
    rootProject.extensions.getByType(NodeJsRootExtension::class.java).nodeVersion = "16.17.0"
}

tasks.dokkaGfmMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
