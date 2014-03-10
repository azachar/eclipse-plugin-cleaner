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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Version;

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;
import eu.chocolatejar.eclipse.plugin.cleaner.model.CleaningMode;

@RunWith(JUnitParamsRunner.class)
public class DuplicationDetectorTest {
	Set<Artifact> samples;

	DuplicationDetectorFactory dd;

	private Artifact a(String symbolicName, String version) {
		return a(symbolicName, version, "eclipse/dropins/eclipse/plugins/" + symbolicName + "_" + version + ".jar");
	}

	private Artifact a(String symbolicName, String version, String file) {
		return new Artifact(new File(file), symbolicName, version);
	}

	@Before
	public void setup() {
		samples = new LinkedHashSet<>();
	}

	@Test
	@Parameters({ "unlimited" })
	public void removeOlderVersions(CleaningMode mode) {
		samples.add(a("symbolicName", "0.3.8.201210040400"));
		samples.add(a("symbolicName", "0.3.7"));
		samples.add(a("symbolicName", "0.3.5"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).extractingResultOf("getVersion").contains(

		new Version("0.3.7"), new Version("0.3.5")

		);
	}

	@Test
	@Parameters({ "dropinsOnly", "prefereDropins" })
	public void removeOlderVersions_dropinsOnly(CleaningMode mode) {
		samples.add(a("symbolicName", "0.3.8.SAME", "/plugins/a.jar"));
		samples.add(a("symbolicName", "0.3.7", "/plugins/b.jar"));
		samples.add(a("symbolicName", "0.3.5", "/dropins/plugins/c.jar"));
		samples.add(a("symbolicName", "0.3.8.SAME", "/dropins/plugins/a.jar\""));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples))

		.contains(a("symbolicName", "0.3.8.SAME", "/dropins/plugins/a.jar\""))

		.doesNotContain(a("symbolicName", "0.3.8.SAME", "/plugins/a.jar"))

		.extractingResultOf("getVersion").contains(

		new Version("0.3.5"), new Version("0.3.8.SAME"));

	}

	@Test
	@Parameters({ "unlimited", "dropinsOnly", "prefereDropins" })
	public void removeOldVersions_basedOnSuffixOnly(CleaningMode mode) {
		samples.add(a("symbolicName", "0.3.8.201210040400"));
		samples.add(a("symbolicName", "0.3.8.201410040400"));
		samples.add(a("symbolicName", "0.3.8.201310040400"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).extractingResultOf("getVersion").contains(

		new Version("0.3.8.201210040400"), new Version("0.3.8.201310040400")

		);
	}

	@Test
	@Parameters({ "unlimited", "dropinsOnly", "prefereDropins" })
	public void originalOfAllDuplicated_referenceToTheNewestArtifact(CleaningMode mode) {
		samples.add(a("symbolicName", "1.0.0", "/dropins/a.jar"));
		samples.add(a("symbolicName", "2.0.0", "/dropins/b.jar"));
		samples.add(a("symbolicName", "3.0.0", "c.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).extractingResultOf("getMaster").containsOnly(
				a("symbolicName", "3.0.0", "c.jar"));
	}

	/**
	 * This test is based on the order of the sample data!
	 */
	@Test
	@Parameters({ "unlimited" })
	public void keepOnlyOneBundleIfLocationIsDifferentAndTheRestIsSame_justInDropins(CleaningMode mode) {
		samples.add(a("same", "1.0.0", "/dropins/a/same.jar"));
		samples.add(a("same", "1.0.0", "/dropins/b/same.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).containsOnly(a("same", "1.0.0", "/dropins/b/same.jar"));
	}

	/**
	 * This test is based on the order of the sample data!
	 */
	@Test
	@Parameters({ "dropinsOnly", "prefereDropins" })
	public void keepOnlyOneBundleIfLocationIsDifferentAndTheRestIsSame_justInDropins2(CleaningMode mode) {
		samples.add(a("same", "1.0.0", "/dropins/a/same.jar"));
		samples.add(a("same", "1.0.0", "/dropins/b/same.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).containsOnly(a("same", "1.0.0", "/dropins/a/same.jar"));
	}

	@Test
	@Parameters({ "unlimited", "dropinsOnly", "prefereDropins" })
	public void keepOnlyNonDropinBundleIfLocationIsDifferentAndTheRestIsSame(CleaningMode mode) {
		samples.add(a("same", "1.0.0", "eclipse/plugins/same.jar"));
		samples.add(a("same", "1.0.0", "/dropins/plugins/same.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).containsOnly(a("same", "1.0.0", "/dropins/plugins/same.jar"));
	}

	@Test
	@Parameters({ "dropinsOnly" })
	public void strategy_preserveEclipseRoot(CleaningMode mode) {
		samples.add(a("same", "2.0.0", "eclipse/plugins/same_2.0.0.jar"));
		samples.add(a("same", "1.0.0", "eclipse/plugins/same_1.0.0.jar"));
		// duplicates
		samples.add(a("same", "1.0.0", "eclipse/dropins/plugins/same_1.0.0.jar"));
		samples.add(a("same", "2.0.0", "eclipse/dropins/plugins/same_2.0.0.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).containsOnly(

		a("same", "1.0.0", "eclipse/dropins/plugins/same_1.0.0.jar"),

		a("same", "2.0.0", "eclipse/dropins/plugins/same_2.0.0.jar")

		);
	}

	@Test
	@Parameters({ "dropinsOnly" })
	public void strategy_preserveEclipseRoot_doesNothing(CleaningMode mode) {
		samples.add(a("same", "2.0.0", "eclipse/plugins/same_2.0.0.jar"));
		samples.add(a("same", "1.0.0", "eclipse/plugins/same_1.0.0.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).isEmpty();
	}

	@Test
	@Parameters({ "dropinsOnly", "prefereDropins" })
	public void keepOnlyNonDropinBundleIfLocationIsDifferentAndTheRestIsSame_oppositeOrder(CleaningMode mode) {
		samples.add(a("same", "1.0.0", "eclipse/dropins/plugins/same.jar"));
		samples.add(a("same", "1.0.0", "eclipse/plugins/same.jar"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).containsOnly(a("same", "1.0.0", "eclipse/dropins/plugins/same.jar"));
	}

	@Test
	@Parameters({ "unlimited", "dropinsOnly", "prefereDropins" })
	public void doNotRemoveIfAreDifferent(CleaningMode mode) {
		samples.add(a("a", "1.0.0"));
		samples.add(a("b", "1.0.0"));

		dd = new DuplicationDetectorFactory(mode);

		assertThat(dd.getDuplicates(samples)).isEmpty();
	}

	@Test
	@Parameters({ "unlimited", "dropinsOnly", "prefereDropins" })
	public void empty(CleaningMode mode) {
		dd = new DuplicationDetectorFactory(mode);
		assertThat(dd.getDuplicates(samples)).isEmpty();
	}

}
