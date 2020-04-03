package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQTestContainer;
import com.hivemq.testcontainer.core.HiveMQTestContainerImpl;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.event.Level;

import java.io.File;

/**
 * @author Yannick Weber
 */
public class HiveMQTestContainerRule extends TestWatcher implements HiveMQTestContainer {

    private final @NotNull HiveMQTestContainerImpl container;

    public HiveMQTestContainerRule(
            final @NotNull String image,
            final @NotNull String tag) {

        container = new HiveMQTestContainerImpl(image, tag);
    }

    public HiveMQTestContainerRule() {
        container = new HiveMQTestContainerImpl();
    }

    @Override
    protected void starting(final @NotNull Description description) {
        container.start();
    }

    @Override
    protected void finished(final @NotNull Description description) {
        container.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withExtension(
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
    public @NotNull HiveMQTestContainerRule withExtension(final @NotNull File extensionDirectory) {
        container.withExtension(extensionDirectory);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withLogLevel(final @NotNull Level logLevel) {
        container.withLogLevel(logLevel);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withDebugging(final int debuggingPortHost) {
        container.withDebugging(debuggingPortHost);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withDebugging() {
        container.withDebugging();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withLicense(final @NotNull File licence) {
        container.withLicense(licence);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withHiveMQConfig(final @NotNull File config) {
        container.withHiveMQConfig(config);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withFileInExtensionHomeFolder(
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
    public @NotNull HiveMQTestContainerRule withFileInExtensionHomeFolder(
            final @NotNull File file,
            final @NotNull String extensionId) {

        container.withFileInExtensionHomeFolder(file, extensionId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withFileInHomeFolder(
            final @NotNull File file,
            final @NotNull String pathInExtensionHome) {

        container.withFileInHomeFolder(file, pathInExtensionHome);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainerRule withFileInHomeFolder(
            final @NotNull File file) {

        container.withFileInHomeFolder(file);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull HiveMQTestContainer disableExtension(@NotNull String id, @NotNull String name) {
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
