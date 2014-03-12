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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;

/**
 * Parses an artifact (plugin or feature) from a folder or a file.
 */
public class ArtifactParser {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactParser.class);

    /*
     * The regexp based on specification (
     * http://www.osgi.org/download/r4v43/osgi.core-4.3.0.pdf chapter 3.2.2 )
     * 
     * version ::= MAJOR( '.' MINOR ( '.' MICRO ( '.' QUALIFIER )? )? )?
     * 
     * MAJOR ::= NUMBER
     * 
     * MINOR ::= NUMBER
     * 
     * MICRO ::= NUMBER
     * 
     * QUALIFIER ::= ( ALPHANUM | ’_’ | '-' )+
     * 
     * where
     * 
     * NUMBER ::= digit+
     * 
     * ALPHANUM ::= alpha | digit
     * 
     * digit ::= [0..9]
     * 
     * alpha ::= [a..zA..Z]
     */
    private static final String ALPHANUM = "A-Za-z0-9";
    private static final String NUMBER = "[0-9]+";
    private static final String MAJOR = NUMBER;
    private static final String MINOR = NUMBER;
    private static final String MICRO = NUMBER;
    private static final String QUALIFIER = "[" + ALPHANUM + "\\_\\-]+";
    private static final String VERSION_REGULAR_EXPRESSION = MAJOR + "(\\." + MINOR + "(\\." + MICRO + "(\\."
            + QUALIFIER + ")?" + ")?" + ")?";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGULAR_EXPRESSION);

    /**
     * Parses a bundle from a folder or a jar file using a manifest. If manifest
     * is unreadable uses the filename to obtain a version.
     * 
     * @param file
     *            with a potential bundle to parse
     */
    public Artifact createFromFile(File file) {
        Artifact a = getArtifactBasedOnManifest(file);

        if (a == null) {
            a = getArtifactBasedOnFilename(file);

            if (a == null) {
                logger.error("Skipping: Unable to parse a version from '{}'!", file);
            }
        }

        return a;
    }

    /**
     * Resolve artifact by reg exp from the filename
     * 
     * @param file
     * @return <code>null</code> if not found
     */
    private Artifact getArtifactBasedOnFilename(File file) {
        if (file == null) {
            return null;
        }
        try {
            String baseName = FilenameUtils.getName(file.getAbsolutePath());

            Matcher versionMatcher = VERSION_PATTERN.matcher(baseName);
            if (versionMatcher.find()) {
                String version = versionMatcher.group(0);
                if (baseName.contains("_")) {
                    String bundleSymbolicName = StringUtils.substringBeforeLast(baseName, "_" + version);
                    return new Artifact(file, bundleSymbolicName, version);
                }
            }
        } catch (Exception e) {
            logger.debug("Unable to parse artifact based on filename from the file '{}'.", file, e);
        }
        return null;
    }

    /**
     * Parses a bundle from a folder or a jar file using a manifest.
     * 
     * @param file
     * @return <code>null</code> if unable to parse
     */
    private Artifact getArtifactBasedOnManifest(File file) {
        if (file == null) {
            return null;
        }
        try {
            if (file.isDirectory()) {
                File manifest = FileUtils.getFile(file, "META-INF/MANIFEST.MF");
                if (manifest.exists()) {
                    return parseFromManifest(file, manifest);
                }
            } else {
                return parseFromManifest(file, file);
            }
        } catch (Exception e) {
            logger.debug("Unable to parse artifact based on manifest from the file '{}'.", file, e);
        }
        return null;
    }

    /**
     * Create an artifact for the jar based on jarsManifest
     * 
     * @param jar
     *            the location of the bundle. It can be a folder or a file.
     * @param jarsManifest
     *            manifest location can be within the folder or within the jar.
     * @return
     */
    private Artifact parseFromManifest(File jar, File jarsManifest) {
        Manifest bundleManifest = readManifestfromJarOrDirectory(jarsManifest);

        if (bundleManifest == null) {
            logger.debug("Invalid manifest '{}'", jarsManifest);
            return null;
        }

        Attributes attributes = bundleManifest.getMainAttributes();
        if (attributes == null) {
            logger.debug("Manifest '{}' doesn't contain attributes.", jarsManifest);
            return null;
        }

        String bundleSymbolicName = attributes.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bundleVersion = attributes.getValue(Constants.BUNDLE_VERSION);

        if (StringUtils.isBlank(bundleSymbolicName) || StringUtils.isBlank(bundleVersion)) {
            logger.warn("Manifest '{}' doesn't contain OSGI attributes.", jarsManifest);
            return null;
        }
        return new Artifact(jar, bundleSymbolicName, bundleVersion);
    }

    private Manifest readManifestfromJarOrDirectory(File file) {
        try {
            Manifest bundleManifest = null;

            try (final FileInputStream is = new FileInputStream(file)) {
                final boolean isJar = "jar".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
                if (isJar) {
                    try (final JarInputStream jis = new JarInputStream(is)) {
                        bundleManifest = jis.getManifest();
                    }
                } else {
                    bundleManifest = new Manifest(is);
                }
            }
            return bundleManifest;
        } catch (IOException e) {
            logger.debug("Unable to read manifest from jar or directory.", e);
            return null;
        }
    }

}
