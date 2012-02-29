/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class HttpHeadersTests extends TestCase {

	private HttpHeaders headers;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		headers = new HttpHeaders();
	}

	@SmallTest
	public void testAccept() {
		MediaType mediaType1 = new MediaType("text", "html");
		MediaType mediaType2 = new MediaType("text", "plain");
		List<MediaType> mediaTypes = new ArrayList<MediaType>(2);
		mediaTypes.add(mediaType1);
		mediaTypes.add(mediaType2);
		headers.setAccept(mediaTypes);
		assertEquals("Invalid Accept header", mediaTypes, headers.getAccept());
		assertEquals("Invalid Accept header", "text/html, text/plain", headers.getFirst("Accept"));
	}

	@SmallTest
	public void testAcceptCharsets() {
		Charset charset1 = Charset.forName("UTF-8");
		Charset charset2 = Charset.forName("ISO-8859-1");
		List<Charset> charsets = new ArrayList<Charset>(2);
		charsets.add(charset1);
		charsets.add(charset2);
		headers.setAcceptCharset(charsets);
		assertEquals("Invalid Accept header", charsets, headers.getAcceptCharset());
		assertEquals("Invalid Accept header", "utf-8, iso-8859-1", headers.getFirst("Accept-Charset"));
	}

	@SmallTest
	public void testAcceptCharsetWildcard() {
		headers.set("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		assertEquals("Invalid Accept header", Arrays.asList(Charset.forName("ISO-8859-1"), Charset.forName("UTF-8")), headers.getAcceptCharset());
	}

	@SmallTest
	public void testAllow() {
		EnumSet<HttpMethod> methods = EnumSet.of(HttpMethod.GET, HttpMethod.POST);
		headers.setAllow(methods);
		assertEquals("Invalid Allow header", methods, headers.getAllow());
		assertEquals("Invalid Allow header", "GET,POST", headers.getFirst("Allow"));
	}

	@SmallTest
	public void testContentLength() {
		long length = 42L;
		headers.setContentLength(length);
		assertEquals("Invalid Content-Length header", length, headers.getContentLength());
		assertEquals("Invalid Content-Length header", "42", headers.getFirst("Content-Length"));
	}

	@SmallTest
	public void testContentType() {
		MediaType contentType = new MediaType("text", "html", Charset.forName("UTF-8"));
		headers.setContentType(contentType);
		assertEquals("Invalid Content-Type header", contentType, headers.getContentType());
		assertEquals("Invalid Content-Type header", "text/html;charset=UTF-8", headers.getFirst("Content-Type"));
	}

	@SmallTest
	public void testLocation() throws URISyntaxException {
		URI location = new URI("http://www.example.com/hotels");
		headers.setLocation(location);
		assertEquals("Invalid Location header", location, headers.getLocation());
		assertEquals("Invalid Location header", "http://www.example.com/hotels", headers.getFirst("Location"));
	}

	@SmallTest
	public void testETag() {
		String eTag = "\"v2.6\"";
		headers.setETag(eTag);
		assertEquals("Invalid ETag header", eTag, headers.getETag());
		assertEquals("Invalid ETag header", "\"v2.6\"", headers.getFirst("ETag"));
	}

	@SmallTest
	public void testIllegalETag() {
		boolean success = false;
		try {
			String eTag = "v2.6";
			headers.setETag(eTag);
			assertEquals("Invalid ETag header", eTag, headers.getETag());
			assertEquals("Invalid ETag header", "\"v2.6\"", headers.getFirst("ETag"));
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testIfNoneMatch() {
		String ifNoneMatch = "\"v2.6\"";
		headers.setIfNoneMatch(ifNoneMatch);
		assertEquals("Invalid If-None-Match header", ifNoneMatch, headers.getIfNoneMatch().get(0));
		assertEquals("Invalid If-None-Match header", "\"v2.6\"", headers.getFirst("If-None-Match"));
	}

	@SmallTest
	public void testIfNoneMatchList() {
		String ifNoneMatch1 = "\"v2.6\"";
		String ifNoneMatch2 = "\"v2.7\"";
		List<String> ifNoneMatchList = new ArrayList<String>(2);
		ifNoneMatchList.add(ifNoneMatch1);
		ifNoneMatchList.add(ifNoneMatch2);
		headers.setIfNoneMatch(ifNoneMatchList);
		assertEquals("Invalid If-None-Match header", ifNoneMatchList, headers.getIfNoneMatch());
		assertEquals("Invalid If-None-Match header", "\"v2.6\", \"v2.7\"", headers.getFirst("If-None-Match"));
	}

	@SmallTest
	public void testDate() {
		Calendar calendar = new GregorianCalendar(2008, 11, 18, 11, 20);
		calendar.setTimeZone(TimeZone.getTimeZone("CET"));
		long date = calendar.getTimeInMillis();
		headers.setDate(date);
		assertEquals("Invalid Date header", date, headers.getDate());
		assertEquals("Invalid Date header", "Thu, 18 Dec 2008 10:20:00 GMT+00:00", headers.getFirst("date"));

		// RFC 850
		headers.set("Date", "Thursday, 18-Dec-08 11:20:00 GMT+01:00");
		assertEquals("Invalid Date header", date, headers.getDate());
	}

	@SmallTest
	public void testDateInvalid() {
		boolean success = false;
		try {
			headers.set("Date", "Foo Bar Baz");
			headers.getDate();
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testDateOtherLocale() {
		Locale defaultLocale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("nl", "nl"));
			Calendar calendar = new GregorianCalendar(2008, 11, 18, 11, 20);
			calendar.setTimeZone(TimeZone.getTimeZone("CET"));
			long date = calendar.getTimeInMillis();
			headers.setDate(date);
			assertEquals("Invalid Date header", "Thu, 18 Dec 2008 10:20:00 GMT+00:00", headers.getFirst("date"));
			assertEquals("Invalid Date header", date, headers.getDate());
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

	@SmallTest
	public void testLastModified() {
		Calendar calendar = new GregorianCalendar(2008, 11, 18, 11, 20);
		calendar.setTimeZone(TimeZone.getTimeZone("CET"));
		long date = calendar.getTimeInMillis();
		headers.setLastModified(date);
		assertEquals("Invalid Last-Modified header", date, headers.getLastModified());
		assertEquals("Invalid Last-Modified header", "Thu, 18 Dec 2008 10:20:00 GMT+00:00", headers.getFirst("last-modified"));
	}

	@SmallTest
	public void testExpires() {
		Calendar calendar = new GregorianCalendar(2008, 11, 18, 11, 20);
		calendar.setTimeZone(TimeZone.getTimeZone("CET"));
		long date = calendar.getTimeInMillis();
		headers.setExpires(date);
		assertEquals("Invalid Expires header", date, headers.getExpires());
		assertEquals("Invalid Expires header", "Thu, 18 Dec 2008 10:20:00 GMT+00:00", headers.getFirst("expires"));
	}

	@SmallTest
	public void testIfModifiedSince() {
		Calendar calendar = new GregorianCalendar(2008, 11, 18, 11, 20);
		calendar.setTimeZone(TimeZone.getTimeZone("CET"));
		long date = calendar.getTimeInMillis();
		headers.setIfModifiedSince(date);
		assertEquals("Invalid If-Modified-Since header", date, headers.getIfNotModifiedSince());
		assertEquals("Invalid If-Modified-Since header", "Thu, 18 Dec 2008 10:20:00 GMT+00:00", headers.getFirst("if-modified-since"));
	}

	@SmallTest
	public void testPragma() {
		String pragma = "no-cache";
		headers.setPragma(pragma);
		assertEquals("Invalid Pragma header", pragma, headers.getPragma());
		assertEquals("Invalid Pragma header", "no-cache", headers.getFirst("pragma"));
	}

	@SmallTest
	public void testCacheControl() {
		String cacheControl = "no-cache";
		headers.setCacheControl(cacheControl);
		assertEquals("Invalid Cache-Control header", cacheControl, headers.getCacheControl());
		assertEquals("Invalid Cache-Control header", "no-cache", headers.getFirst("cache-control"));
	}

	@SmallTest
	public void testContentDisposition() {
		headers.setContentDispositionFormData("name", null);
		assertEquals("Invalid Content-Disposition header", "form-data; name=\"name\"", headers.getFirst("Content-Disposition"));

		headers.setContentDispositionFormData("name", "filename");
		assertEquals("Invalid Content-Disposition header", "form-data; name=\"name\"; filename=\"filename\"", headers.getFirst("Content-Disposition"));
	}

	@SmallTest
	public void testAcceptEncodingList() {
		ContentCodingType encodingType1 = new ContentCodingType("*");
		ContentCodingType encodingType2 = new ContentCodingType("gzip", 0.7);
		ContentCodingType encodingType3 = new ContentCodingType("identity", 0.5);
		List<ContentCodingType> encodingTypes = new ArrayList<ContentCodingType>(3);
		encodingTypes.add(encodingType1);
		encodingTypes.add(encodingType2);
		encodingTypes.add(encodingType3);
		headers.setAcceptEncoding(encodingTypes);
		assertEquals("Invalid Accept-Encoding header", encodingTypes, headers.getAcceptEncoding());
		assertEquals("Invalid Accept-Encoding header", "*, gzip;q=0.7, identity;q=0.5", headers.getFirst("Accept-Encoding"));
	}

	@SmallTest
	public void testAcceptEncodingSingle() {
		ContentCodingType encodingType = new ContentCodingType("gzip");
		headers.setAcceptEncoding(encodingType);
		assertEquals("Invalid Accept-Encoding header", encodingType, headers.getAcceptEncoding().get(0));
		assertEquals("Invalid Accept-Encoding header", "gzip", headers.getFirst("Accept-Encoding"));
	}

	@SmallTest
	public void testContentEncodingList() {
		ContentCodingType encodingType1 = new ContentCodingType("gzip");
		ContentCodingType encodingType2 = new ContentCodingType("identity");
		List<ContentCodingType> encodingTypes = new ArrayList<ContentCodingType>(2);
		encodingTypes.add(encodingType1);
		encodingTypes.add(encodingType2);
		headers.setContentEncoding(encodingTypes);
		assertEquals("Invalid Content-Encoding header", encodingTypes, headers.getContentEncoding());
		assertEquals("Invalid Content-Encoding header", "gzip, identity", headers.getFirst("Content-Encoding"));
	}

	@SmallTest
	public void testContentEncodingSingle() {
		ContentCodingType encodingType = new ContentCodingType("gzip");
		headers.setContentEncoding(encodingType);
		assertEquals("Invalid Content-Encoding header", encodingType, headers.getContentEncoding().get(0));
		assertEquals("Invalid Content-Encoding header", "gzip", headers.getFirst("Content-Encoding"));
	}


}
