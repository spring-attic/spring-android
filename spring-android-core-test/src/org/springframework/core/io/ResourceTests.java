/*
 * Copyright 2002-2011 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import junit.framework.TestCase;

import org.springframework.util.FileCopyUtils;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Juergen Hoeller
 * @author Roy Clarkson
 */
public class ResourceTests extends TestCase {

	@MediumTest
	public void testByteArrayResource() throws IOException {
		Resource resource = new ByteArrayResource("testString".getBytes());
		assertTrue(resource.exists());
		assertFalse(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals(resource, new ByteArrayResource("testString".getBytes()));
	}

	@MediumTest
	public void testByteArrayResourceWithDescription() throws IOException {
		Resource resource = new ByteArrayResource("testString".getBytes(), "my description");
		assertTrue(resource.exists());
		assertFalse(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals("my description", resource.getDescription());
		assertEquals(resource, new ByteArrayResource("testString".getBytes()));
	}

	@MediumTest
	public void testInputStreamResource() throws IOException {
		InputStream is = new ByteArrayInputStream("testString".getBytes());
		Resource resource = new InputStreamResource(is);
		assertTrue(resource.exists());
		assertTrue(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals(resource, new InputStreamResource(is));
	}

	@MediumTest
	public void testInputStreamResourceWithDescription() throws IOException {
		InputStream is = new ByteArrayInputStream("testString".getBytes());
		Resource resource = new InputStreamResource(is, "my description");
		assertTrue(resource.exists());
		assertTrue(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals("my description", resource.getDescription());
		assertEquals(resource, new InputStreamResource(is));
	}

	@MediumTest
	public void testClassPathResource() throws IOException {
		Resource resource = new ClassPathResource("org/springframework/core/io/example.properties");
//		doTestResource(resource);
		Resource resource2 = new ClassPathResource("org/springframework/core/../core/io/./example.properties");
		assertEquals(resource, resource2);
		Resource resource3 = new ClassPathResource("org/springframework/core/").createRelative("../core/io/./example.properties");
		assertEquals(resource, resource3);

		// Check whether equal/hashCode works in a HashSet.
		HashSet<Resource> resources = new HashSet<Resource>();
		resources.add(resource);
		resources.add(resource2);
		assertEquals(1, resources.size());
	}

	@MediumTest
	public void testClassPathResourceWithClassLoader() throws IOException {
		Resource resource =
				new ClassPathResource("org/springframework/core/io/example.properties", getClass().getClassLoader());
//		doTestResource(resource);
		assertEquals(resource,
				new ClassPathResource("org/springframework/core/../core/io/./example.properties", getClass().getClassLoader()));
	}

	@MediumTest
	public void testClassPathResourceWithClass() throws IOException {
		Resource resource = new ClassPathResource("example.properties", getClass());
//		doTestResource(resource);
		assertEquals(resource, new ClassPathResource("example.properties", getClass()));
	}

	@MediumTest
	public void testFileSystemResource() throws IOException {
		Resource resource = new FileSystemResource(getClass().getResource("example.properties").getFile());
		doTestResource(resource);
		assertEquals(new FileSystemResource(getClass().getResource("example.properties").getFile()), resource);
		Resource resource2 = new FileSystemResource("core/io/example.properties");
		assertEquals(resource2, new FileSystemResource("core/../core/io/./example.properties"));
	}

	@MediumTest
	public void testUrlResource() throws IOException {
		Resource resource = new UrlResource(getClass().getResource("example.properties"));
		doTestResource(resource);
		assertEquals(new UrlResource(getClass().getResource("example.properties")), resource);
		Resource resource2 = new UrlResource("file:core/io/example.properties");
		assertEquals(resource2, new UrlResource("file:core/../core/io/./example.properties"));
	}

	private void doTestResource(Resource resource) throws IOException {
		assertEquals("example.properties", resource.getFilename());
		assertTrue(resource.getURL().getFile().endsWith("example.properties"));

		Resource relative1 = resource.createRelative("ClassPathResource.class");
		assertEquals("ClassPathResource.class", relative1.getFilename());
		assertTrue(relative1.getURL().getFile().endsWith("ClassPathResource.class"));
//		assertTrue(relative1.exists());

//		Resource relative2 = resource.createRelative("support/ResourcePatternResolver.class");
//		assertEquals("ResourcePatternResolver.class", relative2.getFilename());
//		assertTrue(relative2.getURL().getFile().endsWith("ResourcePatternResolver.class"));
//		assertTrue(relative2.exists());

		Resource relative3 = resource.createRelative("../MethodParameter.class");
		assertEquals("MethodParameter.class", relative3.getFilename());
		assertTrue(relative3.getURL().getFile().endsWith("MethodParameter.class"));
//		assertTrue(relative3.exists());
		
	}

	@MediumTest
	public void testClassPathResourceWithRelativePath() throws IOException {
		Resource resource = new ClassPathResource("dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new ClassPathResource("dir/subdir"), relative);
	}

	@MediumTest
	public void testFileSystemResourceWithRelativePath() throws IOException {
		Resource resource = new FileSystemResource("dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new FileSystemResource("dir/subdir"), relative);
	}

	@MediumTest
	public void testUrlResourceWithRelativePath() throws IOException {
		Resource resource = new UrlResource("file:dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new UrlResource("file:dir/subdir"), relative);
	}

	@MediumTest
	public void testAbstractResourceExceptions() throws Exception {
		final String name = "test-resource";

		Resource resource = new AbstractResource() {
			public String getDescription() {
				return name;
			}
			public InputStream getInputStream() {
				return null;
			}
		};

		try {
			resource.getURL();
			fail("FileNotFoundException should have been thrown");
		}
		catch (FileNotFoundException ex) {
			assertTrue(ex.getMessage().indexOf(name) != -1);
		}
		try {
			resource.getFile();
			fail("FileNotFoundException should have been thrown");
		}
		catch (FileNotFoundException ex) {
			assertTrue(ex.getMessage().indexOf(name) != -1);
		}
		try {
			resource.createRelative("/testing");
			fail("FileNotFoundException should have been thrown");
		}
		catch (FileNotFoundException ex) {
			assertTrue(ex.getMessage().indexOf(name) != -1);
		}
		try {
			resource.getFilename();
			fail("IllegalStateException should have been thrown");
		}
		catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().indexOf(name) != -1);
		}
	}

}
