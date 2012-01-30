package org.springframework.http.client.support;

import junit.framework.TestCase;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import android.os.Build;
import android.test.suitebuilder.annotation.SmallTest;

public class HttpAccessorTests extends TestCase {
    
    private RestTemplate restTemplate;
    
    @Override
    protected void setUp() throws Exception {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    protected void tearDown() throws Exception {
        this.restTemplate = null;
    }
    
    @SmallTest
    public void testConstructor() {
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            assertTrue(factory instanceof SimpleClientHttpRequestFactory);
        } else {
            assertTrue(factory instanceof HttpComponentsClientHttpRequestFactory);
        }
    }

}
