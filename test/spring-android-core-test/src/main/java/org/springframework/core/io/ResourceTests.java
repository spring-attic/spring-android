/*
 * Copyright 2002-2014 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.springframework.util.FileCopyUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class ResourceTests extends AndroidTestCase {

	public void testByteArrayResource() throws IOException {
		Resource resource = new ByteArrayResource("testString".getBytes());
		assertTrue(resource.exists());
		assertFalse(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals(resource, new ByteArrayResource("testString".getBytes()));
	}

	public void testByteArrayResourceWithDescription() throws IOException {
		Resource resource = new ByteArrayResource("testString".getBytes(), "my description");
		assertTrue(resource.exists());
		assertFalse(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals("my description", resource.getDescription());
		assertEquals(resource, new ByteArrayResource("testString".getBytes()));
	}

	public void testInputStreamResource() throws IOException {
		InputStream is = new ByteArrayInputStream("testString".getBytes());
		Resource resource = new InputStreamResource(is);
		assertTrue(resource.exists());
		assertTrue(resource.isOpen());
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		assertEquals("testString", content);
		assertEquals(resource, new InputStreamResource(is));
	}

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

	public void testClassPathResource() throws IOException {
		Resource resource = new ClassPathResource("org/springframework/core/io/example.properties");
		doTestResource(resource);
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

	public void testClassPathResourceWithClassLoader() throws IOException {
		Resource resource =
				new ClassPathResource("org/springframework/core/io/example.properties", getClass().getClassLoader());
		doTestResource(resource);
		assertEquals(resource,
				new ClassPathResource("org/springframework/core/../core/io/./example.properties", getClass().getClassLoader()));
	}

	public void testClassPathResourceWithClass() throws IOException {
		Resource resource = new ClassPathResource("example.properties", getClass());
		doTestResource(resource);
		assertEquals(resource, new ClassPathResource("example.properties", getClass()));
	}

	public void testFileSystemResource() throws IOException {
		Resource resource = new FileSystemResource(getClass().getResource("example.properties").getFile());
		doTestResource(resource);
		assertEquals(new FileSystemResource(getClass().getResource("example.properties").getFile()), resource);
		Resource resource2 = new FileSystemResource("core/io/example.properties");
		assertEquals(resource2, new FileSystemResource("core/../core/io/./example.properties"));
	}

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
	}

	public void testClassPathResourceWithRelativePath() throws IOException {
		Resource resource = new ClassPathResource("dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new ClassPathResource("dir/subdir"), relative);
	}

	public void testFileSystemResourceWithRelativePath() throws IOException {
		Resource resource = new FileSystemResource("dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new FileSystemResource("dir/subdir"), relative);
	}

	public void testUrlResourceWithRelativePath() throws IOException {
		Resource resource = new UrlResource("file:dir/");
		Resource relative = resource.createRelative("subdir");
		assertEquals(new UrlResource("file:dir/subdir"), relative);
	}

//	public void testNonFileResourceExists() throws Exception {
//		Resource resource = new UrlResource("http://springone2gx.com");
//		assertTrue(resource.exists());
//	}

	public void testAbstractResourceExceptions() throws Exception {
		final String name = "test-resource";

		Resource resource = new AbstractResource() {
			@Override
			public String getDescription() {
				return name;
			}
			@Override
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

		assertThat(resource.getFilename(), nullValue());
	}

	public void testContentLength() throws IOException {
		AbstractResource resource = new AbstractResource() {
			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });
			}
			@Override
			public String getDescription() {
				return null;
			}
		};
		assertThat(resource.contentLength(), is(3L));
	}

	public void testContentLength_withNullInputStream() throws IOException {
		AbstractResource resource = new AbstractResource() {
			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}
			@Override
			public String getDescription() {
				return null;
			}
		};
		boolean success = false;
		try {
			resource.contentLength();
		}
		catch (IllegalStateException e) {
			success = true;
		}
		assertTrue("Expected IllegalStateException", success);
	}

	@MediumTest
	public void testAssetResource() throws IOException {
		Resource resource = new AssetResource(getContext().getAssets(), "logo.jpg");
		assertTrue(resource.exists());
		assertTrue(resource.contentLength() > 0);
		InputStream inputStream = resource.getInputStream();
		assertNotNull(inputStream);

		Resource resource2 = new AssetResource(getContext().getAssets(), "fail.jpg");
		assertFalse(resource2.exists());
	}

}
