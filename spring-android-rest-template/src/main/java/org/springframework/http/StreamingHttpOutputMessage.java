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

package org.springframework.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a HTTP output message that allows for setting a streaming body.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface StreamingHttpOutputMessage extends HttpOutputMessage {

	/**
	 * Sets the streaming body for this message.
	 * @param body the streaming body
	 */
	void setBody(Body body);

	/**
	 * Defines the contract for bodies that can be written directly to a
	 * {@link OutputStream}. It is useful with HTTP client libraries that provide indirect
	 * access to an {@link OutputStream} via a callback mechanism.
	 */
	public interface Body {

		/**
		 * Writes this body to the given {@link OutputStream}.
		 * @param outputStream the output stream to write to
		 * @throws IOException in case of errors
		 */
		void writeTo(OutputStream outputStream) throws IOException;

	}

}
