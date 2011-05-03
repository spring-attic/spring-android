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
import static org.springframework.social.test.client.RequestMatchers.header;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;

import android.test.suitebuilder.annotation.MediumTest;


public class GroupTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetGroup() {
		mockServer.expect(requestTo("https://graph.facebook.com/213106022036379"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/group.json", getClass()), responseHeaders));
		
		Group group = facebook.groupOperations().getGroup("213106022036379");
		assertEquals("213106022036379", group.getId());
		assertEquals("Test Group", group.getName());
		assertEquals("Just a test group", group.getDescription());
		assertEquals("738140579", group.getOwner().getId());
		assertEquals("Craig Walls", group.getOwner().getName());
		assertEquals(Group.Privacy.SECRET, group.getPrivacy());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yN/r/IPw3LB5BsPK.png", group.getIcon());
		assertEquals(toDate("2011-03-30T19:24:59+0000"), group.getUpdatedTime());
		assertEquals("213106022036379@groups.facebook.com", group.getEmail());
	}
	
	@MediumTest
	public void testGetMembers() {
		mockServer.expect(requestTo("https://graph.facebook.com/213106022036379/members"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/group-members.json", getClass()), responseHeaders));
		List<GroupMemberReference> members = facebook.groupOperations().getMembers("213106022036379");
		assertEquals(3, members.size());
		assertEquals("100001387295207", members.get(0).getId());
		assertEquals("Art Names", members.get(0).getName());
		assertFalse(members.get(0).isAdministrator());
		assertEquals("738140579", members.get(1).getId());
		assertEquals("Craig Walls", members.get(1).getName());
		assertTrue(members.get(1).isAdministrator());
		assertEquals("627039468", members.get(2).getId());
		assertEquals("Chuck Wagon", members.get(2).getName());
		assertTrue(members.get(2).isAdministrator());
	}
}
