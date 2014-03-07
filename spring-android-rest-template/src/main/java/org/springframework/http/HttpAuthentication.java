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

/**
 * Represents an abstract HTTP Authentication. Possible subclasses include 
 * representations of HTTP Basic Authentication and HTTP Digest Authentication.
 * @see <a href="http://www.ietf.org/rfc/rfc2617.txt">RFC2617</a>
 * @author Jonathan Sweemer
 * @author Roy Clarkson
 * @since 1.0
 */
public abstract class HttpAuthentication {

	/**
	 * @return the value for the 'Authorization' HTTP header.
	 */
	public abstract String getHeaderValue();

}
