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
package org.springframework.social.twitter.api.impl;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.social.ApiException;
import org.springframework.social.InternalServerErrorException;
import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.RevokedAuthorizationException;
import org.springframework.social.ServerDownException;
import org.springframework.social.ServerOverloadedException;

import android.test.suitebuilder.annotation.MediumTest;

public class ApiErrorTest extends AbstractTwitterApiTest {

//	@MediumTest
//	public void testBadOrMissingAccessToken() {
//		boolean result = false;
//		try {
//			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
//				.andExpect(method(POST))
//				.andExpect(body("status=Some+message"))
//				.andRespond(withResponse("", responseHeaders, HttpStatus.UNAUTHORIZED, ""));
//			twitter.timelineOperations().updateStatus("Some message");
//		} catch(NotAuthorizedException e) {
//			result = true;
//		}
//		assertTrue("Expected BadCredentialsException", result);
//	}
	
	@MediumTest
	public void missingAccessToken() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("error-no-token.json", ApiErrorTest.class), responseHeaders, HttpStatus.UNAUTHORIZED, ""));
			unauthorizedTwitter.userOperations().getUserProfile();
		} catch (MissingAuthorizationException e) {
			success = true;
		}
		assertTrue("Expected MissingAuthorizationException", success);
	}
	
	@MediumTest
	public void badAccessToken() { // token is fabricated or fails signature validation
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("error-invalid-token.json", ApiErrorTest.class), responseHeaders, HttpStatus.UNAUTHORIZED, ""));
			twitter.userOperations().getUserProfile();
		} catch (InvalidAuthorizationException e) {
			success = true;
		}
		assertTrue("Expected InvalidAuthorizationException", success);
	}
	
	@MediumTest
	public void revokedToken() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("error-revoked-token.json", ApiErrorTest.class), responseHeaders, HttpStatus.UNAUTHORIZED, ""));
			twitter.userOperations().getUserProfile();
		} catch (RevokedAuthorizationException e) {
			success = true;
		}
		assertTrue("Expected RevokedAuthorizationException", success);
	}
	
	@MediumTest
	public void testEnhanceYourCalm() {
		boolean result = false;
		try {
			mockServer.expect(requestTo("https://search.twitter.com/search.json?q=%23spring&rpp=50&page=1"))
				.andExpect(method(GET))
				.andRespond(withResponse("{\"error\":\"You have been rate limited. Enhance your calm.\"}", responseHeaders, HttpStatus.valueOf(420), ""));		
			twitter.searchOperations().search("#spring");
		} catch(RateLimitExceededException e) {
			result = true;
		}
		assertTrue("Expected RateLimitException", result);
	}

	@MediumTest
	public void testTwitterIsBroken() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse("Non-JSON body", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, ""));
			twitter.timelineOperations().getHomeTimeline();
		} catch (InternalServerErrorException e) {
			success = true;
			assertEquals("Something is broken at Twitter. Please see http://dev.twitter.com/pages/support to report the issue.", e.getMessage());
		}
		assertTrue("Expected InternalServerErrorException", success);
	}
	
	@MediumTest
	public void testTwitterIsDownOrBeingUpgraded() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse("Non-JSON body", responseHeaders, HttpStatus.BAD_GATEWAY, ""));
			twitter.timelineOperations().getHomeTimeline();
		} catch (ServerDownException e) {
			success = true;
			assertEquals("Twitter is down or is being upgraded.", e.getMessage());
		}
		assertTrue("Expected ServerDownException", success);
	}
	
	@MediumTest
	public void testTwitterIsOverloaded() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse("Non-JSON body", responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, ""));
			twitter.timelineOperations().getHomeTimeline();
			fail();
		} catch (ServerOverloadedException e) {
			success = true;
			assertEquals("Twitter is overloaded with requests. Try again later.", e.getMessage());
		}
		assertTrue("Expected ServerOverloadedException", success);
	}

	@MediumTest
	public void testNonJSONErrorResponse() {
		boolean success = false;
		try { 
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse("<h1>HTML response</h1>", responseHeaders, HttpStatus.BAD_REQUEST, ""));
			twitter.timelineOperations().getHomeTimeline();
		} catch (ApiException e) {
			assertEquals("Error consuming Twitter REST API", e.getMessage());
			success = true;
		}
		assertTrue("Expected ApiException", success);
	}
	
//	@MediumTest
//	public void testUnparseableSuccessResponse() {
//		boolean success = false;
//		try {
//			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
//				.andExpect(method(GET))
//				.andRespond(withResponse("Unparseable {text}", responseHeaders, HttpStatus.OK, ""));
//			twitter.timelineOperations().getHomeTimeline();
//		} catch (ApiException e) {
//			success = true;
//		}
//		assertTrue("Expected ApiException", success);
//	}
}
