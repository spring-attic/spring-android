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

package org.springframework.web.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.springframework.http.HttpStatus;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for {@link HttpStatusCodeException} and subclasses.
 *
 * @author Chris Beams
 * @author Roy Clarkson
 */
public class HttpStatusCodeExceptionTests extends TestCase {

	/**
	 * Corners bug ANDROID-129 (SPR-9273), which reported the fact that following 
	 * the changes made in SPR-7591, {@link HttpStatusCodeException} and subtypes 
	 * became no longer serializable due to the addition of a non-serializable 
	 * {@link Charset} field.
	 */
	@SmallTest
	public void testSerializability() throws IOException, ClassNotFoundException {
		HttpStatusCodeException ex1 = new HttpClientErrorException(HttpStatus.BAD_REQUEST, null, null,
				Charset.forName("US-ASCII"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new ObjectOutputStream(out).writeObject(ex1);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		HttpStatusCodeException ex2 = (HttpStatusCodeException) new ObjectInputStream(in).readObject();
		assertEquals(ex2.getResponseBodyAsString(), ex1.getResponseBodyAsString());
	}

}
