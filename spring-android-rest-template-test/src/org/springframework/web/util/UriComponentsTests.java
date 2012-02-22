/*
 * Copyright 2002-2012 the original author or authors.
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
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://example.com/hotel list/Z\u00fcrich").build();
		UriComponents encoded = uriComponents.encode();
		assertEquals(new URI("http://example.com/hotel%20list/Z%C3%BCrich"), encoded.toUri());
	}

	@SmallTest
	public void testToUriNotEncoded() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://example.com/hotel list/Z\u00fcrich").build();
		assertEquals(new URI("http://example.com/hotel%20list/Z\u00fcrich"), uriComponents.toUri());
	}

	@SmallTest
	public void testExpand() {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://example.com").path("/{foo} {bar}").build();
		uriComponents = uriComponents.expand("1 2", "3 4");
		assertEquals("/1 2 3 4", uriComponents.getPath());
		assertEquals("http://example.com/1 2 3 4", uriComponents.toUriString());
	}

	@SmallTest
	public void testExpandEncoded() {
		boolean success = false;
		try {
			UriComponentsBuilder.fromPath("/{foo}").build().encode().expand("bar");
		} catch (IllegalStateException e) {
			success = true;
		}
		assertTrue("expected IllegalStateException", success);
	}

	@SmallTest
	public void testInvalidCharacters() {
		boolean success = false;
		try {
			UriComponentsBuilder.fromPath("/{foo}").build(true);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testInvalidEncodedSequence() {
		boolean success = false;
		try {
			UriComponentsBuilder.fromPath("/fo%2o").build(true);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("expected IllegalArgumentException", success);
	}

}
