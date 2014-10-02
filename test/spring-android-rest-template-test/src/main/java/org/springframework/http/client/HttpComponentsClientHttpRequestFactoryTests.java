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

package org.springframework.http.client;

import java.net.URI;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;

public class HttpComponentsClientHttpRequestFactoryTests extends HttpComponentsAbstractHttpRequestFactoryTests {

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory();
	}

	public void createHttpUriRequest() throws Exception {
		URI uri = new URI("http://example.com");
		confirmRequestBodyAllowed(uri, HttpMethod.GET, false);
		confirmRequestBodyAllowed(uri, HttpMethod.HEAD, false);
		confirmRequestBodyAllowed(uri, HttpMethod.OPTIONS, false);
		confirmRequestBodyAllowed(uri, HttpMethod.TRACE, false);
		confirmRequestBodyAllowed(uri, HttpMethod.PUT, true);
		confirmRequestBodyAllowed(uri, HttpMethod.POST, true);
		confirmRequestBodyAllowed(uri, HttpMethod.PATCH, true);
		confirmRequestBodyAllowed(uri, HttpMethod.DELETE, true);
	}

	private void confirmRequestBodyAllowed(URI uri, HttpMethod method, boolean allowed) {
		HttpUriRequest request = ((HttpComponentsClientHttpRequestFactory) this.factory).createHttpUriRequest(method, uri);
		assertEquals(allowed, request instanceof HttpEntityEnclosingRequest);
	}

}
