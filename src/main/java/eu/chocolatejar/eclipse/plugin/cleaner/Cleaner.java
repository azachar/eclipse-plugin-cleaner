package eu.chocolatejar.eclipse.plugin.cleaner;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds bundles and move duplicates to a different folder
 */
public class Cleaner {
	private static final Logger logger = LoggerFactory.getLogger(Cleaner.class);

	final static File THIS_EXECUTABLE_JAR = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
			.getPath());

	private File destinationFolder;
	private File sourceFolder;
	private boolean doRealCleanUp;

	/**
	 * @param sourceFolder
	 *            The base directory to scan for files.
	 * @param destinationFolder
	 *            The destination deployment package file.
	 * @param dryRun
	 *            when <code>false</code> than duplicates are moved to the
	 *            {@link #destinationFolder}
	 * 
	 *            when <code>true</code> than duplicates are just displayed, no
	 *            action is taken!
	 * 
	 */
	public Cleaner(File sourceFolder, File destinationFolder, boolean dryRun) {
		this.sourceFolder = sourceFolder;
		this.destinationFolder = destinationFolder;
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

	public void doCleanUp() throws IllegalArgumentException {
		doCleanUpFor("plugins");
		doCleanUpFor("features");

		if (doRealCleanUp) {
			logger.info("\nRemoved duplicates are located at '{}' !", destinationFolder);
		}
		logger.info("Done!");
	}

	private void doCleanUpFor(String type) {
		File destinationTypeFolder = Paths.get(destinationFolder.getAbsolutePath(), type).toFile();

		logger.info("Scanning Eclipse for {} at '{}'\n", type, sourceFolder);
		Set<Artifact> artifacts = find(type);
		if (artifacts.isEmpty()) {
			logger.error("No {} found!", type);
			return;
		}

		DuplicationDetector dp = new DuplicationDetector(artifacts);
		logger.info("\n\nFound {} duplicates from {} {} \n", dp.getDuplicates().size(), artifacts.size(), type);

		if (doRealCleanUp) {
			logger.info("\nCleaning...");
			logger.info(" The duplicates will be moved to the folder '{}'\n", destinationTypeFolder);
		}

		for (Artifact artifact : dp.getDuplicates()) {
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
		}
	}
}
