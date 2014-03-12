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

import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;

import eu.chocolatejar.eclipse.plugin.cleaner.model.Artifact;

public class ArtifactParserTest {

    static final String TEST_BUNDLES = ArtifactParserTest.class.getClassLoader().getResource("test-bundles").getFile();

    File file;

    @Test
    public void doNotThrowAnyException() {
        Artifact a = new ArtifactParser().createFromFile(null);
        assertThat(a).isNull();
    }

    @Test
    public void missingManifest() {
        file = FileUtils.getFile(TEST_BUNDLES, "missing-manifest_1.2.3.GETTHIS");
        Artifact a = new ArtifactParser().createFromFile(file);

        assertThat(a).isNotNull();
        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("missing-manifest");
        assertThat(a.getVersion().toString()).isEqualTo("1.2.3.GETTHIS");
    }

    @Test
    public void brokenManifest() {
        file = FileUtils.getFile(TEST_BUNDLES, "broken-manifest_1.0");
        Artifact a = new ArtifactParser().createFromFile(file);

        assertThat(a).isNotNull();
        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("broken-manifest");
        assertThat(a.getVersion().toString()).isEqualTo("1.0.0");
    }
    
    @Test
    public void brokenManifest_missingVersion() {
        file = FileUtils.getFile(TEST_BUNDLES, "broken-manifest_2.0");
        Artifact a = new ArtifactParser().createFromFile(file);
        
        assertThat(a).isNotNull();
        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("broken-manifest");
        assertThat(a.getVersion().toString()).isEqualTo("2.0.0");
    }

    @Test
    public void invalidBundle() {
        file = FileUtils.getFile(TEST_BUNDLES, "invalid-bundle");
        Artifact a = new ArtifactParser().createFromFile(file);

        assertThat(a).isNull();
    }

    @Test
    public void invalidBundleVersion() {
        file = FileUtils.getFile(TEST_BUNDLES, "1.0.0");
        Artifact a = new ArtifactParser().createFromFile(file);

        assertThat(a).isNull();
    }

    @Test
    public void emptyManifest() {
        file = FileUtils.getFile(TEST_BUNDLES, "empty-manifest_2.0");
        Artifact a = new ArtifactParser().createFromFile(file);

        assertThat(a).isNotNull();
        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("empty-manifest");
        assertThat(a.getVersion().toString()).isEqualTo("2.0.0");
    }

    @Test
    public void folderBundleWithExoticVersion() {
        file = FileUtils.getFile(TEST_BUNDLES,
                "org.eclipse.wst.server_core.feature_3.3.201.v20130412_1040-34Et8s73573C4Da2815");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("org.eclipse.wst.server_core.feature");
        assertThat(a.getVersion().toString()).isEqualTo("3.3.201.v20130412_1040-34Et8s73573C4Da2815");
    }

    @Test
    public void jarBundleWithUpperCase() {
        file = FileUtils.getFile(TEST_BUNDLES, "uppercase_1.0.0.JAR");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("uppercase");
        assertThat(a.getVersion().toString()).isEqualTo("1.0.0");
    }

    @Test
    public void folderBundleWithUnderLineInName() {
        file = FileUtils.getFile(TEST_BUNDLES, "org.chromium.sdk.wipbackend.protocol_1_0_0.3.8.201210040401.jar");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("org.chromium.sdk.wipbackend.protocol_1_0");
        assertThat(a.getVersion().toString()).isEqualTo("0.3.8.201210040401");
    }

    @Test
    public void jarBundleWithUnderlineInJarName() {
        file = FileUtils.getFile(TEST_BUNDLES, "biz.aQute.repository_2.1.0.174214_220REL.jar");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getSymbolicName()).isEqualTo("biz.aQute.repository");
        assertThat(a.getVersion().toString()).isEqualTo("2.1.0.174214_220REL");
    }

    @Test
    public void folderBundle() {
        file = FileUtils.getFile(TEST_BUNDLES, "net.jeeeyul.pdetools.capability_1.2.0.201402202217");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getVersion().toString()).isEqualTo("1.2.0.201402202217");
        assertThat(a.getSymbolicName()).isEqualTo("net.jeeeyul.pdetools.capability");
    }

    @Test
    public void jarBundle() {
        file = FileUtils.getFile(TEST_BUNDLES, "org.eclipse.jst.common.annotations.core_1.1.300.v201004141630.jar");
        Assume.assumeTrue(file.exists());

        Artifact a = new ArtifactParser().createFromFile(file);
        assertThat(a).isNotNull();

        assertThat(a.getSource()).isEqualTo(file);
        assertThat(a.getVersion().toString()).isEqualTo("1.1.300.v201004141630");
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

}