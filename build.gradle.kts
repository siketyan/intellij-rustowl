plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("com.ncorti.ktfmt.gradle") version "0.25.0"
}

group = "jp.s6n.idea"

version = "0.4.1"

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()

    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        rustRover("2025.3.1")
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252.25557"
            untilBuild = "253.*"
        }
    }

    pluginVerification { ides { recommended() } }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing { token = providers.environmentVariable("PUBLISH_TOKEN") }
}

ktfmt { kotlinLangStyle() }
