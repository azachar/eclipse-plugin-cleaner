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
import java.io.IOException;
import java.nio.file.Files;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class FunctionalTest {

    File eclipseMock;

    @Parameters({ "-h", "-help", "--help", "-t", "-test", "--test" })
    @Test
    public void helpAndDryRunDoesNothing(String helpOption) throws IOException {
        createEclipseMock("eclipse-mock");
        String source = eclipseMock.getAbsolutePath();
        Main.main(new String[] { helpOption, "--source=" + source });

        assertThatPluginIsPresentInEclipseRootAndInDropins("uppercase_1.0.0.JAR");
        assertThatFeatureIsPresentInEclipseRootAndInDropins("org.eclipse.wst.server_core.feature_3.3.201.v20130412_1040-34Et8s73573C4Da2815");
    }

    @Parameters({ "-s=", "-source=" })
    @Test
    public void invalidSource(String sourceOption) throws IOException {
        createEclipseMock("eclipse-mock");
        String source = eclipseMock.getAbsolutePath();
        Main.main(new String[] { sourceOption + source });

        assertThatPluginIsPresentInEclipseRootOnly("uppercase_1.0.0.JAR");
        assertThatFeatureIsPresentInEclipseRootOnly("org.eclipse.wst.server_core.feature_3.3.201.v20130412_1040-34Et8s73573C4Da2815");
    }

    private void assertThatPluginIsPresentInEclipseRootOnly(String bundle) {
        assertThat(FileUtils.getFile(eclipseMock, "plugins", bundle)).exists();
        assertThat(FileUtils.getFile(eclipseMock, "dropins", "eclipse", "plugins", bundle)).doesNotExist();
    }
    
    private void assertThatPluginIsPresentInEclipseRootAndInDropins(String bundle) {
        assertThat(FileUtils.getFile(eclipseMock, "plugins", bundle)).exists();
        assertThat(FileUtils.getFile(eclipseMock, "dropins", "eclipse", "plugins", bundle)).exists();
    }

    private void assertThatFeatureIsPresentInEclipseRootOnly(String bundle) {
        assertThat(FileUtils.getFile(eclipseMock, "features", bundle)).exists();
        assertThat(FileUtils.getFile(eclipseMock, "dropins", "eclipse", "features", bundle)).doesNotExist();
    }
    
    private void assertThatFeatureIsPresentInEclipseRootAndInDropins(String bundle) {
        assertThat(FileUtils.getFile(eclipseMock, "features", bundle)).exists();
        assertThat(FileUtils.getFile(eclipseMock, "dropins", "eclipse", "features", bundle)).exists();
    }

    /**
     * Create a sample eclipse installation with several bundles in a temporary
     * directory.
     * 
     * @param scenario
     *            name of the folder in test/resources/test-scenarios
     * @throws IOException
     */
    public void createEclipseMock(String scenario) throws IOException {
        eclipseMock = Files.createTempDirectory("eclipse-mock-").toFile();
        final File source = FileUtils.getFile(FunctionalTest.class.getClassLoader().getResource(scenario).getFile());

        Assume.assumeTrue(eclipseMock.listFiles().length == 0);
        FileUtils.copyDirectory(source, eclipseMock);
        Assume.assumeTrue(eclipseMock.listFiles().length > 0);
    }

    @After
    public void afterTest() throws IOException {
        FileUtils.deleteDirectory(eclipseMock);
    }
}
