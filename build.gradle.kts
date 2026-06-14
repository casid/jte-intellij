import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val intellijBaselineVersion = "2026.1.1"
val intellijSinceBuild = "261"
val intellijVerificationVersions = listOf("2026.1.1")

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // See https://github.com/JetBrains/intellij-platform-gradle-plugin
    intellijPlatform {
        intellijIdea(intellijBaselineVersion)

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        pluginVerifier()
        zipSigner()

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "org.jusecase.jte-intellij"
        name = "jte"
        version = "2.2.4"
    }
    projectName = "jte-intellij"
    publishing {
        token = System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken")
    }
    pluginVerification {
        ides {
            intellijVerificationVersions.forEach { version ->
                create(IntelliJPlatformType.IntellijIdea, version)
            }
        }
    }
}

tasks {
    patchPluginXml {
        sinceBuild = provider { intellijSinceBuild }
        untilBuild = provider { null }
    }
}
