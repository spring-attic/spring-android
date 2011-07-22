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
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.twitter.api.DirectMessage;
import org.springframework.social.twitter.api.MessageTooLongException;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Craig Walls
 */
public class DirectMessageTemplateTest extends AbstractTwitterApiTest {

	@MediumTest
	public void testGetDirectMessagesReceived() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages.json?page=1&count=20"))
			.andExpect(method(GET))
			.andRespond(withResponse(jsonResource("messages"), responseHeaders));
	
		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesReceived();
		assertDirectMessageListContents(messages);
	}
	
	@MediumTest
	public void testGetDirectMessagesReceived_paged() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages.json?page=3&count=12"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("messages"), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesReceived(3, 12);
		assertDirectMessageListContents(messages);
	}
	
	@MediumTest
	public void testGetDirectMessagesReceived_paged_withSinceIdAndMaxId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages.json?page=3&count=12&since_id=112233&max_id=332211"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("messages"), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesReceived(3, 12, 112233, 332211);
		assertDirectMessageListContents(messages);
	}
	
	@SmallTest
	public void testGetDirectMessagesReceived_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.directMessageOperations().getDirectMessagesReceived();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetDirectMessagesSent() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/sent.json?page=1&count=20"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("messages"), responseHeaders));
		
		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesSent();
		assertDirectMessageListContents(messages);
	}
	
	@MediumTest
	public void testGetDirectMessagesSent_paged() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/sent.json?page=3&count=25"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("messages"), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesSent(3, 25);
		assertDirectMessageListContents(messages);
	}
	
	@MediumTest
	public void testGetDirectMessagesSent_paged_withSinceIdAndMaxId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/sent.json?page=3&count=25&since_id=2468&max_id=8642"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("messages"), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesSent(3, 25, 2468, 8642);
		assertDirectMessageListContents(messages);
	}
	
	@MediumTest
	public void testGetDirectMessagesSent_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.directMessageOperations().getDirectMessagesSent();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testSendDirectMessage_toScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/new.json")).andExpect(method(POST))
				.andExpect(body("screen_name=habuma&text=Hello+there%21"))
				.andRespond(withResponse("{}", responseHeaders));
		twitter.directMessageOperations().sendDirectMessage("habuma", "Hello there!");
		mockServer.verify();
	}
	
	@MediumTest
	public void testSendDirectMessage_toScreenName_tooLong() {
		boolean success = false;
		try {
			mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/new.json")).andExpect(method(POST))
					.andExpect(body("screen_name=habuma&text=Really+long+message"))
				.andRespond(withResponse("{\"error\":\"There was an error sending your message: The text of your direct message is over 140 characters.\"}", responseHeaders, HttpStatus.FORBIDDEN, ""));		
			twitter.directMessageOperations().sendDirectMessage("habuma", "Really long message");
			mockServer.verify();
		} catch (MessageTooLongException e) {
			success = true;
		}
		assertTrue("Expected MessageTooLongException", success);
	}
	
	@MediumTest
	public void testSendDirectMessaage_toScreenName_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.directMessageOperations().sendDirectMessage("habuma", "Hello there!");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testSendDirectMessage_toUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/new.json")).andExpect(method(POST))
				.andExpect(body("user_id=11223&text=Hello+there%21")).andRespond(withResponse("{}", responseHeaders));
		twitter.directMessageOperations().sendDirectMessage(11223, "Hello there!");
		mockServer.verify();
	}
	
	@MediumTest
	public void testSendDirectMessaage_toUserId_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.directMessageOperations().sendDirectMessage(112233, "Hello there!");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testDeleteDirectMessage() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/destroy/42.json"))
				.andExpect(method(DELETE))
				.andRespond(withResponse(new ClassPathResource("directMessage.json", getClass()), responseHeaders));
		twitter.directMessageOperations().deleteDirectMessage(42L);
		mockServer.verify();
	}
	
	@MediumTest
	public void testDeleteDirectMessage_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.directMessageOperations().deleteDirectMessage(42L);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	private void assertDirectMessageListContents(List<DirectMessage> messages) {
		assertEquals(2, messages.size());
		assertEquals(12345, messages.get(0).getId());
		assertEquals("Hello there", messages.get(0).getText());
		assertEquals(24680, messages.get(0).getSender().getId());
		assertEquals("rclarkson", messages.get(0).getSender().getScreenName());
		assertEquals(13579, messages.get(0).getRecipient().getId());
		assertEquals("kdonald", messages.get(0).getRecipient().getScreenName());
		// assertTimelineDateEquals("Tue Jul 13 17:38:21 +0000 2010", messages.get(0).getCreatedAt());
		assertEquals(23456, messages.get(1).getId());
		assertEquals("Back at ya", messages.get(1).getText());
		assertEquals(13579, messages.get(1).getSender().getId());
		assertEquals("kdonald", messages.get(1).getSender().getScreenName());
		assertEquals(24680, messages.get(1).getRecipient().getId());
		assertEquals("rclarkson", messages.get(1).getRecipient().getScreenName());
	}

}
