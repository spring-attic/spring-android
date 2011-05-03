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
import org.springframework.social.twitter.api.DirectMessage;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class DirectMessageTemplateTest extends AbstractTwitterApiTest {

	@MediumTest
	public void testGetDirectMessagesReceived() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("messages.json", getClass()), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesReceived();
		assertDirectMessageListContents(messages);
	}

	@MediumTest
	public void testGetDirectMessagesSent() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/sent.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("messages.json", getClass()), responseHeaders));

		List<DirectMessage> messages = twitter.directMessageOperations().getDirectMessagesSent();
		assertDirectMessageListContents(messages);
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
	public void testSendDirectMessage_toUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/new.json")).andExpect(method(POST))
				.andExpect(body("user_id=11223&text=Hello+there%21")).andRespond(withResponse("{}", responseHeaders));
		twitter.directMessageOperations().sendDirectMessage(11223, "Hello there!");
		mockServer.verify();
	}
	
	@MediumTest
	public void testDeleteDirectMessage() {
		mockServer.expect(requestTo("https://api.twitter.com/1/direct_messages/destroy/42.json"))
				.andExpect(method(DELETE))
				.andRespond(withResponse(new ClassPathResource("directMessage.json", getClass()), responseHeaders));
		twitter.directMessageOperations().deleteDirectMessage(42L);
		mockServer.verify();
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
