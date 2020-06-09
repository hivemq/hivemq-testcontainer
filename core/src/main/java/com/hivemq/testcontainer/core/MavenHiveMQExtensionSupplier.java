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

import com.hivemq.extension.sdk.api.annotations.NotNull;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.util.function.Supplier;

/**
 * This class automates the process of packaging a HiveMQ extension from a maven project.
 *
 * @author Yannick Weber
 */
public class MavenHiveMQExtensionSupplier implements Supplier<File> {

    private final @NotNull String pomFile;

    /**
     * This {@link Supplier} can be used if the current maven project is the HiveMQ Extension to supply.
     * It uses the pom.xml file of the current maven project.
     *
     * @return a {@link MavenHiveMQExtensionSupplier} for the current maven project
     */
    public static @NotNull MavenHiveMQExtensionSupplier direct() {
        return new MavenHiveMQExtensionSupplier("pom.xml");
    }

    /**
     * Creates a Maven HiveMQ extension {@link Supplier}.
     *
     * @param pomFile the path of the pom.xml of the HiveMQ extension to supply.
     */
    public MavenHiveMQExtensionSupplier(final @NotNull String pomFile) {
        this.pomFile = pomFile;
    }

    /**
     * Packages the HiveMQ extension, copies it to a temporary directory and returns the directory as a {@link File}.
     *
     * @return the {@link File} of the packaged HiveMQ extension
     */
    @Override
    public @NotNull File get() {
        final PomEquippedEmbeddedMaven embeddedMaven = EmbeddedMaven.forProject(pomFile);
        final BuiltProject aPackage = embeddedMaven.setGoals("package").build();
        final File targetDirectory = aPackage.getTargetDirectory();
        final String version = aPackage.getModel().getVersion();
        final String artifactId = aPackage.getModel().getArtifactId();

        final ZipFile zipFile = new ZipFile(new File(targetDirectory, artifactId + "-" + version + "-distribution.zip"));
        final File tempDir = Files.createTempDir();

        try {
            zipFile.extractAll(tempDir.getAbsolutePath());
        } catch (final ZipException e) {
            throw new RuntimeException(e);
        }
        return new File(tempDir, artifactId);
    }
}
