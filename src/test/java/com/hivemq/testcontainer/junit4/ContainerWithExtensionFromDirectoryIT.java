package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.util.TestPublishModifiedUtil;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * @author Yannick Weber
 */
public class ContainerWithExtensionFromDirectoryIT {

    @Rule
    public final @NotNull HiveMQTestContainerRule extension =
            new HiveMQTestContainerRule()
                    .withExtension(new File("src/test/resources/modifier-extension"))
                    .withLogLevel(Level.DEBUG);

    @Test(timeout = 500_000)
    public void test_extension_from_file() throws ExecutionException, InterruptedException {
        TestPublishModifiedUtil.testPublishModified(extension.getMqttPort());
    }
}
