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
package com.hivemq.testcontainer.junit4;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

public class ContainerWithControlCenterIT {

    public static final int CONTROL_CENTER_PORT = 8080;

    @Test(timeout = 200_000)
    public void test() throws Exception {
        final HiveMQTestContainerRule rule =
                new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
                        .withControlCenter(CONTROL_CENTER_PORT);

        rule.start();

        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpUriRequest request = new HttpGet("http://localhost:" + CONTROL_CENTER_PORT);
        httpClient.execute(request);

        rule.stop();
    }
}
