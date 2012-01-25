/*
 * Copyright 2002-2012 the original author or authors.
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

import org.apache.commons.codec.binary.Base64;

/**
 * Represents HTTP Basic Authentication. Intended for use
 * with {@link org.springframework.http.client.ClientHttpRequest}
 * and {@link org.springframework.web.client.RestTemplate}.
 * 
 * @author Jonathan Sweemer
 */
public class HttpBasicAuthentication extends HttpAuthentication {

	private final String username;
	private final String password;
	
	public HttpBasicAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	/**
	 * @return the value for the 'Authorization' HTTP header.
	 */
	public String getHeaderValue() {
		String value = null;
		byte[] bytes = String.format("%s:%s", username, password).getBytes();
		value = new String(Base64.encodeBase64(bytes));
		return String.format("Basic %s", value);
	}
	
	@Override
	public String toString() {
		String s = null;
		try {
			s = String.format("Authorization: %s", getHeaderValue());
		} catch (RuntimeException re) {
			return null;
		}
		return s;
	}

}
