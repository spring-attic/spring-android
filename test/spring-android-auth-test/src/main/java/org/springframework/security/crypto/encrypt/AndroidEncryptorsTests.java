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

package org.springframework.security.crypto.encrypt;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AndroidEncryptorsTests extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@SmallTest
	public void testStandard() {
		BytesEncryptor encryptor = AndroidEncryptors.standard("password", "5c0744940b5c369b");
		byte[] result = encryptor.encrypt("text".getBytes());
		assertNotNull(result);
		assertFalse(new String(result).equals("text"));
		assertEquals("text", new String(encryptor.decrypt(result)));
		assertFalse(new String(result).equals(new String(encryptor.encrypt("text".getBytes()))));
	}

	@SmallTest
	public void testText() {
		TextEncryptor encryptor = AndroidEncryptors.text("password", "5c0744940b5c369b");
		String result = encryptor.encrypt("text");
		assertNotNull(result);
		assertFalse(result.equals("text"));
		assertEquals("text", encryptor.decrypt(result));
		assertFalse(result.equals(encryptor.encrypt("text")));
	}

	@SmallTest
	public void testQueryableText() {
		TextEncryptor encryptor = AndroidEncryptors.queryableText("password", "5c0744940b5c369b");
		String result = encryptor.encrypt("text");
		assertNotNull(result);
		assertFalse(result.equals("text"));
		assertEquals("text", encryptor.decrypt(result));
		assertTrue(result.equals(encryptor.encrypt("text")));
	}

	@SmallTest
	public void testNoOpText() {
		TextEncryptor encryptor = AndroidEncryptors.noOpText();
		assertEquals("text", encryptor.encrypt("text"));
		assertEquals("text", encryptor.decrypt("text"));
	}
}
