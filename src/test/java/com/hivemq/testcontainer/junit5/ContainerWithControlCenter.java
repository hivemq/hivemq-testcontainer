package com.hivemq.testcontainer.junit5;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.TimeUnit;

public class ContainerWithControlCenter {

    public static final int CONTROL_CENTER_PORT = 8080;

    @RegisterExtension
    public @NotNull HiveMQTestContainerExtension extension =
            new HiveMQTestContainerExtension("hivemq/hivemq4", "latest")
            .withControlCenter(CONTROL_CENTER_PORT);
    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void testHttp() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpUriRequest request = new HttpGet( "http://localhost:" + CONTROL_CENTER_PORT);
        httpClient.execute(request);
    }
}
