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

	final static File THIS_EXECUTABLE_JAR = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
			.getPath());

	private File destinationFolder;
	private File sourceFolder;
	private boolean doRealCleanUp;

	private CleaningMode cleaningMode;

	/**
	 * Create an instance of the Cleaner class and set initial cleaning
	 * parameters.
	 * 
	 * @param sourceFolder
	 *            The base directory to scan for an Eclipse Installation.
	 * @param destinationFolder
	 *            The destination folder for moving duplicates to
	 * @param dryRun
	 *            when <code>false</code> than duplicates are moved to the
	 *            {@link #destinationFolder}
	 * 
	 *            when <code>true</code> than duplicates are just displayed, no
	 *            action is taken!
	 * 
	 * @param mode
	 *            The cleaning mode that indicates the method for resolving
	 *            duplicated artifacts.
	 */
	public Cleaner(File sourceFolder, File destinationFolder, boolean dryRun, CleaningMode mode) {
		this.sourceFolder = sourceFolder;
		this.destinationFolder = destinationFolder;
		this.cleaningMode = mode;
		this.doRealCleanUp = !dryRun;
	}

	private Set<Artifact> find(String type) {
		Set<Artifact> found = new HashSet<>();
		find(FileUtils.getFile(sourceFolder, type), found);
		find(FileUtils.getFile(sourceFolder, "dropins/eclipse/" + type), found);
		find(FileUtils.getFile(sourceFolder, "dropins/" + type), found);
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

			Artifact a = Artifact.createFromFile(f);
			logger.trace("Found bundle {} ", a);
			if (a != null) {
				artifacts.add(a);
			}
		}
	}

	public void doCleanUp() {
		logger.info(
				"\n Parameters summary\n\n Eclipse source folder: '{}'\n Move duplicates to: '{}'\n Dry run: '{}'\n Cleaning mode: '{}'\n\n",
				sourceFolder, destinationFolder, doRealCleanUp ? "No" : "Yes", cleaningMode);

		if (!sourceFolder.exists()) {
			logger.error(
					"The Eclipse installation hasn't been found at '{}', the location doesn't exists. \n\n The program terminated with an error!",
					sourceFolder);
			return;
		}

		if (destinationFolder.exists()) {
			logger.warn("The destination folder '{}' already exists! The duplicates will be move to this folder.",
					destinationFolder);
		}

		doCleanUpFor("plugins");
		doCleanUpFor("features");

		if (doRealCleanUp) {
			logger.info("\nRemoved duplicates are located at '{}' !", destinationFolder);
		}
		logger.info("Done!");
	}

	private void doCleanUpFor(String type) {
		File destinationTypeFolder = FileUtils.getFile(destinationFolder, type);

		logger.info("Scanning your Eclipse installation for {} at '{}'\n", type, destinationTypeFolder);
		Set<Artifact> artifacts = find(type);
		if (artifacts.isEmpty()) {
			logger.error("No {} found!", type);
			return;
		}

		DuplicationDetectorFactory detector = new DuplicationDetectorFactory(cleaningMode);
		final Set<Artifact> duplicates = detector.getDuplicates(artifacts);

		logger.info("\n\nFound {} duplicates from {} {} \n", duplicates.size(), artifacts.size(), type);

		if (doRealCleanUp) {
			logger.info("\nCleaning...");
			logger.info(" The duplicates will be moved to the folder '{}'\n", destinationTypeFolder);
		}

		for (Artifact artifact : duplicates) {
			if (doRealCleanUp) {
				logger.info("Cleaning up: {}", artifact);
				try {
					FileUtils.moveToDirectory(artifact.getSource(), destinationTypeFolder, true);
					logger.info(" OK");
				} catch (FileExistsException e1) {
					// the bundle was already copied there from an other
					// location, so it means we have more duplicates in multiple
					// location(s) with the same version, simply just delete it!
					boolean wasDeleted = FileUtils.deleteQuietly(artifact.getSource());
					if (wasDeleted) {
						logger.warn(
								" --> The duplicate `{}` was deleted direclty without creating a copy in the destination folder due to \n   `{}`.",
								artifact, e1.getLocalizedMessage());
					} else {
						logger.warn(" Unable to remove the duplicate '{}' from '{}'.", artifact, artifact.getSource());
					}
				} catch (Exception e) {
					logger.warn("Unable to remove the duplicate '{}'.", artifact, e);
				}
			} else {
				logger.info("(DRY-RUN) Duplicate: {}", artifact);
			}

		}

		if (doRealCleanUp) {
			logger.info("\nRemoved duplicates ({}) are located at '{}' !", type, destinationTypeFolder);
		} else {
			logger.info("\n(DRY-RUN) Removed duplicates ({}) will be located at '{}' !", type, destinationTypeFolder);
		}
	}
}
