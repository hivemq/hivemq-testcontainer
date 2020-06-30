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

/**
 * @author Yannick Weber
 */
public class ContainerWithMavenExtensionIT {

    @Test(timeout = 500_000)
    public void test() throws Exception {
        final HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule()
                    .withExtension(
                            new MavenHiveMQExtensionSupplier("src/test/resources/maven-extension/pom.xml")
                                    .cleanAfter()
                                    .cleanBefore()
                                    .quiet().get());
        rule.start();
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
        rule.stop();
    }

}
