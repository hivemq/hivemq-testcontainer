package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;
import java.time.Duration;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("UnusedReturnValue")
public interface HiveMQTestContainer<SELF> {

    /**
     * Enables the possibility for remote debugging clients to connect.
     *
     * Must be called before the container is started.
     *
     * @param debuggingPortHost the host port for debugging clients to connect
     * @return self
     */
    @NotNull SELF withDebugging(final int debuggingPortHost);

    /**
     * Enables the possibility for remote debugging clients to connect on host port 9000.
     *
     * Must be called before the container is started.
     *
     * @return self
     */
    @NotNull SELF withDebugging();

    /**
     * Sets the logging {@link Level} inside the container.
     *
     * Must be called before the container is started.
     *
     * @param level the {@link Level}
     */
    @NotNull SELF withLogLevel(final @NotNull Level level);

    /**
     * Wraps the given class and all its subclasses into an extension
     * and puts it into '/opt/hivemq/extensions/{@param id}' inside the container.
     *
     * Must be called before the container is started.
     *
     * @param id        the extension id
     * @param name      the extension name
     * @param version   the extension version
     * @param priority  the extension priority
     * @param mainClazz the {@link ExtensionMain} of the extension
     * @return self
     */
    @NotNull SELF withExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final @NotNull Class<? extends ExtensionMain> mainClazz);

    /**
     * Puts the given extension folder into '/opt/hivemq/extensions/{extension-id}' inside the container.
     * It must at least contain a valid hivemq-extension.xml and a valid extension.jar in order to be executed.
     *
     * Must be called before the container is started.
     *
     * @param extensionDir the extension folder on the host machine
     * @return self
     */
    @NotNull SELF withExtension(final @NotNull File extensionDir);

    /**
     * Puts the given license into '/opt/hivemq/license/' inside the container.
     * It must end with '.lic' or '.elic'.
     *
     * Must be called before the container is started.
     *
     * @param license the license file on the host machine
     * @return self
     */
    @NotNull SELF withLicense(final @NotNull File license);

    /**
     * Overwrites the HiveMQ configuration in '/opt/hivemq/conf/' inside the container.
     *
     * Must be called before the container is started.
     *
     * @param config the config file on the host machine
     * @return self
     */
    @NotNull SELF withHiveMQConfig(final @NotNull File config);

    /**
     * Puts the given file into the root of the extension's home '/opt/hivemq/extensions/{@param extensionId}/'.
     * Note: the extension must be loaded before the file is put.
     *
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @param extensionId the extension
     * @return self
     */
    @NotNull SELF withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId);

    /**
     * Puts the given file into given subdirectory of the extensions's home '/opt/hivemq/extensions/{@param id}/{@param pathInExtensionHome}/'
     * Note: the extension must be loaded before the file is put.
     *
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @param extensionId the extension
     * @param pathInExtensionHome the path
     * @return self
     */
    @NotNull SELF withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome);

    /**
     * Puts the given file into the root of the HiveMQ home folder '/opt/hivemq/'.
     *
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @return self
     */
    @NotNull SELF withFileInHomeFolder(
            final @NotNull File file);

    /**
     * Puts the given file into the given subdirectory of the HiveMQ home folder '/opt/hivemq/{@param pathInHomeFolder}'.
     *
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @param pathInHomeFolder the path
     * @return self
     */
    @NotNull SELF withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInHomeFolder);

    /**
     * Disables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after 60 seconds.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     *
     * This can only be called once the container is started.
     *
     * @param id the extension id
     * @param name the name of the extension
     * @return self
     */
    @NotNull SELF disableExtension(
            final @NotNull String id,
            final @NotNull String name);

    /**
     * Disables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after {@param timeOut}.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     *
     * This can only be called once the container is started.
     *
     * @param id the extension id
     * @param name the name of the extension
     * @return self
     */
    @NotNull SELF disableExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull Duration timeOut);

    /**
     * Get the mapped port for the MQTT port of the container.
     *
     * Must be called after the container is started.
     *
     * @return the port on the host machine for mqtt clients to connect
     */
    int getMqttPort();
    
}
