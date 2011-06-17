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

import org.springframework.core.io.ClassPathResource;
import org.springframework.social.NotAuthorizedException;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Craig Walls
 * @author Roy Clarkson
 */
public class FeedTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed();
		assertEquals(3, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertFeedEntries(feed);
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
	public void getFeed_forOwnerId() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/feed"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().getFeed("12345678");
		assertEquals(3, feed.size());
		assertFeedEntries(feed);
	}	
	
	@SmallTest
	public void getFeed_forOwnerId_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getFeed("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getHomeFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> homeFeed = facebook.feedOperations().getHomeFeed();
		assertEquals(3, homeFeed.size());
		assertFeedEntries(homeFeed);
	}
	
	@SmallTest
	public void getHomeFeed_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getHomeFeed();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getStatuses() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/statuses"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/user-statuses.json", getClass()), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses());
	}

	@SmallTest
	public void getStatuses_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getStatuses();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getStatuses_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/24680/statuses"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/user-statuses.json", getClass()), responseHeaders));		
		assertStatuses(facebook.feedOperations().getStatuses("24680"));
	}
	
	@SmallTest
	public void getStatuses_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getStatuses("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getLinks() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/links"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/links.json", getClass()), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks());
	}
	
	@SmallTest
	public void getLinks_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getLinks();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getLinks_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/13579/links"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/links.json", getClass()), responseHeaders));		
		assertLinks(facebook.feedOperations().getLinks("13579"));
	}
	
	@SmallTest
	public void getLinks_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getLinks("12345678");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getNotes() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/notes"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/user-notes.json", getClass()), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes();
		assertNotes(notes);
	}

	@SmallTest
	public void getNotes_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getNotes();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getNotes_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345/notes"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/user-notes.json", getClass()), responseHeaders));		
		List<NotePost> notes = facebook.feedOperations().getNotes("12345");
		assertNotes(notes);
	}

	@SmallTest
	public void getNotes_unauthorized_forSpecificUser() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getNotes("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void getPosts() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/posts"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts();
		assertEquals(3, feed.size());
		assertFeedEntries(feed);
	}
	
	@SmallTest
	public void getPosts_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getPosts();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void getPosts_forOwnerId() {
		mockServer.expect(requestTo("https://graph.facebook.com/12345678/posts"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().getPosts("12345678");
		assertEquals(3, feed.size());
		assertFeedEntries(feed);
	}	

	@MediumTest
	public void getPosts_unauthorized_forSpecificUser() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getPosts("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest 
	public void getFeedEntry() {
		mockServer.expect(requestTo("https://graph.facebook.com/100001387295207_123939024341978"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/post.json", getClass()), responseHeaders));
		Post feedEntry = facebook.feedOperations().getFeedEntry("100001387295207_123939024341978");
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
	public void getFeedEntry_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().getFeedEntry("12345");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void updateStatus() throws Exception {
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
	public void updateStatus_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().updateStatus("Hello");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void post_message() throws Exception {
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
	public void postMessage_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().post("123456789", "Hello Facebook World");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void post_link() throws Exception {
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
	public void postLink_unauthorized() {
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
	public void post_link_toAnotherFeed() throws Exception {
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
	public void postLink_toAnotherFeed_unauthorized() {
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
	public void deleteFeedEntry() {
		String requestBody = "method=delete";
		mockServer.expect(requestTo("https://graph.facebook.com/123456_78901234"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken")).andExpect(body(requestBody))
				.andRespond(withResponse("{}", responseHeaders));
		facebook.feedOperations().deleteFeedEntry("123456_78901234");
		mockServer.verify();
	}

	@SmallTest
	public void deleteFeedEntry_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().deleteFeedEntry("123456_78901234");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void searchPublicFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/search?q=Dr+Seuss&type=post"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/post-list.json", getClass()), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchPublicFeed("Dr Seuss");
		assertPostList(posts);
	}

	@MediumTest
	public void searchHomeFeed() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/home?q=Dr+Seuss"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/post-list.json", getClass()), responseHeaders));
		List<Post> posts = facebook.feedOperations().searchHomeFeed("Dr Seuss");
		assertPostList(posts);
	}

	@SmallTest
	public void searchHomeFeed_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().searchHomeFeed("Dr Seuss");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest 
	public void searchUserFeed_currentUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/feed?q=Football"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("Football");		
		assertEquals(3, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertFeedEntries(feed);
	}
	
	@SmallTest
	public void searchUserFeed_currentUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.feedOperations().searchUserFeed("Football");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest 
	public void searchUserFeed_specificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/feed?q=Football"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/feed.json", getClass()), responseHeaders));
		List<Post> feed = facebook.feedOperations().searchUserFeed("123456789", "Football");		
		assertEquals(3, feed.size());		
		assertTrue(feed.get(0) instanceof StatusPost);
		assertTrue(feed.get(1) instanceof PhotoPost);
		assertTrue(feed.get(2) instanceof StatusPost);
		assertFeedEntries(feed);
	}

	@SmallTest
	public void searchUserFeed_specificUser_unauthorized() {
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
		assertEquals("100001387295207_160065090716400", feed.get(0).getId());
		assertEquals("Just trying something", feed.get(0).getMessage());
		assertEquals("100001387295207", feed.get(0).getFrom().getId());
		assertEquals("Art Names", feed.get(0).getFrom().getName());
		assertNull(feed.get(0).getApplication());
		assertEquals("100001387295207_160064384049804", feed.get(1).getId());
		assertEquals("Check out my ride", feed.get(1).getMessage());
		assertEquals("100001387295207", feed.get(1).getFrom().getId());
		assertEquals("Art Names", feed.get(1).getFrom().getName());
		assertNull(feed.get(1).getApplication());
		assertEquals("100001387295207_153453231377586", feed.get(2).getId());
		assertEquals("Hello Facebook!", feed.get(2).getMessage());
		assertEquals("100001387295207", feed.get(2).getFrom().getId());
		assertEquals("Art Names", feed.get(2).getFrom().getName());
		assertEquals("162886103757745", feed.get(2).getApplication().getId());
		assertEquals("Spring Social Showcase", feed.get(2).getApplication().getName());
	}
	
	private void assertLinks(List<LinkPost> feed) {
		assertEquals(2, feed.size());
		assertEquals("125736073702566", feed.get(0).getId());
		assertEquals("Warning about Facebook Phishing: See http://www.facebook.com/group.php?gid=9874388706", feed.get(0).getMessage());
		assertEquals("738140579", feed.get(0).getFrom().getId());
		assertEquals("Craig Walls", feed.get(0).getFrom().getName());
		assertEquals("http://profile.ak.fbcdn.net/hprofile-ak-snc4/50255_9874388706_6623_n.jpg", feed.get(0).getPicture());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yD/r/aS8ecmYRys0.gif", feed.get(0).getIcon());
		assertEquals("Facebook Phishing Scam Awareness", feed.get(0).getName());
		assertNull(feed.get(0).getDescription());
		assertNull(feed.get(0).getLink()); // sometimes links are in the message
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
		assertEquals("161200187269557", notes.get(0).getId());
		assertEquals("100001387295207", notes.get(0).getFrom().getId());
		assertEquals("Art Names", notes.get(0).getFrom().getName());
		assertEquals("Just a note", notes.get(0).getSubject());
		assertEquals("<p>This is just a test note. Nothing special to see here.</p>", notes.get(0).getMessage());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yY/r/1gBp2bDGEuh.gif", notes.get(0).getIcon());
		assertEquals(toDate("2011-03-28T15:17:41+0000"), notes.get(0).getCreatedTime());
		assertEquals(toDate("2011-03-28T15:17:41+0000"), notes.get(0).getUpdatedTime());
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
		assertEquals("161195833936659", statuses.get(0).getId());
		assertEquals("100001387295207", statuses.get(0).getFrom().getId());
		assertEquals("Art Names", statuses.get(0).getFrom().getName());
		assertEquals("One more...just for fun", statuses.get(0).getMessage());
		assertEquals(toDate("2011-03-28T14:54:07+0000"), statuses.get(0).getUpdatedTime());
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
