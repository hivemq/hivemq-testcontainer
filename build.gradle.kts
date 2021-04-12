plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

description = "Testcontainers for testing HiveMQ Extensions and Java MQTT Applications."

nexusPublishing {
    repositories {
        sonatype()
    }
}