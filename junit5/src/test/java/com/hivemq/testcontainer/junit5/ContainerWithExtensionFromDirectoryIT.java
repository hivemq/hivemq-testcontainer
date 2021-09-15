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

import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.event.Level;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class ContainerWithExtensionFromDirectoryIT {

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test() throws Exception {
        final HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension()
                        .withExtension(MountableFile.forClasspathResource("/modifier-extension"))
                        .waitForExtension("Modifier Extension")
                        .withLogLevel(Level.DEBUG);

        extension.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.afterEach(null);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_wrongDirectoryName() throws Exception {
        final HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension()
                        .withExtension(MountableFile.forClasspathResource("/modifier-extension-wrong-name"))
                        .waitForExtension("Modifier Extension")
                        .withLogLevel(Level.DEBUG);

        extension.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.afterEach(null);
    }
}
