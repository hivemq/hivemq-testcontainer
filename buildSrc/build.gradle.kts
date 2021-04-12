plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("gradle.plugin.com.hierynomus.gradle.plugins:license-gradle-plugin:${property("license.version")}")
    implementation("gradle.plugin.com.github.sgtsilvio.gradle:gradle-utf8:${property("utf8.version")}")
    implementation("gradle.plugin.com.github.sgtsilvio.gradle:gradle-metadata:${property("metadata.version")}")
    implementation("gradle.plugin.com.github.sgtsilvio.gradle:gradle-javadoc-links:${property("javadoc-links.version")}")
}