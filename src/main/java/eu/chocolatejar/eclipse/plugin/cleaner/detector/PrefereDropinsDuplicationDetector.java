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

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;

/**
 * If there are two bundles with the same version the bundle that is in the
 * dropins folder is considered to be duplicated. If both of them are from a non
 * dropins folder than first come is kept the second one is marked as a
 * duplicate.
 */
public class PrefereDropinsDuplicationDetector extends AbstractDuplicationDetector {
    /**
     * There is already a master bundle for the given artifact and has as the
     * same version as the master artifact
     * 
     * Resolve which artifact to keep
     * 
     * @param artifact
     * @param master
     */
    protected void detectDuplicateForSameVersions(Artifact artifact, Artifact master) {
        // choose rather from the dropins folder
        if (master.isInDropinsFolder()) {
            keepAsDuplicate(master);
            keepAsOriginal(artifact);
        } else {
            keepAsDuplicate(artifact);
        }
    }
}
