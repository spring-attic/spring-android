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
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.social.twitter.api.TwitterProfile;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class FriendTemplateTest extends AbstractTwitterApiTest {

	@MediumTest
	public void testGetFriends_currentUser() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/friends.json?cursor=-1"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> friends = twitter.friendOperations().getFriends();
		assertEquals(2, friends.size());
		assertEquals("royclarkson", friends.get(0).getScreenName());
		assertEquals("kdonald", friends.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFriends_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/friends.json?cursor=-1&user_id=98765"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> friends = twitter.friendOperations().getFriends(
				98765L);
		assertEquals(2, friends.size());
		assertEquals("royclarkson", friends.get(0).getScreenName());
		assertEquals("kdonald", friends.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFriends_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/friends.json?cursor=-1&screen_name=habuma"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> friends = twitter.friendOperations().getFriends(
				"habuma");
		assertEquals(2, friends.size());
		assertEquals("royclarkson", friends.get(0).getScreenName());
		assertEquals("kdonald", friends.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFriendIds_currentUser() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friends/ids.json?cursor=-1"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations().getFriendIds();
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testGetFriendIds_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friends/ids.json?cursor=-1&user_id=98765"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations()
				.getFriendIds(98765L);
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testGetFriendIds_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friends/ids.json?cursor=-1&screen_name=habuma"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations().getFriendIds(
				"habuma");
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testGetFollowers_currentUser() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/followers.json?cursor=-1"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> followers = twitter.friendOperations()
				.getFollowers();
		assertEquals(2, followers.size());
		assertEquals("royclarkson", followers.get(0).getScreenName());
		assertEquals("kdonald", followers.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFollowers_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/followers.json?cursor=-1&user_id=98765"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> followers = twitter.friendOperations()
				.getFollowers(98765L);
		assertEquals(2, followers.size());
		assertEquals("royclarkson", followers.get(0).getScreenName());
		assertEquals("kdonald", followers.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFollowers_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/statuses/followers.json?cursor=-1&screen_name=oizik"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friends-or-followers.json", getClass()),
								responseHeaders));

		List<TwitterProfile> followers = twitter.friendOperations()
				.getFollowers("oizik");
		assertEquals(2, followers.size());
		assertEquals("royclarkson", followers.get(0).getScreenName());
		assertEquals("kdonald", followers.get(1).getScreenName());
	}

	@MediumTest
	public void testGetFollowerIds_currentUser() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/followers/ids.json?cursor=-1"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations().getFollowerIds();
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testGetFollowerIds_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/followers/ids.json?cursor=-1&user_id=98765"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations().getFollowerIds(
				98765L);
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testGetFollowerIds_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/followers/ids.json?cursor=-1&screen_name=habuma"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"friend-or-follower-ids.json", getClass()),
								responseHeaders));

		List<Long> followerIds = twitter.friendOperations().getFollowerIds(
				"habuma");
		assertEquals(3, followerIds.size());
		assertEquals(12345L, (long) followerIds.get(0));
		assertEquals(9223372036854775807L, (long) followerIds.get(1));
		assertEquals(34567L, (long) followerIds.get(2));
	}

	@MediumTest
	public void testFollow_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/create.json?user_id=98765"))
				.andExpect(method(POST))
				.andRespond(
						withResponse(new ClassPathResource("follow.json",
								getClass()), responseHeaders));

		String followedScreenName = twitter.friendOperations().follow(98765);
		assertEquals("oizik2", followedScreenName);

		mockServer.verify();
	}

	@MediumTest
	public void testFollow_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/create.json?screen_name=oizik2"))
				.andExpect(method(POST))
				.andRespond(
						withResponse(new ClassPathResource("follow.json",
								getClass()), responseHeaders));

		String followedScreenName = twitter.friendOperations().follow("oizik2");
		assertEquals("oizik2", followedScreenName);

		mockServer.verify();
	}

	@MediumTest
	public void testUnfollow_byUserId() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/destroy.json?user_id=98765"))
				.andExpect(method(POST))
				.andRespond(
						withResponse(new ClassPathResource("unfollow.json",
								getClass()), responseHeaders));

		String unFollowedScreenName = twitter.friendOperations()
				.unfollow(98765);
		assertEquals("oizik2", unFollowedScreenName);

		mockServer.verify();
	}

	@MediumTest
	public void testUnfollow_byScreenName() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/destroy.json?screen_name=oizik2"))
				.andExpect(method(POST))
				.andRespond(
						withResponse(new ClassPathResource("unfollow.json",
								getClass()), responseHeaders));

		String unFollowedScreenName = twitter.friendOperations().unfollow(
				"oizik2");
		assertEquals("oizik2", unFollowedScreenName);

		mockServer.verify();
	}

	@MediumTest
	public void testExists() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/exists.json?user_a=kdonald&user_b=tinyrod"))
				.andExpect(method(GET))
				.andRespond(withResponse("true", responseHeaders));
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/exists.json?user_a=royclarkson&user_b=charliesheen"))
				.andExpect(method(GET))
				.andRespond(withResponse("false", responseHeaders));

		assertTrue(twitter.friendOperations().friendshipExists("kdonald",
				"tinyrod"));
		assertFalse(twitter.friendOperations().friendshipExists("royclarkson",
				"charliesheen"));
	}

	@MediumTest
	public void testGetIncomingFriendships() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/incoming.json"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"incoming-or-outgoing-friendships.json",
								getClass()), responseHeaders));

		List<Long> friendships = twitter.friendOperations()
				.getIncomingFriendships();
		assertEquals(3, friendships.size());
		assertEquals(12345, (long) friendships.get(0));
		assertEquals(23456, (long) friendships.get(1));
		assertEquals(34567, (long) friendships.get(2));
	}

	@MediumTest
	public void testGetOutgoingFriendships() {
		mockServer
				.expect(requestTo("https://api.twitter.com/1/friendships/outgoing.json"))
				.andExpect(method(GET))
				.andRespond(
						withResponse(new ClassPathResource(
								"incoming-or-outgoing-friendships.json",
								getClass()), responseHeaders));

		List<Long> friendships = twitter.friendOperations()
				.getOutgoingFriendships();
		assertEquals(3, friendships.size());
		assertEquals(12345, (long) friendships.get(0));
		assertEquals(23456, (long) friendships.get(1));
		assertEquals(34567, (long) friendships.get(2));
	}
}
