package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.util.MyExtension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;

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

    @Rule
    public final @NotNull HiveMQTestContainerRule extension =
            new HiveMQTestContainerRule()
                    .withExtension(
                        "extension-1",
                        "my-extension",
                        "1.0",
                        100,
                        1000,
                        MyExtension.class)
                    .withDebugging(DEBUGGING_PORT_HOST);

    @Test(timeout = 500_000)
    public void test_debug_port_open() throws IOException {

        final Socket localhost = new Socket("localhost", 9000);
        localhost.close();
    }

}
