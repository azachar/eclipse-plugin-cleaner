package eu.chocolatejar.eclipse.plugin.cleaner;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds duplicates and move them to an other directory
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static CommandLine input;

	public static void main(String[] args) {

		logger.info("Welcome to Eclipse Plugin Cleaner {} (c) 2014 Chocolate Jar", getImplementationVersion());

		// create the Options
		Options options = new Options();
		options.addOption(generateOption("s", "source", true, "path to the source folder with eclipse installation, default is ."));
		options.addOption(generateOption("d", "destination", true,
				"path to the destination folder where duplicated bundles will be moved, default is duplicates with the timestamp"));
		options.addOption(generateOption("t", "test", false,
				"Test - dry run, no action will be take, only displaying duplicates"));
		options.addOption(generateOption("h", "help", false, "show help"));

		try {
			// parse the command line arguments
			input = new GnuParser().parse(options, args);
			if (input.hasOption("help")) {
				showHelp(options);
				return;
			}

			File sourceFolder = new File(getParam("source", "."));
			File destionFolder = new File(getParam("destination", "./duplicates_" + new Date()));
			boolean dryRun = input.hasOption("test");

			Cleaner bundlesDuplicateCleaner = new Cleaner(sourceFolder, destionFolder, dryRun);
			bundlesDuplicateCleaner.doCleanUp();

		} catch (ParseException exp) {
			logger.error(exp.getMessage());

			showHelp(options);
		}

	}

	/**
	 * Read version from jar manifest, if it fails the exception is swallow and
	 * empty string is returned
	 */
	private static String getImplementationVersion() {
		try {
			URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes mainAttribs = manifest.getMainAttributes();
			String version = mainAttribs.getValue("Implementation-Version");
			if (version != null) {
				return version;
			}
		} catch (Exception e) {
			logger.trace("Unable to read the manifest version of this program", e);
		}
		return "-";
	}

	private static void showHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Reinstall Cleaner", options);
	}

	@SuppressWarnings("static-access")
	private static Option generateOption(String opt, String longOpt, boolean hasArg, String description) {
		return OptionBuilder.withLongOpt(longOpt).withDescription(description).hasArg(hasArg).isRequired(false)
				.withArgName(longOpt).create(opt);
	}

	private static String getParam(String option, String defautlValue) {
		if (input.hasOption(option)) {
			return StringUtils.trimToNull(input.getOptionValue(option));
		}
		return defautlValue;
	}
}
