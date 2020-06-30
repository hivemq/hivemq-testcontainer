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

import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("ConstantConditions")
public class ContainerWithExtensionIT {

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test() throws Exception {
        final HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension()
                        .withExtension(HiveMQExtension.builder()
                                .id("extension-1")
                                .name("my-extension")
                                .version("1.0")
                                .mainClass(MyExtension.class).build());

        extension.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.afterEach(null);
    }
}
