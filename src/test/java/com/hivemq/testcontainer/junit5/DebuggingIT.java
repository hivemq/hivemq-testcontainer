package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQExtension;
import com.hivemq.testcontainer.util.MyExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * This IT is to test the debugging server in the docker container.
 *
 * @author Yannick Weber
 */
public class DebuggingIT {

    public static final int DEBUGGING_PORT_HOST = 9000;

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension()
                    .withExtension(HiveMQExtension.builder()
                            .id("extension-1")
                            .name("my-extension")
                            .version("1.0")
                            .mainClass(MyExtension.class).build())
                    .withDebugging(DEBUGGING_PORT_HOST);

    @Test()
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_debug_port_open() throws IOException {

        final Socket localhost = new Socket("localhost", 9000);
        localhost.close();
    }

}
