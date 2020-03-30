package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.annotations.NotNull;

public class PathUtil {

    public static @NotNull String preparePath(@NotNull String pathInExtensionHome) {
        if ("/".equals(pathInExtensionHome) || pathInExtensionHome.isEmpty()) {
            return "/";
        }
        if (!pathInExtensionHome.startsWith("/")) {
            pathInExtensionHome = "/" + pathInExtensionHome;
        }
        if (!pathInExtensionHome.endsWith("/")) {
            pathInExtensionHome += "/";
        }
        return pathInExtensionHome;
    }
}
