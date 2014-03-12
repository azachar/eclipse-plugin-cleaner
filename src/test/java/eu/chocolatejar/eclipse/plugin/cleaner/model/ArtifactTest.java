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
package eu.chocolatejar.eclipse.plugin.cleaner.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.osgi.framework.Version;

public class ArtifactTest {

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
    public void hashCodeAndEquals_withoutFile() throws Exception {
        Artifact a = new Artifact(null, "eu.chocolatejar.a", "1.0.0.RELEASE");
        Artifact b = new Artifact(null, "eu.chocolatejar.a", "2.0.0.RELEASE");
        
        assertThat(a).isNotEqualTo(b);
        assertThat(b).isNotEqualTo(a);
        
        assertThat(a.hashCode()).isNotZero();
        assertThat(b.hashCode()).isNotZero();
    }

    @Test
    public void isInDropinFolde() throws Exception {
        Artifact a = new Artifact(new File("eclipse/dropins/eclipse/dropins/eclipse/plugins/a_1.0.jar"), "a", "1.0");

        assertThat(a.isInDropinsFolder()).isTrue();
    }

    @Test
    public void isNotInDropinFolder() throws Exception {
        Artifact a = new Artifact(new File("eclipse/plugins/a_1.0.jar"), "a", "1.0");

        assertThat(a.isInDropinsFolder()).isFalse();
    }

    @Test
    public void comparisonWithNull() throws Exception {
        Artifact a = new Artifact(new File("old/a_1.1.300.v20130514-0733.jar"), "a", "1.1.300.v20130514-0733");

        assertThat(a.compareTo(null)).isPositive();
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
