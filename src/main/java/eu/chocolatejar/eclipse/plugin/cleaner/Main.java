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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chocolatejar.eclipse.plugin.cleaner.model.CleaningMode;

/**
 * Finds duplicates and move them to a back up directory.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    CommandLine input;

    /**
     * Executes this program based on provided command line parameters
     * 
     * @param args
     */
    public static void main(String[] args) {
        new Main().run(args);
    }

    private void run(String[] args) {
        logger.info("Welcome to Eclipse Plugin Cleaner {} (c) 2014 Chocolate Jar", getImplementationVersion());

        // create the Options
        Options options = new Options();

        options.addOption(generateOption("h", "help", false, "Shows this help."));

        options.addOption(generateOption("s", "source", true,
                "Path to Eclipse installation. The default is the current folder."));
        options.addOption(generateOption(
                "d",
                "destination",
                true,
                "Path to folder where duplicated bundles will be moved. The default is the absolute path to <source>/duplicates-<timestamp>."));
        options.addOption(generateOption("t", "test", false,
                "Test - Enables a dry run mode, e.g. no action will be taken."));

        options.addOption(generateOption(
                "m",
                "mode",
                true,
                "To specify a duplication detection mode as follows: \n'"
                        + CleaningMode.dropinsOnly.name()
                        + "' (default) Duplicates can only be artifacts located in the ``dropins`` folder.\n'"
                        + CleaningMode.prefereDropins.name()
                        + "' If there are two bundles with the same version, then the bundle that is in the ``dropins`` folder is considered to be duplicated. If both of them are from a ``non-dropins`` folder, than the first one is kept and the second one is marked as a duplicate.\n'"
                        + CleaningMode.unlimited.name() + "' Resolves duplicates regardless their location."));

        try {
            // parse the command line arguments
            input = new GnuParser().parse(options, args);
            if (input.hasOption("help")) {
                showHelp(options);
                return;
            }

            File sourceFolder = new File(getParam("source", "."));
            File destinationFolder = new File(getParam("destination", getDefaultDestinationFolder(sourceFolder)));
            boolean dryRun = input.hasOption("test");
            final CleaningMode cleaningMode = getParamMode(CleaningMode.dropinsOnly);

            Cleaner bundlesDuplicateCleaner = new Cleaner(sourceFolder, destinationFolder, dryRun, cleaningMode);
            bundlesDuplicateCleaner.doCleanUp();

        } catch (ParseException exp) {
            logger.error(exp.getMessage());

            showHelp(options);
        }
    }

    /**
     * Resolves the command line parameter "mode".
     * 
     * @param defaultMode
     *            If no mode specified in the command line this mode is used.
     */
    private CleaningMode getParamMode(CleaningMode defaultMode) {
        String mode = getParam("mode", defaultMode.name());
        try {
            return CleaningMode.valueOf(mode);
        } catch (Exception e) {
            logger.warn("Unable to parse mode '{}', using default mode '{}'.", mode, defaultMode);
            return defaultMode;
        }
    }

    /**
     * The default destination is based on the <source
     * folder>/duplicates_<timestamp>
     * 
     * @param sourceFolder
     *            cannot be <code>null</code>
     * @return absolute path
     */
    String getDefaultDestinationFolder(File sourceFolder) {
        assert sourceFolder != null;
        File file = FileUtils.getFile(sourceFolder,
                "duplicates_" + DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date()));
        return file.getAbsolutePath();

    }

    /**
     * Read version from jar manifest, if it fails the exception is swallow and
     * empty string is returned
     */
    private String getImplementationVersion() {
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
            logger.trace("Unable to read a manifest version of this program.", e);
        }
        return "X.X.X.X";
    }

    private void showHelp(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar plugin-cleaner-" + getImplementationVersion() + "-jar-with-dependencies.jar ",
                "", options, " For more information visit http://blog.chocolatejar.eu/eclipse-plugin-cleaner", true);
    }

    @SuppressWarnings("static-access")
    private Option generateOption(String opt, String longOpt, boolean hasArg, String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(description).hasArg(hasArg).isRequired(false)
                .withArgName(longOpt).create(opt);
    }

    /**
     * Returns a command line option or a default value if the value is empty or
     * <code>null</code>.
     * 
     * @param option
     *            command line option, for example --source
     * @param defautlValue
     * @return never <code>null</code>, unless default value is
     *         <code>null</code>
     */
    String getParam(String option, String defautlValue) {
        String returnValue = null;
        if (input.hasOption(option)) {
            returnValue = StringUtils.trimToNull(input.getOptionValue(option));
        }
        if (returnValue != null) {
            return returnValue;
        }
        return defautlValue;

    }
}
