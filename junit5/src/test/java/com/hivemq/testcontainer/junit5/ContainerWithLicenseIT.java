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

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class ContainerWithLicenseIT {

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test() throws Exception {
        final HiveMQTestContainerExtension extension =
                new HiveMQTestContainerExtension()
                        .withExtension(HiveMQExtension.builder()
                                .id("extension-1")
                                .name("my-extension")
                                .version("1.0")
                                .mainClass(LicenceCheckerExtension.class).build())
                        .withLicense(new File("src/test/resources/myLicense.lic"))
                        .withLicense(new File("src/test/resources/myExtensionLicense.elic"))
                        .withDebugging();

        extension.beforeEach(null);
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
        extension.afterEach(null);
    }

    @SuppressWarnings("CodeBlock2Expr")
    public static class LicenceCheckerExtension implements ExtensionMain {

        @Override
        public void extensionStart(@NotNull ExtensionStartInput extensionStartInput, @NotNull ExtensionStartOutput extensionStartOutput) {

            final PublishInboundInterceptor publishInboundInterceptor = (publishInboundInput, publishInboundOutput) -> {

                final File homeFolder = extensionStartInput.getServerInformation().getHomeFolder();
                final File myLicence = new File(homeFolder, "license/myLicense.lic");
                final File myExtensionLicence = new File(homeFolder, "license/myExtensionLicense.elic");

                if (myLicence.exists() && myExtensionLicence.exists()) {
                    publishInboundOutput.getPublishPacket().setPayload(ByteBuffer.wrap("modified".getBytes(StandardCharsets.UTF_8)));
                }
            };

            final ClientInitializer clientInitializer = (initializerInput, clientContext) -> {
                clientContext.addPublishInboundInterceptor(publishInboundInterceptor);
            };

            Services.initializerRegistry().setClientInitializer(clientInitializer);
        }

        @Override
        public void extensionStop(@NotNull ExtensionStopInput extensionStopInput, @NotNull ExtensionStopOutput extensionStopOutput) {

        }
    }

}
