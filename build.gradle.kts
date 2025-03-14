plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.3.0"
    id("com.ncorti.ktfmt.gradle") version "0.22.0"
}

group = "jp.s6n.idea"

version = "0.2.2"

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()

    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        rustRover("251.23774.119") // 2025.1 EAP 6
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
            untilBuild = "251.*"
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
