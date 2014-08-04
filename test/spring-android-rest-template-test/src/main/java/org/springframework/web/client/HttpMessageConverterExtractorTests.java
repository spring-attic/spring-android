/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.web.client;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Test fixture for {@link HttpMessageConverter}.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
public class HttpMessageConverterExtractorTests extends TestCase {

	private HttpMessageConverterExtractor<?> extractor;

	private ClientHttpResponse response;

	@Override
	public void setUp() {
		response = mock(ClientHttpResponse.class);
	}

	public void testNoContent() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		extractor = new HttpMessageConverterExtractor<String>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.NO_CONTENT);

		Object result = extractor.extractData(response);

		assertNull(result);
	}

	public void testNotModified() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		extractor = new HttpMessageConverterExtractor<String>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.NOT_MODIFIED);

		Object result = extractor.extractData(response);

		assertNull(result);
	}

	public void testZeroContentLength() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentLength(0);
		extractor = new HttpMessageConverterExtractor<String>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);

		Object result = extractor.extractData(response);

		assertNull(result);
	}

	@SuppressWarnings("unchecked")
	public void testNormal() throws IOException {
		HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		String expected = "Foo";
		extractor = new HttpMessageConverterExtractor<String>(String.class, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(String.class, contentType)).willReturn(true);
		given(converter.read(String.class, response)).willReturn(expected);

		Object result = extractor.extractData(response);

		assertEquals(expected, result);
	}

	@SuppressWarnings("unchecked")
	public void testCannotRead() throws IOException {
		HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		extractor = new HttpMessageConverterExtractor<String>(String.class, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(String.class, contentType)).willReturn(false);

		try {
			extractor.extractData(response);
			fail("RestClientException expected");
		}
		catch (RestClientException expected) {
			// expected
		}
	}

	@SuppressWarnings("unchecked")
	public void testGenerics() throws IOException {
		GenericHttpMessageConverter<String> converter = mock(GenericHttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = createConverterList(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		String expected = "Foo";
		ParameterizedTypeReference<List<String>> reference = new ParameterizedTypeReference<List<String>>() {};
		Type type = reference.getType();
		extractor = new HttpMessageConverterExtractor<List<String>>(type, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(type, null, contentType)).willReturn(true);
		given(converter.read(type, null, response)).willReturn(expected);

		Object result = extractor.extractData(response);

		assertEquals(expected, result);
	}

	private List<HttpMessageConverter<?>> createConverterList(HttpMessageConverter<?> converter) {
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(1);
		converters.add(converter);
		return converters;
	}

}
