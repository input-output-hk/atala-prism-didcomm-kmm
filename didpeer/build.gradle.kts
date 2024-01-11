import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target
import java.net.URL

val currentModuleName: String = "DIDCommDIDPeer"
val os: OperatingSystem = OperatingSystem.current()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.21"
    id("com.android.library")
    id("org.jetbrains.dokka")
}

/**
 * The `javadocJar` variable is used to register a `Jar` task to generate a Javadoc JAR file.
 * The Javadoc JAR file is created with the classifier "javadoc" and it includes the HTML documentation generated
 * by the `dokkaHtml` task.
 */
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
    }
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        withSourcesJar()
        publishing {
            publications {
                withType<MavenPublication> {
                    artifact(javadocJar)
                }
            }
        }
    }
    iosX64 {
        binaries.framework {
            baseName = currentModuleName
        }
    }
    iosArm64 {
        binaries.framework {
            baseName = currentModuleName
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = currentModuleName
        }
    }
    js(IR) {
        this.moduleName = currentModuleName
        this.binaries.library()
        this.useCommonJs()
        generateTypeScriptDefinitions()
        this.compilations["main"].packageJson {
            this.version = rootProject.version.toString()
        }
        this.compilations["test"].packageJson {
            this.version = rootProject.version.toString()
        }
        browser {
            this.webpackTask {
                this.output.library = currentModuleName
                this.output.libraryTarget = Target.VAR
            }
            this.testTask {
                this.useKarma {
                    this.useChromeHeadless()
                }
            }
        }
        nodejs {
            this.testTask {
                this.useKarma {
                    this.useChromeHeadless()
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.iohk.atala.prism.apollo:multibase:1.0.2")
                implementation("io.iohk.atala.prism.apollo:varint:1.0.2")
                implementation("io.iohk.atala.prism.apollo:base64:1.0.2")
                implementation("io.iohk.atala.prism.apollo:base58:1.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("com.squareup.okio:okio:3.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val androidMain by getting
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val jsMain by getting
        val jsTest by getting
        val iosMain by getting
        val iosTest by getting

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }

    // Enable the export of KDoc (Experimental feature) to Generated Native targets (Apple, Linux, etc.)
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations.getByName("main") {
            compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
        }
    }
}

android {
    namespace = "io.iohk.atala.prism.didcomm.didpeer"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    /**
     * Because Software Components will not be created automatically for Maven publishing from
     * Android Gradle Plugin 8.0. To opt-in to the future behavior, set the Gradle property android.
     * disableAutomaticComponentCreation=true in the `gradle.properties` file or use the new
     * publishing DSL.
     */
    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}

// Dokka implementation
tasks.withType<DokkaTask>().configureEach {
    moduleName.set(currentModuleName)
    moduleVersion.set(rootProject.version.toString())
    description = "This is a Kotlin Multiplatform Library for Mercury DIDPeer"
    pluginConfiguration<org.jetbrains.dokka.base.DokkaBase, org.jetbrains.dokka.base.DokkaBaseConfiguration> {
        customAssets = listOf(rootDir.resolve("Logo.png"))
    }
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(11)
            languageVersion.set("1.8.20")
            apiVersion.set("2.0")
            includes.from(
                "docs/DIDPeer.md"
            )
            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/input-output-hk/atala-prism-didcomm-kmm/tree/main/src"))
                remoteLineSuffix.set("#L")
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/latest/jvm/stdlib/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx.serialization/"))
            }
            externalDocumentationLink {
                url.set(URL("https://api.ktor.io/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx-datetime/"))
                packageListUrl.set(URL("https://kotlinlang.org/api/kotlinx-datetime/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx.coroutines/"))
            }
        }
    }
}

afterEvaluate {
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.withType<Sign>())
    }
    if (tasks.findByName("lintAnalyzeDebug") != null) {
        tasks.named("lintAnalyzeDebug") {
            this.enabled = false
        }
    }
    if (tasks.findByName("lintAnalyzeRelease") != null) {
        tasks.named("lintAnalyzeRelease") {
            this.enabled = false
        }
    }
}
