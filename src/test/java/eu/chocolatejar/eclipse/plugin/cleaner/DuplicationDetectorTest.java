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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class DuplicationDetectorTest {
	Set<Artifact> samples;

	DuplicationDetector dd;

	private Artifact a(String symbolicName, String version) {
		return new Artifact(null, symbolicName, version);
	}

	private Artifact a(String symbolicName, String version, String file) {
		return new Artifact(new File(file), symbolicName, version);
	}

	@Before
	public void setup() {
		samples = new LinkedHashSet<>();
	}

	@Test
	public void removeOlderVersions() {
		samples.add(a("symbolicName", "0.3.8.201210040400"));
		samples.add(a("symbolicName", "0.3.7"));
		samples.add(a("symbolicName", "0.3.5"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).extractingResultOf("getVersion").contains(

		new Version("0.3.7"), new Version("0.3.5")

		);
	}

	@Test
	public void removeOldVersions_basedOnSuffixOnly() {
		samples.add(a("symbolicName", "0.3.8.201210040400"));
		samples.add(a("symbolicName", "0.3.8.201410040400"));
		samples.add(a("symbolicName", "0.3.8.201310040400"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).extractingResultOf("getVersion").contains(

		new Version("0.3.8.201210040400"), new Version("0.3.8.201310040400")

		);
	}

	@Test
	public void getDuplicated_referenceToTheNewestArtifact() {
		samples.add(a("symbolicName", "1.0.0"));
		samples.add(a("symbolicName", "2.0.0"));
		samples.add(a("symbolicName", "3.0.0"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).extractingResultOf("getDuplicate").containsOnly(a("symbolicName", "3.0.0"));
	}

	@Test
	public void keepOnlyOneBundleIfLocationIsDifferentAndTheRestIsSame() {
		samples.add(a("same", "1.0.0", "a/same.jar"));
		samples.add(a("same", "1.0.0", "b/same.jar"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).containsOnly(a("same", "1.0.0", "b/same.jar"));
	}

	@Test
	public void keepOnlyNonDropinBundleIfLocationIsDifferentAndTheRestIsSame() {
		samples.add(a("same", "1.0.0", "eclipse/plugins/same.jar"));
		samples.add(a("same", "1.0.0", "dropins/plugins/same.jar"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).containsOnly(a("same", "1.0.0", "dropins/plugins/same.jar"));
	}

	@Test
	public void keepOnlyNonDropinBundleIfLocationIsDifferentAndTheRestIsSame_oppositeOrder() {
		samples.add(a("same", "1.0.0", "dropins/plugins/same.jar"));
		samples.add(a("same", "1.0.0", "eclipse/plugins/same.jar"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).containsOnly(a("same", "1.0.0", "dropins/plugins/same.jar"));
	}

	@Test
	public void doNotRemoveIfAreDifferent() {
		samples.add(a("a", "1.0.0"));
		samples.add(a("b", "1.0.0"));

		dd = new DuplicationDetector(samples);

		assertThat(dd.getDuplicates()).isEmpty();
	}

	@Test
	public void empty() {
		dd = new DuplicationDetector(samples);
		assertThat(dd.getDuplicates()).isEmpty();
	}

}
