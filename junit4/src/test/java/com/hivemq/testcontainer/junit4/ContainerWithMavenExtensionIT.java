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

import com.hivemq.testcontainer.core.MavenHiveMQExtensionSupplier;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Test;
import org.testcontainers.utility.MountableFile;

/**
 * @author Yannick Weber
 */
public class ContainerWithMavenExtensionIT {

    @Test(timeout = 200_000)
    public void test() throws Exception {
        final MountableFile mavenExtension = new MavenHiveMQExtensionSupplier(
                getClass().getResource("/maven-extension/pom.xml").getPath())
                .addProperty("HIVEMQ_GROUP_ID", "com.hivemq")
                .addProperty("HIVEMQ_EXTENSION_SDK", "hivemq-extension-sdk")
                .addProperty("HIVEMQ_EXTENSION_SDK_VERSION", "4.3.0")
                .quiet()
                .get();

        final HiveMQTestContainerRule rule = new HiveMQTestContainerRule()
                .waitForExtension("Maven Extension")
                .withExtension(mavenExtension);

        rule.start();
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
        rule.stop();
    }

}
