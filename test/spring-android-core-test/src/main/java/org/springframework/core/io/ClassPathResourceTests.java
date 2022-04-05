/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Unit tests that serve as regression tests for the bugs described in SPR-6888
 * and SPR-9413.
 *
 * @author Chris Beams
 * @author Sam Brannen
 */
public class ClassPathResourceTests extends TestCase {

	private static final String PACKAGE_PATH = "org/springframework/core/io";
	private static final String NONEXISTENT_RESOURCE_NAME = "nonexistent.xml";
	private static final String FQ_RESOURCE_PATH = PACKAGE_PATH + '/' + NONEXISTENT_RESOURCE_NAME;

	/**
	 * Absolute path version of {@link #FQ_RESOURCE_PATH}.
	 */
	private static final String FQ_RESOURCE_PATH_WITH_LEADING_SLASH = '/' + FQ_RESOURCE_PATH;

	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^class path resource \\[(.+?)\\]$");


	private void assertDescriptionContainsExpectedPath(ClassPathResource resource, String expectedPath) {
		Matcher matcher = DESCRIPTION_PATTERN.matcher(resource.getDescription());
		assertTrue(matcher.matches());
		assertEquals(1, matcher.groupCount());
		String match = matcher.group(1);

		assertEquals(expectedPath, match);
	}

	private void assertExceptionContainsFullyQualifiedPath(ClassPathResource resource) {
		try {
			resource.getInputStream();
			fail("FileNotFoundException expected for resource: " + resource);
		}
		catch (IOException ex) {
			assertThat(ex, instanceOf(FileNotFoundException.class));
			assertThat(ex.getMessage(), containsString(FQ_RESOURCE_PATH));
		}
	}

	public void testStringConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH));
	}

	public void testClassLiteralConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(NONEXISTENT_RESOURCE_NAME, this.getClass()));
	}

	public void testClassLoaderConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH,
			this.getClass().getClassLoader()));
	}

	public void testGetDescriptionWithStringConstructor() {
		assertDescriptionContainsExpectedPath(new ClassPathResource(FQ_RESOURCE_PATH), FQ_RESOURCE_PATH);
	}

	public void testGetDescriptionWithStringConstructorAndLeadingSlash() {
		assertDescriptionContainsExpectedPath(new ClassPathResource(FQ_RESOURCE_PATH_WITH_LEADING_SLASH),
			FQ_RESOURCE_PATH);
	}

	public void testGetDescriptionWithClassLiteralConstructor() {
		assertDescriptionContainsExpectedPath(new ClassPathResource(NONEXISTENT_RESOURCE_NAME, this.getClass()),
			FQ_RESOURCE_PATH);
	}

	public void testGetDescriptionWithClassLiteralConstructorAndLeadingSlash() {
		assertDescriptionContainsExpectedPath(
			new ClassPathResource(FQ_RESOURCE_PATH_WITH_LEADING_SLASH, this.getClass()), FQ_RESOURCE_PATH);
	}

	public void testGetDescriptionWithClassLoaderConstructor() {
		assertDescriptionContainsExpectedPath(
			new ClassPathResource(FQ_RESOURCE_PATH, this.getClass().getClassLoader()), FQ_RESOURCE_PATH);
	}

	public void testGetDescriptionWithClassLoaderConstructorAndLeadingSlash() {
		assertDescriptionContainsExpectedPath(new ClassPathResource(FQ_RESOURCE_PATH_WITH_LEADING_SLASH,
			this.getClass().getClassLoader()), FQ_RESOURCE_PATH);
	}

}
