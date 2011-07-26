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

import org.springframework.social.NotAuthorizedException;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class EventTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetInvitations() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/events?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-events"), responseHeaders));
		List<Invitation> events = facebook.eventOperations().getInvitations();
		assertInvitations(events);
	}
	
	@MediumTest
	public void testGetInvitations_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/events?offset=50&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-events"), responseHeaders));
		List<Invitation> events = facebook.eventOperations().getInvitations(50, 25);
		assertInvitations(events);
	}

	@SmallTest
	public void testGetInvitations_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().getInvitations();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetInvitations_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/events?offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-events"), responseHeaders));
		List<Invitation> events = facebook.eventOperations().getInvitations("123456789");
		assertInvitations(events);
	}
	
	@MediumTest
	public void testGetInvitations_forSpecificUser_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/events?offset=60&limit=30"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/user-events"), responseHeaders));
		List<Invitation> events = facebook.eventOperations().getInvitations("123456789", 60, 30);
		assertInvitations(events);
	}

	@SmallTest
	public void testGetInvitations_forSpecificUser_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().getInvitations("123456789");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetEvent() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/simple-event"), responseHeaders));
		Event event = facebook.eventOperations().getEvent("193482154020832");
		assertEquals("193482154020832", event.getId());
		assertEquals("100001387295207", event.getOwner().getId());
		assertEquals("Art Names", event.getOwner().getName());
		assertEquals("Breakdancing Class", event.getName());
		assertEquals(Event.Privacy.OPEN, event.getPrivacy());
		assertEquals(toDate("2011-03-30T14:30:00+0000"), event.getStartTime());
		assertEquals(toDate("2011-03-30T17:30:00+0000"), event.getEndTime());
		assertEquals(toDate("2011-03-30T14:30:28+0000"), event.getUpdatedTime());
		assertNull(event.getDescription());
		assertNull(event.getLocation());
	}
	
	@MediumTest
	public void testGetEvent_withLocationAndDescription() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/full-event"), responseHeaders));
		Event event = facebook.eventOperations().getEvent("193482154020832");
		assertEquals("193482154020832", event.getId());
		assertEquals("100001387295207", event.getOwner().getId());
		assertEquals("Art Names", event.getOwner().getName());
		assertEquals("Breakdancing Class", event.getName());
		assertEquals(Event.Privacy.SECRET, event.getPrivacy());
		assertEquals(toDate("2011-03-30T14:30:00+0000"), event.getStartTime());
		assertEquals(toDate("2011-03-30T17:30:00+0000"), event.getEndTime());
		assertEquals(toDate("2011-03-30T14:38:40+0000"), event.getUpdatedTime());
		assertEquals("Bring your best parachute pants!", event.getDescription());
		assertEquals("2400 Dunlavy Dr, Denton, TX", event.getLocation());
	}
	
	@MediumTest
	public void testCreateEvent() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/events"))
			.andExpect(method(POST))
			.andExpect(body("name=Test+Event&start_time=2011-04-01T15%3A30%3A00&end_time=2011-04-01T18%3A30%3A00"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"193482145020832\"}", responseHeaders));
		String eventId = facebook.eventOperations().createEvent("Test Event", "2011-04-01T15:30:00", "2011-04-01T18:30:00");
		assertEquals("193482145020832", eventId);
	}
	
	@SmallTest
	public void testCreateEvent_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().createEvent("Test Event", "2011-04-01T15:30:00", "2011-04-01T18:30:00");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testDeleteEvent() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789"))
			.andExpect(method(POST))
			.andExpect(body("method=delete"))
			.andRespond(withResponse("", responseHeaders));
		facebook.eventOperations().deleteEvent("123456789");
		mockServer.verify();
	}
	
	@SmallTest
	public void testDeleteEvent_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().deleteEvent("123456789");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testGetInvited() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/invited"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/invited"), responseHeaders));
		List<EventInvitee> invited = facebook.eventOperations().getInvited("193482154020832");
		assertEquals(3, invited.size());
		assertInvitee(invited.get(0), "100001387295207", "Art Names", RsvpStatus.ATTENDING);
		assertInvitee(invited.get(1), "738140579", "Craig Walls", RsvpStatus.UNSURE);
		assertInvitee(invited.get(2), "975041837", "Chuck Wagon", RsvpStatus.NOT_REPLIED);
	}
	
	@MediumTest
	public void testGetAttending() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/attending"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/attending"), responseHeaders));
		List<EventInvitee> invited = facebook.eventOperations().getAttending("193482154020832");
		assertEquals(3, invited.size());
		assertInvitee(invited.get(0), "100001387295207", "Art Names", RsvpStatus.ATTENDING);
		assertInvitee(invited.get(1), "738140579", "Craig Walls", RsvpStatus.ATTENDING);
		assertInvitee(invited.get(2), "975041837", "Chuck Wagon", RsvpStatus.ATTENDING);
	}
	
	@MediumTest
	public void testGetMaybeAttending() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/maybe"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/maybe-attending"), responseHeaders));
		List<EventInvitee> invited = facebook.eventOperations().getMaybeAttending("193482154020832");
		assertEquals(3, invited.size());
		assertInvitee(invited.get(0), "100001387295207", "Art Names", RsvpStatus.UNSURE);
		assertInvitee(invited.get(1), "738140579", "Craig Walls", RsvpStatus.UNSURE);
		assertInvitee(invited.get(2), "975041837", "Chuck Wagon", RsvpStatus.UNSURE);
	}
	
	@MediumTest
	public void testGetNoReplies() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/noreply"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/no-replies"), responseHeaders));
		List<EventInvitee> invited = facebook.eventOperations().getNoReplies("193482154020832");
		assertEquals(3, invited.size());
		assertInvitee(invited.get(0), "100001387295207", "Art Names", RsvpStatus.NOT_REPLIED);
		assertInvitee(invited.get(1), "738140579", "Craig Walls", RsvpStatus.NOT_REPLIED);
		assertInvitee(invited.get(2), "975041837", "Chuck Wagon", RsvpStatus.NOT_REPLIED);
	}
	
	@MediumTest
	public void testGetDeclined() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/declined"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/declined"), responseHeaders));
		List<EventInvitee> invited = facebook.eventOperations().getDeclined("193482154020832");
		assertEquals(3, invited.size());
		assertInvitee(invited.get(0), "100001387295207", "Art Names", RsvpStatus.DECLINED);
		assertInvitee(invited.get(1), "738140579", "Craig Walls", RsvpStatus.DECLINED);
		assertInvitee(invited.get(2), "975041837", "Chuck Wagon", RsvpStatus.DECLINED);
	}

	@MediumTest
	public void testAcceptInvitation() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/attending"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("true", responseHeaders));
		facebook.eventOperations().acceptInvitation("193482154020832");
		mockServer.verify();
	}
	
	@SmallTest
	public void testAcceptInvitation_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().acceptInvitation("123456789");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testMaybeInvitation() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/maybe"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("true", responseHeaders));
		facebook.eventOperations().maybeInvitation("193482154020832");
		mockServer.verify();
	}
	
	@SmallTest
	public void testMaybeInvitation_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().maybeInvitation("123456789");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testDeclineInvitation() {
		mockServer.expect(requestTo("https://graph.facebook.com/193482154020832/declined"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("true", responseHeaders));
		facebook.eventOperations().declineInvitation("193482154020832");
		mockServer.verify();
	}
	
	@SmallTest
	public void testDeclineInvitation_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.eventOperations().declineInvitation("123456789");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testSearch() {
		mockServer.expect(requestTo("https://graph.facebook.com/search?q=Spring+User+Group&type=event&offset=0&limit=25"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/event-list"), responseHeaders));
		List<Event> results = facebook.eventOperations().search("Spring User Group");
		assertEquals(1, results.size());
		assertEquals("196119297091135", results.get(0).getId());
		assertEquals("FLUG (Florida Local Users Group) Spring User Conference", results.get(0).getName());
		assertEquals("Radisson Resort at the Port", results.get(0).getLocation());
		assertEquals(toDate("2011-06-01T08:00:00+0000"), results.get(0).getStartTime());
		assertEquals(toDate("2011-06-03T16:00:00+0000"), results.get(0).getEndTime());
	}
	
	@MediumTest
	public void testSearch_withOffsetAndLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/search?q=Spring+User+Group&type=event&offset=30&limit=15"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/event-list"), responseHeaders));
		List<Event> results = facebook.eventOperations().search("Spring User Group", 30, 15);
		assertEquals(1, results.size());
		assertEquals("196119297091135", results.get(0).getId());
		assertEquals("FLUG (Florida Local Users Group) Spring User Conference", results.get(0).getName());
		assertEquals("Radisson Resort at the Port", results.get(0).getLocation());
		assertEquals(toDate("2011-06-01T08:00:00+0000"), results.get(0).getStartTime());
		assertEquals(toDate("2011-06-03T16:00:00+0000"), results.get(0).getEndTime());
	}
	
	private void assertInvitee(EventInvitee invitee, String id, String name, RsvpStatus rsvpStatus) {
		assertEquals(id, invitee.getId());
		assertEquals(name, invitee.getName());
		assertEquals(rsvpStatus, invitee.getRsvpStatus());
	}
	
	private void assertInvitations(List<Invitation> events) {
		assertEquals(2, events.size());
		assertEquals("188420717869087", events.get(0).getEventId());
		assertEquals("Afternoon naptime", events.get(0).getName());
		assertEquals("On the couch", events.get(0).getLocation());
		// Facebook event times don't have a timezone component, so they end up parsed as in +0000
		// Unfortunately, this is probably not the actual time of the event.
		assertEquals(toDate("2011-03-26T14:00:00+0000"), events.get(0).getStartTime());
		assertEquals(toDate("2011-03-26T15:00:00+0000"), events.get(0).getEndTime());
		assertEquals(RsvpStatus.ATTENDING, events.get(0).getRsvpStatus());
		assertEquals("188420717869780", events.get(1).getEventId());
		assertEquals("Mow the lawn", events.get(1).getName());
		assertNull(events.get(1).getLocation());
		assertEquals(toDate("2011-03-26T15:00:00+0000"), events.get(1).getStartTime());
		assertEquals(toDate("2011-03-26T16:00:00+0000"), events.get(1).getEndTime());
		assertEquals(RsvpStatus.NOT_REPLIED, events.get(1).getRsvpStatus());
	}
	
}
