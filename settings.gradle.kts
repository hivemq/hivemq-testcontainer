rootProject.name = "hivemq-testcontainer"

include("core")
project(":core").name = "hivemq-testcontainer-core"

include("junit4")
project(":junit4").name = "hivemq-testcontainer-junit4"

include("junit5")
project(":junit5").name = "hivemq-testcontainer-junit5"

pluginManagement {
    plugins {
        id("io.github.gradle-nexus.publish-plugin") version "${extra["plugin.nexus-publish.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("com.github.sgtsilvio.gradle.utf8") version "${extra["plugin.utf8.version"]}"
        id("com.github.sgtsilvio.gradle.metadata") version "${extra["plugin.metadata.version"]}"
        id("com.github.sgtsilvio.gradle.javadoc-links") version "${extra["plugin.javadoc-links.version"]}"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
