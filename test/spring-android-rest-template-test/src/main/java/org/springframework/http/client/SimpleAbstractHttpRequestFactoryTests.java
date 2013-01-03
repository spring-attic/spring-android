/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;

import android.os.Build;
import android.test.suitebuilder.annotation.MediumTest;

public abstract class SimpleAbstractHttpRequestFactoryTests extends AbstractHttpRequestFactoryTestCase {

	// SPR-8809
	@MediumTest
	public void testInterceptor() throws Exception {
		final String headerName = "MyHeader";
		final String headerValue = "MyValue";
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				request.getHeaders().add(headerName, headerValue);
				return execution.execute(request, body);
			}
		};
		InterceptingClientHttpRequestFactory factory = new InterceptingClientHttpRequestFactory(createRequestFactory(), Collections.singletonList(interceptor));

		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.GET);
			response = request.execute();
			assertEquals("Invalid response status", HttpStatus.OK, response.getStatusCode());
			HttpHeaders responseHeaders = response.getHeaders();
			assertEquals("Custom header invalid", headerValue, responseHeaders.getFirst(headerName));
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	@MediumTest
	public void testGetAcceptEncodingNone() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/gzip"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertFalse("Header found", response.getHeaders().containsKey("Content-Encoding"));
			byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(body.length);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			FileCopyUtils.copy(body, gzipOutputStream);
			byte[] compressedBody = byteArrayOutputStream.toByteArray();
			long contentLength = response.getHeaders().getContentLength();
			// Gingerbread and newer seamlessly request and handle gzip responses from the server 
			if (Build.VERSION.SDK_INT == 17) {
				// content-length is not being set in Jelly Bean 4.2!!
				assertEquals(-1, contentLength);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				assertEquals("Invalid content-length", compressedBody.length, contentLength);
			} else {
				assertEquals("Invalid content-length", body.length, contentLength);
			}
		} finally {
			response.close();
		}
	}

}
