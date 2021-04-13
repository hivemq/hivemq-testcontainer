plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

group = "com.hivemq"
description = "Testcontainers for testing HiveMQ Extensions and Java MQTT Applications."

nexusPublishing {
    repositories {
        sonatype()
    }
}