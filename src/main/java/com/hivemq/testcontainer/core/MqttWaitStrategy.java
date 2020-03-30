package com.hivemq.testcontainer.core;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.hivemq.testcontainer.core.HiveMQTestContainerImpl.MQTT_PORT;

public class MqttWaitStrategy extends AbstractWaitStrategy {

    private @NotNull Duration retryInterval = Duration.ofMillis(500);
    private @NotNull Duration initialWaitDuration = Duration.ofSeconds(5);

    @Override
    protected void waitUntilReady() {
        final Integer mappedMqttPort = waitStrategyTarget.getMappedPort(MQTT_PORT);

        final Mqtt5RxClient retryClient = Mqtt5Client.builder()
                .identifier("retry-client")
                .serverPort(mappedMqttPort)
                .serverHost("localhost")
                .buildRx();

        try {
            Thread.sleep(initialWaitDuration.toMillis());
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        retryClient.connect()
                .retryWhen(flowable -> flowable.delay(retryInterval.getSeconds(), TimeUnit.SECONDS))
                .timeout(startupTimeout.getSeconds(), TimeUnit.SECONDS)
                .ignoreElement()
                .andThen(retryClient.disconnect())
                .blockingAwait();
    }

    public @NotNull MqttWaitStrategy withRetryInterval(final @NotNull Duration retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public @NotNull MqttWaitStrategy withInitialWait(final @NotNull Duration initialWaitDuration) {
        this.initialWaitDuration = initialWaitDuration;
        return this;
    }
}
