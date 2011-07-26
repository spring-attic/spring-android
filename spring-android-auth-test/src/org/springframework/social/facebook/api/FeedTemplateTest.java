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

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.social.test.client.RequestMatchers.body;
import static org.springframework.social.test.client.RequestMatchers.header;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.social.DuplicateStatusException;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.facebook.api.Post.PostType;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Craig Walls
 * @author Roy Clarkson
 */
public class FeedTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?offset=0&limit=25"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed();
		assertEquals(5, feed.size());
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}
	
	@MediumTest
	public void testGetFeed_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?offset=40&limit=20"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed(40, 20);
		assertEquals(5, feed.size());
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}


	@MediumTest
	public void testGetFeed_withUnknownType() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?offset=0&limit=25"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed-with-unknown-type"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed();
		assertEquals(1, feed.size());		
		assertTrue(feed.get(0) instanceof Post);
		assertEquals(PostType.POST, feed.get(0).getType());
		assertEquals("100001387295207_160065090716400", feed.get(0).getId());
		assertEquals("Just trying something", feed.get(0).getMessage());
		assertEquals("100001387295207", feed.get(0).getFrom().getId());
		assertEquals("Art Names", feed.get(0).getFrom().getName());
		assertNull(feed.get(0).getApplication());
	}

	@SmallTest
	public void testGetFeed_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getFeed();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetFeed_forOwnerId() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/feed?offset=0&limit=25"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed("12345678");
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}
	
	@MediumTest
	public void testGetFeed_forOwnerId_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/feed?offset=100&limit=50"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed("12345678", 100, 50);
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}
	
	@SmallTest
	public void testGetFeed_forOwnerId_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getFeed("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetHomeFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> homeFeed = facebook.feedOperations().getHomeFeed();
		assertEquals(5, homeFeed.size());
		assertFeedEntries(homeFeed);
	}
	
	@MediumTest
	public void testGetHomeFeed_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home?offset=40&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> homeFeed = facebook.feedOperations().getHomeFeed(40, 20);
		assertEquals(5, homeFeed.size());
		assertFeedEntries(homeFeed);
	}
	
	@SmallTest
	public void testGetHomeFeed_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getHomeFeed();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetStatuses() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/statuses?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-statuses"), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses());
	}
	
	@MediumTest
	public void testGetStatuses_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/statuses?offset=30&limit=10"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-statuses"), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses(30, 10));
	}

	@SmallTest
	public void testGetStatuses_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getStatuses();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetStatuses_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/24680/statuses?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-statuses"), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses("24680"));
	}
	
	@MediumTest
	public void testGetStatuses_forSpecificUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/24680/statuses?offset=15&limit=5"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-statuses"), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses("24680", 15, 5));
	}
	
	@SmallTest
	public void testGetStatuses_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getStatuses("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetLinks() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/links?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/links"), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks());
	}
	
	@MediumTest
	public void testGetLinks_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/links?offset=40&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/links"), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks(40, 20));
	}
	
	@SmallTest
	public void testGetLinks_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getLinks();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetLinks_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/13579/links?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/links"), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks("13579"));
	}
	
	@MediumTest
	public void testGetLinks_forSpecificUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/13579/links?offset=40&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/links"), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks("13579", 40, 20));
	}
	
	@SmallTest
	public void testGetLinks_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getLinks("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetNotes() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/notes?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-notes"), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes();
		assertNotes(notes);
	}
	
	@MediumTest
	public void testGetNotes_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/notes?offset=60&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-notes"), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes(60, 20);
		assertNotes(notes);
	}

	@SmallTest
	public void testGetNotes_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getNotes();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetNotes_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345/notes?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-notes"), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes("12345");
		assertNotes(notes);
	}
	
	@MediumTest
	public void testGetNotes_forSpecificUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345/notes?offset=60&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-notes"), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes("12345", 60, 20);
		assertNotes(notes);
	}

	@SmallTest
	public void testGetNotes_unauthorized_forSpecificUser() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getNotes("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testGetPosts() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/posts?offset=0&limit=25"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts();
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}
	
	@MediumTest
	public void testGetPosts_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/posts?offset=30&limit=15"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts(30, 15);
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}
	
	@SmallTest
	public void testGetPosts_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getPosts();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetPosts_forOwnerId() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/posts?offset=0&limit=25"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts("12345678");
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}	
	
	@MediumTest
	public void testGetPosts_forOwnerId_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/posts?offset=30&limit=15"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts("12345678", 30, 15);
		assertEquals(5, feed.size());
		assertFeedEntries(feed);
	}

	@SmallTest
	public void testGetPosts_unauthorized_forSpecificUser() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getPosts("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest 
	public void testGetFeedEntry() {
		mockServer.expect(requestTo("https://graph.facebook.com/100001387295207_123939024341978"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/post"), responseHeaders));
		Post feedEntry = facebook.feedOperations().getPost("100001387295207_123939024341978");
		assertEquals(PostType.STATUS, feedEntry.getType());
		assertEquals("100001387295207_123939024341978", feedEntry.getId());
		assertEquals("Hello world!", feedEntry.getMessage());
		assertEquals("100001387295207", feedEntry.getFrom().getId());
		assertEquals("Art Names", feedEntry.getFrom().getName());
		assertEquals(2, feedEntry.getLikes().size());
		assertEquals("1533260333", feedEntry.getLikes().get(0).getId());
		assertEquals("Roy Clarkson", feedEntry.getLikes().get(0).getName());
		assertEquals("1322692345", feedEntry.getLikes().get(1).getId());
		assertEquals("Jim Smith", feedEntry.getLikes().get(1).getName());
		assertEquals(2, feedEntry.getComments().size());
		assertNull(feedEntry.getComments().get(1).getLikes());
		assertEquals(3, feedEntry.getComments().get(1).getLikesCount());
	}

	@SmallTest
	public void testGetFeedEntry_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getPost("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testUpdateStatus() throws Exception {
		String requestBody = "message=Hello+Facebook+World";
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		assertEquals("123456_78901234", facebook.feedOperations().updateStatus("Hello Facebook World"));
		mockServer.verify();
	}

	@SmallTest
	public void testUpdateStatus_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().updateStatus("Hello");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@SmallTest
	public void testUpdateStatus_duplicate() {
		boolean success = false;
		try {
			String requestBody = "message=Duplicate";
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse(jsonResource("testdata/error-duplicate-status"), responseHeaders, HttpStatus.BAD_REQUEST, ""));
		facebook.feedOperations().updateStatus("Duplicate");
		} catch (DuplicateStatusException e) {
			success = true;
		}
		assertTrue("Expected DuplicateStatusException", success);
	}

	@MediumTest
	public void testPost_message() throws Exception {
		String requestBody = "message=Hello+Facebook+World";
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		assertEquals("123456_78901234", facebook.feedOperations().post("123456789", "Hello Facebook World"));
		mockServer.verify();
	}

	@SmallTest
	public void testPostMessage_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().post("123456789", "Hello Facebook World");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testPost_link() throws Exception {
		String requestBody = "link=someLink&name=some+name&caption=some+caption&description=some+description&message=Hello+Facebook+World";
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed")).andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
		assertEquals("123456_78901234", facebook.feedOperations().postLink("Hello Facebook World", link));
		mockServer.verify();
	}

	@SmallTest
	public void testPostLink_unauthorized() {
		boolean success = false;
		try {
			FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
			unauthorizedFacebook.feedOperations().postLink("Hello Facebook World", link);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testPost_link_toAnotherFeed() throws Exception {
		String requestBody = "link=someLink&name=some+name&caption=some+caption&description=some+description&message=Hello+Facebook+World";
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/feed")).andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
		assertEquals("123456_78901234", facebook.feedOperations().postLink("123456789", "Hello Facebook World", link));
		mockServer.verify();
	}

	@SmallTest
	public void testPostLink_toAnotherFeed_unauthorized() {
		boolean success = false;
		try {
			FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
			unauthorizedFacebook.feedOperations().postLink("123456789", "Hello Facebook World", link);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testDeleteFeedEntry() {
		String requestBody = "method=delete";
		mockServer.expect(requestTo("https://graph.facebook.com/123456_78901234"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken")).andExpect(body(requestBody))
				.andRespond(withResponse("{}", responseHeaders));
		facebook.feedOperations().deletePost("123456_78901234");
		mockServer.verify();
	}

	@SmallTest
	public void testDeleteFeedEntry_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().deletePost("123456_78901234");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testSearchPublicFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/search?q=Dr+Seuss&type=post&offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/post-list"), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchPublicFeed("Dr Seuss");
		assertPostList(posts);
	}
	
	@MediumTest
	public void testSearchPublicFeed_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/search?q=Dr+Seuss&type=post&offset=40&limit=10"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/post-list"), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchPublicFeed("Dr Seuss", 40, 10);
		assertPostList(posts);
	}

	@MediumTest
	public void testSearchHomeFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home?q=Dr+Seuss&offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/post-list"), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchHomeFeed("Dr Seuss");
		assertPostList(posts);
	}
	
	@MediumTest
	public void testSearchHomeFeed_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home?q=Dr+Seuss&offset=20&limit=5"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/post-list"), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchHomeFeed("Dr Seuss", 20, 5);
		assertPostList(posts);
	}

	@SmallTest
	public void testSearchHomeFeed_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().searchHomeFeed("Dr Seuss");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest 
	public void testSearchUserFeed_currentUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?q=Football&offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("Football");		
		assertEquals(5, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}
	
	@MediumTest 
	public void searchUserFeed_currentUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?q=Football&offset=50&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("Football", 50, 25);		
		assertEquals(5, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}
	
	@SmallTest
	public void testSearchUserFeed_currentUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().searchUserFeed("Football");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest 
	public void testSearchUserFeed_specificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/feed?q=Football&offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("123456789", "Football");		
		assertEquals(5, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}
	
	@MediumTest 
	public void testSearchUserFeed_specificUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/feed?q=Football&offset=80&limit=20"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/feed"), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("123456789", "Football", 80, 20);		
		assertEquals(5, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertTrue(feed.get(3) instanceof SwfPost);
		assertTrue(feed.get(4) instanceof MusicPost);
		assertFeedEntries(feed);
	}

	@SmallTest
	public void testSearchUserFeed_specificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().searchUserFeed("123456789", "Football");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	private void assertPostList(List<Post> posts) {
		assertEquals(3, posts.size());
		assertTrue(posts.get(0) instanceof StatusPost);
		assertEquals("825100071_10150596184825072", posts.get(0).getId());
		assertEquals("825100071", posts.get(0).getFrom().getId());
		assertEquals("Adrian Hunley", posts.get(0).getFrom().getName());
		assertEquals("\"Today you are you! That is truer than true! There is no one alive who is you-er than you!\"\n-Dr. Seuss", posts.get(0).getMessage());
		assertEquals(toDate("2011-05-13T02:32:21+0000"), posts.get(0).getCreatedTime());
		assertEquals(toDate("2011-05-13T02:32:21+0000"), posts.get(0).getUpdatedTime());
		assertTrue(posts.get(1) instanceof StatusPost);
		assertEquals("100000224762665_227473300603494", posts.get(1).getId());
		assertEquals("100000224762665", posts.get(1).getFrom().getId());
		assertEquals("Machelle Allen Bitton", posts.get(1).getFrom().getName());
		assertEquals("When beetles fight these battles in a bottle with their paddles\nand the bottle's on a poodle and the poodle's eating noodles...\n...they call this a muddle puddle tweetle poodle beetle noodle\nbottle paddle battle.\"\n\u2014 Dr. Seuss (Fox in Socks)", posts.get(1).getMessage());
		assertEquals(toDate("2011-05-13T01:41:50+0000"), posts.get(1).getCreatedTime());
		assertEquals(toDate("2011-05-13T01:41:50+0000"), posts.get(1).getUpdatedTime());
		assertEquals(1, posts.get(1).getLikes().size());
		assertEquals("100000695403650", posts.get(1).getLikes().get(0).getId());
		assertEquals("Courtney Briscoe", posts.get(1).getLikes().get(0).getName());
		assertTrue(posts.get(2) instanceof StatusPost);
		assertEquals("100000132946459_227565820591181", posts.get(2).getId());
		assertEquals("100000132946459", posts.get(2).getFrom().getId());
		assertEquals("William Terry", posts.get(2).getFrom().getName());
		assertEquals("and that's when I realized, the greatest rapper of all time, was Dr. Seuss", posts.get(2).getMessage());
		assertEquals(toDate("2011-05-13T01:26:13+0000"), posts.get(2).getCreatedTime());
		assertEquals(toDate("2011-05-13T01:26:13+0000"), posts.get(2).getUpdatedTime());
	}
	
	private void assertFeedEntries(List<Post> feed) {
		assertEquals(PostType.STATUS, feed.get(0).getType());
		assertEquals("100001387295207_160065090716400", feed.get(0).getId());
		assertEquals("Just trying something", feed.get(0).getMessage());
		assertEquals("100001387295207", feed.get(0).getFrom().getId());
		assertEquals("Art Names", feed.get(0).getFrom().getName());
		assertNull(feed.get(0).getApplication());
		assertEquals(PostType.PHOTO, feed.get(1).getType());
		assertEquals("100001387295207_160064384049804", feed.get(1).getId());
		assertEquals("Check out my ride", feed.get(1).getMessage());
		assertEquals("100001387295207", feed.get(1).getFrom().getId());
		assertEquals("Art Names", feed.get(1).getFrom().getName());
		assertNull(feed.get(1).getApplication());
		assertEquals(PostType.STATUS, feed.get(2).getType());
		assertEquals("100001387295207_153453231377586", feed.get(2).getId());
		assertEquals("Hello Facebook!", feed.get(2).getMessage());
		assertEquals("100001387295207", feed.get(2).getFrom().getId());
		assertEquals("Art Names", feed.get(2).getFrom().getName());
		assertEquals("162886103757745", feed.get(2).getApplication().getId());
		assertEquals("Spring Social Showcase", feed.get(2).getApplication().getName());
	}
	
	private void assertLinks(List<LinkPost> feed) {
		assertEquals(2, feed.size());
		assertEquals(PostType.LINK, feed.get(0).getType());
		assertEquals("125736073702566", feed.get(0).getId());
		assertEquals("Warning about Facebook Phishing: See http://www.facebook.com/group.php?gid=9874388706", feed.get(0).getMessage());
		assertEquals("738140579", feed.get(0).getFrom().getId());
		assertEquals("Craig Walls", feed.get(0).getFrom().getName());
		assertEquals("http://profile.ak.fbcdn.net/hprofile-ak-snc4/50255_9874388706_6623_n.jpg", feed.get(0).getPicture());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yD/r/aS8ecmYRys0.gif", feed.get(0).getIcon());
		assertEquals("Facebook Phishing Scam Awareness", feed.get(0).getName());
		assertNull(feed.get(0).getDescription());
		assertNull(feed.get(0).getLink()); // sometimes links are in the message
		assertEquals(PostType.LINK, feed.get(1).getType());
		assertEquals("147264864601", feed.get(1).getId());
		assertEquals("Hey, let's go buy some furniture from the guy who's off his meds.", feed.get(1).getMessage());
		assertEquals("738140579", feed.get(1).getFrom().getId());
		assertEquals("Craig Walls", feed.get(1).getFrom().getName());
		assertEquals("http://i.ytimg.com/vi/QSLT2N-Ome8/2.jpg", feed.get(1).getPicture());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yD/r/aS8ecmYRys0.gif", feed.get(1).getIcon());
		assertEquals("Competition Beatdown Fail", feed.get(1).getName());
		assertEquals("What was this guy thinking?", feed.get(1).getDescription());
		assertEquals("http://www.youtube.com/watch?v=QSLT2N-Ome8", feed.get(1).getLink());
	}

	private void assertNotes(List<NotePost> notes) {
		assertEquals(2, notes.size());
		assertEquals(PostType.NOTE, notes.get(0).getType());
		assertEquals("161200187269557", notes.get(0).getId());
		assertEquals("100001387295207", notes.get(0).getFrom().getId());
		assertEquals("Art Names", notes.get(0).getFrom().getName());
		assertEquals("Just a note", notes.get(0).getSubject());
		assertEquals("<p>This is just a test note. Nothing special to see here.</p>", notes.get(0).getMessage());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yY/r/1gBp2bDGEuh.gif", notes.get(0).getIcon());
		assertEquals(toDate("2011-03-28T15:17:41+0000"), notes.get(0).getCreatedTime());
		assertEquals(toDate("2011-03-28T15:17:41+0000"), notes.get(0).getUpdatedTime());
		assertEquals(PostType.NOTE, notes.get(1).getType());
		assertEquals("160546394001603", notes.get(1).getId());
		assertEquals("100001387295207", notes.get(1).getFrom().getId());
		assertEquals("Art Names", notes.get(1).getFrom().getName());
		assertEquals("Test Note", notes.get(1).getSubject());
		assertEquals("<p>Just a <strong>test</strong> note...nothing to see here.</p>", notes.get(1).getMessage());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yY/r/1gBp2bDGEuh.gif", notes.get(1).getIcon());
		assertEquals(toDate("2011-03-25T18:25:01+0000"), notes.get(1).getCreatedTime());
		assertEquals(toDate("2011-03-25T20:08:27+0000"), notes.get(1).getUpdatedTime());
	}
	
	private void assertStatuses(List<StatusPost> statuses) {
		assertEquals(3, statuses.size());
		assertEquals(PostType.STATUS, statuses.get(0).getType());
		assertEquals("161195833936659", statuses.get(0).getId());
		assertEquals("100001387295207", statuses.get(0).getFrom().getId());
		assertEquals("Art Names", statuses.get(0).getFrom().getName());
		assertEquals("One more...just for fun", statuses.get(0).getMessage());
		assertEquals(toDate("2011-03-28T14:54:07+0000"), statuses.get(0).getUpdatedTime());
		assertEquals(PostType.STATUS, statuses.get(1).getType());
		assertEquals("161195783936664", statuses.get(1).getId());
		assertEquals("100001387295207", statuses.get(1).getFrom().getId());
		assertEquals("Art Names", statuses.get(1).getFrom().getName());
		assertEquals("Just another status.", statuses.get(1).getMessage());
		assertEquals(toDate("2011-03-28T14:53:57+0000"), statuses.get(1).getUpdatedTime());
		assertEquals("161195107270065", statuses.get(2).getId());
		assertEquals("100001387295207", statuses.get(2).getFrom().getId());
		assertEquals("Art Names", statuses.get(2).getFrom().getName());
		assertEquals("Good morning Monday!", statuses.get(2).getMessage());
		assertEquals(toDate("2011-03-28T14:50:27+0000"), statuses.get(2).getUpdatedTime());
	}

}
