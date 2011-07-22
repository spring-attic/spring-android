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
package org.springframework.social.facebook.api;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.social.test.client.RequestMatchers.body;
import static org.springframework.social.test.client.RequestMatchers.header;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.social.NotAuthorizedException;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class FriendTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetFriendLists() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/friendlists"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friend-lists"), responseHeaders));
		List<Reference> friendLists = facebook.friendOperations().getFriendLists();
		assertFriendLists(friendLists);
	}

	@SmallTest
	public void testGetFriendLists_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendLists();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendLists_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/11223344/friendlists"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friend-lists"), responseHeaders));
		List<Reference> friendLists = facebook.friendOperations().getFriendLists("11223344");
		assertFriendLists(friendLists);
	}
	
	@SmallTest
	public void testGetFriendLists_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendLists("11223344");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendList() {
		mockServer.expect(requestTo("https://graph.facebook.com/11929590579"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friend-list"), responseHeaders));
		Reference friendList = facebook.friendOperations().getFriendList("11929590579");
		assertEquals("11929590579", friendList.getId());
		assertEquals("High School Friends", friendList.getName());
	}

	@SmallTest
	public void testGetFriendList_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendList("11929590579");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendListMembers() {
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/members"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friends"), responseHeaders));
		List<Reference> members = facebook.friendOperations().getFriendListMembers("192837465");
		assertFriends(members);
	}
	
	@SmallTest
	public void testGetFriendListMembers_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendListMembers("192837465");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testCreateFriendList() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/friendlists"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andExpect(body("name=My+List"))
			.andRespond(withResponse(jsonResource("testdata/create-friend-list"), responseHeaders));
		String friendListId = facebook.friendOperations().createFriendList("My List");
		assertEquals("11929590579", friendListId);
		mockServer.verify();
	}
	
	@SmallTest
	public void testCreateFriendList_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().createFriendList("My List");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testDeleteFriendList() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456"))
			.andExpect(method(POST))
			.andExpect(body("method=delete"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("", responseHeaders));
		facebook.friendOperations().deleteFriendList("123456");
		mockServer.verify();
	}

	@SmallTest
	public void testDeleteFriendList_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().deleteFriendList("123456");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testAddToFriendList() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/members/7890123"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("true", responseHeaders));
		facebook.friendOperations().addToFriendList("123456", "7890123");
		mockServer.verify();
	}
	
	@SmallTest
	public void testAddToFriendList_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().addToFriendList("123456", "7890123");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testRemoveFromFriendList() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/members/7890123"))
			.andExpect(method(DELETE))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("true", responseHeaders));
		facebook.friendOperations().removeFromFriendList("123456", "7890123");
		mockServer.verify();		
	}

	@SmallTest
	public void testRemoveFromFriendList_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().removeFromFriendList("123456", "7890123");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriends() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/friends"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/friends"), responseHeaders));
		List<Reference> friends = facebook.friendOperations().getFriends();
		assertFriends(friends);
	}

	@SmallTest
	public void testGetFriends_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriends();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriends_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/912873465/friends"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/friends"), responseHeaders));
		List<Reference> friends = facebook.friendOperations().getFriends("912873465");
		assertFriends(friends);
	}

	@SmallTest
	public void testGetFriends_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriends("912873465");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendIds() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/friends?fields=id"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friend-ids"), responseHeaders));
		List<String> friendIds = facebook.friendOperations().getFriendIds();
		assertFriendIds(friendIds);
	}

	@SmallTest
	public void testGetFriendIds_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendIds();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendIds_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/912873465/friends?fields=id"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/friend-ids"), responseHeaders));
		// TODO: Come up with a better set of representative test data
		List<String> friendIds = facebook.friendOperations().getFriendIds("912873465");
		assertFriendIds(friendIds);
	}
	
	@SmallTest
	public void testGetFriendIds_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendIds("912873465");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendProfiles() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/friends?fields=id%2Cusername%2Cname%2Cfirst_name%2Clast_name%2Cgender%2Clocale%2Ceducation%2Cwork%2Cemail%2Cthird_party_id%2Clink%2Ctimezone%2Cupdated_time%2Cverified%2Cabout%2Cbio%2Cbirthday%2Clocation%2Chometown%2Cinterested_in%2Creligion%2Cpolitical%2Cquotes%2Crelationship_status%2Csignificant_other%2Cwebsite"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-profiles"), responseHeaders));
		List<FacebookProfile> friends = facebook.friendOperations().getFriendProfiles();
		assertFriendProfiles(friends);
	}
	
	@SmallTest
	public void testGetFriendProfiles_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendProfiles();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFriendProfiles_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/1234567/friends?fields=id%2Cusername%2Cname%2Cfirst_name%2Clast_name%2Cgender%2Clocale%2Ceducation%2Cwork%2Cemail%2Cthird_party_id%2Clink%2Ctimezone%2Cupdated_time%2Cverified%2Cabout%2Cbio%2Cbirthday%2Clocation%2Chometown%2Cinterested_in%2Creligion%2Cpolitical%2Cquotes%2Crelationship_status%2Csignificant_other%2Cwebsite"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-profiles"), responseHeaders));
		List<FacebookProfile> friends = facebook.friendOperations().getFriendProfiles("1234567");
		assertFriendProfiles(friends);
	}

	@SmallTest
	public void testGetFriendProfiles_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.friendOperations().getFriendProfiles("912873465");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	private void assertFriends(List<Reference> friends) {
		assertEquals(3, friends.size());
		assertEquals("12345", friends.get(0).getId());
		assertEquals("Roy Clarkson", friends.get(0).getName());
		assertEquals("67890", friends.get(1).getId());
		assertEquals("Keith Donald", friends.get(1).getName());
		assertEquals("24680", friends.get(2).getId());
		assertEquals("Rod Johnson", friends.get(2).getName());
	}

	private void assertFriendLists(List<Reference> friendLists) {
		assertEquals(3, friendLists.size());
		assertEquals("11929590579", friendLists.get(0).getId());
		assertEquals("High School Friends", friendLists.get(0).getName());
		assertEquals("7770595579", friendLists.get(1).getId());
		assertEquals("Family", friendLists.get(1).getName());
		assertEquals("7716889379", friendLists.get(2).getId());
		assertEquals("College Friends", friendLists.get(2).getName());
	}

	private void assertFriendIds(List<String> friendIds) {
		assertEquals(3, friendIds.size());
		assertEquals("7918522", friendIds.get(0));
		assertEquals("149000307", friendIds.get(1));
		assertEquals("151101314", friendIds.get(2));
	}

	private void assertFriendProfiles(List<FacebookProfile> friends) {
		// TODO assert friend profiles		
	}

}
