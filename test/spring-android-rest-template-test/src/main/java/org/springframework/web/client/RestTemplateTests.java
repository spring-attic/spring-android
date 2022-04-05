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

package org.springframework.web.client;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * @author Arjen Poutsma
 * @author Roy Clarkson
 */
@SuppressWarnings("unchecked")
public class RestTemplateTests extends TestCase {

	private RestTemplate template;

	private ClientHttpRequestFactory requestFactory;

	private ClientHttpRequest request;

	private ClientHttpResponse response;

	private ResponseErrorHandler errorHandler;

	private HttpMessageConverter converter;

	@Override
	public void setUp() {
		requestFactory = mock(ClientHttpRequestFactory.class);
		request = mock(ClientHttpRequest.class);
		response = mock(ClientHttpResponse.class);
		errorHandler = mock(ResponseErrorHandler.class);
		converter = mock(HttpMessageConverter.class);
		template = new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
		template.setRequestFactory(requestFactory);
		template.setErrorHandler(errorHandler);
	}

	public void testVarArgsTemplateVariables() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com/hotels/42/bookings/21"), HttpMethod.GET))
				.willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.execute("https://example.com/hotels/{hotel}/bookings/{booking}", HttpMethod.GET, null, null, "42",
				"21");

		verify(response).close();
	}

	public void testVarArgsNullTemplateVariable() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com/-foo"), HttpMethod.GET))
				.willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.execute("https://example.com/{first}-{last}", HttpMethod.GET, null, null, null, "foo");

		verify(response).close();
	}

	public void testMapTemplateVariables() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com/hotels/42/bookings/42"), HttpMethod.GET))
				.willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		Map<String, String> vars = Collections.singletonMap("hotel", "42");
		template.execute("https://example.com/hotels/{hotel}/bookings/{hotel}", HttpMethod.GET, null, null, vars);

		verify(response).close();
	}

	public void testMapNullTemplateVariable() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com/-foo"), HttpMethod.GET))
				.willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		Map<String, String> vars = new HashMap<String, String>(2);
		vars.put("first", null);
		vars.put("last", "foo");
		template.execute("https://example.com/{first}-{last}", HttpMethod.GET, null, null, vars);

		verify(response).close();
	}

	public void testErrorHandling() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET)).willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(true);
		given(response.getStatusCode()).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
		given(response.getStatusText()).willReturn("Internal Server Error");
		willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).given(errorHandler).handleError(response);

		try {
			template.execute("https://example.com", HttpMethod.GET, null, null);
			fail("HttpServerErrorException expected");
		}
		catch (HttpServerErrorException ex) {
			// expected
		}

		verify(response).close();
	}

	public void testGetForObject() throws Exception {
		given(converter.canRead(String.class, null)).willReturn(true);
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(String.class, textPlain)).willReturn(true);
		String expected = "Hello World";
		given(converter.read(String.class, response)).willReturn(expected);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		String result = template.getForObject("https://example.com", String.class);
		assertEquals("Invalid GET result", expected, result);
		assertEquals("Invalid Accept header", textPlain.toString(), requestHeaders.getFirst("Accept"));

		verify(response).close();
	}

	public void testGetUnsupportedMediaType() throws Exception {
		given(converter.canRead(String.class, null)).willReturn(true);
		MediaType supportedMediaType = new MediaType("foo", "bar");
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(supportedMediaType));
		given(requestFactory.createRequest(new URI("https://example.com/resource"), HttpMethod.GET)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = new MediaType("bar", "baz");
		responseHeaders.setContentType(contentType);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(String.class, contentType)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		try {
			template.getForObject("https://example.com/{p}", String.class, "resource");
			fail("UnsupportedMediaTypeException expected");
		}
		catch (RestClientException ex) {
			// expected
		}

		verify(response).close();
	}

	public void testGetForEntity() throws Exception {
		given(converter.canRead(String.class, null)).willReturn(true);
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.GET)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(String.class, textPlain)).willReturn(true);
		String expected = "Hello World";
		given(converter.read(String.class, response)).willReturn(expected);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		ResponseEntity<String> result = template.getForEntity("https://example.com", String.class);
		assertEquals("Invalid GET result", expected, result.getBody());
		assertEquals("Invalid Accept header", textPlain.toString(), requestHeaders.getFirst("Accept"));
		assertEquals("Invalid Content-Type header", textPlain, result.getHeaders().getContentType());
		assertEquals("Invalid status code", HttpStatus.OK, result.getStatusCode());

		verify(response).close();
	}

	public void testHeadForHeaders() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.HEAD)).willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		HttpHeaders result = template.headForHeaders("https://example.com");

		assertSame("Invalid headers returned", responseHeaders, result);

		verify(response).close();
	}

	public void testPostForLocation() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		String helloWorld = "Hello World";
		given(converter.canWrite(String.class, null)).willReturn(true);
		converter.write(helloWorld, null, request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		URI expected = new URI("https://example.com/hotels");
		responseHeaders.setLocation(expected);
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		URI result = template.postForLocation("https://example.com", helloWorld);
		assertEquals("Invalid POST result", expected, result);

		verify(response).close();
	}

	public void testPostForLocationEntityContentType() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		String helloWorld = "Hello World";
		MediaType contentType = MediaType.TEXT_PLAIN;
		given(converter.canWrite(String.class, contentType)).willReturn(true);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		converter.write(helloWorld, contentType, request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		URI expected = new URI("https://example.com/hotels");
		responseHeaders.setLocation(expected);
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.setContentType(contentType);
		HttpEntity<String> entity = new HttpEntity<String>(helloWorld, entityHeaders);

		URI result = template.postForLocation("https://example.com", entity);
		assertEquals("Invalid POST result", expected, result);

		verify(response).close();
	}

	public void testPostForLocationEntityCustomHeader() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		String helloWorld = "Hello World";
		given(converter.canWrite(String.class, null)).willReturn(true);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		converter.write(helloWorld, null, request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		URI expected = new URI("https://example.com/hotels");
		responseHeaders.setLocation(expected);
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.set("MyHeader", "MyValue");
		HttpEntity<String> entity = new HttpEntity<String>(helloWorld, entityHeaders);

		URI result = template.postForLocation("https://example.com", entity);
		assertEquals("Invalid POST result", expected, result);
		assertEquals("No custom header set", "MyValue", requestHeaders.getFirst("MyHeader"));

		verify(response).close();
	}

	public void testPostForLocationNoLocation() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		String helloWorld = "Hello World";
		given(converter.canWrite(String.class, null)).willReturn(true);
		converter.write(helloWorld, null, request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		URI result = template.postForLocation("https://example.com", helloWorld);
		assertNull("Invalid POST result", result);

		verify(response).close();
	}

	public void testPostForLocationNull() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.postForLocation("https://example.com", null);
		assertEquals("Invalid content length", 0, requestHeaders.getContentLength());

		verify(response).close();
	}

	public void testPostForObject() throws Exception {
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.canRead(Integer.class, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(this.request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(this.request.getHeaders()).willReturn(requestHeaders);
		String request = "Hello World";
		given(converter.canWrite(String.class, null)).willReturn(true);
		converter.write(request, null, this.request);
		given(this.request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		Integer expected = 42;
		given(converter.canRead(Integer.class, textPlain)).willReturn(true);
		given(converter.read(Integer.class, response)).willReturn(expected);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		Integer result = template.postForObject("https://example.com", request, Integer.class);
		assertEquals("Invalid POST result", expected, result);
		assertEquals("Invalid Accept header", textPlain.toString(), requestHeaders.getFirst("Accept"));

		verify(response).close();
	}

	public void testPostForEntity() throws Exception {
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.canRead(Integer.class, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(this.request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(this.request.getHeaders()).willReturn(requestHeaders);
		String request = "Hello World";
		given(converter.canWrite(String.class, null)).willReturn(true);
		converter.write(request, null, this.request);
		given(this.request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		Integer expected = 42;
		given(converter.canRead(Integer.class, textPlain)).willReturn(true);
		given(converter.read(Integer.class, response)).willReturn(expected);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		ResponseEntity<Integer> result = template.postForEntity("https://example.com", request, Integer.class);
		assertEquals("Invalid POST result", expected, result.getBody());
		assertEquals("Invalid Content-Type", textPlain, result.getHeaders().getContentType());
		assertEquals("Invalid Accept header", textPlain.toString(), requestHeaders.getFirst("Accept"));
		assertEquals("Invalid status code", HttpStatus.OK, result.getStatusCode());

		verify(response).close();
	}

	public void testPostForObjectNull() throws Exception {
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.canRead(Integer.class, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(Integer.class, textPlain)).willReturn(true);
		given(converter.read(Integer.class, response)).willReturn(null);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		Integer result = template.postForObject("https://example.com", null, Integer.class);
		assertNull("Invalid POST result", result);
		assertEquals("Invalid content length", 0, requestHeaders.getContentLength());

		verify(response).close();
	}

	public void testPostForEntityNull() throws Exception {
		MediaType textPlain = new MediaType("text", "plain");
		given(converter.canRead(Integer.class, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(textPlain));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(textPlain);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(converter.canRead(Integer.class, textPlain)).willReturn(true);
		given(converter.read(Integer.class, response)).willReturn(null);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		ResponseEntity<Integer> result = template.postForEntity("https://example.com", null, Integer.class);
		assertFalse("Invalid POST result", result.hasBody());
		assertEquals("Invalid Content-Type", textPlain, result.getHeaders().getContentType());
		assertEquals("Invalid content length", 0, requestHeaders.getContentLength());
		assertEquals("Invalid status code", HttpStatus.OK, result.getStatusCode());

		verify(response).close();
	}

	public void testPut() throws Exception {
		given(converter.canWrite(String.class, null)).willReturn(true);
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.PUT)).willReturn(request);
		String helloWorld = "Hello World";
		converter.write(helloWorld, null, request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.put("https://example.com", helloWorld);

		verify(response).close();
	}

	public void testPutNull() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.PUT)).willReturn(request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(request.getHeaders()).willReturn(requestHeaders);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.put("https://example.com", null);
		assertEquals("Invalid content length", 0, requestHeaders.getContentLength());

		verify(response).close();
	}

	public void testDelete() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.DELETE)).willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		template.delete("https://example.com");

		verify(response).close();
	}

	public void testPptionsForAllow() throws Exception {
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.OPTIONS)).willReturn(request);
		given(request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		EnumSet<HttpMethod> expected = EnumSet.of(HttpMethod.GET, HttpMethod.POST);
		responseHeaders.setAllow(expected);
		given(response.getHeaders()).willReturn(responseHeaders);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		Set<HttpMethod> result = template.optionsForAllow("https://example.com");
		assertEquals("Invalid OPTIONS result", expected, result);

		verify(response).close();
	}

	public void testIoException() throws Exception {
		given(converter.canRead(String.class, null)).willReturn(true);
		MediaType mediaType = new MediaType("foo", "bar");
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(mediaType));
		given(requestFactory.createRequest(new URI("https://example.com/resource"), HttpMethod.GET)).willReturn(request);
		given(request.getHeaders()).willReturn(new HttpHeaders());
		given(request.execute()).willThrow(new IOException());

		try {
			template.getForObject("https://example.com/resource", String.class);
			fail("RestClientException expected");
		}
		catch (ResourceAccessException ex) {
			// expected
		}
	}

	public void testExchange() throws Exception {
		given(converter.canRead(Integer.class, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(MediaType.TEXT_PLAIN));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(this.request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(this.request.getHeaders()).willReturn(requestHeaders);
		given(converter.canWrite(String.class, null)).willReturn(true);
		String body = "Hello World";
		converter.write(body, null, this.request);
		given(this.request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.TEXT_PLAIN);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		Integer expected = 42;
		given(converter.canRead(Integer.class, MediaType.TEXT_PLAIN)).willReturn(true);
		given(converter.read(Integer.class, response)).willReturn(expected);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.set("MyHeader", "MyValue");
		HttpEntity<String> requestEntity = new HttpEntity<String>(body, entityHeaders);
		ResponseEntity<Integer> result = template.exchange("https://example.com", HttpMethod.POST, requestEntity, Integer.class);
		assertEquals("Invalid POST result", expected, result.getBody());
		assertEquals("Invalid Content-Type", MediaType.TEXT_PLAIN, result.getHeaders().getContentType());
		assertEquals("Invalid Accept header", MediaType.TEXT_PLAIN_VALUE, requestHeaders.getFirst("Accept"));
		assertEquals("Invalid custom header", "MyValue", requestHeaders.getFirst("MyHeader"));
		assertEquals("Invalid status code", HttpStatus.OK, result.getStatusCode());

		verify(response).close();
	}

	public void testExchangeParameterizedType() throws Exception {
		GenericHttpMessageConverter converter = mock(GenericHttpMessageConverter.class);
		template.setMessageConverters(Collections.<HttpMessageConverter<?>>singletonList(converter));

		ParameterizedTypeReference<List<Integer>> intList = new ParameterizedTypeReference<List<Integer>>() {};
		given(converter.canRead(intList.getType(), null, null)).willReturn(true);
		given(converter.getSupportedMediaTypes()).willReturn(Collections.singletonList(MediaType.TEXT_PLAIN));
		given(requestFactory.createRequest(new URI("https://example.com"), HttpMethod.POST)).willReturn(this.request);
		HttpHeaders requestHeaders = new HttpHeaders();
		given(this.request.getHeaders()).willReturn(requestHeaders);
		given(converter.canWrite(String.class, null)).willReturn(true);
		String requestBody = "Hello World";
		converter.write(requestBody, null, this.request);
		given(this.request.execute()).willReturn(response);
		given(errorHandler.hasError(response)).willReturn(false);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.TEXT_PLAIN);
		responseHeaders.setContentLength(10);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		List<Integer> expected = Collections.singletonList(42);
		given(converter.canRead(intList.getType(), null, MediaType.TEXT_PLAIN)).willReturn(true);
		given(converter.read(intList.getType(), null, response)).willReturn(expected);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		HttpStatus status = HttpStatus.OK;
		given(response.getStatusCode()).willReturn(status);
		given(response.getStatusText()).willReturn(status.getReasonPhrase());

		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.set("MyHeader", "MyValue");
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody, entityHeaders);
		ResponseEntity<List<Integer>> result = template.exchange("https://example.com", HttpMethod.POST, requestEntity, intList);
		assertEquals("Invalid POST result", expected, result.getBody());
		assertEquals("Invalid Content-Type", MediaType.TEXT_PLAIN, result.getHeaders().getContentType());
		assertEquals("Invalid Accept header", MediaType.TEXT_PLAIN_VALUE, requestHeaders.getFirst("Accept"));
		assertEquals("Invalid custom header", "MyValue", requestHeaders.getFirst("MyHeader"));
		assertEquals("Invalid status code", HttpStatus.OK, result.getStatusCode());

		verify(response).close();
	}
}
