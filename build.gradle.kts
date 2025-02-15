plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.10"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "jp.s6n.idea"
version = "0.1.0"

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    rustRover("2024.3.4")
    pluginVerifier()
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "243"
      untilBuild = "243.*"
    }
  }

  pluginVerification {
    ides {
      recommended()
    }
  }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
  }
}
