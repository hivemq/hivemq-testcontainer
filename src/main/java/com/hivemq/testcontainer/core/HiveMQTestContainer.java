package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("UnusedReturnValue")
public interface HiveMQTestContainer {

    /**
     * Enables the possibility for remote debugging clients to connect.
     *
     * @param debuggingPortHost the exposed post on the host for debugging clients to connect.
     * @return self
     */
    @NotNull HiveMQTestContainer withDebugging(final int debuggingPortHost);

    /**
     * Enables the possibility for remote debugging clients to connect on port 9000.
     *
     * @return self
     */
    @NotNull HiveMQTestContainer withDebugging();

    /**
     * Sets the logging {@link Level} inside the container.
     *
     * @param level the {@link Level}
     */
    @NotNull HiveMQTestContainer withLogLevel(final @NotNull Level level);

    /**
     * Wraps the given class into an extension and puts it into '/opt/hivemq/extensions/' inside the container.
     *
     * @param id        the extension id
     * @param name      the extension name
     * @param version   the extension version
     * @param priority  the extension priority
     * @param mainClazz the {@link ExtensionMain} of the extension
     * @return self
     */
    @NotNull HiveMQTestContainer withExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final @NotNull Class<? extends ExtensionMain> mainClazz);

    /**
     * Puts the given extension folder into '/opt/hivemq/extensions' inside the container.
     * It must at least contain a valid hivemq-extension.xml and a valid extension.jar in order to be executed.
     *
     * @param extensionDir the extension folder.
     * @return self
     */
    @NotNull HiveMQTestContainer withExtension(final @NotNull File extensionDir);

    /**
     * Puts the given license into '/opt/hivemq/license/'.
     * It must end with '.lic' or '.elic'.
     *
     * @param license the license file
     * @return self
     */
    @NotNull HiveMQTestContainer withLicense(final @NotNull File license);

    /**
     * Overwrites the HiveMQ configuration in '/opt/hivemq/conf/'.
     *
     * @param config the config file.
     * @return self
     */
    @NotNull HiveMQTestContainer withHiveMQConfig(final @NotNull File config);

    /**
     * Puts the given file into the root of the extension home of {@param extensionId}.
     * Note: the extension must be loaded before the file is put.
     *
     * @param file the file
     * @param extensionId the extension
     * @return self.
     */
    @NotNull HiveMQTestContainer withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId);

    /**
     * Puts the given file into given subdirectory {@param pathInExtensionHome} of the extension home of {@param extensionId}.
     * Note: the extension must be loaded before the file is put.
     *
     * @param file the file
     * @param extensionId the extension
     * @param pathInExtensionHome the path
     * @return self.
     */
    @NotNull HiveMQTestContainer withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome);
    /**
     * Puts the given file into the root of the HiveMQ home folder.
     *
     * @param file the file
     * @return self.
     */
    @NotNull HiveMQTestContainer withFileInHomeFolder(
            final @NotNull File file);

    /**
     * Puts the given file into the given subdirectory {@param pathInHomeFolder} of the HiveMQ home folder.
     *
     * @param file the file
     * @param pathInHomeFolder the path
     * @return self.
     */
    @NotNull HiveMQTestContainer withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInHomeFolder);

    /**
     * Get the actual mapped port for the MQTT port of the container.
     *
     * @return the port.
     */
    int getMqttPort();
    
}
