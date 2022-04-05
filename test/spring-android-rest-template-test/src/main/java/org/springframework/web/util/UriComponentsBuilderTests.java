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

package org.springframework.web.util;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @author Phillip Webb
 * @author Oliver Gierke
 */
public class UriComponentsBuilderTests extends TestCase {

	@SmallTest
	public void testPlain() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		UriComponents result = builder.scheme("http").host("example.com").path("foo").queryParam("bar").fragment("baz").build();
		assertEquals("http", result.getScheme());
		assertEquals("example.com", result.getHost());
		assertEquals("foo", result.getPath());
		assertEquals("bar", result.getQuery());
		assertEquals("baz", result.getFragment());

		URI expected = new URI("https://example.com/foo?bar#baz");
		assertEquals("Invalid result URI", expected, result.toUri());
	}

	@SmallTest
	public void testMultipleFromSameBuilder() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme("http").host("example.com").pathSegment("foo");
		UriComponents result1 = builder.build();
		builder = builder.pathSegment("foo2").queryParam("bar").fragment("baz");
		UriComponents result2 = builder.build();

		assertEquals("http", result1.getScheme());
		assertEquals("example.com", result1.getHost());
		assertEquals("/foo", result1.getPath());
		URI expected = new URI("https://example.com/foo");
		assertEquals("Invalid result URI", expected, result1.toUri());

		assertEquals("http", result2.getScheme());
		assertEquals("example.com", result2.getHost());
		assertEquals("/foo/foo2", result2.getPath());
		assertEquals("bar", result2.getQuery());
		assertEquals("baz", result2.getFragment());
		expected = new URI("https://example.com/foo/foo2?bar#baz");
		assertEquals("Invalid result URI", expected, result2.toUri());
	}

	@SmallTest
	public void testFromPath() throws URISyntaxException {
		UriComponents result = UriComponentsBuilder.fromPath("foo").queryParam("bar").fragment("baz").build();
		assertEquals("foo", result.getPath());
		assertEquals("bar", result.getQuery());
		assertEquals("baz", result.getFragment());

		assertEquals("Invalid result URI String", "foo?bar#baz", result.toUriString());

		URI expected = new URI("foo?bar#baz");
		assertEquals("Invalid result URI", expected, result.toUri());

		result = UriComponentsBuilder.fromPath("/foo").build();
		assertEquals("/foo", result.getPath());

		expected = new URI("/foo");
		assertEquals("Invalid result URI", expected, result.toUri());
	}

	@SmallTest
	public void testFromHierarchicalUri() throws URISyntaxException {
		URI uri = new URI("https://example.com/foo?bar#baz");
		UriComponents result = UriComponentsBuilder.fromUri(uri).build();
		assertEquals("http", result.getScheme());
		assertEquals("example.com", result.getHost());
		assertEquals("/foo", result.getPath());
		assertEquals("bar", result.getQuery());
		assertEquals("baz", result.getFragment());

		assertEquals("Invalid result URI", uri, result.toUri());
	}

	@SmallTest
	public void testFromOpaqueUri() throws URISyntaxException {
		URI uri = new URI("mailto:foo@bar.com#baz");
		UriComponents result = UriComponentsBuilder.fromUri(uri).build();
		assertEquals("mailto", result.getScheme());
		assertEquals("foo@bar.com", result.getSchemeSpecificPart());
		assertEquals("baz", result.getFragment());

		assertEquals("Invalid result URI", uri, result.toUri());
	}

	// SPR-9317

	@SmallTest
	public void testFromUriEncodedQuery() throws URISyntaxException {
		URI uri = new URI("https://www.example.org/?param=aGVsbG9Xb3JsZA%3D%3D");
		String fromUri = UriComponentsBuilder.fromUri(uri).build().getQueryParams().get("param").get(0);
		String fromUriString = UriComponentsBuilder.fromUriString(uri.toString()).build().getQueryParams().get("param").get(0);

		assertEquals(fromUri, fromUriString);
	}

	@SmallTest
	public void testFromUriString() {
		UriComponents result = UriComponentsBuilder.fromUriString("https://www.ietf.org/rfc/rfc3986.txt").build();
		assertEquals("http", result.getScheme());
		assertNull(result.getUserInfo());
		assertEquals("www.ietf.org", result.getHost());
		assertEquals(-1, result.getPort());
		assertEquals("/rfc/rfc3986.txt", result.getPath());
		assertEquals(Arrays.asList("rfc", "rfc3986.txt"), result.getPathSegments());
		assertNull(result.getQuery());
		assertNull(result.getFragment());

		result = UriComponentsBuilder.fromUriString(
				"https://arjen:foobar@java.sun.com:80/javase/6/docs/api/java/util/BitSet.html?foo=bar#and(java.util.BitSet)")
				.build();
		assertEquals("http", result.getScheme());
		assertEquals("arjen:foobar", result.getUserInfo());
		assertEquals("java.sun.com", result.getHost());
		assertEquals(80, result.getPort());
		assertEquals("/javase/6/docs/api/java/util/BitSet.html", result.getPath());
		assertEquals("foo=bar", result.getQuery());
		MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<String, String>(1);
		expectedQueryParams.add("foo", "bar");
		assertEquals(expectedQueryParams, result.getQueryParams());
		assertEquals("and(java.util.BitSet)", result.getFragment());

		result = UriComponentsBuilder.fromUriString("mailto:java-net@java.sun.com#baz").build();
		assertEquals("mailto", result.getScheme());
		assertNull(result.getUserInfo());
		assertNull(result.getHost());
		assertEquals(-1, result.getPort());
		assertEquals("java-net@java.sun.com", result.getSchemeSpecificPart());
		assertNull(result.getPath());
		assertNull(result.getQuery());
		assertEquals("baz", result.getFragment());

		result = UriComponentsBuilder.fromUriString("docs/guide/collections/designfaq.html#28").build();
		assertNull(result.getScheme());
		assertNull(result.getUserInfo());
		assertNull(result.getHost());
		assertEquals(-1, result.getPort());
		assertEquals("docs/guide/collections/designfaq.html", result.getPath());
		assertNull(result.getQuery());
		assertEquals("28", result.getFragment());
	}

	// SPR-9832

	@SmallTest
	public void testFromUriStringQueryParamWithReservedCharInValue() throws URISyntaxException {
		String uri = "https://www.google.com/ig/calculator?q=1USD=?EUR";
		UriComponents result = UriComponentsBuilder.fromUriString(uri).build();

		assertEquals("q=1USD=?EUR", result.getQuery());
		assertEquals("1USD=?EUR", result.getQueryParams().getFirst("q"));
	}

	// SPR-10779

	@SmallTest
	public void testFromHttpUrlStringCaseInsesitiveScheme() {
		assertEquals("http", UriComponentsBuilder.fromHttpUrl("HTTP://www.google.com").build().getScheme());
		assertEquals("https", UriComponentsBuilder.fromHttpUrl("HTTPS://www.google.com").build().getScheme());
	}

	// SPR-10539

	@SmallTest
	public void testFromHttpUrlStringInvalidIPv6Host() throws URISyntaxException {
		try {
			UriComponentsBuilder.fromHttpUrl("http://[1abc:2abc:3abc::5ABC:6abc:8080/resource").build().encode();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	// SPR-10539

	@SmallTest
	public void testFromUriStringIPv6Host() throws URISyntaxException {
		UriComponents result = UriComponentsBuilder
				.fromUriString("http://[1abc:2abc:3abc::5ABC:6abc]:8080/resource")
				.build().encode();
		assertEquals("[1abc:2abc:3abc::5ABC:6abc]", result.getHost());

		UriComponents resultWithScopeId = UriComponentsBuilder
				.fromUriString("http://[1abc:2abc:3abc::5ABC:6abc%eth0]:8080/resource")
				.build().encode();
		assertEquals("[1abc:2abc:3abc::5ABC:6abc%25eth0]", resultWithScopeId.getHost());

		UriComponents resultIPv4compatible = UriComponentsBuilder
				.fromUriString("http://[::192.168.1.1]:8080/resource").build().encode();
		assertEquals("[::192.168.1.1]", resultIPv4compatible.getHost());
	}

	// SPR-11970

	@SmallTest
	public void testFromUriStringNoPathWithReservedCharInQuery() {
		UriComponents result = UriComponentsBuilder.fromUriString("https://example.com?foo=bar@baz").build();
		assertTrue(StringUtils.isEmpty(result.getUserInfo()));
		assertEquals("example.com", result.getHost());
		assertTrue(result.getQueryParams().containsKey("foo"));
		assertEquals("bar@baz", result.getQueryParams().getFirst("foo"));
	}

	@SmallTest
	public void testPath() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/foo/bar");
		UriComponents result = builder.build();

		assertEquals("/foo/bar", result.getPath());
		assertEquals(Arrays.asList("foo", "bar"), result.getPathSegments());
	}

	@SmallTest
	public void testPathSegments() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		UriComponents result = builder.pathSegment("foo").pathSegment("bar").build();

		assertEquals("/foo/bar", result.getPath());
		assertEquals(Arrays.asList("foo", "bar"), result.getPathSegments());
	}

	@SmallTest
	public void testPathThenPath() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/foo/bar").path("ba/z");
		UriComponents result = builder.build().encode();

		assertEquals("/foo/barba/z", result.getPath());
		assertEquals(Arrays.asList("foo", "barba", "z"), result.getPathSegments());
	}

	@SmallTest
	public void testPathThenPathSegments() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/foo/bar").pathSegment("ba/z");
		UriComponents result = builder.build().encode();

		assertEquals("/foo/bar/ba%2Fz", result.getPath());
		assertEquals(Arrays.asList("foo", "bar", "ba%2Fz"), result.getPathSegments());
	}

	@SmallTest
	public void testPathSegmentsThenPathSegments() {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().pathSegment("foo").pathSegment("bar");
		UriComponents result = builder.build();

		assertEquals("/foo/bar", result.getPath());
		assertEquals(Arrays.asList("foo", "bar"), result.getPathSegments());
	}

	@SmallTest
	public void testPathSegmentsThenPath() {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().pathSegment("foo").path("/");
		UriComponents result = builder.build();

		assertEquals("/foo/", result.getPath());
		assertEquals(Arrays.asList("foo"), result.getPathSegments());
	}

	@SmallTest
	public void testPathSegmentsSomeEmpty() {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().pathSegment("", "foo", "", "bar");
		UriComponents result = builder.build();

		assertEquals("/foo/bar", result.getPath());
		assertEquals(Arrays.asList("foo", "bar"), result.getPathSegments());
	}

	// SPR-12398

	@SmallTest
	public void pathWithDuplicateSlashes() throws URISyntaxException {
		UriComponents uriComponents = UriComponentsBuilder.fromPath("/foo/////////bar").build();
		assertEquals("/foo/bar", uriComponents.getPath());
	}

	@SmallTest
	public void testReplacePath() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.ietf.org/rfc/rfc2396.txt");
		builder.replacePath("/rfc/rfc3986.txt");
		UriComponents result = builder.build();

		assertEquals("https://www.ietf.org/rfc/rfc3986.txt", result.toUriString());

		builder = UriComponentsBuilder.fromUriString("https://www.ietf.org/rfc/rfc2396.txt");
		builder.replacePath(null);
		result = builder.build();

		assertEquals("https://www.ietf.org", result.toUriString());
	}

	@SmallTest
	public void testReplaceQuery() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://example.com/foo?foo=bar&baz=qux");
		builder.replaceQuery("baz=42");
		UriComponents result = builder.build();

		assertEquals("https://example.com/foo?baz=42", result.toUriString());

		builder = UriComponentsBuilder.fromUriString("https://example.com/foo?foo=bar&baz=qux");
		builder.replaceQuery(null);
		result = builder.build();

		assertEquals("https://example.com/foo", result.toUriString());
	}

	@SmallTest
	public void testQueryParams() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		UriComponents result = builder.queryParam("baz", "qux", 42).build();

		assertEquals("baz=qux&baz=42", result.getQuery());
		MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<String, String>(2);
		expectedQueryParams.add("baz", "qux");
		expectedQueryParams.add("baz", "42");
		assertEquals(expectedQueryParams, result.getQueryParams());
	}

	@SmallTest
	public void testEmptyQueryParam() throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		UriComponents result = builder.queryParam("baz").build();

		assertEquals("baz", result.getQuery());
		MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<String, String>(2);
		expectedQueryParams.add("baz", null);
		assertEquals(expectedQueryParams, result.getQueryParams());
	}

	@SmallTest
	public void testReplaceQueryParam() {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().queryParam("baz", "qux", 42);
		builder.replaceQueryParam("baz", "xuq", 24);
		UriComponents result = builder.build();

		assertEquals("baz=xuq&baz=24", result.getQuery());

		builder = UriComponentsBuilder.newInstance().queryParam("baz", "qux", 42);
		builder.replaceQueryParam("baz");
		result = builder.build();

		assertNull("Query param should have been deleted", result.getQuery());
	}

	@SmallTest
	public void testBuildAndExpandHierarchical() {
		UriComponents result = UriComponentsBuilder.fromPath("/{foo}").buildAndExpand("fooValue");
		assertEquals("/fooValue", result.toUriString());

		Map<String, String> values = new HashMap<String, String>();
		values.put("foo", "fooValue");
		values.put("bar", "barValue");
		result = UriComponentsBuilder.fromPath("/{foo}/{bar}").buildAndExpand(values);
		assertEquals("/fooValue/barValue", result.toUriString());
	}

	@SmallTest
	public void testBuildAndExpandOpaque() {
		UriComponents result = UriComponentsBuilder.fromUriString("mailto:{user}@{domain}").buildAndExpand("foo", "example.com");
		assertEquals("mailto:foo@example.com", result.toUriString());

		Map<String, String> values = new HashMap<String, String>();
		values.put("user", "foo");
		values.put("domain", "example.com");
		UriComponentsBuilder.fromUriString("mailto:{user}@{domain}").buildAndExpand(values);
		assertEquals("mailto:foo@example.com", result.toUriString());
	}

	@SmallTest
	public void testQueryParamWithValueWithEquals() throws Exception {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("https://example.com/foo?bar=baz").build();
		assertThat(uriComponents.toUriString(), equalTo("https://example.com/foo?bar=baz"));
	}

	@SmallTest
	public void testQueryParamWithoutValueWithEquals() throws Exception {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("https://example.com/foo?bar=").build();
		assertThat(uriComponents.toUriString(), equalTo("https://example.com/foo?bar="));
	}

	@SmallTest
	public void testQueryParamWithoutValueWithoutEquals() throws Exception {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("https://example.com/foo?bar").build();
		assertThat(uriComponents.toUriString(), equalTo("https://example.com/foo?bar"));
	}

	@SmallTest
	public void testRelativeUrls() throws Exception {
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/foo/../bar").build().toString(), equalTo("https://example.com/foo/../bar"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/foo/../bar").build().toUriString(), equalTo("https://example.com/foo/../bar"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/foo/../bar").build().toUri().getPath(), equalTo("/foo/../bar"));
		assertThat(UriComponentsBuilder.fromUriString("../../").build().toString(), equalTo("../../"));
		assertThat(UriComponentsBuilder.fromUriString("../../").build().toUriString(), equalTo("../../"));
		assertThat(UriComponentsBuilder.fromUriString("../../").build().toUri().getPath(), equalTo("../../"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com").path("foo/../bar").build().toString(), equalTo("https://example.com/foo/../bar"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com").path("foo/../bar").build().toUriString(), equalTo("https://example.com/foo/../bar"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com").path("foo/../bar").build().toUri().getPath(), equalTo("/foo/../bar"));
	}

	@SmallTest
	public void testEmptySegments() throws Exception {
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/abc/").path("/x/y/z").build().toString(), equalTo("https://example.com/abc/x/y/z"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/abc/").pathSegment("x", "y", "z").build().toString(), equalTo("https://example.com/abc/x/y/z"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/abc/").path("/x/").path("/y/z").build().toString(), equalTo("https://example.com/abc/x/y/z"));
		assertThat(UriComponentsBuilder.fromUriString("https://example.com/abc/").pathSegment("x").path("y").build().toString(), equalTo("https://example.com/abc/x/y"));
	}

	@SmallTest
	public void testParsesEmptyFragment() {
		UriComponents components = UriComponentsBuilder.fromUriString("/example#").build();
		assertThat(components.getFragment(), is(nullValue()));
		assertThat(components.toString(), equalTo("/example"));
	}
}
