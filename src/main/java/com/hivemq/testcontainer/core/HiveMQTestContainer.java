package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;
import java.time.Duration;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("UnusedReturnValue")
public interface HiveMQTestContainer {

    /**
     * Enables the possibility for remote debugging clients to connect.
     * <p>
     * Must be called before the container is started.
     *
     * @param debuggingPortHost the host port for debugging clients to connect
     * @return self
     */
    @NotNull HiveMQTestContainer withDebugging(final int debuggingPortHost);

    /**
     * Enables the possibility for remote debugging clients to connect on host port 9000.
     * <p>
     * Must be called before the container is started.
     *
     * @return self
     */
    @NotNull HiveMQTestContainer withDebugging();

    /**
     * Sets the logging {@link Level} inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param level the {@link Level}
     */
    @NotNull HiveMQTestContainer withLogLevel(final @NotNull Level level);

    /**
     * Wraps the given class and all its subclasses into an extension
     * and puts it into '/opt/hivemq/extensions/{extension-id}' inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param hiveMQExtension the {@link HiveMQExtension} of the extension
     * @return self
     */
    @NotNull HiveMQTestContainer withExtension(final @NotNull HiveMQExtension hiveMQExtension);

    /**
     * Puts the given extension folder into '/opt/hivemq/extensions/{extension-id}' inside the container.
     * It must at least contain a valid hivemq-extension.xml and a valid extension.jar in order to be executed.
     * <p>
     * Must be called before the container is started.
     *
     * @param extensionDir the extension folder on the host machine
     * @return self
     */
    @NotNull HiveMQTestContainer withExtension(final @NotNull File extensionDir);

    /**
     * Puts the given license into '/opt/hivemq/license/' inside the container.
     * It must end with '.lic' or '.elic'.
     * <p>
     * Must be called before the container is started.
     *
     * @param license the license file on the host machine
     * @return self
     */
    @NotNull HiveMQTestContainer withLicense(final @NotNull File license);

    /**
     * Overwrites the HiveMQ configuration in '/opt/hivemq/conf/' inside the container.
     * <p>
     * Must be called before the container is started.
     *
     * @param config the config file on the host machine
     * @return self
     */
    @NotNull HiveMQTestContainer withHiveMQConfig(final @NotNull File config);

    /**
     * Puts the given file into the root of the extension's home '/opt/hivemq/extensions/{@param extensionId}/'.
     * Note: the extension must be loaded before the file is put.
     * <p>
     * Must be called before the container is started.
     *
     * @param file        the file on the host machine
     * @param extensionId the extension
     * @return self
     */
    @NotNull HiveMQTestContainer withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId);

    /**
     * Puts the given file into given subdirectory of the extensions's home '/opt/hivemq/extensions/{@param id}/{@param pathInExtensionHome}/'
     * Note: the extension must be loaded before the file is put.
     * <p>
     * Must be called before the container is started.
     *
     * @param file                the file on the host machine
     * @param extensionId         the extension
     * @param pathInExtensionHome the path
     * @return self
     */
    @NotNull HiveMQTestContainer withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome);

    /**
     * Puts the given file into the root of the HiveMQ home folder '/opt/hivemq/'.
     * <p>
     * Must be called before the container is started.
     *
     * @param file the file on the host machine
     * @return self
     */
    @NotNull HiveMQTestContainer withFileInHomeFolder(
            final @NotNull File file);

    /**
     * Puts the given file into the given subdirectory of the HiveMQ home folder '/opt/hivemq/{@param pathInHomeFolder}'.
     * <p>
     * Must be called before the container is started.
     *
     * @param file             the file on the host machine
     * @param pathInHomeFolder the path
     * @return self
     */
    @NotNull HiveMQTestContainer withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInHomeFolder);

    /**
     * Disables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after 60 seconds.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    @NotNull HiveMQTestContainer disableExtension(final @NotNull HiveMQExtension hiveMQExtension);

    /**
     * Disables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after {@param timeOut}.
     * Note: Disabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     * <p>
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    @NotNull HiveMQTestContainer disableExtension(
            final @NotNull HiveMQExtension hiveMQExtension,
            final @NotNull Duration timeOut);

    /**
     * Enables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after 60 seconds.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     *
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    @NotNull HiveMQTestContainer enableExtension(final @NotNull HiveMQExtension hiveMQExtension);

    /**
     * Disables the extension with the corresponding {@param id}.
     * This method blocks until the HiveMQ log for successful disabling is consumed or it times out after {@param timeOut}.
     * Note: Enabling Extensions is a HiveMQ Enterprise feature, it will not work when using HiveMQ Community Edition.
     *
     * This can only be called once the container is started.
     *
     * @param hiveMQExtension the extension
     * @return self
     */
    @NotNull HiveMQTestContainer enableExtension(
            final @NotNull HiveMQExtension hiveMQExtension,
            final @NotNull Duration timeOut);

    /**
     * Enables connection to the HiveMQ Control Center on host port {@param controlCenterPort}.
     * Note: the control center is a HiveMQ 4 Enterprise feature.
     * <p>
     * Must be called before the container is started.
     *
     * @param controlCenterPort the host post
     * @return self
     */
    @NotNull HiveMQTestContainer withControlCenter(final int controlCenterPort);

    /**
     * Enables connection to the HiveMQ Control Center on host port 8080.
     * Note: the control center is a HiveMQ 4 Enterprise feature.
     * <p>
     * Must be called before the container is started.
     *
     * @return self
     */
    @NotNull HiveMQTestContainer withControlCenter();

    /**
     * Controls whether the stdout of the container is printed to System.out.
     *
     * @param silent whether the container is silent.
     */
    @NotNull HiveMQTestContainer silent(final boolean silent);

    /**
     * Get the mapped port for the MQTT port of the container.
     * <p>
     * Must be called after the container is started.
     *
     * @return the port on the host machine for mqtt clients to connect
     */
    int getMqttPort();

}
