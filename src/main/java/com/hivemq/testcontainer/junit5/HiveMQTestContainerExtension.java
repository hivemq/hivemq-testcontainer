package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQTestContainerCore;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Yannick Weber
 */
public class HiveMQTestContainerExtension extends HiveMQTestContainerCore<HiveMQTestContainerExtension> implements BeforeEachCallback, AfterEachCallback {

    public HiveMQTestContainerExtension() {
        super();
    }

    public HiveMQTestContainerExtension(final @NotNull String image, final @NotNull String tag) {
        super(image, tag);
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) {
        start();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        stop();
    }

}
