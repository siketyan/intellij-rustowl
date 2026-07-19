plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("com.ncorti.ktfmt.gradle") version "0.25.0"
}

group = "jp.s6n.idea"

version = "0.5.0"

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()

    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        rustRover("2025.3.6")
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
            untilBuild = "262.*"
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
