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

import java.util.List;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.social.test.client.MockRestServiceServer;
import org.springframework.social.twitter.api.Tweet;

public abstract class AbstractTwitterApiTest extends TestCase {

	protected TwitterTemplate twitter;
	
	protected TwitterTemplate unauthorizedTwitter;

	protected MockRestServiceServer mockServer;

	protected HttpHeaders responseHeaders;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		twitter = new TwitterTemplate("API_KEY", "API_SECRET", "ACCESS_TOKEN", "ACCESS_TOKEN_SECRET");
		mockServer = MockRestServiceServer.createServer(twitter.getRestTemplate());
		responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		unauthorizedTwitter = new TwitterTemplate();
		 // create a mock server just to avoid hitting real twitter if something gets past the authorization check
		MockRestServiceServer.createServer(unauthorizedTwitter.getRestTemplate());
	}
	
	@Override
	public void tearDown() {
		twitter = null;
		mockServer = null;
		responseHeaders = null;
		unauthorizedTwitter = null;
	}

	protected Resource jsonResource(String filename) {
		return new ClassPathResource(filename + ".json", getClass());
	}

	protected void assertSingleTweet(Tweet tweet) {
		assertEquals(12345, tweet.getId());
		assertEquals("Tweet 1", tweet.getText());
		assertEquals("habuma", tweet.getFromUser());
		assertEquals(112233, tweet.getFromUserId());
		assertEquals("http://a3.twimg.com/profile_images/1205746571/me2_300.jpg", tweet.getProfileImageUrl());
		assertEquals("Spring Social Showcase", tweet.getSource());
		assertEquals(1279042701000L, tweet.getCreatedAt().getTime());		
	}
	
	protected void assertTimelineTweets(List<Tweet> tweets) {
		assertEquals(2, tweets.size());
		assertSingleTweet(tweets.get(0));
		Tweet tweet2 = tweets.get(1);
		assertEquals(54321, tweet2.getId());
		assertEquals("Tweet 2", tweet2.getText());
		assertEquals("rclarkson", tweet2.getFromUser());
		assertEquals(332211, tweet2.getFromUserId());
		assertEquals("http://a3.twimg.com/profile_images/1205746571/me2_300.jpg", tweet2.getProfileImageUrl());
		assertEquals("Twitter", tweet2.getSource());
		assertEquals(1279654701000L, tweet2.getCreatedAt().getTime());
	}
}
