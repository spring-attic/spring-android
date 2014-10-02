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

package org.springframework.http.client.support;

import junit.framework.TestCase;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import android.test.suitebuilder.annotation.SmallTest;

public class HttpAccessorTests extends TestCase {

	private RestTemplate restTemplate;

	@Override
	protected void setUp() throws Exception {
		this.restTemplate = new RestTemplate();
	}

	@Override
	protected void tearDown() throws Exception {
		this.restTemplate = null;
	}

	@SmallTest
	public void testConstructor() {
		ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		assertTrue(factory instanceof HttpComponentsClientHttpRequestFactory);
	}

}
