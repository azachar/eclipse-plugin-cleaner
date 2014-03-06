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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Detects duplicates between bundles. The bundle with the older version or with
 * the same version are considered to be duplicates. If bundle exists on
 * multiple places the location that contains <code>dropins</code> folder is
 * considered to be a duplicate.
 */
public class DuplicationDetector {
	final Set<Artifact> duplicates = new HashSet<>();

	public DuplicationDetector(Set<Artifact> artifacts) {
		Map<String, Artifact> map = new HashMap<>();
		for (Artifact artifact : artifacts) {
			String symbolicName = artifact.getSymbolicName();
			if (map.containsKey(symbolicName)) {
				// there is already such a bundle
				// compare versions
				Artifact current = map.get(symbolicName);

				final boolean isNewer = artifact.compareTo(current) > 0;
				if (isNewer) {
					map.put(symbolicName, artifact);
					duplicates.add(current);
				} else {
					// is older or other duplicate with the same version
					final boolean hasSameVersion = artifact.compareTo(current) == 0;
					if (hasSameVersion) {
						// choose rather from dropin folder
						if (current.isInDropinsFolder()) {
							duplicates.add(current);
							map.put(symbolicName, artifact); // replace with a
																// potentially
																// non dropins
																// folder
																// variant
						} else {
							duplicates.add(artifact);
						}
					} else {
						// is older
						duplicates.add(artifact);
					}
				}
			} else {
				map.put(symbolicName, artifact);
			}
		}

		// set the newest reference to all duplicates
		for (Artifact duplicate : duplicates) {
			Artifact original = map.get(duplicate.getSymbolicName());
			assert original != null;
			duplicate.setDuplicate(original);
		}
	}

	public Set<Artifact> getDuplicates() {
		return Collections.unmodifiableSet(duplicates);
	}

}
