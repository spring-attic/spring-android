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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Roy Clarkson
 */
public class UriTemplateTests extends TestCase {

	@SmallTest
	public void testGetVariableNames() throws Exception {
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		List<String> variableNames = template.getVariableNames();
		assertEquals("Invalid variable names", Arrays.asList("hotel", "booking"), variableNames);
	}

	@SmallTest
	public void testExpandVarArgs() throws Exception {
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		URI result = template.expand("1", "42");
		assertEquals("Invalid expanded template", new URI("http://example.com/hotels/1/bookings/42"), result);
	}

	@SmallTest
	public void testExpandVarArgsNotEnoughVariables() throws Exception {
		try {
			UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
			template.expand("1");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testExpandMap() throws Exception {
		Map<String, String> uriVariables = new HashMap<String, String>(2);
		uriVariables.put("booking", "42");
		uriVariables.put("hotel", "1");
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		URI result = template.expand(uriVariables);
		assertEquals("Invalid expanded template", new URI("http://example.com/hotels/1/bookings/42"), result);
	}

	@SmallTest
	public void testExpandMapDuplicateVariables() throws Exception {
		UriTemplate template = new UriTemplate("/order/{c}/{c}/{c}");
		assertEquals("Invalid variable names", Arrays.asList("c", "c", "c"), template.getVariableNames());
		URI result = template.expand(Collections.singletonMap("c", "cheeseburger"));
		assertEquals("Invalid expanded template", new URI("/order/cheeseburger/cheeseburger/cheeseburger"), result);
	}

	@SmallTest
	public void testExpandMapNonString() throws Exception {
		Map<String, Integer> uriVariables = new HashMap<String, Integer>(2);
		uriVariables.put("booking", 42);
		uriVariables.put("hotel", 1);
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		URI result = template.expand(uriVariables);
		assertEquals("Invalid expanded template", new URI("http://example.com/hotels/1/bookings/42"), result);
	}

	@SmallTest
	public void testExpandMapEncoded() throws Exception {
		Map<String, String> uriVariables = Collections.singletonMap("hotel", "Z\u00fcrich");
		UriTemplate template = new UriTemplate("http://example.com/hotel list/{hotel}");
		URI result = template.expand(uriVariables);
		assertEquals("Invalid expanded template", new URI("http://example.com/hotel%20list/Z%C3%BCrich"), result);
	}

	@SmallTest
	public void testExpandMapUnboundVariables() throws Exception {
		try {
			Map<String, String> uriVariables = new HashMap<String, String>(2);
			uriVariables.put("booking", "42");
			uriVariables.put("bar", "1");
			UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
			template.expand(uriVariables);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testExpandEncoded() throws Exception {
		UriTemplate template = new UriTemplate("http://example.com/hotel list/{hotel}");
		URI result = template.expand("Z\u00fcrich");
		assertEquals("Invalid expanded template", new URI("http://example.com/hotel%20list/Z%C3%BCrich"), result);
	}

	@SmallTest
	public void testMatches() throws Exception {
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		assertTrue("UriTemplate does not match", template.matches("http://example.com/hotels/1/bookings/42"));
		assertFalse("UriTemplate matches", template.matches("http://example.com/hotels/bookings"));
		assertFalse("UriTemplate matches", template.matches(""));
		assertFalse("UriTemplate matches", template.matches(null));
	}

	@SmallTest
	public void testMatchesCustomRegex() throws Exception {
		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel:\\d+}");
		assertTrue("UriTemplate does not match", template.matches("http://example.com/hotels/42"));
		assertFalse("UriTemplate matches", template.matches("http://example.com/hotels/foo"));
	}

	@SmallTest
	public void testMatch() throws Exception {
		Map<String, String> expected = new HashMap<String, String>(2);
		expected.put("booking", "42");
		expected.put("hotel", "1");

		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
		Map<String, String> result = template.match("http://example.com/hotels/1/bookings/42");
		assertEquals("Invalid match", expected, result);
	}

	@SmallTest
	public void testMatchCustomRegex() throws Exception {
		Map<String, String> expected = new HashMap<String, String>(2);
		expected.put("booking", "42");
		expected.put("hotel", "1");

		UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel:\\d}/bookings/{booking:\\d+}");
		Map<String, String> result = template.match("http://example.com/hotels/1/bookings/42");
		assertEquals("Invalid match", expected, result);
	}

	@SmallTest
	public void testMatchDuplicate() throws Exception {
		UriTemplate template = new UriTemplate("/order/{c}/{c}/{c}");
		Map<String, String> result = template.match("/order/cheeseburger/cheeseburger/cheeseburger");
		Map<String, String> expected = Collections.singletonMap("c", "cheeseburger");
		assertEquals("Invalid match", expected, result);
	}

	@SmallTest
	public void testMatchMultipleInOneSegment() throws Exception {
		UriTemplate template = new UriTemplate("/{foo}-{bar}");
		Map<String, String> result = template.match("/12-34");
		Map<String, String> expected = new HashMap<String, String>(2);
		expected.put("foo", "12");
		expected.put("bar", "34");
		assertEquals("Invalid match", expected, result);
	}

	@SmallTest
	public void testQueryVariables() throws Exception {
		UriTemplate template = new UriTemplate("/search?q={query}");
		assertTrue(template.matches("/search?q=foo"));
	}

	@SmallTest
	public void testFragments() throws Exception {
		UriTemplate template = new UriTemplate("/search#{fragment}");
		assertTrue(template.matches("/search#foo"));

		template = new UriTemplate("/search?query={query}#{fragment}");
		assertTrue(template.matches("/search?query=foo#bar"));
	}

	@SmallTest
	public void testExpandWithDollar() {
		UriTemplate template = new UriTemplate("/{a}");
		URI uri = template.expand("$replacement");
		assertEquals("/$replacement", uri.toString());
	}

	@SmallTest
	public void testExpandWithAtSign() {
		UriTemplate template = new UriTemplate("http://localhost/query={query}");
		URI uri = template.expand("foo@bar");
		assertEquals("http://localhost/query=foo@bar", uri.toString());
	}

}
