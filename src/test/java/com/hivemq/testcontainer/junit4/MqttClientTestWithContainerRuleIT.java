package com.hivemq.testcontainer.junit4;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

public class MqttClientTestWithContainerRuleIT {

    @Rule
    final public @NotNull HiveMQTestContainerRule rule = new HiveMQTestContainerRule();

    @Test(timeout = 50_000)
    public void test_mqtt() {
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverPort(rule.getMqttPort())
                .buildBlocking();

        client.connect();
        client.disconnect();
    }
}
