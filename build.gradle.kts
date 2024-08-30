import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
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
        intellijIdeaCommunity("2024.2.1")

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        pluginVerifier()
        zipSigner()

        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "org.jusecase.jte-intellij"
        name = "jte"
        version = "2.2.0"
    }
    projectName = "jte-intellij"
    publishing {
        token = System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken")
    }
    pluginVerification {
        ides {
            ide("2024.2.1")
        }
    }
}

tasks {
    patchPluginXml {
        untilBuild = provider { null }
    }
}
