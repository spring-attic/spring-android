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
import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;

import android.os.Build;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

/**
 * @author Stéphane Nicolas
 * @author Roy Clarkson
 */
public class OkHttpClientHttpRequestFactoryTests extends AbstractHttpRequestFactoryTestCase {

	private static final String TAG = "OkHttpRequestFctryTests";


	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new OkHttpClientHttpRequestFactory();
	}

	@Override
	protected void setUp() throws Exception {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, "OkHttp is only supported on Android 2.3 and above");
			}
		} 
		else {
			super.setUp();
		}
	}

	@Override
	protected void runTest() throws Throwable {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			assertTrue(true);
		}
		else {
			super.runTest();
		}
	}

	@MediumTest
	@Override
	public void testGetAcceptEncodingNone() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/noencoding"), HttpMethod.GET);
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
			long contentLength = response.getHeaders().getContentLength();
			// OkHttp is not setting a content-length when the response is gzip encoded
			assertEquals(-1, contentLength);
		}
		finally {
			response.close();
		}
	}

}
