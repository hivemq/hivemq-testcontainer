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
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class automates the process of packaging a HiveMQ extension from a gradle project.
 * It uses ./gradlew on MacOS and Linux and uses gradlew.bat on Windows to execute the gradle task.
 *
 * @author Yannick Weber
 * @since 1.3.0
 */
public class GradleHiveMQExtensionSupplier implements Supplier<File> {

    private static final @NotNull Pattern ROOT_PROJECT_PATTERN = Pattern.compile(".*'(.*)'");
    private static final @NotNull String PROPERTY_REGEX = "(.*): (.*)";

    private static final @NotNull String BUILD_STARTED =
            "=================================================================\n" +
                    "===   Embedded Gradle build started: %s   ===\n" +
                    "=================================================================\n";

    private static final @NotNull String BUILD_STOPPED =
            "=================================================================\n" +
                    "===   Embedded Gradle build stopped: %s   ===\n" +
                    "=================================================================";

    private static final @NotNull String TASK = "hivemqExtensionZip";
    private static final @NotNull String GRADLE_WRAPPER_BASH_COMMAND = "./gradlew";

    private final @NotNull String gradleBuild;
    private boolean quiet = false;

    /**
     * This {@link Supplier} can be used if the current gradle project is the HiveMQ Extension to supply.
     * It uses the build.gradle file and the gradle wrapper of the current gradle project.
     *
     * @return a {@link GradleHiveMQExtensionSupplier} for the current gradle project
     * @since 1.3.0
     */
    public static @NotNull GradleHiveMQExtensionSupplier direct() {

        return new GradleHiveMQExtensionSupplier("build.gradle");
    }

    /**
     * Creates a Maven HiveMQ extension {@link Supplier}.
     * It uses the gradle wrapper of the gradle project associated with the given It uses the build.gradle file.
     *
     * @param gradleBuild the path of the build.gradle of the HiveMQ extension to supply.
     * @since 1.3.0
     */
    public GradleHiveMQExtensionSupplier(
            final @NotNull String gradleBuild) {

        this.gradleBuild = gradleBuild;
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

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(gradleBuildFile.getParentFile());
            processBuilder.command(getCommandForOs(gradleBuildFile), TASK);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            if (!quiet) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            }
            final Process gradleBuild = processBuilder.start();
            final int extensionBuildExitCode = gradleBuild.waitFor();
            if (extensionBuildExitCode != 0) {
                throw new IllegalStateException("Gradle build exited with code " + extensionBuildExitCode);
            }

            final ProcessBuilder propertiesTask = new ProcessBuilder();
            propertiesTask.directory(gradleBuildFile.getParentFile());
            propertiesTask.command(getCommandForOs(gradleBuildFile), "properties", "-q");
            propertiesTask.redirectError(ProcessBuilder.Redirect.INHERIT);

            final Process propertiesTaskProcess = propertiesTask.start();
            final int propertiesExitCode = propertiesTaskProcess.waitFor();
            if (propertiesExitCode != 0) {
                throw new IllegalStateException("Gradle build exited with code " + propertiesExitCode);
            }

            final BufferedReader br = new BufferedReader(new InputStreamReader(propertiesTaskProcess.getInputStream()));

            final Map<String, String> gradleProperties = br.lines()
                    .filter(s -> s.matches(PROPERTY_REGEX))
                    .map(s -> {
                        final String[] splits = s.split(": ");
                        return new AbstractMap.SimpleEntry<>(splits[0], splits[1]);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            System.out.printf((BUILD_STOPPED) + "%n", this.gradleBuild);

            final String projectVersion = gradleProperties.get("version");
            final String rootProject = gradleProperties.get("rootProject");
            final Matcher matcher = ROOT_PROJECT_PATTERN.matcher(rootProject);
            final boolean b = matcher.find();
            final String projectName = matcher.group(1);

            final ZipFile zipFile = new ZipFile(
                    new File(gradleBuildFile.getParentFile(),
                            "build/hivemq-extension/" + projectName + "-" + projectVersion + ".zip"));
            final File tempDir = Files.createTempDirectory("").toFile();

            zipFile.extractAll(tempDir.getAbsolutePath());
            return new File(tempDir, projectName);

        } catch (final Exception e) {
            throw new RuntimeException("Exception while building the HiveMQ extension with gradle.", e);
        }
    }

    private @NotNull String getCommandForOs(final @NotNull File gradleBuildFile) {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX) {
            final String gradleWrapper = gradleBuildFile.getParent() + "/gradlew";
            final File gradleWrapperBashFile = new File(gradleWrapper);
            if (gradleWrapperBashFile.exists()) {
                if (!gradleWrapperBashFile.canExecute()) {
                    throw new IllegalStateException("Gradle Wrapper " + gradleWrapperBashFile.getAbsolutePath() + " can not be executed.");
                }
                return GRADLE_WRAPPER_BASH_COMMAND;
            }
        } else if (SystemUtils.IS_OS_WINDOWS) {
            final String gradleWrapperBat = gradleBuildFile.getParent() + "/gradlew.bat";
            final File gradleWrapperBatFile = new File(gradleWrapperBat);
            if (gradleWrapperBatFile.exists()) {
                if (!gradleWrapperBatFile.canExecute()) {
                    throw new IllegalStateException("Gradle Wrapper " + gradleWrapperBatFile.getAbsolutePath() + " can not be executed.");
                }
                return gradleWrapperBatFile.getAbsolutePath();
            }
        }
        throw new IllegalStateException("Unkown OS Version");

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
