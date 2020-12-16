/*
 * Copyright 2020 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.testcontainer.junit5;

import org.jetbrains.annotations.NotNull;
import com.hivemq.testcontainer.core.HiveMQTestContainerCore;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Yannick Weber
 */
public class HiveMQTestContainerExtension extends HiveMQTestContainerCore<HiveMQTestContainerExtension> implements BeforeEachCallback, AfterEachCallback {

    public HiveMQTestContainerExtension() {
        super();
    }

    public HiveMQTestContainerExtension(final @NotNull String image, final @NotNull String tag) {
        super(image, tag);
    }

    @Override
    public void beforeEach(final @Nullable ExtensionContext context) {
        start();
    }

    @Override
    public void afterEach(final @Nullable ExtensionContext context) {
        stop();
    }

}
