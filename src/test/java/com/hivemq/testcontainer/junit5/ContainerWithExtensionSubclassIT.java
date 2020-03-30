package com.hivemq.testcontainer.junit5;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.MyExtensionWithSubclasses;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class ContainerWithExtensionSubclassIT {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension()
                    .withExtension(
                    "extension-1",
                    "my-extension",
                    "1.0",
                    100,
                    1000,
                    MyExtensionWithSubclasses.class)
                    .withLogLevel(Level.DEBUG);

    @Test()
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_extension_with_subclass() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }
}
