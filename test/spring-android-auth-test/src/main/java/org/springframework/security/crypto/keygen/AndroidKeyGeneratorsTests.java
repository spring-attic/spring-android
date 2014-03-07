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

package org.springframework.security.crypto.keygen;

import java.util.Arrays;

import junit.framework.TestCase;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import android.test.suitebuilder.annotation.SmallTest;

public class AndroidKeyGeneratorsTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@SmallTest
	public void secureRandom() {
		BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom();
		assertEquals(8, keyGenerator.getKeyLength());
		byte[] key = keyGenerator.generateKey();
		assertEquals(8, key.length);
		byte[] key2 = keyGenerator.generateKey();
		assertFalse(Arrays.equals(key, key2));
	}

	@SmallTest
	public void secureRandomCustomLength() {
		BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom(21);
		assertEquals(21, keyGenerator.getKeyLength());
		byte[] key = keyGenerator.generateKey();
		assertEquals(21, key.length);
		byte[] key2 = keyGenerator.generateKey();
		assertFalse(Arrays.equals(key, key2));
	}

	@SmallTest
	public void shared() throws Exception {
		BytesKeyGenerator keyGenerator = KeyGenerators.shared(21);
		assertEquals(21, keyGenerator.getKeyLength());
		byte[] key = keyGenerator.generateKey();
		assertEquals(21, key.length);
		byte[] key2 = keyGenerator.generateKey();
		assertTrue(Arrays.equals(key, key2));
	}

	@SmallTest
	public void string() {
		StringKeyGenerator keyGenerator = KeyGenerators.string();
		String hexStringKey = keyGenerator.generateKey();
		assertEquals(16, hexStringKey.length());
		assertEquals(8, Hex.decode(hexStringKey).length);
		String hexStringKey2 = keyGenerator.generateKey();
		assertFalse(hexStringKey.equals(hexStringKey2));
	}

}
