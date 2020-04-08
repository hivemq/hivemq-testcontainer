package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerDisableExtensionIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .mainClass(MyExtension.class).build())
                    .withLogLevel(Level.DEBUG);

    @Test(timeout = 500_000)
    public void test_disable_enterprise_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
        rule.disableExtension("extension-1", "my-extension");
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(rule.getMqttPort()));
    }

}
