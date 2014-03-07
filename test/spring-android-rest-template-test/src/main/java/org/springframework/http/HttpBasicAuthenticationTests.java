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

package org.springframework.http;

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.util.Base64Utils;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Roy Clarkson
 */
public class HttpBasicAuthenticationTests extends TestCase {
	
	private HttpAuthentication authentication;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.authentication = new HttpBasicAuthentication("foo", "bar");
	}
	
	@Override
	public void tearDown() {
		this.authentication = null;
	}
	
	@SmallTest
	public void testGetHeaderValue() throws IOException {
		String credentials = "foo:bar";
		byte[] credentialsBytes = credentials.getBytes("UTF-8");
		String encodedCredentials = Base64Utils.encodeToString(credentialsBytes);
		String headerValue = "Basic " + encodedCredentials;
		assertEquals(headerValue, this.authentication.getHeaderValue());
	}
	
	@SmallTest
	public void testToString() throws IOException {
		String credentials = "foo:bar";
		byte[] credentialsBytes = credentials.getBytes("UTF-8");
		byte[] encodedCredentialsBytes = Base64Utils.encode(credentialsBytes);
		String encodedCredentials = new String(encodedCredentialsBytes, "UTF-8");
		String authValue = "Authorization: Basic " + encodedCredentials;
		assertEquals(authValue, this.authentication.toString());
	}
	
	@SmallTest
	public void testEncodedCredentials() throws IOException {
		String encodedCredentials = this.authentication.getHeaderValue().split(" ")[1];
		byte[] encodedCredentialBytes = encodedCredentials.getBytes("UTF-8");
		byte[] decodedCredentialBytes = Base64Utils.decode(encodedCredentialBytes);
		String decodedCredentials = new String(decodedCredentialBytes, "UTF-8");
		String[] credentials = decodedCredentials.split(":");
		assertEquals("foo", credentials[0]);
		assertEquals("bar", credentials[1]);
	}
}
