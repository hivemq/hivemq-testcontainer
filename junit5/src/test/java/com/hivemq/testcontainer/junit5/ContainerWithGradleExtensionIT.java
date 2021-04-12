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

import com.hivemq.testcontainer.core.GradleHiveMQExtensionSupplier;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 * @since 1.3.0
 */
public class ContainerWithGradleExtensionIT {

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    public void test() throws Exception {
        final File gradleExtension = new GradleHiveMQExtensionSupplier(
                new File(getClass().getResource("/gradle-extension").toURI()))
                .get();

        final HiveMQTestContainerExtension container = new HiveMQTestContainerExtension()
                .waitForExtension("Gradle Extension")
                .withExtension(gradleExtension);

        container.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(container.getMqttPort());
        container.afterEach(null);
    }
}
