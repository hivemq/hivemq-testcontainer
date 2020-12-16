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

import com.hivemq.testcontainer.core.GradleHiveMQExtensionSupplier;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Test;

import java.io.File;

/**
 * @author Yannick Weber
 * @since 1.3.0
 */
public class ContainerWithGradleExtensionIT {

    @Test(timeout = 200_000)
    public void test() throws Exception {
        final File gradleExtension = new GradleHiveMQExtensionSupplier(
                "src/test/resources/gradle-extension/build.gradle")
                .get();

        final HiveMQTestContainerRule container = new HiveMQTestContainerRule()
                .waitForExtension("Gradle Extension")
                .withExtension(gradleExtension);

        container.start();
        TestPublishModifiedUtil.testPublishModified(container.getMqttPort());
        container.stop();
    }
}
