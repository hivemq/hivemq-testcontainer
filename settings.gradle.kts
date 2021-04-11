rootProject.name = "hivemq-testcontainer"

for (module in listOf("core", "junit4", "junit5")) {
    include("${rootProject.name}-$module")
    project(":${rootProject.name}-$module").projectDir = file(module)
}

pluginManagement {
    plugins {
        id("io.github.gradle-nexus.publish-plugin") version "${extra["plugin.nexus-publish.version"]}"
    }
}