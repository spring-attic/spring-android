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

package org.springframework.web.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class UriComponentsTests extends TestCase {

	@SmallTest
	public void testEncode() {
		UriComponents uriComponents = UriComponentsBuilder.fromPath("/hotel list").build();
		UriComponents encoded = uriComponents.encode();
		assertEquals("/hotel%20list", encoded.getPath());
	}

	@SmallTest
	public void testToUriEncoded() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://example.com/hotel list/Z\u00fcrich").build();
		assertEquals(new URI("http://example.com/hotel%20list/Z%C3%BCrich"), uriComponents.encode().toUri());
	}

	@SmallTest
	public void testToUriNotEncoded() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://example.com/hotel list/Z\u00fcrich").build();
		assertEquals(new URI("http://example.com/hotel%20list/Z\u00fcrich"), uriComponents.toUri());
	}

	@SmallTest
	public void testToUriAlreadyEncoded() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://example.com/hotel%20list/Z%C3%BCrich").build(true);
		UriComponents encoded = uriComponents.encode();
		assertEquals(new URI("http://example.com/hotel%20list/Z%C3%BCrich"), encoded.toUri());
	}

	@SmallTest
	public void testToUriWithIpv6HostAlreadyEncoded() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://[1abc:2abc:3abc::5ABC:6abc]:8080/hotel%20list/Z%C3%BCrich").build(true);
		UriComponents encoded = uriComponents.encode();
		assertEquals(new URI("http://[1abc:2abc:3abc::5ABC:6abc]:8080/hotel%20list/Z%C3%BCrich"), encoded.toUri());
	}

	@SmallTest
	public void testExpand() {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://example.com").path("/{foo} {bar}").build();
		uriComponents = uriComponents.expand("1 2", "3 4");
		assertEquals("/1 2 3 4", uriComponents.getPath());
		assertEquals("http://example.com/1 2 3 4", uriComponents.toUriString());
	}

	// SPR-12123

	@SmallTest
	public void port() {
		UriComponents uri1 = UriComponentsBuilder.fromUriString("http://example.com:8080/bar").build();
		UriComponents uri2 = UriComponentsBuilder.fromUriString("http://example.com/bar").port(8080).build();
		UriComponents uri3 = UriComponentsBuilder.fromUriString("http://example.com/bar").port("{port}").build().expand(8080);
		UriComponents uri4 = UriComponentsBuilder.fromUriString("http://example.com/bar").port("808{digit}").build().expand(0);
		assertEquals(8080, uri1.getPort());
		assertEquals("http://example.com:8080/bar", uri1.toUriString());
		assertEquals(8080, uri2.getPort());
		assertEquals("http://example.com:8080/bar", uri2.toUriString());
		assertEquals(8080, uri3.getPort());
		assertEquals("http://example.com:8080/bar", uri3.toUriString());
		assertEquals(8080, uri4.getPort());
		assertEquals("http://example.com:8080/bar", uri4.toUriString());
	}

	@SmallTest
	public void testExpandEncoded() {
		try {
			UriComponentsBuilder.fromPath("/{foo}").build().encode().expand("bar");
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
		}
	}

	@SmallTest
	public void testInvalidCharacters() {
		try {
			UriComponentsBuilder.fromPath("/{foo}").build(true);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testInvalidEncodedSequence() {
		try {
			UriComponentsBuilder.fromPath("/fo%2o").build(true);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testNormalize() {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://example.com/foo/../bar").build();
		assertEquals("http://example.com/bar", uriComponents.normalize().toString());
	}

	@SmallTest
	public void testSerializable() throws Exception {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(
				"http://example.com").path("/{foo}").query("bar={baz}").build();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(uriComponents);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
		UriComponents readObject = (UriComponents) ois.readObject();
		assertThat(uriComponents.toString(), equalTo(readObject.toString()));
	}

	@SmallTest
	public void testEqualsHierarchicalUriComponents() throws Exception {
		UriComponents uriComponents1 = UriComponentsBuilder.fromUriString("http://example.com").path("/{foo}").query("bar={baz}").build();
		UriComponents uriComponents2 = UriComponentsBuilder.fromUriString("http://example.com").path("/{foo}").query("bar={baz}").build();
		UriComponents uriComponents3 = UriComponentsBuilder.fromUriString("http://example.com").path("/{foo}").query("bin={baz}").build();
		assertThat(uriComponents1, instanceOf(HierarchicalUriComponents.class));
		assertThat(uriComponents1, equalTo(uriComponents1));
		assertThat(uriComponents1, equalTo(uriComponents2));
		assertThat(uriComponents1, not(equalTo(uriComponents3)));
	}

	@SmallTest
	public void testEqualsOpaqueUriComponents() throws Exception {
		UriComponents uriComponents1 = UriComponentsBuilder.fromUriString("http:example.com/foo/bar").build();
		UriComponents uriComponents2 = UriComponentsBuilder.fromUriString("http:example.com/foo/bar").build();
		UriComponents uriComponents3 = UriComponentsBuilder.fromUriString("http:example.com/foo/bin").build();
		assertThat(uriComponents1, instanceOf(OpaqueUriComponents.class));
		assertThat(uriComponents1, equalTo(uriComponents1));
		assertThat(uriComponents1, equalTo(uriComponents2));
		assertThat(uriComponents1, not(equalTo(uriComponents3)));
	}

}
