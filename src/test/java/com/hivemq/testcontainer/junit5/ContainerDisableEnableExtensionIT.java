package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerDisableEnableExtensionIT {

    private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .disabledOnStartup(true)
            .mainClass(MyExtension.class).build();

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
                    .withExtension(hiveMQExtension)
                    .withLogLevel(Level.DEBUG);

    @Test()
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_disable_enable_extension() throws ExecutionException, InterruptedException {
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(extension.getMqttPort()));
        extension.enableExtension(hiveMQExtension);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.disableExtension(hiveMQExtension);
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(extension.getMqttPort()));
        extension.enableExtension(hiveMQExtension);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }

}
