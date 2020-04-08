package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import com.hivemq.testcontainer.util.MyExtensionWithSubclasses;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;

import java.util.concurrent.ExecutionException;

/**
 * @author Yannick Weber
 */
public class ContainerWithExtensionSubclassIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule extension =
            new HiveMQTestContainerRule()
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .priority(100)
                            .startPriority(1000)
                            .mainClass(MyExtensionWithSubclasses.class).build())
                    .withLogLevel(Level.DEBUG);

    @Test(timeout = 500_000)
    public void test_extension_with_subclass() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }
}
