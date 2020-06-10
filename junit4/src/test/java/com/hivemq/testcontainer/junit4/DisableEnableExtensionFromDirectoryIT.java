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
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yannick Weber
 */
public class DisableEnableExtensionFromDirectoryIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
                    .withExtension(new File("src/test/resources/modifier-extension"))
                    .withLogLevel(Level.DEBUG);

    @Test(timeout = 500_000)
    public void test_disable_enable_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
        rule.disableExtension("Modifier Extension", "modifier-extension");
        assertThrows(ExecutionException.class, () -> TestPublishModifiedUtil.testPublishModified(rule.getMqttPort()));
        rule.enableExtension("Modifier Extension", "modifier-extension");
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
    }

}
