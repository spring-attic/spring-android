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
import static org.springframework.social.test.client.RequestMatchers.body;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.UserList;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class ListsTemplateTest extends AbstractTwitterApiTest {
	
	@MediumTest
	public void testGetLists_currentUser() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getLists());
	}

	@MediumTest
	public void testGetLists_byId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists.json?user_id=161064614"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getLists(161064614));
	}

	@MediumTest
	public void testGetLists_byScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists.json?screen_name=habuma"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getLists("habuma"));
	}

	@MediumTest
	public void testGetList_byListId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/show.json?list_id=40841803"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		assertSingleList(twitter.listOperations().getList(40841803));
	}
	
	@MediumTest
	public void testCreateList_publicListForUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/create.json"))
			.andExpect(method(POST))
			.andExpect(body("name=forfun&description=Just+for+Fun&mode=public"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		assertSingleList(twitter.listOperations().createList("forfun", "Just for Fun", true));
	}

	@MediumTest
	public void testCreateList_privateListForUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/create.json"))
			.andExpect(method(POST))
			.andExpect(body("name=forfun2&description=Just+for+Fun%2C+too&mode=private"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		assertSingleList(twitter.listOperations().createList("forfun2", "Just for Fun, too", false));
	}
	
	@MediumTest
	public void testUpdateList_publicListForUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/update.json"))
			.andExpect(method(POST))
			.andExpect(body("name=forfun&description=Just+for+Fun&mode=public&list_id=40841803"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		assertSingleList(twitter.listOperations().updateList(40841803, "forfun", "Just for Fun", true));
	}

	@MediumTest
	public void testUpdateList_privateListForUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/update.json"))
			.andExpect(method(POST))
			.andExpect(body("name=forfun2&description=Just+for+Fun%2C+too&mode=private&list_id=40841803"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		assertSingleList(twitter.listOperations().updateList(40841803, "forfun2", "Just for Fun, too", false));
	}

	@MediumTest
	public void testDeleteList_forUserIdByListId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/destroy.json?list_id=40841803"))
			.andExpect(method(DELETE))
			.andRespond(withResponse("{}", responseHeaders));
		twitter.listOperations().deleteList(40841803);
		mockServer.verify();
	}
	
	@MediumTest
	public void testGetListMembers_byUserIdAndListId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members.json?list_id=40841803"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-members.json", getClass()), responseHeaders));
		assertListMembers(twitter.listOperations().getListMembers(40841803));
	}

	@MediumTest
	public void testGetListMembers_byScreenNameAndListSlug() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members.json?owner_screen_name=habuma&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-members.json", getClass()), responseHeaders));
		assertListMembers(twitter.listOperations().getListMembers("habuma", "forfun"));
	}
	
	@MediumTest
	public void testAddToList_forUserIdListIdSingle() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/create_all.json"))
			.andExpect(method(POST))
			.andExpect(body("user_id=123456&list_id=40841803"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));		

		assertSingleList(twitter.listOperations().addToList(40841803, 123456));
	}

	@MediumTest
	public void testAddToList_forUserIdListIdMultiple() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/create_all.json"))
			.andExpect(method(POST))
			.andExpect(body("user_id=123456%2C234567%2C345678&list_id=40841803"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));		

		assertSingleList(twitter.listOperations().addToList(40841803, 123456, 234567, 345678));
	}

	@MediumTest
	public void testAddToList_forScreenNameMultiple() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/create_all.json"))
			.andExpect(method(POST))
			.andExpect(body("screen_name=habuma%2Croyclarkson&list_id=40841803"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));		

		assertSingleList(twitter.listOperations().addToList(40841803, "habuma", "royclarkson"));
	}

	@MediumTest
	public void testRemoveFromList_ownerIdListIdMemberId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/destroy.json"))
			.andExpect(method(POST))
			.andExpect(body("user_id=12345&list_id=40841803"))
			.andRespond(withResponse("{}", responseHeaders));
		twitter.listOperations().removeFromList(40841803, 12345);
		mockServer.verify();
	}

	@MediumTest
	public void testRemoveFromList_screenName() {		
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/destroy.json"))
			.andExpect(method(POST))
			.andExpect(body("screen_name=habuma&list_id=40841803"))
			.andRespond(withResponse("{}", responseHeaders));
		twitter.listOperations().removeFromList(40841803, "habuma");
		mockServer.verify();
	}

	@MediumTest
	public void testGetListSubscribers_byUserIdAndListId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers.json?list_id=40841803"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-members.json", getClass()), responseHeaders));
		assertListMembers(twitter.listOperations().getListSubscribers(161064614, 40841803));
	}

	@MediumTest
	public void testGetListSubscribers_byScreenNameAndListSlug() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers.json?owner_screen_name=habuma&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-members.json", getClass()), responseHeaders));
		assertListMembers(twitter.listOperations().getListSubscribers("habuma", "forfun"));
	}

	@MediumTest
	public void testGetMemberships_forUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/memberships.json?user_id=161064614"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getMemberships(161064614));
	}

	@MediumTest
	public void testGetMemberships_forScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/memberships.json?screen_name=habuma"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getMemberships("habuma"));
	}

	@MediumTest
	public void testGetSubscriptions_forUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscriptions.json?user_id=161064614"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getSubscriptions(161064614));
	}

	@MediumTest
	public void testGetSubscriptions_forScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscriptions.json?screen_name=habuma"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-lists.json", getClass()), responseHeaders));
		assertListOfLists(twitter.listOperations().getSubscriptions("habuma"));
	}
	
	@MediumTest
	public void testIsMember_byUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/show.json?list_id=40841803&user_id=123456"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("twitter-profile.json", getClass()), responseHeaders));
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/show.json?list_id=40841803&user_id=987654"))
			.andExpect(method(GET))
			.andRespond(withResponse("{}", responseHeaders, HttpStatus.NOT_FOUND, ""));
		assertTrue(twitter.listOperations().isMember(40841803, 123456));
		assertFalse(twitter.listOperations().isMember(40841803, 987654));
	}

	@MediumTest
	public void testIsMember_byScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/show.json?owner_screen_name=habuma&screen_name=royclarkson&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("twitter-profile.json", getClass()), responseHeaders));
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/members/show.json?owner_screen_name=habuma&screen_name=kdonald&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse("{}", responseHeaders, HttpStatus.NOT_FOUND, ""));
		assertTrue(twitter.listOperations().isMember("habuma", "forfun", "royclarkson"));
		assertFalse(twitter.listOperations().isMember("habuma", "forfun", "kdonald"));
	}
	
	@MediumTest
	public void testIsSubscriber_byUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/show.json?list_id=40841803&user_id=123456"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("twitter-profile.json", getClass()), responseHeaders));
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/show.json?list_id=40841803&user_id=987654"))
			.andExpect(method(GET))
			.andRespond(withResponse("{}", responseHeaders, HttpStatus.NOT_FOUND, ""));
		assertTrue(twitter.listOperations().isSubscriber(40841803, 123456));
		assertFalse(twitter.listOperations().isSubscriber(40841803, 987654));
	}

	@MediumTest
	public void testIsSubscriber_byScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/show.json?owner_screen_name=habuma&screen_name=royclarkson&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("twitter-profile.json", getClass()), responseHeaders));
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/show.json?owner_screen_name=habuma&screen_name=kdonald&slug=forfun"))
			.andExpect(method(GET))
			.andRespond(withResponse("{}", responseHeaders, HttpStatus.NOT_FOUND, ""));
		assertTrue(twitter.listOperations().isSubscriber("habuma", "forfun", "royclarkson"));
		assertFalse(twitter.listOperations().isSubscriber("habuma", "forfun", "kdonald"));
	}
	
	@MediumTest
	public void testSubscribe() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/create.json"))
			.andExpect(method(POST))
			.andExpect(body("list_id=54321"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		UserList list = twitter.listOperations().subscribe(54321);
		assertSingleList(list);
	}
	
	@MediumTest
	public void testSubscribe_usernameAndSlug() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/create.json"))
		.andExpect(method(POST))
		.andExpect(body("owner_screen_name=habuma&slug=somelist"))
		.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		UserList list = twitter.listOperations().subscribe("habuma", "somelist");
		assertSingleList(list);
	}
	
	@MediumTest
	public void testUnsubscribe() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/destroy.json"))
			.andExpect(method(POST))
			.andExpect(body("list_id=54321"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		UserList list = twitter.listOperations().unsubscribe(54321);
		assertSingleList(list);
		mockServer.verify();
	}
	
	@MediumTest
	public void testUnsubscribe_usernameAndSlug() {
		mockServer.expect(requestTo("https://api.twitter.com/1/lists/subscribers/destroy.json"))
			.andExpect(method(POST))
			.andExpect(body("owner_screen_name=habuma&slug=somelist"))
			.andRespond(withResponse(new ClassPathResource("single-list.json", getClass()), responseHeaders));
		twitter.listOperations().unsubscribe("habuma", "somelist");
		mockServer.verify();
	}

	
	// private helpers
	
	private void assertSingleList(UserList list) {
		assertEquals(40841803, list.getId());
		assertEquals("forFun", list.getName());
		assertEquals("@habuma/forfun", list.getFullName());
		assertEquals("forfun", list.getSlug());
		assertEquals("Just for fun", list.getDescription());
		assertEquals(22, list.getMemberCount());
		assertEquals(100, list.getSubscriberCount());
		assertEquals("/habuma/forfun", list.getUriPath());
	}

	private void assertListOfLists(List<UserList> lists) {
		assertEquals(2, lists.size());
		UserList list1 = lists.get(0);
		assertEquals(40842137, list1.getId());
		assertEquals("forFun2", list1.getName());
		assertEquals("@habuma/forfun2", list1.getFullName());
		assertEquals("forfun2", list1.getSlug());
		assertEquals("Just for fun, too", list1.getDescription());
		assertEquals(3, list1.getMemberCount());
		assertEquals(0, list1.getSubscriberCount());
		assertEquals("/habuma/forfun2", list1.getUriPath());
		UserList list2 = lists.get(1);
		assertEquals(40841803, list2.getId());
		assertEquals("forFun", list2.getName());
		assertEquals("@habuma/forfun", list2.getFullName());
		assertEquals("forfun", list2.getSlug());
		assertEquals("Just for fun", list2.getDescription());
		assertEquals(22, list2.getMemberCount());
		assertEquals(100, list2.getSubscriberCount());
		assertEquals("/habuma/forfun", list2.getUriPath());
	}

	private void assertListMembers(List<TwitterProfile> members) {
		assertEquals(2, members.size());
		TwitterProfile profile1 = members.get(0);
		assertEquals(14846645, profile1.getId());
		assertEquals("royclarkson", profile1.getScreenName());
		assertEquals("Roy Clarkson", profile1.getName());
		assertEquals("Follower of mobile, social, and web technology trends. I write lots of code, and work at SpringSource.", 
				profile1.getDescription());
		assertEquals("Atlanta, GA, USA", profile1.getLocation());
		TwitterProfile profile2 = members.get(1);
		assertEquals(14718006, profile2.getId());
		assertEquals("kdonald", profile2.getScreenName());
		assertEquals("Keith Donald", profile2.getName());
		assertEquals("SpringSource co-founder", profile2.getDescription());
		assertEquals("Melbourne, Fl", profile2.getLocation());
	}

}
