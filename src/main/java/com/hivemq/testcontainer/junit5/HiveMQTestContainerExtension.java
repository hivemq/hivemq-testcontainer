package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQTestContainer;
import com.hivemq.testcontainer.core.HiveMQTestContainerImpl;
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

    private final @NotNull HiveMQTestContainerImpl container;

    public HiveMQTestContainerExtension(
            final @NotNull String image,
            final @NotNull String tag) {

        container = new HiveMQTestContainerImpl(image, tag);
    }

    public HiveMQTestContainerExtension() {
        container = new HiveMQTestContainerImpl();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        container.stop();
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) {
        container.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final int priority,
            final int startPriority,
            final @NotNull Class<? extends ExtensionMain> mainClazz) {

        container.withExtension(id, name, version, priority, startPriority, mainClazz);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withExtension(final @NotNull File extensionDirectory) {
        container.withExtension(extensionDirectory);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withLogLevel(final @NotNull Level logLevel) {
        container.withLogLevel(logLevel);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withDebugging(final int debuggingPortHost) {
        container.withDebugging(debuggingPortHost);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withDebugging() {
        container.withDebugging();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withLicense(final @NotNull File licence) {
        container.withLicense(licence);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withHiveMQConfig(final @NotNull File config) {
        container.withHiveMQConfig(config);
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

        container.withFileInExtensionHomeFolder(file, extensionId, pathInExtensionHome);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId) {

        container.withFileInExtensionHomeFolder(file, extensionId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInExtensionHome) {

        container.withFileInHomeFolder(file, pathInExtensionHome);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerExtension withFileInHomeFolder(
            final @NotNull File file) {

        container.withFileInHomeFolder(file);
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

        container.disableExtension(id, name, timeOut);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainer disableExtension(final @NotNull String id, final @NotNull String name) {
        container.disableExtension(id, name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMqttPort() {
        return container.getMqttPort();
    }
}
