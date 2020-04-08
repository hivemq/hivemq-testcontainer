package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.core.HiveMQTestContainer;
import com.hivemq.testcontainer.core.HiveMQTestContainerCore;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.event.Level;

import java.io.File;
import java.time.Duration;

/**
 * @author Yannick Weber
 */
public class HiveMQTestContainerExtension implements HiveMQTestContainer, BeforeEachCallback, AfterEachCallback {

    private final @NotNull HiveMQTestContainerCore core;

    public HiveMQTestContainerExtension(
            final @NotNull String image,
            final @NotNull String tag) {

        core = new HiveMQTestContainerCore(image, tag);
    }

    public HiveMQTestContainerExtension() {
        core = new HiveMQTestContainerCore();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        core.stop();
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) {
        core.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withExtension(final @NotNull HiveMQExtension hiveMQExtension) {
        core.withExtension(hiveMQExtension);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withExtension(final @NotNull File extensionDirectory) {
        core.withExtension(extensionDirectory);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withLogLevel(final @NotNull Level logLevel) {
        core.withLogLevel(logLevel);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withDebugging(final int debuggingPortHost) {
        core.withDebugging(debuggingPortHost);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withDebugging() {
        core.withDebugging();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withLicense(final @NotNull File licence) {
        core.withLicense(licence);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withHiveMQConfig(final @NotNull File config) {
        core.withHiveMQConfig(config);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId,
            final @NotNull String pathInExtensionHome) {

        core.withFileInExtensionHomeFolder(file, extensionId, pathInExtensionHome);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId) {

        core.withFileInExtensionHomeFolder(file, extensionId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInExtensionHome) {

        core.withFileInHomeFolder(file, pathInExtensionHome);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInHomeFolder(
            final @NotNull File file) {

        core.withFileInHomeFolder(file);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainer disableExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull Duration timeOut) {

        core.disableExtension(id, name, timeOut);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainer disableExtension(final @NotNull String id, final @NotNull String name) {
        core.disableExtension(id, name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMqttPort() {
        return core.getMqttPort();
    }

    /**
     * Returns the underlying {@link HiveMQTestContainerCore}.
     * This is useful for extending the behaviour of the container.
     *
     * @return the HiveMQTestContainerCore
     */
    public @NotNull HiveMQTestContainerCore getCore() {
        return core;
    }
}
