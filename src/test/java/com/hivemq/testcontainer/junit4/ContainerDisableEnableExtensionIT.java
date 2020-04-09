package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerDisableEnableExtensionIT {

    private final @NotNull HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
            .id("extension-1")
            .name("my-extension")
            .version("1.0")
            .disabledOnStartup(true)
            .mainClass(MyExtension.class).build();

    @Rule
    public final @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
                    .withExtension(hiveMQExtension);

    @Test(timeout = 500_000)
    public void test_disable_enable_extension() throws ExecutionException, InterruptedException {
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(rule.getMqttPort()));
        rule.enableExtension(hiveMQExtension);
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
        rule.disableExtension(hiveMQExtension);
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(rule.getMqttPort()));
        rule.enableExtension(hiveMQExtension);
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
    }

}
