/*
 * Copyright 2011 the original author or authors.
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

import org.springframework.security.crypto.util.EncodingUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AndroidKeyGeneratorsTests extends AndroidTestCase {
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
	}

    @SmallTest
    public void testSecureRandom() {
        BytesKeyGenerator keyGenerator = AndroidKeyGenerators.secureRandom();
        assertEquals(8, keyGenerator.getKeyLength());
        byte[] key = keyGenerator.generateKey();
        assertEquals(8, key.length);
        byte[] key2 = keyGenerator.generateKey();
        assertFalse(Arrays.equals(key, key2));
    }

    @SmallTest
    public void testSecureRandomCustomLength() {
        BytesKeyGenerator keyGenerator = AndroidKeyGenerators.secureRandom(16);
        assertEquals(16, keyGenerator.getKeyLength());
        byte[] key = keyGenerator.generateKey();
        assertEquals(16, key.length);
        byte[] key2 = keyGenerator.generateKey();
        assertFalse(Arrays.equals(key, key2));
    }

    @SmallTest
    public void testString() {
        StringKeyGenerator keyGenerator = AndroidKeyGenerators.string();
        String hexStringKey = keyGenerator.generateKey();
        assertEquals(16, hexStringKey.length());
        assertEquals(8, EncodingUtils.hexDecode(hexStringKey).length);
        String hexStringKey2 = keyGenerator.generateKey();
        assertFalse(hexStringKey.equals(hexStringKey2));
    }

}
