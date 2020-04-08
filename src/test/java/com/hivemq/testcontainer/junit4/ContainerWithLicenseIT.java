package com.hivemq.testcontainer.junit4;

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
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * @author Yannick Weber
 */
public class ContainerWithLicenseIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule extension =
            new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .mainClass(LicenceCheckerExtension.class).build())
            .withLicense(new File("src/test/resources/myLicense.lic"))
            .withLicense(new File("src/test/resources/myExtensionLicense.elic"))
            .withDebugging();

    @Test(timeout = 500_000)
    public void test_single_class_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
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
