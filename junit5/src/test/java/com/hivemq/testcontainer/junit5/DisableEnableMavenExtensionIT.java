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

import com.hivemq.testcontainer.core.MavenHiveMQExtensionSupplier;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yannick Weber
 */
public class DisableEnableMavenExtensionIT {

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test() throws Exception {
        final MountableFile extensionDir = new MavenHiveMQExtensionSupplier(
                getClass().getResource("/maven-extension/pom.xml").getPath())
                .addProperty("HIVEMQ_GROUP_ID", "com.hivemq")
                .addProperty("HIVEMQ_EXTENSION_SDK", "hivemq-extension-sdk")
                .addProperty("HIVEMQ_EXTENSION_SDK_VERSION", "4.3.0")
                .quiet()
                .get();

        final HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4").withTag("latest"))
                        .waitForExtension("Maven Extension")
                        .withExtension(extensionDir);

        extension.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.disableExtension("Maven Extension", "maven-extension");
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(extension.getMqttPort()));
        extension.enableExtension("Maven Extension", "maven-extension");
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.afterEach(null);
    }

}
