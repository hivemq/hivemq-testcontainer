plugins {
    id("hivemq-testcontainer-module")
}

description = "JUnit 4 API for testing HiveMQ Extensions and Java MQTT Applications."

metadata {
    readableName.set("HiveMQ Testcontainer JUnit4")
}

dependencies {
    api(project(":hivemq-testcontainer-core"))

    compileOnly("junit:junit:${property("junit4.version")}")
    compileOnly("com.hivemq:hivemq-extension-sdk:${property("hivemq-extension-sdk.version")}")

    testImplementation(project(":hivemq-testcontainer-core").dependencyProject.sourceSets.test.get().output)
    testImplementation("com.hivemq:hivemq-extension-sdk:${property("hivemq-extension-sdk.version")}")
    testImplementation("com.hivemq:hivemq-mqtt-client:${property("hivemq-mqtt-client.version")}")
    testImplementation("org.apache.httpcomponents:httpclient:${property("httpclient.version")}")
    testImplementation("ch.qos.logback:logback-classic:${property("logback.version")}")
}
