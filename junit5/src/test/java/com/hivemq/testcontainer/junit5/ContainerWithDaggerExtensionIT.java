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
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import com.hivemq.testcontainer.util.dagger.MyExtensionWithDagger;
import com.hivemq.testcontainer.util.dagger.MyModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class ContainerWithDaggerExtensionIT {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension()
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .mainClass(MyExtensionWithDagger.class)
                            .addAdditionalClass(MyModule.class)
                            .addAdditionalClass(Class.forName("com.hivemq.testcontainer.util.dagger.MyModule_ProvidePublishModifierFactory"))
                            .build());

    public ContainerWithDaggerExtensionIT() throws ClassNotFoundException {
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_single_class_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }
}