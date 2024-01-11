import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import java.util.Base64

val publishedMavenId: String = "io.iohk.atala.prism.didcomm"

plugins {
    id("org.jetbrains.dokka") version "1.9.10"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")
    }
}

group = publishedMavenId

allprojects {
    group = publishedMavenId

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

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.signing")

    publishing {
        publications {
            withType<MavenPublication> {
                groupId = publishedMavenId
                artifactId = project.name
                version = project.version.toString()
                pom {
                    name.set("Atala PRISM DIDPeer")
                    description.set("DIDComm V2 & Peer:DID")
                    url.set("https://docs.atalaprism.io/")
                    organization {
                        name.set("IOG")
                        url.set("https://iog.io/")
                    }
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/input-output-hk/atala-prism-didcomm-kmm")
                    }
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("hamada147")
                            name.set("Ahmed Moussa")
                            email.set("ahmed.moussa@iohk.io")
                            organization.set("IOG")
                            roles.add("developer")
                            url.set("https://github.com/hamada147")
                        }
                        developer {
                            id.set("elribonazo")
                            name.set("Javier Ribó")
                            email.set("javier.ribo@iohk.io")
                            organization.set("IOG")
                            roles.add("developer")
                        }
                        developer {
                            id.set("cristianIOHK")
                            name.set("Cristian Gonzalez")
                            email.set("cristian.castro@iohk.io")
                            organization.set("IOG")
                            roles.add("developer")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://input-output-hk/atala-prism-didcomm-kmm.git")
                        developerConnection.set("scm:git:ssh://input-output-hk/atala-prism-didcomm-kmm.git")
                        url.set("https://github.com/input-output-hk/atala-prism-didcomm-kmm")
                    }
                }
                signing {
                    val base64EncodedAsciiArmoredSigningKey: String =
                        System.getenv("BASE64_ARMORED_GPG_SIGNING_KEY_MAVEN") ?: ""
                    val signingKeyPassword: String =
                        System.getenv("SIGNING_KEY_PASSWORD") ?: ""
                    useInMemoryPgpKeys(
                        String(
                            Base64.getDecoder().decode(
                                base64EncodedAsciiArmoredSigningKey.toByteArray()
                            )
                        ),
                        signingKeyPassword
                    )
                    sign(this@withType)
                }
            }
        }
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

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
    }
}

rootProject.plugins.withType(NodeJsRootPlugin::class.java) {
    rootProject.extensions.getByType(NodeJsRootExtension::class.java).nodeVersion = "16.17.0"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
