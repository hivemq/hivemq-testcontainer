package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import com.hivemq.testcontainer.util.dagger.MyExtensionWithDagger;
import com.hivemq.testcontainer.util.dagger.MyModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class ContainerWithDaggerExtensionIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule()
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

    @Test(timeout = 500_000)
    public void test_single_class_extension() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(rule.getMqttPort());
    }
}