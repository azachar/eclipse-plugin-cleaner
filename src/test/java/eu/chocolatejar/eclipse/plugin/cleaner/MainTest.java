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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MainTest {

	@Mock
	CommandLine mockInput;

	Main main;

	@Before
	public void before() {
		main = new Main();
		when(mockInput.hasOption(eq("testParam"))).thenReturn(true);
		main.input = mockInput;
	}

	@Test
	public void resolvingDefaultParams_forNull() {
		when(mockInput.getOptionValue(eq("testParam"))).thenReturn(null);
		String param = main.getParam("testParam", "defaultValue");
		assertThat(param).isEqualTo("defaultValue");
	}

	@Test
	public void resolvingDefaultParams_forEmpty() {
		when(mockInput.getOptionValue(eq("testParam"))).thenReturn("");

		String param = main.getParam("testParam", "defaultValue");
		assertThat(param).isEqualTo("defaultValue");
	}

	@Test
	public void resolvingDefaultParams_forWhitespaces() {
		when(mockInput.getOptionValue(eq("testParam"))).thenReturn("   ");

		String param = main.getParam("testParam", "defaultValue");
		assertThat(param).isEqualTo("defaultValue");
	}

	@Test
	public void resolveParams() {
		when(mockInput.getOptionValue(eq("testParam"))).thenReturn("userInput");

		String param = main.getParam("testParam", "defaultValue");
		assertThat(param).isEqualTo("userInput");
	}

	@Test
	public void resolveDefaultFolder() {
		String file = main.getDefaultDestinationFolder(new File("."));
		assertThat(file).contains("." + File.separator + "duplicates_");
	}

	@Test
	public void resolveDefaultFolder_isAbsolute() {
		String file = main.getDefaultDestinationFolder(new File("."));
		assertThat(file.startsWith(".")).isFalse();
	}
}
