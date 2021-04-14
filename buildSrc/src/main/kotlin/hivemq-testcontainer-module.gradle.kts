plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.github.hierynomus.license")
    id("com.github.sgtsilvio.gradle.utf8")
    id("com.github.sgtsilvio.gradle.metadata")
    id("com.github.sgtsilvio.gradle.javadoc-links")
}

group = "com.hivemq"

metadata {
    organization {
        name.set("HiveMQ and the HiveMQ Community")
        url.set("https://www.hivemq.com/")
    }
    license {
        apache2()
    }
    developers {
        developer {
            id.set("YW")
            name.set("Yannick Weber")
            email.set("yannick.weber@hivemq.com")
        }
    }
    github {
        org.set("hivemq")
        repo.set("hivemq-testcontainer")
        issues()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withJavadocJar()
    withSourcesJar()
}

tasks.compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.javadoc {
    exclude("**/internal/**")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:${property("junit4.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit5.version")}")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit5.version")}")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:${property("junit5.version")}")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed", "passed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    val outputCache = mutableListOf<String>()
    addTestOutputListener { _, outputEvent ->
        outputCache.add(outputEvent.message)
        while (outputCache.size > 1000) {
            outputCache.removeAt(0)
        }
    }
    addTestListener(object : TestListener {
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) = outputCache.clear()
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            if (result.resultType == TestResult.ResultType.FAILURE && outputCache.size > 0) {
                println()
                println(" Output of ${testDescriptor.className}.${testDescriptor.name}:")
                outputCache.forEach { print(" > $it") }
            }
        }
    })
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

signing {
    val signKey: String? by project
    val signKeyPass: String? by project
    useInMemoryPgpKeys(signKey, signKeyPass)
    sign(publishing.publications["maven"])
}

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}