/*
 * Copyright 2020 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.ExtensionMain;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("UnusedReturnValue")
public class HiveMQTestContainerCore<SELF extends HiveMQTestContainerCore<SELF>>
        extends FixedHostPortGenericContainer<SELF> {

    private final static @NotNull Logger logger = LoggerFactory.getLogger(HiveMQTestContainerCore.class);

    private static final @NotNull String validPluginXML =
            "<hivemq-extension>" + //
                    "   <id>%s</id>" + //
                    "   <name>%s</name>" + //
                    "   <version>%s</version>" + //
                    "   <priority>%s</priority>" +  //
                    "   <start-priority>%s</start-priority>" +  //
                    "</hivemq-extension>";

    private static final @NotNull String DEFAULT_HIVEMQ_IMAGE = "hivemq/hivemq-ce";
    private static final @NotNull String DEFAULT_HIVEMQ_TAG = "latest";
    public static final int DEBUGGING_PORT = 9000;
    public static final int MQTT_PORT = 1883;
    public static final int CONTROL_CENTER_PORT = 8080;
    public static final int MODE = 0777;
    public static final @NotNull Pattern EXTENSION_ID_PATTERN = Pattern.compile("<id>(.+?)</id>");

    private final @NotNull ConcurrentHashMap<String, CountDownLatch> containerOutputLatches = new ConcurrentHashMap<>();
    private volatile boolean silent = false;

    private final @NotNull MultiLogMessageWaitStrategy waitStrategy = new MultiLogMessageWaitStrategy();

    public HiveMQTestContainerCore() {
        this(DEFAULT_HIVEMQ_IMAGE, DEFAULT_HIVEMQ_TAG);
    }

    public HiveMQTestContainerCore(final @NotNull String image, final @NotNull String tag) {
        super(image + ":" + tag);
        addExposedPort(MQTT_PORT);

        waitStrategy.withRegEx("(.*)Started HiveMQ in(.*)");
        waitingFor(waitStrategy);

        withLogConsumer(outputFrame -> {
            if (!silent) {
                System.out.print(outputFrame.getUtf8String());
            }
        });
        withLogConsumer((outputFrame) -> {
            if (!containerOutputLatches.isEmpty()) {
                containerOutputLatches.forEach((regEx, latch) -> {
                    if (outputFrame.getUtf8String().matches("(?s)" + regEx)) {
                        logger.debug("Container Output '{}' matched RegEx '{}'", outputFrame.getUtf8String(), regEx);
                        latch.countDown();
                    } else {
                        logger.debug("Container Output '{}' did not match RegEx '{}'", outputFrame.getUtf8String(), regEx);
                    }
                });
            }
        });
    }

    /**
     * Adds a wait condition for the extension with this name.
     * <p>
     * Must be called before the container is started.
     *
     * @param extensionName the extension to wait for
     * @return self
     */
    public @NotNull SELF waitForExtension(final @NotNull String extensionName) {
        final String regEX = "(.*)Extension \"" + extensionName + "\" version (.*) started successfully(.*)";
        waitStrategy.withRegEx(regEX);
        return self();
    }

    /**
     * Adds a wait condition for this {@link HiveMQExtension}
     * <p>
     * Must be called before the container is started.
     *
     * @param extension the extension to wait for
     * @return self
     */
    public @NotNull SELF waitForExtension(final @NotNull HiveMQExtension extension) {
        return this.waitForExtension(extension.getName());
    }

    /**
     * Enables the possibility for remote debugging clients to connect.
     * <p>
     * Must be called before the container is started.
     *
     * @param debuggingPortHost the host port for debugging clients to connect
     * @return self
     */
    public @NotNull SELF withDebugging(final int debuggingPortHost) {
        addExposedPorts(DEBUGGING_PORT);
        addFixedExposedPort(debuggingPortHost, DEBUGGING_PORT);
        withEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,address=0.0.0.0:" + DEBUGGING_PORT + ",server=y,suspend=n");
        return self();
    }

    /**
     * Enables the possibility for remote debugging clients to connect on host port 9000.
     * <p>
     * Must be called before the container is started.
     *
     * @return self
     */
    public @NotNull SELF withDebugging() {
        withDebugging(DEBUGGING_PORT);
        return self();
    }

    /**
     * Sets the logging {@link Level} inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param level the {@link Level}
     * @return self
     */
    public @NotNull SELF withLogLevel(final @NotNull Level level) {
        this.withEnv("HIVEMQ_LOG_LEVEL", level.name());
        return self();
    }

    /**
     * Wraps the given class and all its subclasses into an extension
     * and puts it into '/opt/hivemq/extensions/{extension-id}' inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param hiveMQExtension the {@link HiveMQExtension} of the extension
     * @return self
     */
    public @NotNull SELF withExtension(final @NotNull HiveMQExtension hiveMQExtension) {
        try {
            final File extension = createExtension(hiveMQExtension);
            final MountableFile mountableExtension = MountableFile.forHostPath(extension.getPath(), MODE);
            withCopyFileToContainer(mountableExtension, "/opt/hivemq/extensions/" + hiveMQExtension.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return self();
    }

    /**
     * Puts the given extension folder into '/opt/hivemq/extensions/{directory-name}' inside the container.
     * It must at least contain a valid hivemq-extension.xml and a valid extension.jar in order to be executed.
     * The directory-name is taken from the id defined in the hivemq-extension.xml.
     * <p>
     * Must be called before the container is started.
     *
     * @param extensionDir the extension folder on the host machine
     * @return self
     */
    public @NotNull SELF withExtension(final @NotNull File extensionDir) {
        if (!extensionDir.exists()) {
            logger.warn("Extension {} could not be mounted. It does not exist", extensionDir.getAbsolutePath());
            return self();
        }
        if (!extensionDir.isDirectory()) {
            logger.warn("Extension {} could not be mounted. It is not a directory.", extensionDir.getAbsolutePath());
            return self();
        }
        try {
            final MountableFile mountableExtension = MountableFile.forHostPath(extensionDir.getPath(), MODE);
            final String extensionDirName = getExtensionDirectoryName(extensionDir);
            final String containerPath = "/opt/hivemq/extensions/" + extensionDirName;
            withCopyFileToContainer(mountableExtension, containerPath);
            logger.info("Putting extension {} into {}", extensionDirName, containerPath);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return self();
    }

    private @NotNull String getExtensionDirectoryName(final @NotNull File extensionDirectory) throws IOException {
        final File file = new File(extensionDirectory, "hivemq-extension.xml");
        final String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        final Matcher matcher = EXTENSION_ID_PATTERN.matcher(xml);

        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse extension id from '" + file.getAbsolutePath() + "'");
        }
        return matcher.group(1);
    }

    private @NotNull File createExtension(final @NotNull HiveMQExtension hiveMQExtension)
            throws Exception {

        final File tempDir = Files.createTempDirectory("").toFile();

        final File extensionDir = new File(tempDir, hiveMQExtension.getId());
        FileUtils.writeStringToFile(new File(extensionDir, "hivemq-extension.xml"),
                String.format(
                        validPluginXML,
                        hiveMQExtension.getId(),
                        hiveMQExtension.getName(),
                        hiveMQExtension.getVersion(),
                        hiveMQExtension.getPriority(),
                        hiveMQExtension.getStartPriority()),
                Charset.defaultCharset());

        if (hiveMQExtension.isDisabledOnStartup()) {
            final File disabled = new File(extensionDir, "DISABLED");
            final boolean newFile = disabled.createNewFile();
            if (!newFile) {
                logger.warn("Could not create DISABLED file {} on host machine", disabled.getAbsolutePath());
            }
        }

        final JavaArchive javaArchive =
                ShrinkWrap.create(JavaArchive.class)
                        .addAsServiceProviderAndClasses(ExtensionMain.class, hiveMQExtension.getMainClass());

        putSubclassesIntoJar(hiveMQExtension.getId(), hiveMQExtension.getMainClass(), javaArchive);
        for (final Class<?> additionalClass : hiveMQExtension.getAdditionalClasses()) {
            javaArchive.addClass(additionalClass);
            putSubclassesIntoJar(hiveMQExtension.getId(), additionalClass, javaArchive);
        }

        javaArchive.as(ZipExporter.class).exportTo(new File(extensionDir, "extension.jar"));

        final File jar = new File(extensionDir, "extension.jar");
        if (hiveMQExtension.sign()) {
            signExtension(hiveMQExtension.getId(), jar);
        }

        return extensionDir;
    }

    private void putSubclassesIntoJar(
            final @NotNull String extensionId,
            final @Nullable Class<?> clazz,
            final @NotNull JavaArchive javaArchive) throws NotFoundException {

        if (clazz != null) {
            final Set<String> subClassNames =
                    ClassPool.getDefault().get(clazz.getName()).getClassFile().getConstPool().getClassNames();
            for (final String subClassName : subClassNames) {
                final String className = subClassName.replaceAll("/", ".");

                if (!className.startsWith("[L")) {
                    logger.debug("Trying to package subclass {} into extension {}.", className, extensionId);
                    javaArchive.addClass(className);
                } else {
                    logger.debug("Class {} will be ignored.", className);
                }
            }
        }
    }

    /**
     * Removes the specified prepackaged extension folders from '/opt/hivemq/extensions' before the container is built.
     * Note: this creates a custom docker image.
     * <p>
     * Must be called before the container is started.
     *
     * @param extensionIds the prepackaged extensions to remove
     * @return self
     */
    public @NotNull SELF withoutPrepackagedExtensions(final @NotNull String... extensionIds) {
        final String dockerImageName = getDockerImageName();
        setImage(new ImageFromDockerfile(dockerImageName + "-custom")
                .withDockerfileFromBuilder(builder -> {
                    builder.from(dockerImageName);
                    for (final String extensionId : extensionIds) {
                        builder.run("rm", "-rf", "/opt/hivemq/extensions/" + extensionId);
                    }
                }));
        return self();
    }

    /**
     * Removes all prepackaged extension folders from '/opt/hivemq/extensions' before the container is built.
     * Note: this creates a custom docker image.
     * <p>
     * Must be called before the container is started.
     *
     * @return self
     */
    public @NotNull SELF withoutPrepackagedExtensions() {
        final String dockerImageName = getDockerImageName();
        setImage(new ImageFromDockerfile(dockerImageName + "-custom")
                .withDockerfileFromBuilder(builder ->
                        builder.from(dockerImageName)
                                .run("rm", "-rf", "/opt/hivemq/extensions/")));
        return self();
    }

    /**
     * Puts the given license into '/opt/hivemq/license/' inside the container.
     * It must end with '.lic' or '.elic'.
     * <p>
     * Must be called before the container is started.
     *
     * @param license the license file on the host machine
     * @return self
     */
    public @NotNull SELF withLicense(final @NotNull File license) {
        if (!license.exists()) {
            logger.warn("License file {} does not exist.", license.getAbsolutePath());
            return self();
        }
        if (!license.getName().endsWith(".lic") && !license.getName().endsWith(".elic")) {
            logger.warn("License file {} does not end wit '.lic' or '.elic'", license.getAbsolutePath());
            return self();
        }
        final MountableFile mountableFile = MountableFile.forHostPath(license.getAbsolutePath(), MODE);
        final String containerPath = "/opt/hivemq/license/" + license.getName();
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting license {} into {}", license.getAbsolutePath(), containerPath);
        return self();
    }

    /**
     * Overwrites the HiveMQ configuration in '/opt/hivemq/conf/' inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param config the config file on the host machine
     * @return self
     */
    public @NotNull SELF withHiveMQConfig(final @NotNull File config) {
        if (!config.exists()) {
            logger.warn("HiveMQ config file {} does not exist.", config.getAbsolutePath());
            return self();
        }
        final MountableFile mountableFile = MountableFile.forHostPath(config.getAbsolutePath(), MODE);
        final String containerPath = "/opt/hivemq/conf/config.xml";
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting {} into {}", config.getAbsolutePath(), containerPath);
        return self();
    }

    /**
     * Puts the given file into the root of the extension's home '/opt/hivemq/extensions/{extensionId}/'.
     * Note: the extension must be loaded before the file is put.
     * <p>
     * Must be called before the container is started.
     *
     * @param file        the file on the host machine
     * @param extensionId the extension
     * @return self
     */
    public @NotNull SELF withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId) {

        return withFileInExtensionHomeFolder(file, extensionId, "");
    }

    /**
     * Puts the given file into given subdirectory of the extensions's home '/opt/hivemq/extensions/{id}/{pathInExtensionHome}/'
     * Note: the extension must be loaded before the file is put.
     * <p>
     * Must be called before the container is started.
     *
     * @param file                the file on the host machine
     * @param extensionId         the extension
     * @param pathInExtensionHome the path
     * @return self
     */
    public @NotNull SELF withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome) {

        return withFileInHomeFolder(file, "/extensions/" + extensionId + PathUtil.preparePath(pathInExtensionHome));
    }

    /**
     * Puts the given file into the root of the HiveMQ home folder '/opt/hivemq/'.
     * <p>
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @return self
     */
    public @NotNull SELF withFileInHomeFolder(final @NotNull File file) {
        return withFileInHomeFolder(file, "");
    }

    /**
     * Puts the given file into the given subdirectory of the HiveMQ home folder '/opt/hivemq/{pathInHomeFolder}'.
     * <p>
     * Must be called before the container is started.
     *
     * @param file             the file on the host machine
     * @param pathInHomeFolder the path
     * @return self
     */
    public @NotNull SELF withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInHomeFolder) {

        if (!file.exists()) {
            logger.warn("File {} does not exist.", file.getAbsolutePath());
            return self();
        }
        final MountableFile mountableFile = MountableFile.forHostPath(file.getAbsolutePath(), MODE);
        final String containerPath = "/opt/hivemq" + PathUtil.preparePath(pathInHomeFolder) + file.getName();
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting file {} into container path {}", file.getAbsolutePath(), containerPath);
        return self();
    }

    /**
     * Disables the extension with the given name and extension directory name.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after {timeOut}.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param extensionName      the name of the extension to disable
     * @param extensionDirectory the name of the extension's directory
     * @param timeout            the timeout
     * @return self
     * @since 1.1.0
     */
    public @NotNull SELF disableExtension(
            final @NotNull String extensionName,
            final @NotNull String extensionDirectory,
            final @NotNull Duration timeout) {

        final String regEX = "(.*)Extension \"" + extensionName + "\" version (.*) stopped successfully(.*)";
        try {
            final String containerPath = "/opt/hivemq/extensions" + PathUtil.preparePath(extensionDirectory) + "DISABLED";

            final CountDownLatch latch = new CountDownLatch(1);
            containerOutputLatches.put(regEX, latch);

            execInContainer("touch", containerPath);
            logger.info("Putting DISABLED file into container path {}", containerPath);

            final boolean await = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!await) {
                logger.warn("Extension disabling timed out after {} seconds. " +
                        "Maybe you are using a HiveMQ Community Edition image, " +
                        "which does not support disabling of extensions", timeout.getSeconds());
            }
        } catch (final InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            containerOutputLatches.remove(regEX);
        }
        return self();
    }

    /**
     * Disables the extension with the given name and extension directory name.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after 60 seconds.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param extensionName      the name of the extension to disable
     * @param extensionDirectory the name of the extension's directory
     * @return self
     * @since 1.1.0
     */
    public @NotNull SELF disableExtension(
            final @NotNull String extensionName,
            final @NotNull String extensionDirectory) {
        return disableExtension(extensionName, extensionDirectory, Duration.ofSeconds(60));
    }

    /**
     * Disables the extension.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after {timeOut}.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @param timeout         the timeout
     * @return self
     */
    public @NotNull SELF disableExtension(
            final @NotNull HiveMQExtension hiveMQExtension,
            final @NotNull Duration timeout) {
        return disableExtension(hiveMQExtension.getName(), hiveMQExtension.getId(), timeout);
    }

    /**
     * Disables the extension.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after 60 seconds.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    public @NotNull SELF disableExtension(final @NotNull HiveMQExtension hiveMQExtension) {
        return disableExtension(hiveMQExtension, Duration.ofSeconds(60));
    }

    /**
     * Enables the extension with the given name and extension directory name.
     * This method blocks until the HiveMQ log for successful enabling is consumed or it times out after {timeOut}.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param extensionName      the name of the extension to disable
     * @param extensionDirectory the name of the extension's directory
     * @param timeout            the timeout
     * @return self
     * @since 1.1.0
     */
    public @NotNull SELF enableExtension(
            final @NotNull String extensionName,
            final @NotNull String extensionDirectory,
            final @NotNull Duration timeout) {

        final String regEX = "(.*)Extension \"" + extensionName + "\" version (.*) started successfully(.*)";
        try {
            final String containerPath = "/opt/hivemq/extensions" + PathUtil.preparePath(extensionDirectory) + "DISABLED";

            final CountDownLatch latch = new CountDownLatch(1);
            containerOutputLatches.put(regEX, latch);

            execInContainer("rm", "-rf", containerPath);
            logger.info("Removing DISABLED file in container path {}", containerPath);

            final boolean await = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!await) {
                logger.warn("Extension enabling timed out after {} seconds. " +
                        "Maybe you are using a HiveMQ Community Edition image, " +
                        "which does not support disabling of extensions", timeout.getSeconds());
            }
        } catch (final InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            containerOutputLatches.remove(regEX);
        }
        return self();
    }

    /**
     * Enables the extension with the given name and extension directory name.
     * This method blocks until the HiveMQ log for successful enabling is consumed or it times out after 60 seconds.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param extensionName      the name of the extension to disable
     * @param extensionDirectory the name of the extension's directory
     * @return self
     * @since 1.1.0
     */
    public @NotNull SELF enableExtension(
            final @NotNull String extensionName,
            final @NotNull String extensionDirectory) {
        return enableExtension(extensionName, extensionDirectory, Duration.ofSeconds(60));
    }

    /**
     * Enables the extension.
     * This method blocks until the HiveMQ log for successful enabling is consumed or it times out after {timeOut}.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @param timeout         the timeout
     * @return self
     */
    public @NotNull SELF enableExtension(
            final @NotNull HiveMQExtension hiveMQExtension,
            final @NotNull Duration timeout) {
        return enableExtension(hiveMQExtension.getName(), hiveMQExtension.getId(), timeout);
    }

    /**
     * Enables the extension.
     * This method blocks until the HiveMQ log for successful enabling is consumed or it times out after {timeOut}.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using the HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    public @NotNull SELF enableExtension(final @NotNull HiveMQExtension hiveMQExtension) {
        return enableExtension(hiveMQExtension, Duration.ofSeconds(60));
    }

    /**
     * Determines whether the stdout of the container is printed to System.out.
     *
     * @param silent whether the container is silent.
     * @return self
     */
    public @NotNull SELF silent(final boolean silent) {
        this.silent = silent;
        return self();
    }

    /**
     * Enables connection to the HiveMQ Control Center on host port 8080.
     * Note: the control center is a HiveMQ 4 Enterprise feature.
     * <p>
     * Must be called before the container is started.
     *
     * @return self
     */
    public @NotNull SELF withControlCenter() {
        return withControlCenter(CONTROL_CENTER_PORT);
    }

    /**
     * Enables connection to the HiveMQ Control Center on host port {controlCenterPort}.
     * Note: the control center is a HiveMQ 4 Enterprise feature.
     * <p>
     * Must be called before the container is started.
     *
     * @param controlCenterPort the host post
     * @return self
     */
    public @NotNull SELF withControlCenter(final int controlCenterPort) {
        addExposedPorts(CONTROL_CENTER_PORT);
        addFixedExposedPort(controlCenterPort, CONTROL_CENTER_PORT);
        return self();
    }

    /**
     * Get the mapped port for the MQTT port of the container.
     * <p>
     * Must be called after the container is started.
     *
     * @return the port on the host machine for mqtt clients to connect
     */
    public int getMqttPort() {
        return this.getMappedPort(MQTT_PORT);
    }

    @Override
    public void stop() {
        waitStrategy.reset();
        super.stop();
    }

    protected void signExtension(final @NotNull String extensionId, final @NotNull File jar) {
        // NOOP
    }

}
