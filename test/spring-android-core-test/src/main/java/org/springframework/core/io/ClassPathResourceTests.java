/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests cornering bug SPR-6888.
 *
 * @author Chris Beams
 * @author Roy Clarkson
 */
public class ClassPathResourceTests extends TestCase {
	private static final String PACKAGE_PATH = "org/springframework/core/io";
	private static final String RESOURCE_NAME = "notexist.xml";
	private static final String FQ_RESOURCE_PATH = PACKAGE_PATH + '/' + RESOURCE_NAME;

	@SmallTest
	public void testStringConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH));
	}

	@SmallTest
	public void testClassLiteralConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(RESOURCE_NAME, this.getClass()));
	}

	@SmallTest
	public void testClassLoaderConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH, this.getClass().getClassLoader()));
	}

	private void assertExceptionContainsFullyQualifiedPath(ClassPathResource resource) {
		try {
			resource.getInputStream();
			fail("FileNotFoundException expected for resource: " + resource);
		} catch (IOException ex) {
			assertTrue(ex instanceof FileNotFoundException);
			assertTrue(ex.getMessage().contains(FQ_RESOURCE_PATH));
		}
	}
}
