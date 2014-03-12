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
package eu.chocolatejar.eclipse.plugin.cleaner.detector;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;
import eu.chocolatejar.eclipse.plugin.cleaner.model.DuplicationDetector;

/**
 * The default implementation of a duplication detector.
 * 
 * This class is intended to be used a prototype. Each calling of
 * {@link #getDuplicates(Set)} must be create a new object.
 */
abstract class AbstractDuplicationDetector implements DuplicationDetector {

    final Map<String, Artifact> masterBundles = new HashMap<>();
    final Set<Artifact> duplicates = new HashSet<>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.chocolatejar.eclipse.plugin.cleaner.detector.Detector#getDuplicates
     * (java.util.Set)
     */
    @Override
    public Set<Artifact> getDuplicates(Set<Artifact> artifacts) {
        doDuplicationAnalysis(artifacts);
        updateMasterReferenceToAllDuplicates();
        return Collections.unmodifiableSet(duplicates);
    }

    protected void doDuplicationAnalysis(Set<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {

            Artifact master = getMasterFor(artifact);
            if (master == null) {
                keepAsOriginal(artifact);
            } else {
                // there is already a master bundle for the given artifact,
                // resolve which version to keep

                final int compareTo = artifact.compareTo(master);
                final boolean isNewer = compareTo > 0;
                final boolean isSame = compareTo == 0;

                if (isNewer) {
                    replaceMaster(artifact, master);
                } else {
                    if (isSame) {
                        detectDuplicateForSameVersions(artifact, master);
                    } else {
                        // is older
                        keepAsDuplicate(artifact);
                    }
                }
            }
        }
    }

    /**
     * Resolves which artifact to keep.
     * 
     * There is already a master bundle for the given artifact and has as the
     * same version as the master artifact.
     * 
     * @param artifact
     * @param master
     */
    protected abstract void detectDuplicateForSameVersions(Artifact artifact, Artifact master);

    protected void keepAsOriginal(Artifact original) {
        masterBundles.put(original.getSymbolicName(), original);
    }

    protected void keepAsDuplicate(Artifact artifact) {
        duplicates.add(artifact);
        if (masterBundles.containsValue(artifact)) {
            masterBundles.remove(artifact.getSymbolicName());
        }
    }

    protected void replaceMaster(Artifact newMaster, Artifact oldMaster) {
        keepAsOriginal(newMaster);
        keepAsDuplicate(oldMaster);
    }

    private Artifact getMasterFor(Artifact artifact) {
        return masterBundles.get(artifact.getSymbolicName());
    }

    /**
     * Set a master (the latest resolved artifact) reference to all duplicates
     */
    private void updateMasterReferenceToAllDuplicates() {
        for (Artifact duplicate : duplicates) {
            Artifact original = getMasterFor(duplicate);
            assert original != null;
            duplicate.setMaster(original);
        }
    }

}
