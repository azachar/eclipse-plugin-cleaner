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

import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.Version;

public class ArtifactTest {

	final static String TEST_BUNDLES = ArtifactTest.class.getClassLoader().getResource("test-bundles/").getFile();

	File file;

	@Test
	public void folderBundleWithExoticVersion() {
		file = new File(TEST_BUNDLES + "org.eclipse.wst.server_core.feature_3.3.201.v20130412_1040-34Et8s73573C4Da2815");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getSource().toString()).isEqualTo(
				TEST_BUNDLES + "org.eclipse.wst.server_core.feature_3.3.201.v20130412_1040-34Et8s73573C4Da2815");
		assertThat(a.getSymbolicName()).isEqualTo("org.eclipse.wst.server_core.feature");
		assertThat(a.getVersion().toString()).isEqualTo("3.3.201.v20130412_1040-34Et8s73573C4Da2815");
	}

	@Test
	public void jarBundleWithUpperCase() {
		file = new File(TEST_BUNDLES + "uppercase_1.0.0.JAR");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getSource().toString()).isEqualTo(TEST_BUNDLES + "uppercase_1.0.0.JAR");
		assertThat(a.getSymbolicName()).isEqualTo("uppercase");
		assertThat(a.getVersion().toString()).isEqualTo("1.0.0");
	}

	@Test
	public void folderBundleWithUnderLineInName() {
		file = new File(TEST_BUNDLES + "org.chromium.sdk.wipbackend.protocol_1_0_0.3.8.201210040401.jar");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getSource().toString()).isEqualTo(
				TEST_BUNDLES + "org.chromium.sdk.wipbackend.protocol_1_0_0.3.8.201210040401.jar");
		assertThat(a.getSymbolicName()).isEqualTo("org.chromium.sdk.wipbackend.protocol_1_0");
		assertThat(a.getVersion().toString()).isEqualTo("0.3.8.201210040401");
	}

	@Test
	public void jarBundleWithUnderlineInJarName() {
		file = new File(TEST_BUNDLES + "biz.aQute.repository_2.1.0.174214_220REL.jar");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getSource().toString()).isEqualTo(TEST_BUNDLES + "biz.aQute.repository_2.1.0.174214_220REL.jar");
		assertThat(a.getSymbolicName()).isEqualTo("biz.aQute.repository");
		assertThat(a.getVersion().toString()).isEqualTo("2.1.0.174214_220REL");
	}

	@Test
	public void folderBundle() {
		file = new File(TEST_BUNDLES + "net.jeeeyul.pdetools.capability_1.2.0.201402202217");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getVersion().toString()).isEqualTo("1.2.0.201402202217");
		assertThat(a.getSource().toString()).isEqualTo(
				TEST_BUNDLES + "net.jeeeyul.pdetools.capability_1.2.0.201402202217");
		assertThat(a.getSymbolicName()).isEqualTo("net.jeeeyul.pdetools.capability");
	}

	@Test
	public void jarBundle() {
		file = new File(TEST_BUNDLES + "org.eclipse.jst.common.annotations.core_1.1.300.v201004141630.jar");
		Assume.assumeTrue(file.exists());

		Artifact a = Artifact.createFromFile(file);
		assertThat(a).isNotNull();

		assertThat(a.getVersion().toString()).isEqualTo("1.1.300.v201004141630");
		assertThat(a.getSource().toString()).isEqualTo(
				TEST_BUNDLES + "org.eclipse.jst.common.annotations.core_1.1.300.v201004141630.jar");
		assertThat(a.getSymbolicName()).isEqualTo("org.eclipse.jst.common.annotations.core");
	}

	@Test
	public void hashCodeAndEquals_same() throws Exception {
		Artifact a = new Artifact(new File("eu.chocolatejar.a_1.0.0.RELEASE.jar"), "eu.chocolatejar.a", "1.0.0.RELEASE");
		Artifact b = new Artifact(new File("eu.chocolatejar.a_1.0.0.RELEASE.jar"), "eu.chocolatejar.a", "1.0.0.RELEASE");

		assertThat(a).isEqualTo(b);
		assertThat(b).isEqualTo(a);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	public void hashCodeAndEquals_different() throws Exception {
		Artifact a = new Artifact(new File("eu.chocolatejar.a_1.0.0.RELEASE.jar"), "eu.chocolatejar.a", "1.0.0.RELEASE");
		Artifact b = new Artifact(new File("eu.chocolatejar.b_1.0.0.RELEASE.jar"), "eu.chocolatejar.b", "1.0.0.RELEASE");

		assertThat(a).isNotEqualTo(b);
		assertThat(b).isNotEqualTo(a);

		assertThat(a.hashCode()).isNotZero();
		assertThat(b.hashCode()).isNotZero();
	}

	@Test
	public void comparisonOfQualifiersWithTimestamps() throws Exception {
		Artifact a = new Artifact(new File("old/a_1.1.300.v20130514-0733.jar"), "a", "1.1.300.v20130514-0733");
		Artifact b = new Artifact(new File("new/a_1.1.300.201402281424.jar"), "a", "1.1.300.201402281424");

		assertThat(a.compareTo(b)).isNegative();
	}

	@Test
	public void normalizeQualifier() throws Exception {
		assertThat(Artifact.normalizeQualifier(new Version("1.2.300.v20130514-0733")).toString()).isEqualTo(
				"1.2.300.201305140733");
	}

}
