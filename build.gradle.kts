import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("com.ncorti.ktfmt.gradle") version "0.27.0"
}

group = "jp.s6n.idea"

version = "0.6.0"

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()

    intellijPlatform { defaultRepositories() }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    intellijPlatform {
        rustRover("2025.3.6")
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
            untilBuild = "262.*"
        }
    }

    pluginVerification {
        ides {
            recommended()

            // Also verify that the plugin can be installed on IDEs other than RustRover, including
            // free ones. Selects the IntelliJ IDEA releases within the since/until builds.
            select { types = listOf(IntelliJPlatformType.IntellijIdea) }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing { token = providers.environmentVariable("PUBLISH_TOKEN") }
}

ktfmt { kotlinLangStyle() }
