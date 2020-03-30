package com.hivemq.testcontainer.junit5;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yannick Weber
 */
public class ContainerWithCustomConfigIT {

    @RegisterExtension
    final @NotNull HiveMQTestContainerExtension extension = new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
            .withHiveMQConfig(new File("src/test/resources/config.xml"));

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_custom_config() {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverPort(extension.getMqttPort())
                .buildBlocking();

        publisher.connect();

        assertThrows(MqttSessionExpiredException.class, () -> {
            // this should fail since only QoS 0 is allowed by the configuration
            publisher.publishWith()
                    .topic("test/topic")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();
        });
    }
}
