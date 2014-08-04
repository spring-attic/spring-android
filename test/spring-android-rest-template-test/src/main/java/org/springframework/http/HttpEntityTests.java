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

package org.springframework.http;

import junit.framework.TestCase;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class HttpEntityTests extends TestCase {

	public void testNoHeaders() {
		String body = "foo";
		HttpEntity<String> entity = new HttpEntity<String>(body);
		assertSame(body, entity.getBody());
		assertTrue(entity.getHeaders().isEmpty());
	}

	public void testHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		String body = "foo";
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		assertEquals(body, entity.getBody());
		assertEquals(MediaType.TEXT_PLAIN, entity.getHeaders().getContentType());
		assertEquals("text/plain", entity.getHeaders().getFirst("Content-Type"));
	}

	public void testMultiValueMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.set("Content-Type", "text/plain");
		String body = "foo";
		HttpEntity<String> entity = new HttpEntity<String>(body, map);
		assertEquals(body, entity.getBody());
		assertEquals(MediaType.TEXT_PLAIN, entity.getHeaders().getContentType());
		assertEquals("text/plain", entity.getHeaders().getFirst("Content-Type"));
	}

	public void testEquals() {
		MultiValueMap<String, String> map1 = new LinkedMultiValueMap<String, String>();
		map1.set("Content-Type", "text/plain");

		MultiValueMap<String, String> map2 = new LinkedMultiValueMap<String, String>();
		map2.set("Content-Type", "application/json");

		assertTrue(new HttpEntity<Object>().equals(new HttpEntity<Object>()));
		assertFalse(new HttpEntity<Object>(map1).equals(new HttpEntity<Object>()));
		assertFalse(new HttpEntity<Object>().equals(new HttpEntity<Object>(map2)));

		assertTrue(new HttpEntity<Object>(map1).equals(new HttpEntity<Object>(map1)));
		assertFalse(new HttpEntity<Object>(map1).equals(new HttpEntity<Object>(map2)));

		assertTrue(new HttpEntity<String>(null, null).equals(new HttpEntity<String>(null, null)));
		assertFalse(new HttpEntity<String>("foo", null).equals(new HttpEntity<String>(null, null)));
		assertFalse(new HttpEntity<String>(null, null).equals(new HttpEntity<String>("bar", null)));

		assertTrue(new HttpEntity<String>("foo", map1).equals(new HttpEntity<String>("foo", map1)));
		assertFalse(new HttpEntity<String>("foo", map1).equals(new HttpEntity<String>("bar", map1)));
	}

	public void testResponseEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		String body = "foo";
		ResponseEntity<String> entity = new ResponseEntity<String>(body, headers, HttpStatus.OK);
		assertEquals(body, entity.getBody());
		assertEquals(MediaType.TEXT_PLAIN, entity.getHeaders().getContentType());
		assertEquals("text/plain", entity.getHeaders().getFirst("Content-Type"));
		assertEquals("text/plain", entity.getHeaders().getFirst("Content-Type"));

	}

}
