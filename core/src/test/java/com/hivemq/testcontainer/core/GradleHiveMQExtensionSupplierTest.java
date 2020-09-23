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
package com.hivemq.testcontainer.core;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Yannick Weber
 * @since 1.3.0
 */
class GradleHiveMQExtensionSupplierTest {

    @Test
    void test_gradleBuildNotFound() {
        final File tempDir = Files.createTempDir();
        final File buildGradle = new File(tempDir, "build.gradle");

        final GradleHiveMQExtensionSupplier supplier = new GradleHiveMQExtensionSupplier(
                buildGradle.getAbsolutePath(),
                "project.name",
                "project.version");

        final IllegalStateException ex = assertThrows(IllegalStateException.class, supplier::get);
        assertEquals(buildGradle.getAbsolutePath() + " does not exist.", ex.getMessage());
    }

    @Test
    void test_gradleBuildNotReadable() throws IOException {
        final File tempDir = Files.createTempDir();
        final File buildGradle = new File(tempDir, "build.gradle");
        assertTrue(buildGradle.createNewFile());
        assertTrue(buildGradle.setReadable(false));

        final GradleHiveMQExtensionSupplier supplier = new GradleHiveMQExtensionSupplier(
                buildGradle.getAbsolutePath(),
                "project.name",
                "project.version");

        final IllegalStateException ex = assertThrows(IllegalStateException.class, supplier::get);
        assertEquals(buildGradle.getAbsolutePath() + " is not readable.", ex.getMessage());
    }

    @Test
    void test_wrapperNotFound() throws IOException {
        final File tempDir = Files.createTempDir();
        final File buildGradle = new File(tempDir, "build.gradle");
        assertTrue(buildGradle.createNewFile());

        final File wrapper = new File(tempDir, "gradlew");

        final GradleHiveMQExtensionSupplier supplier = new GradleHiveMQExtensionSupplier(
                buildGradle.getAbsolutePath(),
                "project.name",
                "project.version");

        final IllegalStateException ex = assertThrows(IllegalStateException.class, supplier::get);
        assertEquals("Gradle Wrapper " + wrapper.getAbsolutePath() + " does not exists.", ex.getMessage());
    }

    @Test
    void test_wrapperNotExecutable() throws IOException {
        final File tempDir = Files.createTempDir();
        final File buildGradle = new File(tempDir, "build.gradle");
        assertTrue(buildGradle.createNewFile());

        final File wrapper = new File(tempDir, "gradlew");
        assertTrue(wrapper.createNewFile());
        assertTrue(wrapper.setExecutable(false));

        final GradleHiveMQExtensionSupplier supplier = new GradleHiveMQExtensionSupplier(
                buildGradle.getAbsolutePath(),
                "project.name",
                "project.version");

        final IllegalStateException ex = assertThrows(IllegalStateException.class, supplier::get);
        assertEquals("Gradle Wrapper " + wrapper.getAbsolutePath() + " can not be executed.", ex.getMessage());
    }
}