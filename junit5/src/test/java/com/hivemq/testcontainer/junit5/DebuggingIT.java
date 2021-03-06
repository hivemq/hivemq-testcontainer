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
package com.hivemq.testcontainer.junit5;

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * This IT is to test the debugging server in the docker container.
 *
 * @author Yannick Weber
 */
public class DebuggingIT {

    public static final int DEBUGGING_PORT_HOST = 5005;

    @Test()
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test() throws IOException {

        HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension()
                        .withExtension(HiveMQExtension.builder()
                                .id("extension-1")
                                .name("my-extension")
                                .version("1.0")
                                .mainClass(MyExtension.class).build())
                        .withDebugging(DEBUGGING_PORT_HOST);

        extension.beforeEach(null);

        final Socket localhost = new Socket("localhost", DEBUGGING_PORT_HOST);
        localhost.close();

        final Mqtt3BlockingClient client = Mqtt3Client.builder().serverPort(extension.getMqttPort()).buildBlocking();
        client.connect();
        client.disconnect();

        extension.afterEach(null);
    }

}
