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
package eu.chocolatejar.eclipse.plugin.cleaner.model;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Version;

import eu.chocolatejar.eclipse.plugin.cleaner.util.DropinsFilter;

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

    private static final DropinsFilter DROPINS_FILTER = new DropinsFilter();

    private final File location;
    private final String bundleSymbolicName;
    private final Version bundleVersion;

    private Artifact master;

    /**
     * This constructor is not intendet to be used
     * 
     * @param location
     * @param bundleSymbolicName
     * @param bundleVersion
     */
    public Artifact(File location, String bundleSymbolicName, String bundleVersion) {
        this.location = location;
        this.bundleSymbolicName = StringUtils.substringBefore(bundleSymbolicName, ";");
        this.bundleVersion = new Version(bundleVersion);

        if (StringUtils.isBlank(bundleSymbolicName)) {
            throw new IllegalArgumentException("Invalid bundle name for: " + location);
        }
    }

    @Override
    public int compareTo(Artifact o) {
        if (o == null) {
            // is newer than nothing
            return 1;
        }
        return normalizeQualifier(getVersion()).compareTo(normalizeQualifier(o.getVersion()));
    }

    protected static Version normalizeQualifier(Version orig) {
        String normalizedQualifier = orig.getQualifier().replaceAll("v", "");
        normalizedQualifier = normalizedQualifier.replaceAll("-", "");
        return new Version(orig.getMajor(), orig.getMinor(), orig.getMicro(), normalizedQualifier);
    }

    /**
     * @return whether the artifact is located within the
     *         <strong>dropins</strong> folder
     */
    public boolean isInDropinsFolder() {
        return DROPINS_FILTER.accept(getLocation());
    }

    /**
     * The location of this bundle.
     * 
     * @return a jar file or a folder
     */
    public File getLocation() {
        return location;
    }

    /**
     * @return The symbolic name of the artifact, never <code>null</code>
     */
    public String getSymbolicName() {
        return bundleSymbolicName;
    }

    /**
     * @return The version of the artifact, never <code>null</code>
     */
    public Version getVersion() {
        return bundleVersion;
    }

    /**
     * Indicates which artifact is duplicated by this artifact. The master
     * artifact represents a "version parent" of this artifact.
     * 
     * The value of this field is set by a {@link DuplicationDetector}.
     * 
     * @return Non-<code>null</code> value of the master indicates that this
     *         artifact is a duplicate of the master.
     * 
     *         <code>null</code> indicates that this artifact is master and has
     *         no duplicates e.g. has no better alternative during a
     *         duplications detection.
     */
    public Artifact getMaster() {
        return master;
    }

    /**
     * @see #getMaster()
     */
    public void setMaster(Artifact parent) {
        this.master = parent;
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

    @Override
    public String toString() {
        return getArtifactNameVersionAndLocation()
                + (getMaster() == null ? "" : " DUPLICATES " + getMaster().getArtifactNameVersionAndLocation());
    }

    private String getArtifactNameVersionAndLocation() {
        return "'" + getSymbolicName() + " #" + getVersion() + " @" + FilenameUtils.getFullPath(location.getPath())
                + "'";
    }

}
