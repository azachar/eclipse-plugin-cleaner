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
import java.io.FileNotFoundException;
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
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a bundle with a version. The version is obtained via manifest or
 * via filename. The artifact can be file based (jar) or a folder.
 * 
 * Comparison is based on purely on the version.
 * 
 * HashCode and Equals are based on {@link #file} & {@link #bundleSymbolicName}
 * & {@link #bundleVersion}.
 */
public class Artifact implements Comparable<Artifact> {

	private static final Logger logger = LoggerFactory.getLogger(Artifact.class);

	private final File location;
	private final String bundleSymbolicName;
	private final Version bundleVersion;

	private Artifact duplicate;

	Artifact(File location, String bundleSymbolicName, String bundleVersion) {
		this.location = location;
		this.bundleSymbolicName = StringUtils.substringBefore(bundleSymbolicName, ";");
		this.bundleVersion = new Version(bundleVersion);
	}

	/**
	 * The location of this bundle.
	 * 
	 * @return a jar file or a folder
	 */
	public File getSource() {
		return location;
	}

	public String getSymbolicName() {
		return bundleSymbolicName;
	}

	public Version getVersion() {
		return bundleVersion;
	}

	/**
	 * Parses a bundle from a folder or a jar file using a manifest. If manifest
	 * is unreadable uses the filename to obtain a version.
	 * 
	 * @param file
	 *            with a potential bundle to parse
	 */
	public static Artifact createFromFile(File file) {
		Artifact a = null;
		try {
			if (file.isDirectory()) {
				File manifest = FileUtils.getFile(file, "META-INF/MANIFEST.MF");
				if (manifest.exists()) {
					a = parseFromManifest(file, manifest);
				}
			} else {
				a = parseFromManifest(file, file);
			}
		} catch (Exception e) {
			logger.warn("Unable to parse an artifact from '{}' due to '{}', trying to parse from the filename.", file,
					e.getMessage());
			a = null;
		}
		if (a != null) {
			return a;
		}
		try {
			a = parseFromFileName(file);
		} catch (Exception e) {
			logger.error("Skipping: Unable to parse a version from '{}'!", file);
			a = null;
		}
		return a;
	}

	private static Artifact parseFromManifest(File bundle, File manifest) throws Exception, IOException,
			FileNotFoundException {
		Manifest bundleManifest = readManifestfromJarOrDirectory(manifest);

		if (bundleManifest == null) {
			logger.debug("The invalid manifest {}", manifest);
		}
		Attributes attributes = bundleManifest.getMainAttributes();
		String bundleSymbolicName = getRequiredHeader(attributes, Constants.BUNDLE_SYMBOLICNAME);
		String bundleVersion = getRequiredHeader(attributes, Constants.BUNDLE_VERSION);
		return new Artifact(bundle, bundleSymbolicName, bundleVersion);
	}

	private static Manifest readManifestfromJarOrDirectory(File file) throws IOException, FileNotFoundException {
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
	}

	/*
	 * Based on specification (
	 * http://www.osgi.org/download/r4v43/osgi.core-4.3.0.pdf chapter 3.2.2 )
	 * 
	 * version ::= major( '.' minor ( '.' micro ( '.' qualifier )? )? )?
	 * 
	 * major ::= number
	 * 
	 * minor ::= number
	 * 
	 * micro ::= number
	 * 
	 * qualifier ::= ( alphanum | ’_’ | '-' )+
	 * 
	 * where
	 * 
	 * number ::= digit+
	 * 
	 * alphanum ::= alpha | digit
	 * 
	 * digit ::= [0..9]
	 * 
	 * alpha ::= [a..zA..Z]
	 */

	private static final String alphanum = "A-Za-z0-9";
	private static final String number = "[0-9]+";
	private static final String major = number;
	private static final String minor = number;
	private static final String micro = number;
	private static final String qualifier = "[" + alphanum + "\\_\\-]+";

	private static final String versionRegExp = major + "(\\." + minor + "(\\." + micro + "(\\." + qualifier + ")?"
			+ ")?" + ")?";
	private static final Pattern versionPattern = Pattern.compile(versionRegExp);

	/**
	 * Resolve artifact by reg exp from the filename
	 * 
	 * @param file
	 * @return <code>null</code> if not found
	 */
	private static Artifact parseFromFileName(File file) {
		String baseName = FilenameUtils.getName(file.getAbsolutePath());

		Matcher versionMatcher = versionPattern.matcher(baseName);
		if (versionMatcher.find()) {
			String version = versionMatcher.group(0);
			String bundleSymbolicName = StringUtils.substringBeforeLast(baseName, "_" + version);
			return new Artifact(file, bundleSymbolicName, version);
		}
		return null;
	}

	private static String getRequiredHeader(Attributes mainAttributes, String headerName) throws Exception {
		String value = mainAttributes.getValue(headerName);
		if (StringUtils.isBlank(value)) {
			throw new Exception("Missing or invalid " + headerName + " header.");
		}
		return value;
	}

	@Override
	public String toString() {
		return "'" + getSymbolicName() + "'" + " with the version '" + getVersion()
				+ (getDuplicate() == null ? "" : "' duplicates '" + getDuplicate().getVersion() + "'");
	}

	@Override
	public int compareTo(Artifact o) {
		return getVersion().compareTo(o.getVersion());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleSymbolicName == null) ? 0 : bundleSymbolicName.hashCode());
		result = prime * result + ((bundleVersion == null) ? 0 : bundleVersion.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (bundleSymbolicName == null) {
			if (other.bundleSymbolicName != null)
				return false;
		} else if (!bundleSymbolicName.equals(other.bundleSymbolicName))
			return false;
		if (bundleVersion == null) {
			if (other.bundleVersion != null)
				return false;
		} else if (!bundleVersion.equals(other.bundleVersion))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	public Artifact getDuplicate() {
		return duplicate;
	}

	public void setDuplicate(Artifact duplicate) {
		this.duplicate = duplicate;
	}

	/**
	 * @return whether the artifact is located within the
	 *         <strong>dropins</strong> folder
	 */
	public boolean isInDropinsFolder() {
		return getSource().toString().contains("dropins");
	}

}
