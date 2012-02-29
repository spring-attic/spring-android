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

package org.springframework.http.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;

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

}
