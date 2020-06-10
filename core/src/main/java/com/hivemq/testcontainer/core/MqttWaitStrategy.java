/*
 * Copyright 2020 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.testcontainer.core;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.hivemq.testcontainer.core.HiveMQTestContainerCore.MQTT_PORT;

/**
 * @author Yannick Weber
 */
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
                .retryWhen(flowable -> flowable.delay(retryInterval.toNanos(), TimeUnit.NANOSECONDS))
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
