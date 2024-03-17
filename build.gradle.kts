plugins {
  kotlin("jvm") version "1.9.22"
  application

  id("com.diffplug.spotless") version "6.25.0"

  kotlin("plugin.serialization") version "1.9.22"
}

group = "com.hopskipnfall"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

val ktorVersion = "2.3.9"

dependencies {
  testImplementation(kotlin("test"))
  implementation(kotlin("script-runtime"))

  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-cbor:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-protobuf:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(8) }

application { mainClass.set("MainKt") }

// Formatting/linting.
spotless {
  kotlin {
    target("**/*.kt", "**/*.kts")
    targetExclude("build/", ".git/", ".idea/", ".mvn")
    ktfmt().googleStyle()
  }

  yaml {
    target("**/*.yml", "**/*.yaml")
    targetExclude("build/", ".git/", ".idea/", ".mvn")
    jackson()
  }
}
