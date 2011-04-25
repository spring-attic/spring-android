package org.springframework.security.crypto.encrypt;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AndroidEncryptorsTests extends AndroidTestCase {
	
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
