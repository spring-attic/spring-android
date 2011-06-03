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
package org.springframework.social.oauth1;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class SigningSupportTest extends AndroidTestCase {
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
	}

	@SmallTest
	public void testBuildAuthorizationHeaderValue_URI() throws Exception {
		SigningSupport signingUtils = new SigningSupport();
		signingUtils.setTimestampGenerator(new MockTimestampGenerator(123456789, 987654321));
		Map<String, String> oauthParameters = signingUtils.commonOAuthParameters("9djdj82h48djs9d2");
		oauthParameters.put("oauth_token", "kkk9d7dh3k39sjv7");
		LinkedMultiValueMap<String, String> additionalParameters = new LinkedMultiValueMap<String, String>();
		additionalParameters.add("c2", ""); // body parameter
		additionalParameters.add("a3", "2 q"); // body parameter
		additionalParameters.add("b5", "=%3D"); // query parameter
		additionalParameters.add("a3", "a"); // query parameter
		additionalParameters.add("c@", ""); // query parameter
		additionalParameters.add("a2", "r b"); // query parameter
		String authorizationHeader = signingUtils.buildAuthorizationHeaderValue(HttpMethod.POST, new URI("http://example.com/request"), oauthParameters, additionalParameters, "consumer_secret", "token_secret");
		assertAuthorizationHeader(authorizationHeader, "qz6HT3AG1Z9J%2BP99O4HeMtClGeY%3D");
	}

	@SmallTest
	public void testBuildAuthorizationHeaderValue_Request() throws Exception {
		SigningSupport signingUtils = new SigningSupport();
		signingUtils.setTimestampGenerator(new MockTimestampGenerator(123456789, 987654321));
		URI uri = URIBuilder.fromUri("http://example.com/request").queryParam("b5", "=%3D").queryParam("a3", "a").queryParam("c@", "")
			.queryParam("a2", "r b").build();
		HttpRequest request = new SimpleClientHttpRequestFactory().createRequest(uri, HttpMethod.POST);
		request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String authorizationHeader = signingUtils.buildAuthorizationHeaderValue(request, "c2&a3=2+q".getBytes(), new OAuth1Credentials("9djdj82h48djs9d2", "consumer_secret", "kkk9d7dh3k39sjv7", "token_secret"));
		assertAuthorizationHeader(authorizationHeader, "qz6HT3AG1Z9J%2BP99O4HeMtClGeY%3D");
	}
	
	@SmallTest
	public void testSpring30buildAuthorizationHeaderValue() throws Exception {
		SigningSupport signingUtils = new SigningSupport();
		signingUtils.setTimestampGenerator(new MockTimestampGenerator(123456789, 987654321));
		URI uri = URIBuilder.fromUri("http://example.com/request").queryParam("b5", "=%3D").queryParam("a3", "a").queryParam("c@", "")
			.queryParam("a2", "r b").build();
		ClientHttpRequest request = new SimpleClientHttpRequestFactory().createRequest(uri, HttpMethod.POST);
		request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String authorizationHeader = signingUtils.spring30buildAuthorizationHeaderValue(request, "c2&a3=2+q".getBytes(), new OAuth1Credentials("9djdj82h48djs9d2", "consumer_secret", "kkk9d7dh3k39sjv7", "token_secret"));
		assertAuthorizationHeader(authorizationHeader, "qz6HT3AG1Z9J%2BP99O4HeMtClGeY%3D");
	}

	private void assertAuthorizationHeader(String authorizationHeader, String expectedSignature) {
		String[] headerElements = authorizationHeader.split(", ");
		assertEquals("OAuth oauth_version=\"1.0\"", headerElements[0]);
		assertEquals("oauth_nonce=\"987654321\"", headerElements[1]);
		assertEquals("oauth_signature_method=\"HMAC-SHA1\"", headerElements[2]);
		assertEquals("oauth_consumer_key=\"9djdj82h48djs9d2\"", headerElements[3]);
		assertEquals("oauth_token=\"kkk9d7dh3k39sjv7\"", headerElements[4]);
		assertEquals("oauth_timestamp=\"123456789\"", headerElements[5]);
		assertEquals("oauth_signature=\""+expectedSignature+"\"", headerElements[6]);
	}

	/*
	 * Tests the buildBaseString() method using the example given in the OAuth 1 spec
	 * at http://tools.ietf.org/html/rfc5849#section-3.4.1 as the test data.
	 */
	@SmallTest
	public void testBuildBaseString_specificationExample() {
		SigningSupport signingUtils = new SigningSupport();
		signingUtils.setTimestampGenerator(new MockTimestampGenerator(2468013579L, 1357924680));
		Map<String, String> oauthParameters = signingUtils.commonOAuthParameters("9djdj82h48djs9d2");
		oauthParameters.put("oauth_token", "kkk9d7dh3k39sjv7");
		LinkedMultiValueMap<String, String> collectedParameters = new LinkedMultiValueMap<String, String>();
		collectedParameters.add("b5", "=%3D");
		collectedParameters.add("a3", "a");
		collectedParameters.add("c@", "");
		collectedParameters.add("a2", "r b");
		collectedParameters.add("c2", "");
		collectedParameters.add("a3", "2 q");
		collectedParameters.setAll(oauthParameters);
		String baseString = signingUtils.buildBaseString(HttpMethod.POST, "http://example.com/request", collectedParameters);
		
		String[] baseStringParts = baseString.split("&");
		assertEquals(3, baseStringParts.length);
		assertEquals("POST", baseStringParts[0]);
		assertEquals("http%3A%2F%2Fexample.com%2Frequest", baseStringParts[1]);
			
		String[] parameterParts = baseStringParts[2].split("%26");
		assertEquals(12, parameterParts.length);
		assertEquals("a2%3Dr%2520b", parameterParts[0]);
		assertEquals("a3%3D2%2520q", parameterParts[1]);
		assertEquals("a3%3Da", parameterParts[2]);
		assertEquals("b5%3D%253D%25253D", parameterParts[3]);
		assertEquals("c%2540%3D", parameterParts[4]);
		assertEquals("c2%3D", parameterParts[5]);
		assertEquals("oauth_consumer_key%3D9djdj82h48djs9d2", parameterParts[6]);
		assertEquals("oauth_nonce%3D1357924680", parameterParts[7]);
		assertEquals("oauth_signature_method%3DHMAC-SHA1", parameterParts[8]);
		assertEquals("oauth_timestamp%3D2468013579", parameterParts[9]);
		assertEquals("oauth_token%3Dkkk9d7dh3k39sjv7", parameterParts[10]);
		assertEquals("oauth_version%3D1.0", parameterParts[11]);
	}
	
	/*
	 * Tests the buildBaseString() method using the example given at http://dev.twitter.com/pages/auth#signing-requests
	 * as the test data.
	 */
	@SmallTest
	public void testBuildBaseString_twitterExample() {
		SigningSupport signingUtils = new SigningSupport();
		signingUtils.setTimestampGenerator(new MockTimestampGenerator(2468013579L, 1357924680));
		Map<String, String> oauthParameters = signingUtils.commonOAuthParameters("GDdmIQH6jhtmLUypg82g");
		oauthParameters.put("oauth_callback", "http://localhost:3005/the_dance/process_callback?service_provider_id=11");
		LinkedMultiValueMap<String, String> collectedParameters = new LinkedMultiValueMap<String, String>();
		collectedParameters.setAll(oauthParameters);
		String baseString = signingUtils.buildBaseString(HttpMethod.POST, "https://api.twitter.com/oauth/request_token", collectedParameters);
		
		String[] baseStringParts = baseString.split("&");
		assertEquals(3, baseStringParts.length);
		assertEquals("POST", baseStringParts[0]);
		assertEquals("https%3A%2F%2Fapi.twitter.com%2Foauth%2Frequest_token", baseStringParts[1]);
		
		String[] parameterParts = baseStringParts[2].split("%26");
		assertEquals(6, parameterParts.length);
		assertEquals("oauth_callback%3Dhttp%253A%252F%252Flocalhost%253A3005%252Fthe_dance%252Fprocess_callback%253Fservice_provider_id%253D11", parameterParts[0]);
		assertEquals("oauth_consumer_key%3DGDdmIQH6jhtmLUypg82g", parameterParts[1]);
		assertEquals("oauth_nonce%3D1357924680", parameterParts[2]);
		assertEquals("oauth_signature_method%3DHMAC-SHA1", parameterParts[3]);
		assertEquals("oauth_timestamp%3D2468013579", parameterParts[4]);
		assertEquals("oauth_version%3D1.0", parameterParts[5]);
	}
}
