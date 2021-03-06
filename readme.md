# HiveMQ Test Container

![image](https://www.hivemq.com/img/logo-hivemq-testcontainer.png)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hivemq/hivemq-testcontainer-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hivemq/hivemq-testcontainer-core)
[![javadoc](https://javadoc.io/badge2/com.hivemq/hivemq-testcontainer-core/javadoc.svg)](https://javadoc.io/doc/com.hivemq/hivemq-testcontainer-core)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/hivemq/hivemq-testcontainer/CI%20Check/master)](https://github.com/hivemq/hivemq-testcontainer/actions/workflows/check.yml?query=branch%3Amaster)

Automatic starting HiveMQ docker containers for JUnit4 and JUnit5 tests.
This enables testing MQTT client applications and integration testing of custom HiveMQ extensions. 

- Community forum: https://community.hivemq.com/
- HiveMQ website: https://www.hivemq.com/
- MQTT resources:
  - [MQTT Essentials](https://www.hivemq.com/mqtt-essentials/)
  - [MQTT 5 Essentials](https://www.hivemq.com/mqtt-5/)

## Features
- [load user defined HiveMQ images and tags](#add-to-your-project)
- [wait strategy](#wait-strategy)
- [test your MQTT 3 and MQTT 5 client applications](#test-your-mqtt-3-and-mqtt-5-client-application)
- [add a custom hivemq config](#add-a-custom-hivemq-configuration)
- [load an extension from a gradle project](#load-an-extension-from-a-gradle-project)
- [load an extension from a maven project](#load-an-extension-from-a-maven-project)
- [load an extension from a folder](#load-an-extension-from-a-folder)
- [load an extension directly from your code](#load-an-extension-directly-from-code)
- [enable or disable an extension](#enabledisable-an-extension)
- [enable or disable an extension loaded from a folder](#enabledisable-an-extension-loaded-from-a-folder)
- [remove prepackaged HiveMQ Extensions](#remove-prepackaged-hivemq-extensions)
- [set logging level](#set-logging-level)
- [set control center port](#set-control-center-port)
- [debug a directly loaded extension that is running inside the container](#debug-directly-loaded-extensions)
- [put files into the container](#put-files-into-the-container)
    - [put files into hivemq home](#put-a-file-into-hivemq-home)
    - [put files into extension home](#put-files-into-extension-home)
    - [put license files into the license folder](#put-license-files-into-the-container)
- [configure docker resources](#configure-docker-resources)
- [customizing the container](#customize-the-container-further)
    
## Add to your project

### Gradle + JUnit 4

add these dependencies to your `build.gradle`:

````groovy
testImplementation "com.hivemq:hivemq-testcontainer-junit4:1.3.2"
testImplementation "junit:junit:4.13"
````

### Gradle + JUnit 5

````groovy
testImplementation "com.hivemq:hivemq-testcontainer-junit5:1.3.2"
testImplementation "org.junit.jupiter:junit-jupiter-engine:5.6.1"
testRuntimeOnly "org.junit.jupiter:junit-jupiter-api:5.6.1"
````


### Maven + JUnit 4

add these dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>com.hivemq</groupId>
    <artifactId>hivemq-testcontainer-junit4</artifactId>
    <version>1.3.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13</version>
    <scope>test</scope>
</dependency>
```

### Maven + JUnit 5

add these dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>com.hivemq</groupId>
    <artifactId>hivemq-testcontainer-junit5</artifactId>
    <version>1.3.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.6.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.6.1</version>
    <scope>test</scope>
</dependency>
```
    
## User defined HiveMQ image and tag

Per default the 'hivemq/hivemq4' image and the 'latest' tags are used.
You can define a custom image and tag in the constructor:

### JUnit 4

```java
@Rule
final public @NotNull HiveMQTestContainerRule rule 
    = new HiveMQTestContainerRule("hivemq/hivemq-ce", "2020.6");
```

### JUnit 5

```java    
@RegisterExtension
final public @NotNull HiveMQTestContainerExtension extension 
    = new HiveMQTestContainerExtension("hivemq/hivemq-ce", "2020.6");
```
        
## Wait Strategy

Per default the HiveMQ Testcontainer waits for the HiveMQ startup log message, but you can define additional
wait conditions for your HiveMQ Extensions:

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
        new HiveMQTestContainerRule()
            .withExtension(new File("src/test/resources/modifier-extension"))
            .waitForExtension("My Extension Name");
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
        new HiveMQTestContainerExtension()
            .withExtension(new File("src/test/resources/modifier-extension"))
            .waitForExtension("My Extension Name");
```
                
### JUnit 4

```java
private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
    .id("extension-1")
    .name("my-extension")
    .version("1.0")
    .mainClass(MyExtension.class).build();

@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withExtension(hiveMQExtension)
        .waitForExtension(hiveMQExtension);
```

### JUnit 5
```java
private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
    .id("extension-1")
    .name("my-extension")
    .version("1.0")
    .mainClass(MyExtension.class).build();

@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withExtension(hiveMQExtension)
        .waitForExtension(hiveMQExtension);
```

## Test your MQTT 3 and MQTT 5 client application

### JUnit 4

```java
@Rule
final @NotNull HiveMQTestContainerRule rule = new HiveMQTestContainerRule();

@Test
public void test_mqtt() {
    final Mqtt5BlockingClient client = Mqtt5Client.builder()
        .serverPort(rule.getMqttPort())
        .buildBlocking();

    client.connect();
    client.disconnect();
}
```

### JUnit 5

```java
@RegisterExtension
final @NotNull HiveMQTestContainerExtension extension = new HiveMQTestContainerExtension();

@Test
public void test_mqtt() {
    final Mqtt5BlockingClient client = Mqtt5Client.builder()
        .serverPort(extension.getMqttPort())
        .buildBlocking();

    client.connect();
    client.disconnect();
}
```
    
## Add a custom HiveMQ configuration

### JUnit 4

```java
@Rule
final @NotNull HiveMQTestContainerRule rule = 
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withHiveMQConfig(new File("src/test/resources/config.xml"));
```

### JUnit 5

```java
@RegisterExtension
final @NotNull HiveMQTestContainerExtension extension = 
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withHiveMQConfig(new File("src/test/resources/config.xml"));
```
            
## Load an extension from a Gradle project

You can package and load an extension from a Gradle project, by providing the
location of the Gradle project:

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withExtension(new GradleHiveMQExtensionSupplier(new File("path/to/extension/")).get());
```

If your current project is the HiveMQ Extension you want to load into the HiveMQ Testcontainer, you can do it by:
* adapting your `build.gradle.kts`:

```kotlin
val prepareExtensionTest by tasks.registering(Sync::class) {
    group = "hivemq extension"
    description = "Prepares the extension for integration testing."

    from(tasks.hivemqExtensionZip.map { extensionZip -> zipTree(extensionZip.archiveFile) })
    into(buildDir.resolve("hivemq-extension-test"))
}

tasks.test {
    useJUnit()
    dependsOn("prepareExtensionTest")
}
```

* adding the target directory of the `hivemqExtensionZip` / `hivemqEnterpriseExtensionZip` task

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
        new HiveMQTestContainerRule()
                .withExtension(new File("build/hivemq-extension-test/<extension-id>"));
```


### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withExtension(new GradleHiveMQExtensionSupplier(new File("path/to/extension/")).get());
```
                    
If your current project is the HiveMQ Extension you want to load into the HiveMQ Testcontainer, you can do it by:
* adapting your `build.gradle.kts`:

```kotlin
val prepareExtensionTest by tasks.registering(Sync::class) {
    group = "hivemq extension"
    description = "Prepares the extension for integration testing."

    from(tasks.hivemqExtensionZip.map { extensionZip -> zipTree(extensionZip.archiveFile) })
    into(buildDir.resolve("hivemq-extension-test"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn("prepareExtensionTest")
}
```
* adding the target directory of the `hivemqExtensionZip` / `hivemqEnterpriseExtensionZip` task

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
        new HiveMQTestContainerExtension()
            .withExtension(new File("build/hivemq-extension-test/<extension-id>"));
```


## Load an extension from a maven project

You can package and load an extension from a maven project. 

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withExtension(new MavenHiveMQExtensionSupplier("path/to/extension/pom.xml").get());
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withExtension(new MavenHiveMQExtensionSupplier("path/to/extension/pom.xml").get());
```
                    
If your current project is the HiveMQ Extension you want to load into the HiveMQ Testcontainer, you can simply use:

```java
MavenHiveMQExtensionSupplier.direct()
```

## Load an extension from a folder

You can load an extension from an extension directory into the container.
The extension will be placed in the container before startup. 

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
        new HiveMQTestContainerRule()
            .withExtension(new File("src/test/resources/modifier-extension"));
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
        new HiveMQTestContainerExtension()
            .withExtension(new File("src/test/resources/modifier-extension"));
```
                
## Load an extension directly from code

You can load an extension directly from code, by providing the extension's main class and extension information.
The extension will be packaged properly und put into the container before startup. 

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withExtension(HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .mainClass(MyExtension.class).build())
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withExtension(HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .mainClass(MyExtension.class).build())
```
                    
## Enable/Disable an extension

It is possible to enable and disable HiveMQ extensions during runtime. Extensions can also be disabled on startup.
Note that disabling or enabling of extension during runtime is only supported in HiveMQ 4 Enterprise Edition Containers.

### JUnit 4

```java
private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
    .id("extension-1")
    .name("my-extension")
    .version("1.0")
    .disabledOnStartup(true)
    .mainClass(MyExtension.class).build();

@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withExtension(hiveMQExtension);

@Test
public void test_disable_enable_extension() throws ExecutionException, InterruptedException {
    rule.enableExtension(hiveMQExtension);
    rule.disableExtension(hiveMQExtension);
}
```

### JUnit 5

```java
private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
    .id("extension-1")
    .name("my-extension")
    .version("1.0")
    .disabledOnStartup(true)
    .mainClass(MyExtension.class).build();

@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withExtension(hiveMQExtension);

@Test
void test_disable_enable_extension() throws ExecutionException, InterruptedException {
    extension.enableExtension(hiveMQExtension);
    extension.disableExtension(hiveMQExtension);
}
```
    
## Enable/Disable an extension loaded from a folder

You can enable or disable extensions loaded from an extension folder during runtime.
If the extension folder contains a DISABLED file, the extension will be disabled during startup.
Note that disabling or enabling of extension during runtime is only supported in HiveMQ 4 Enterprise Edition Containers.

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
        new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
            .withExtension(new File("src/test/resources/modifier-extension"));
            
@Test
void test_disable_enable_extension() throws ExecutionException, InterruptedException {
    extension.disableExtension("Modifier Extension", "modifier-extension");
    extension.enableExtension("Modifier Extension", "modifier-extension");
}
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
        new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
            .withExtension(new File("src/test/resources/modifier-extension"));
            
@Test
void test_disable_enable_extension() throws ExecutionException, InterruptedException {
    extension.disableExtension("Modifier Extension", "modifier-extension");
    extension.enableExtension("Modifier Extension", "modifier-extension");
}
```
    
## Remove prepackaged HiveMQ Extensions

Since HiveMQ's 4.4 release, HiveMQ Docker images come with the HiveMQ Extension for Kafka, the HiveMQ Enterprise Bridge Extension
and the HiveMQ Enterprise Security Extension.
These Extensions are disabled by default, but sometimes you my need to remove them before the container starts.

### JUnit4
Remove all prepackaged extensions:

```java
@Rule
final HiveMQTestContainerRule container = new HiveMQTestContainerRule("hivemq/hivemq4", "4.4.4")
    .withoutPrepackagedExtensions();
```

Remove specific prepackaged extensions: 

```java
@Rule
final HiveMQTestContainerRule container = new HiveMQTestContainerRule("hivemq/hivemq4", "4.4.4")
    .withoutPrepackagedExtensions("hivemq-kafka-extension");`
```

### JUnit5
Remove all prepackaged extensions:

```java
@RegisterExtension
final HiveMQTestContainerExtension container = new HiveMQTestContainerExtension("hivemq/hivemq4", "4.4.4")
    .withoutPrepackagedExtensions();
```

Remove specific prepackaged extensions: 

```java
@RegisterExtension
final HiveMQTestContainerExtension container = new HiveMQTestContainerExtension("hivemq/hivemq4", "4.4.4")
    .withoutPrepackagedExtensions("hivemq-kafka-extension");
```
                    
## Set logging level

You can set the logging level of the HiveMQ instance running inside the container.

Note: you can silence the container at any time using the `.silent(true)` method.

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withLogLevel(Level.DEBUG);
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withLogLevel(Level.DEBUG);
```
            

## Set Control Center Port

You can set the HiveMQ Control Center port on the host machine that is mapped to the control center port inside the container.
Note that the HiveMQ Control Center is feature of the HiveMQ Enterprise Edition.

### JUnit 4

```java
@Rule
public @NotNull HiveMQTestContainerRule rule = 
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withControlCenter(CONTROL_CENTER_PORT);
```
        
### JUnit 5

```java
@RegisterExtension
public @NotNull HiveMQTestContainerExtension extension = 
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withControlCenter(CONTROL_CENTER_PORT);
```

## Debug directly loaded extensions

You can debug extensions that are directly loaded from your code.

- put a break point in your extension

- enable remote debugging on your container

### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withDebugging(9000);
```

### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withDebugging(9000);
```


- create a Debug Client run configuration (make sure that the port matches with the code):

![image](https://www.hivemq.com/img/DEBUGCLIENT.png)

- run the JUnit test that starts your container

- run the DEBUG-CLIENT configuration to attach to the debug server

## Put files into the container

### Put a file into HiveMQ home

#### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withFileInHomeFolder(
            new File("src/test/resources/additionalFile.txt"),
            "/path/in/home/folder");
```

#### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withFileInHomeFolder(
            new File("src/test/resources/additionalFile.txt"),
            "/path/in/home/folder");
```
            
### Put files into extension home

#### JUnit 4

```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule()
        .withExtension(HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .mainClass(MyExtension.class).build())
        .withFileInExtensionHomeFolder(
            new File("src/test/resources/additionalFile.txt"),
            "extension-1",
            "/path/in/extension/home")`
```

#### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension()
        .withExtension(HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .mainClass(MyExtension.class).build())
        .withFileInExtensionHomeFolder(
            new File("src/test/resources/additionalFile.txt"),
            "extension-1",
            "/path/in/extension/home");
```
            
### Put license files into the container

#### JUnit 4
    
```java
@Rule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withLicense(new File("src/test/resources/myLicense.lic"))
        .withLicense(new File("src/test/resources/myExtensionLicense.elic"));
```
    
#### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withLicense(new File("src/test/resources/myLicense.lic"))
        .withLicense(new File("src/test/resources/myExtensionLicense.elic"));
```

### Configure Docker resources

#### JUnit 4

```java
@RegisterRule
public final @NotNull HiveMQTestContainerRule rule =
    new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
        .withCreateContainerCmdModifier(createContainerCmd -> {
            final HostConfig hostConfig = HostConfig.newHostConfig();
            hostConfig.withCpuCount(2L);
            hostConfig.withMemory(2 * 1024 * 1024L);
        });
```
    
#### JUnit 5

```java
@RegisterExtension
public final @NotNull HiveMQTestContainerExtension extension =
    new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
        .withCreateContainerCmdModifier(createContainerCmd -> {
            final HostConfig hostConfig = HostConfig.newHostConfig();
            hostConfig.withCpuCount(2L);
            hostConfig.withMemory(2 * 1024 * 1024L);
        });
```
            
### Customize the Container further

Since the JUnit 4 `HiveMQContainerRule` and the JUnit 5 `HiveMQContainerExtension` directly inherit all
methods from the [Testcontainer's](https://github.com/testcontainers) `GenericContainer` the container
can be customized as desired.

## Contributing

If you want to contribute to the HiveMQ Testcontainer, see the [contribution guidelines](CONTRIBUTING.md).

## License

The HiveMQ Testcontainer is licensed under the `APACHE LICENSE, VERSION 2.0`. A copy of the license can be found [here](LICENSE).
