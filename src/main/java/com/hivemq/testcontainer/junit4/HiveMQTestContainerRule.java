package com.hivemq.testcontainer.junit4;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQTestContainerCore;

/**
 * @author Yannick Weber
 */
public class HiveMQTestContainerRule extends HiveMQTestContainerCore<HiveMQTestContainerRule> {

    public HiveMQTestContainerRule() {
        super();
    }

    public HiveMQTestContainerRule(final @NotNull String image, final @NotNull String tag) {
        super(image, tag);
    }
}
