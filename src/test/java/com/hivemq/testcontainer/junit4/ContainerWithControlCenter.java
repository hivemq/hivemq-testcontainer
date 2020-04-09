package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

public class ContainerWithControlCenter {

    public static final int CONTROL_CENTER_PORT = 8080;

    @Rule
    public @NotNull HiveMQTestContainerRule rule =
            new HiveMQTestContainerRule("hivemq/hivemq4", "latest")
            .withControlCenter(CONTROL_CENTER_PORT);
    @Test
    public void testHttp() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpUriRequest request = new HttpGet( "http://localhost:" + CONTROL_CENTER_PORT);
        httpClient.execute(request);
    }
}
