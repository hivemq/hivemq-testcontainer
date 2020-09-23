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

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.util.function.Supplier;

/**
 * This class automates the process of packaging a HiveMQ extension from a gradle project.
 *
 * @author Yannick Weber
 * @since 1.3.0
 */
public class GradleHiveMQExtensionSupplier implements Supplier<File> {

    private static final @NotNull String BUILD_STARTED =
            "=================================================================\n" +
                    "===   Embedded Gradle build started: %s   ===\n" +
                    "=================================================================\n";

    private static final @NotNull String BUILD_STOPPED =
            "=================================================================\n" +
                    "===   Embedded Gradle build stopped: %s   ===\n" +
                    "=================================================================";

    private static final @NotNull String TASK = "hivemqExtensionZip";
    private static final @NotNull String GRADLE_WRAPPER_COMMAND = "./gradlew";

    private final @NotNull String gradleBuild;
    private final @NotNull String projectName;
    private final @NotNull String projectVersion;
    private boolean quiet = false;

    /**
     * This {@link Supplier} can be used if the current gradle project is the HiveMQ Extension to supply.
     * It uses the build.gradle file and the gradle wrapper of the current gradle project.
     *
     * @return a {@link GradleHiveMQExtensionSupplier} for the current gradle project
     * @param projectName the project.name of HiveMQ extension to supply
     * @param projectVersion the project.version of the HiveMQ extension to supply
     * @since 1.3.0
     */
    public static @NotNull GradleHiveMQExtensionSupplier direct(
            final @NotNull String projectName,
            final @NotNull String projectVersion) {

        return new GradleHiveMQExtensionSupplier("build.gradle", projectName, projectVersion);
    }

    /**
     * Creates a Maven HiveMQ extension {@link Supplier}.
     * It uses the gradle wrapper of the gradle project associated with the given It uses the build.gradle file.
     *
     * @param gradleBuild the path of the build.gradle of the HiveMQ extension to supply.
     * @param projectName the project.name of HiveMQ extension to supply
     * @param projectVersion the project.version of the HiveMQ extension to supply
     * @since 1.3.0
     */
    public GradleHiveMQExtensionSupplier(
            final @NotNull String gradleBuild,
            final @NotNull String projectName,
            final @NotNull String projectVersion) {

        this.gradleBuild = gradleBuild;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
    }

    /**
     * Packages the HiveMQ extension, copies it to a temporary directory and returns the directory as a {@link File}.
     *
     * @return the {@link File} of the packaged HiveMQ extension
     * @since 1.3.0
     */
    @Override
    public @NotNull File get() {
        final File gradleBuildFile = new File(gradleBuild);
        if (!gradleBuildFile.exists()) {
            throw new IllegalStateException(gradleBuild + " does not exist.");
        }
        if (!gradleBuildFile.canRead()) {
            throw new IllegalStateException(gradleBuild + " is not readable.");
        }

        System.out.printf((BUILD_STARTED) + "%n", gradleBuild);

        final String gradleWrapper = gradleBuildFile.getParent() + "/gradlew";
        final File gradleWrapperFile = new File(gradleWrapper);
        if (!gradleWrapperFile.exists()) {
            throw new IllegalStateException("Gradle Wrapper " + gradleWrapperFile.getAbsolutePath() + " does not exists.");
        }
        if (!gradleWrapperFile.canExecute()) {
            throw new IllegalStateException("Gradle Wrapper " + gradleWrapperFile.getAbsolutePath() + " can not be executed.");
        }

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(gradleBuildFile.getParentFile());
            processBuilder.command(GRADLE_WRAPPER_COMMAND, TASK);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            if (!quiet) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            }
            final Process gradleBuild = processBuilder.start();
            final int exitCode = gradleBuild.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Gradle build exited with code " + exitCode);
            }

            System.out.printf((BUILD_STOPPED) + "%n", this.gradleBuild);

            final ZipFile zipFile = new ZipFile(
                    new File(gradleBuildFile.getParentFile(),
                            "build/hivemq-extension/" + projectName + "-" + projectVersion + ".zip"));
            final File tempDir = Files.createTempDir();

            try {
                zipFile.extractAll(tempDir.getAbsolutePath());
            } catch (final ZipException e) {
                throw new RuntimeException(e);
            }
            return new File(tempDir, projectName);

        } catch (final Exception e) {
            throw new RuntimeException("Exception while building the HiveMQ extension with gradle.", e);
        }
    }

    /**
     * Suppress stdout of the gradle build.
     *
     * @return self
     * @since 1.3.0
     */
    @NotNull
    public GradleHiveMQExtensionSupplier quiet() {
        this.quiet = true;
        return this;
    }
}
