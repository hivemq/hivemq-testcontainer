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

import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

/**
 * This IT is to test the debugging server in the docker container.
 *
 * @author Yannick Weber
 */
public class DebuggingIT {

    public static final int DEBUGGING_PORT_HOST = 9000;

    @Test(timeout = 200_000)
    public void test() throws IOException {
        final HiveMQExtension hiveMQExtension = HiveMQExtension.builder()
                .id("extension-1")
                .name("my-extension")
                .version("1.0")
                .mainClass(MyExtension.class).build();

        final HiveMQTestContainerRule rule =
                new HiveMQTestContainerRule()
                        .withExtension(hiveMQExtension)
                        .withDebugging(DEBUGGING_PORT_HOST);
        rule.start();
        final Socket localhost = new Socket("localhost", 9000);
        localhost.close();
        rule.stop();
    }

}
