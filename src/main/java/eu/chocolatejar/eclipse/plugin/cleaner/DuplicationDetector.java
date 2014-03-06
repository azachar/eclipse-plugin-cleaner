package eu.chocolatejar.eclipse.plugin.cleaner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Detects duplicates between bundles, the older bundles are considered as
 * duplicates or bundle that exists on multiple places
 * 
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
							map.put(symbolicName, artifact); // replace with
																// potentially
																// non dropins folder variant
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
