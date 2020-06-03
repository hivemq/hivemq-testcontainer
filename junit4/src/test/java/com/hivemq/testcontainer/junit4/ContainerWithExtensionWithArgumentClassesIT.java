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

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtensionWithArgumentClasses;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * @author Yannick Weber
 */
public class ContainerWithExtensionWithArgumentClassesIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule()
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .mainClass(MyExtensionWithArgumentClasses.class).build());


    @Test(timeout = 500_000)
    public void test_extension_with_argument_classes() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
    }


}
