/*******************************************************************************
 * Copyright 2014 Chocolate Jar, Andrej Zachar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *******************************************************************************/
package eu.chocolatejar.eclipse.plugin.cleaner;

import static eu.chocolatejar.eclipse.plugin.cleaner.util.DropinsFilter.DROPINS;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;
import eu.chocolatejar.eclipse.plugin.cleaner.model.CleaningMode;

/**
 * Finds duplicate bundles and move them to a back up folder
 */
public class Cleaner {

    private static final Logger logger = LoggerFactory.getLogger(Cleaner.class);

    private static final String FEATURES = "features";
    private static final String PLUGINS = "plugins";

    private static final File THIS_EXECUTABLE_JAR = new File(Main.class.getProtectionDomain().getCodeSource()
            .getLocation().getPath());

    private final ArtifactParser artifactParser = new ArtifactParser();
    private final DuplicationDetectorFactory detector;

    private final File backupFolder;
    private final File eclipseFolder;
    private final boolean dryRun;
    private final CleaningMode cleaningMode;

    /**
     * Create an instance of the Cleaner class and set initial cleaning
     * parameters.
     * 
     * @param eclipseFolder
     *            The base directory to scan for an Eclipse Installation.
     * @param backupFolder
     *            The destination folder for duplicates back up
     * @param dryRun
     *            when <code>false</code> than duplicates are moved to the
     *            {@link #backupFolder}
     * 
     *            when <code>true</code> than duplicates are just displayed, no
     *            action is taken!
     * 
     * @param mode
     *            The cleaning mode that indicates the method for resolving
     *            duplicated artifacts.
     */
    public Cleaner(File sourceFolder, File destinationFolder, boolean dryRun, CleaningMode mode) {
        this.eclipseFolder = sourceFolder;
        this.backupFolder = destinationFolder;
        this.cleaningMode = mode;
        this.dryRun = dryRun;
        this.detector = new DuplicationDetectorFactory(cleaningMode);
    }

    /**
     * Executes clean up based on provided parameters in the constructor.
     */
    public void run() {
        logger.info(
                "\n Parameters summary\n\n Eclipse folder (source): '{}'\n Back up duplicates to: '{}'\n Dry run: '{}'\n Cleaning mode: '{}'\n\n",
                eclipseFolder, backupFolder, dryRun, cleaningMode);

        if (!eclipseFolder.exists()) {
            logger.error(
                    "The Eclipse installation hasn't been found at '{}', the location doesn't exists. \n\n The program terminated with an error!",
                    eclipseFolder);
            return;
        }

        if (backupFolder.exists()) {
            logger.warn("The destination folder '{}' already exists! The duplicates will be move to this folder.",
                    backupFolder);
        }

        logger.info("Scanning '{}'...", eclipseFolder);

        simulateOrDoRealCleanUp();

        logger.info("Done!");
    }

    /**
     * Simulates or executes real clean up of plugins and features.
     */
    private void simulateOrDoRealCleanUp() {
        Set<Artifact> plugins = findArtifacts(PLUGINS);
        Set<Artifact> features = findArtifacts(FEATURES);

        Set<Artifact> pluginsDuplicates = detector.getDuplicates(plugins);
        Set<Artifact> featuresDuplicates = detector.getDuplicates(features);

        if (dryRun) {
            logger.info("\n Simulating clean up...");

            showDuplicates(pluginsDuplicates);
            showDuplicates(featuresDuplicates);
        } else {
            logger.info("\n Cleaning up...");

            removeAndBackupDuplicates(pluginsDuplicates, PLUGINS);
            removeAndBackupDuplicates(featuresDuplicates, FEATURES);

            logger.warn("\n Duplicates are located at '{}'", backupFolder);
        }

        logger.warn("\n Found {} duplicates from overall {} plugins and {} duplicates from overall {} features.",
                pluginsDuplicates.size(), plugins.size(), featuresDuplicates.size(), features.size());
    }

    private void showDuplicates(Set<Artifact> duplicates) {
        for (Artifact a : duplicates) {
            logger.info("{}", a);
        }
    }

    /**
     * Removes and back duplicates up.
     * 
     * @param duplicates
     *            list of artifacts to remove
     * @param type
     *            either {@link #PLUGINS} or {@link #FEATURES}
     */
    private void removeAndBackupDuplicates(final Set<Artifact> duplicates, final String type) {
        File destinationTypeFolder = FileUtils.getFile(backupFolder, type);

        for (Artifact artifact : duplicates) {
            logger.info("Cleaning {}", artifact);
            try {
                FileUtils.moveToDirectory(artifact.getLocation(), destinationTypeFolder, true);
                logger.info(" OK");
            } catch (FileExistsException e1) {
                // the bundle was already copied there from an other
                // location, so it means we have more duplicates in multiple
                // location(s) with the same version, simply just delete it!
                boolean wasDeleted = FileUtils.deleteQuietly(artifact.getLocation());
                if (wasDeleted) {
                    logger.warn(
                            " --> The duplicate `{}` was deleted directly without creating a copy in the destination folder due to \n   `{}`.",
                            artifact, e1.getLocalizedMessage());
                } else {
                    logger.warn(" Unable to remove the duplicate '{}' from '{}'.", artifact, artifact.getLocation());
                }
            } catch (Exception e) {
                logger.error("Unable to remove the duplicate '{}'.", artifact, e);
            }

        }
    }

    /**
     * Finds either plugins or features within Eclipse.
     * 
     * @param type
     *            either {@link #PLUGINS} or {@link #FEATURES}
     * @return never <code>null</code>
     */
    private Set<Artifact> findArtifacts(String type) {
        Set<Artifact> found = new HashSet<>();
        find(FileUtils.getFile(eclipseFolder, type), found);
        find(FileUtils.getFile(eclipseFolder, DROPINS, "eclipse", type), found);
        find(FileUtils.getFile(eclipseFolder, DROPINS, type), found);
        return found;
    }

    private void find(File base, Set<Artifact> artifacts) {
        File[] entries = base.listFiles();
        if (entries == null) {
            return;
        }

        for (File f : entries) {
            if (f.isFile() && f.getName().equals(THIS_EXECUTABLE_JAR.getName())) {
                logger.debug("Skipping this executable jar: {}", THIS_EXECUTABLE_JAR);
                continue;
            }

            Artifact a = artifactParser.createFromFile(f);
            logger.trace("Found bundle {} ", a);
            if (a != null) {
                artifacts.add(a);
            }
        }
    }
}
