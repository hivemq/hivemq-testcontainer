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

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.MavenHiveMQExtensionSupplier;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yannick Weber
 */
public class DisableEnableMavenExtensionIT {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
                    .withExtension(new MavenHiveMQExtensionSupplier("src/test/resources/maven-extension/pom.xml").get());

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_disable_enable_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.disableExtension("Maven Extension", "maven-extension");
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(extension.getMqttPort()));
        extension.enableExtension("Maven Extension", "maven-extension");
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }

}