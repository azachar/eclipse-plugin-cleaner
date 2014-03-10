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
 * Resolves duplicates regardless their location.
 */
public class UnlimitedDuplicationDetector extends AbstractDuplicationDetector {

	/**
	 * Since the artifact and the master have the same version but are located
	 * in a different folder we can choose one of them as to be marked as a
	 * duplicate.
	 * 
	 * This implementation keeps the current master as a master and the artifact
	 * as a duplicate.
	 */
	@Override
	protected void detectDuplicateForSameVersions(Artifact artifact, Artifact master) {
		keepAsDuplicate(artifact);
	}
}