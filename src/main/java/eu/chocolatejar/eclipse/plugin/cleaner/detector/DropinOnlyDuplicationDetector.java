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
 * In contrast to {@link PrefereDropinsDuplicationDetector} this detector marks
 * as duplicates only artifacts located in the dropins folder.
 */
public class DropinOnlyDuplicationDetector extends PrefereDropinsDuplicationDetector {

	@Override
	protected void keepAsDuplicate(Artifact artifact) {
		// Only in the dropins folder can be the artifact marked as duplicated
		if (artifact.isInDropinsFolder()) {
			super.keepAsDuplicate(artifact);
		}
	}
}
