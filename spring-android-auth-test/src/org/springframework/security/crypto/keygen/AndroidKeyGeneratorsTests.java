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
	
    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {
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
