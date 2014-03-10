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

import java.util.Set;

import eu.chocolatejar.eclipse.plugin.cleaner.detector.DropinsOnlyDuplicationDetector;
import eu.chocolatejar.eclipse.plugin.cleaner.detector.PrefereDropinsDuplicationDetector;
import eu.chocolatejar.eclipse.plugin.cleaner.detector.UnlimitedDuplicationDetector;
import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;
import eu.chocolatejar.eclipse.plugin.cleaner.model.CleaningMode;
import eu.chocolatejar.eclipse.plugin.cleaner.model.Detector;

/**
 * Create a bundle duplication {@link Detector} based on a provided mode.
 * 
 * @see CleaningMode
 */
public class DuplicationDetectorFactory implements Detector {

	private final CleaningMode mode;

	/**
	 * Create a detector based on a provided mode.
	 * 
	 * @param mode
	 *            - a {@link CleaningMode} that specify detector to resolve
	 *            duplicates
	 */
	public DuplicationDetectorFactory(CleaningMode mode) {
		this.mode = mode;
	}

	/**
	 * Factory method to create a Detector object based on a provided
	 * cleaningMode
	 * 
	 * @param cleaningMode
	 * @return Detector instance, never <code>null</code>
	 */
	private Detector createDetector(CleaningMode cleaningMode) {
		switch (cleaningMode) {

		case dropinsOnly:
			return new DropinsOnlyDuplicationDetector();

		case unlimited:
			return new UnlimitedDuplicationDetector();

		case prefereDropins:
		default:
			return new PrefereDropinsDuplicationDetector();
		}
	}

	@Override
	public Set<Artifact> getDuplicates(Set<Artifact> artifacts) {
		// we need to always return a new detector object for each call
		return createDetector(mode).getDuplicates(artifacts);
	}
}
