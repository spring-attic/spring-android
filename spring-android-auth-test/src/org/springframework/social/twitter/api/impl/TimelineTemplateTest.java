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

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.social.test.client.RequestMatchers.body;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.social.twitter.api.DuplicateTweetException;
import org.springframework.social.twitter.api.MessageTooLongException;
import org.springframework.social.twitter.api.StatusDetails;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;


/**
 * @author Craig Walls
 * @author Roy Clarkson
 */
public class TimelineTemplateTest extends AbstractTwitterApiTest {

	@SmallTest
	public void testGetPublicTimeline() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/public_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getPublicTimeline();
		assertTimelineTweets(timeline);
	}

	@SmallTest
	public void testGetHomeTimeline() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/home_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getHomeTimeline();
		assertTimelineTweets(timeline);
	}
	
	@SmallTest
	public void testGetHomeTimeline_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getHomeTimeline();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testGetFriendsTimeline() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/friends_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getFriendsTimeline();
		assertTimelineTweets(timeline);
	}

	@SmallTest
	public void testGetFriendsTimeline_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getFriendsTimeline();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testGetUserTimeline() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/user_timeline.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getUserTimeline();
		assertTimelineTweets(timeline);
	}

	@SmallTest
	public void testGetUserTimeline_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getUserTimeline();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testGetUserTimeline_forScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/user_timeline.json?screen_name=habuma"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getUserTimeline("habuma");
		assertTimelineTweets(timeline);
	}

	@SmallTest
	public void testGetUserTimeline_forUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/user_timeline.json?user_id=12345"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getUserTimeline(12345);
		assertTimelineTweets(timeline);
	}

	@SmallTest
	public void testGetMentions() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/mentions.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> mentions = twitter.timelineOperations().getMentions();
		assertTimelineTweets(mentions);
	}

	@SmallTest
	public void testGetMentions_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getMentions();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testGetRetweetedByMe() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweeted_by_me.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getRetweetedByMe();
		assertTimelineTweets(timeline);		
	}

	@SmallTest
	public void testGetRetweetedByMe_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getRetweetedByMe();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testGetRetweetedToMe() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweeted_to_me.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getRetweetedToMe();
		assertTimelineTweets(timeline);				
	}
	
	@SmallTest
	public void testGetRetweetedToMe_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getRetweetedToMe();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
		
	@SmallTest
	public void testGetRetweetsOfMe() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweets_of_me.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getRetweetsOfMe();
		assertTimelineTweets(timeline);				
	}
	
	@SmallTest
	public void testGetRetweetsOfMe_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getRetweetsOfMe();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testGetStatus() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/show/12345.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("status.json", getClass()), responseHeaders));
		
		Tweet tweet = twitter.timelineOperations().getStatus(12345);
		assertSingleTweet(tweet);
	}
	
	@SmallTest
	public void testUpdateStatus() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
				.andExpect(method(POST))
				.andExpect(body("status=Test+Message"))
				.andRespond(withResponse("{}", responseHeaders));

		twitter.timelineOperations().updateStatus("Test Message");

		mockServer.verify();
	}

	@SmallTest
	public void testUpdateStatus_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().updateStatus("Shouldn't work");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testUpdateStatus_withLocation() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
				.andExpect(method(POST))
				.andExpect(body("status=Test+Message&lat=123.1&long=-111.2"))
				.andRespond(withResponse("{}", responseHeaders));

		StatusDetails details = new StatusDetails();
		details.setLocation(123.1f, -111.2f);
		twitter.timelineOperations().updateStatus("Test Message", details);

		mockServer.verify();
	}
	
	@SmallTest
	public void testUpdateStatus_withLocation_unauthorized() {
		boolean success = false;
		try {
			StatusDetails details = new StatusDetails();
			details.setLocation(123.1f, -111.2f);
			unauthorizedTwitter.timelineOperations().updateStatus("Test Message", details);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testUpdateStatus_duplicateTweet() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
				.andExpect(method(POST))
				.andExpect(body("status=Test+Message"))
				.andRespond(withResponse("{\"error\":\"You already said that\"}", responseHeaders, FORBIDDEN, ""));
			twitter.timelineOperations().updateStatus("Test Message");
		} catch (DuplicateTweetException e) {
			success = true;
		}
		assertTrue("Expected DuplicateTweetException", success);
	}
	
	@SmallTest
	public void testUpdateStatus_tweetTooLong() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
				.andExpect(method(POST))
				.andExpect(body("status=Really+long+message"))
				.andRespond(withResponse("{\"error\":\"Status is over 140 characters.\"}", responseHeaders, HttpStatus.FORBIDDEN, ""));
			twitter.timelineOperations().updateStatus("Really long message");
		} catch (MessageTooLongException e) {
			success = true;
		}
		assertTrue("Expected MessageTooLongException", success);
	}
	
	@SmallTest
	public void testUpdateStatus_forbidden() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/update.json"))
				.andExpect(method(POST))
				.andExpect(body("status=Test+Message"))
				.andRespond(withResponse("{\"error\":\"Forbidden\"}", responseHeaders, FORBIDDEN, ""));
			twitter.timelineOperations().updateStatus("Test Message");
		} catch (OperationNotPermittedException e) {
			success = true;
		}
		assertTrue("Expected OperationNotPermittedException", success);
	}

	@SmallTest
	public void testDeleteStatus() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/destroy/12345.json"))
			.andExpect(method(DELETE))
			.andRespond(withResponse("{}", responseHeaders));		
		twitter.timelineOperations().deleteStatus(12345L);
		mockServer.verify();
	}
	
	@SmallTest
	public void testDeleteStatus_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().deleteStatus(12345L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testRetweet() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweet/12345.json"))
				.andExpect(method(POST))
				.andRespond(withResponse("{}", responseHeaders));

		twitter.timelineOperations().retweet(12345);

		mockServer.verify();
	}
	
	@SmallTest
	public void testRetweet_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().retweet(12345L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testRetweet_duplicateTweet() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweet/12345.json"))
				.andExpect(method(POST))
				.andRespond(withResponse("{\"error\":\"You already said that\"}", responseHeaders, FORBIDDEN, ""));
			twitter.timelineOperations().retweet(12345);
		} catch (DuplicateTweetException e) {
			success = true;
		}
		assertTrue("Expected DuplicateTweetException", success);
	}

	@SmallTest
	public void testRetweet_forbidden() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweet/12345.json"))
				.andExpect(method(POST))
				.andRespond(withResponse("{\"error\":\"Forbidden\"}", responseHeaders, FORBIDDEN, ""));
			twitter.timelineOperations().retweet(12345);
		} catch (OperationNotPermittedException e) {
			success = true;
		}
		assertTrue("Expected OperationNotPermittedException", success);
	}
	
	@SmallTest
	public void testGetRetweets() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/retweets/42.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("timeline.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getRetweets(42L);
		assertTimelineTweets(timeline);						
	}

	@SmallTest
	public void testGetRetweetedBy() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/42/retweeted_by.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("retweeted-by.json", getClass()), responseHeaders));
		List<TwitterProfile> retweetedBy = twitter.timelineOperations().getRetweetedBy(42L);
		assertEquals(2, retweetedBy.size());
		assertEquals("royclarkson", retweetedBy.get(0).getScreenName());
		assertEquals("kdonald", retweetedBy.get(1).getScreenName());
		
		mockServer.verify();
	}
	
	@SmallTest
	public void testGetRetweetedByIds() {
		mockServer.expect(requestTo("https://api.twitter.com/1/statuses/42/retweeted_by/ids.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("retweeted-by-ids.json", getClass()), responseHeaders));
		List<Long> retweetedByIds = twitter.timelineOperations().getRetweetedByIds(42L);
		assertEquals(3, retweetedByIds.size());
		assertEquals(12345, (long) retweetedByIds.get(0));
		assertEquals(9223372036854775807L, (long) retweetedByIds.get(1));
		assertEquals(34567, (long) retweetedByIds.get(2));
	}
	
	@SmallTest
	public void testGetRetweetedByIds_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getRetweetedByIds(12345L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testGetFavorites() {
		mockServer.expect(requestTo("https://api.twitter.com/1/favorites.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("favorite.json", getClass()), responseHeaders));
		List<Tweet> timeline = twitter.timelineOperations().getFavorites();
		assertTimelineTweets(timeline);
	}
	
	@SmallTest
	public void testGetFavorites_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().getFavorites();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testAddToFavorites() {
		mockServer.expect(requestTo("https://api.twitter.com/1/favorites/create/42.json"))
			.andExpect(method(POST))
			.andRespond(withResponse("{}", responseHeaders));
		twitter.timelineOperations().addToFavorites(42L);
		mockServer.verify();
	}
	
	@SmallTest
	public void testAddToFavorites_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().addToFavorites(12345L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testRemoveFromFavorites() {
		mockServer.expect(requestTo("https://api.twitter.com/1/favorites/destroy/71.json"))
			.andExpect(method(POST))
			.andRespond(withResponse("{}", responseHeaders));
		twitter.timelineOperations().removeFromFavorites(71L);
		mockServer.verify();
	}
	
	@SmallTest
	public void testrRemoveFromFavorites_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.timelineOperations().removeFromFavorites(12345L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
}
