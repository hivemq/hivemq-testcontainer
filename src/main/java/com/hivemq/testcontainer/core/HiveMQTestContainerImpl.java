package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import javassist.ClassPool;
import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("UnusedReturnValue")
public class HiveMQTestContainerImpl extends FixedHostPortGenericContainer<HiveMQTestContainerImpl> implements HiveMQTestContainer {

    private final static @NotNull Logger logger = LoggerFactory.getLogger(HiveMQTestContainerImpl.class);

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

    private final @NotNull ConcurrentHashMap<String, CountDownLatch> containerOutputLatches = new ConcurrentHashMap<>();

    public HiveMQTestContainerImpl() {
        this(DEFAULT_HIVEMQ_IMAGE, DEFAULT_HIVEMQ_TAG);
    }

    public HiveMQTestContainerImpl(final @NotNull String image, final @NotNull String tag) {
        super(image + ":" + tag);
        withExposedPorts(MQTT_PORT);

        final MqttWaitStrategy mqttWaitStrategy = new MqttWaitStrategy();
        waitingFor(mqttWaitStrategy);

        withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()));
        withLogConsumer((outputFrame) -> {
            if (!containerOutputLatches.isEmpty()) {
                containerOutputLatches.forEach((regEx, latch) -> {
                    if (outputFrame.getUtf8String().matches("(?s)" + regEx)) {
                        logger.debug("Contanier Output '{}' matched RegEx '{}'", outputFrame.getUtf8String(), regEx);
                        latch.countDown();
                    } else {
                        logger.debug("Contanier Output '{}' did not match RegEx '{}'", outputFrame.getUtf8String(), regEx);
                    }
                });
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withDebugging(final int debuggingPortHost) {
        withExposedPorts(DEBUGGING_PORT);
        withFixedExposedPort(debuggingPortHost, DEBUGGING_PORT);
        withEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,address=0.0.0.0:" + DEBUGGING_PORT + ",server=y,suspend=n");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withDebugging() {
        withDebugging(DEBUGGING_PORT);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withLogLevel(final @NotNull Level level) {
        this.withEnv("HIVEMQ_LOG_LEVEL", level.name());
        return this;
    }

    public @NotNull HiveMQTestContainerImpl withExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final boolean sign,
            final @NotNull Class<? extends ExtensionMain> mainClazz) {
        try {
            final File extension = createExtension(id, name, version, priority, startPriority, sign, mainClazz);
            final MountableFile mountableExtension = MountableFile.forHostPath(extension.getPath());
            withCopyFileToContainer(mountableExtension, "/opt/hivemq/extensions/" + id);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final @NotNull Class<? extends ExtensionMain> mainClazz) {

        return withExtension(id, name, version, priority, startPriority, false, mainClazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withExtension(final @NotNull File extensionDir) {
        if (!extensionDir.exists()) {
            logger.warn("Extension {} could not be mounted. It does not exist", extensionDir.getAbsolutePath());
            return this;
        }
        if (!extensionDir.isDirectory()) {
            logger.warn("Extension {} could not be mounted. It is not a directory.", extensionDir.getAbsolutePath());
            return this;
        }
        try {
            final MountableFile mountableExtension = MountableFile.forHostPath(extensionDir.getPath());
            final String containerPath = "/opt/hivemq/extensions/" + extensionDir.getName();
            withCopyFileToContainer(mountableExtension, containerPath);
            logger.info("Putting extension {} into {}", extensionDir.getName(), containerPath);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private @NotNull File createExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final boolean sign,
            final @NotNull Class<? extends ExtensionMain> mainClazz)
            throws Exception {

        final File tempDir = Files.createTempDir();

        final File extensionDir = new File(tempDir, id);
        FileUtils.writeStringToFile(new File(extensionDir, "hivemq-extension.xml"),
                String.format(validPluginXML, id, name, version, priority, startPriority), Charset.defaultCharset());

        final JavaArchive javaArchive =
                ShrinkWrap.create(JavaArchive.class)
                        .addAsServiceProviderAndClasses(ExtensionMain.class, mainClazz);


        //try to add all inner and anonymous classes of extension main to the extension jar
        try {
            final Set<String> subClassNames =
                    ClassPool.getDefault().get(mainClazz.getName()).getClassFile().getConstPool().getClassNames();
            for (final String subClassName : subClassNames) {
                javaArchive.addClass(subClassName.replaceAll("/", "."));
            }
        } catch (final Exception e) {
            //ignore
        }

        javaArchive.as(ZipExporter.class).exportTo(new File(extensionDir, "extension.jar"));

        final File jar = new File(extensionDir, "extension.jar");
        if (sign) {
            signExtension(id, jar);
        }

        return extensionDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withLicense(final @NotNull File license) {
        if (!license.exists()) {
            logger.warn("License file {} does not exist.", license.getAbsolutePath());
            return this;
        }
        if (!license.getName().endsWith(".lic") && !license.getName().endsWith(".elic")) {
            logger.warn("License file {} does not end wit '.lic' or '.elic'", license.getAbsolutePath());
            return this;
        }
        final MountableFile mountableFile = MountableFile.forHostPath(license.getAbsolutePath());
        final String containerPath = "/opt/hivemq/license/" + license.getName();
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting license {} into {}", license.getAbsolutePath(), containerPath);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withHiveMQConfig(final @NotNull File config) {
        if (!config.exists()) {
            logger.warn("HiveMQ config file {} does not exist.", config.getAbsolutePath());
            return this;
        }
        final MountableFile mountableFile = MountableFile.forHostPath(config.getAbsolutePath());
        final String containerPath = "/opt/hivemq/conf/config.xml";
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting {} into {}", config.getAbsolutePath(), containerPath);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId) {

        return withFileInExtensionHomeFolder(file, extensionId, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome) {

        return withFileInHomeFolder(file, "/extensions/" + extensionId + PathUtil.preparePath(pathInExtensionHome));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withFileInHomeFolder(
            final @NotNull File file) {

        return withFileInHomeFolder(file, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerImpl withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInHomeFolder) {

        if (!file.exists()) {
            logger.warn("File {} does not exist.", file.getAbsolutePath());
            return this;
        }
        final MountableFile mountableFile = MountableFile.forHostPath(file.getAbsolutePath());
        final String containerPath = "/opt/hivemq" + PathUtil.preparePath(pathInHomeFolder) + file.getName();
        withCopyFileToContainer(mountableFile, containerPath);
        logger.info("Putting file {} into container path {}", file.getAbsolutePath(), containerPath);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainer disableExtension(
            final @NotNull String id,
            final @NotNull String name) {

        final File tempDir = Files.createTempDir();
        final File disabled = new File(tempDir, "DISABLED");
        try {
            //noinspection ResultOfMethodCallIgnored
            disabled.createNewFile();
        } catch (final IOException e) {
            logger.warn("Unable to create DISABLED file on host machine.", e);
            return this;
        }
        final String regEX = "(.*)Extension \"" + name + "\" version (.*) stopped successfully(.*)";
        try {
            final MountableFile mountableFile = MountableFile.forHostPath(disabled.getAbsolutePath());
            final String containerPath = "/opt/hivemq/extensions" + PathUtil.preparePath(id) + disabled.getName();

            final CountDownLatch latch = new CountDownLatch(1);
            containerOutputLatches.put(regEX, latch);

            this.copyFileToContainer(mountableFile, containerPath);
            logger.info("Putting file {} into container path {}", disabled.getAbsolutePath(), containerPath);

            latch.await();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            containerOutputLatches.remove(regEX);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMqttPort() {
        return this.getMappedPort(MQTT_PORT);
    }

    protected void signExtension(final @NotNull String extensionId, final @NotNull File jar) {
        // NOOP
    }

}
