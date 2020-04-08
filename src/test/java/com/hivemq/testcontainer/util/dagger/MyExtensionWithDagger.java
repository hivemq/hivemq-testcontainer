package com.hivemq.testcontainer.util.dagger;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;

/**
 * @author Yannick Weber
 */
@SuppressWarnings("CodeBlock2Expr")
public class MyExtensionWithDagger implements ExtensionMain {

    @Override
    public void extensionStart(@NotNull ExtensionStartInput extensionStartInput, @NotNull ExtensionStartOutput extensionStartOutput) {
        try {
            final MyComponent component = DaggerMyComponent.builder().build();
            final PublishModifier publishInboundInterceptor = component.providePublishModifier();
            final ClientInitializer clientInitializer = (initializerInput, clientContext) -> {
                clientContext.addPublishInboundInterceptor(publishInboundInterceptor);
            };
            Services.initializerRegistry().setClientInitializer(clientInitializer);
        } catch (final Throwable t) {
            t.printStackTrace();
        }

    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput extensionStopInput, @NotNull ExtensionStopOutput extensionStopOutput) {

    }
}