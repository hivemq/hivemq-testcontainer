plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin")
    id("com.github.hierynomus.license")
    id("com.github.sgtsilvio.gradle.utf8")
    id("com.github.sgtsilvio.gradle.metadata")
    id("com.github.sgtsilvio.gradle.javadoc-links")
}

/* ******************** metadata ******************** */

subprojects {
    group = "com.hivemq"
    description = "Testcontainers for testing HiveMQ Extensions and Java MQTT Applications."

    plugins.apply("com.github.sgtsilvio.gradle.metadata")

    metadata {
        readableName.set("HiveMQ Testcontainer")
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
}


/* ******************** java ******************** */

allprojects {
    plugins.withId("java") {
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        plugins.apply("com.github.sgtsilvio.gradle.utf8")
    }
}


/* ******************** test ******************** */

subprojects {
    plugins.withId("java") {
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

//            /* Log stdout for failed tests */
//            val outputCache = java.util.LinkedList<String>()
//
//            beforeTest { TestDescriptor td -> outputCache.clear() }    // clear everything right before the test starts
//
//            onOutput { TestDescriptor td, TestOutputEvent toe ->       // when output is coming put it in the cache
//                outputCache.add(toe.getMessage())
//                while (outputCache.size() > 1000) outputCache.remove() // if we have more than 1000 lines -> drop first
//            }
//
//            /** after test -> decide what to print */
//            afterTest { TestDescriptor td, TestResult tr ->
//                if (tr.resultType == TestResult.ResultType.FAILURE && outputCache.size() > 0) {
//                    println()
//                    println(" Output of ${td.className}.${td.name}:")
//                    outputCache.each { print(" > $it") }
//                }
//            }
        }
    }
}


/* ******************** jars ******************** */

subprojects {
    plugins.withId("java-library") {
        java {
            withJavadocJar()
            withSourcesJar()
        }

        plugins.apply("com.github.sgtsilvio.gradle.javadoc-links")

        tasks.javadoc {
            exclude("**/internal/**")
        }
    }
}


/* ******************** publishing ******************** */

subprojects {
    plugins.withId("java-library") {
        plugins.apply("maven-publish")
        plugins.apply("signing")

        publishing {
            publications {
                register<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
        }

        signing {
            sign(publishing.publications["maven"])
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}


/* ******************** checks ******************** */

subprojects {
    plugins.apply("com.github.hierynomus.license")

    license {
        header = rootDir.resolve("HEADER")
        mapping("java", "SLASHSTAR_STYLE")
    }
}