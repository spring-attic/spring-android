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
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class UserTemplateTest extends AbstractFacebookApiTest {
	
	@MediumTest
	public void testGetUserProfile_authenticatedUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/me"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/full-profile.json", getClass()), responseHeaders));

		FacebookProfile profile = facebook.userOperations().getUserProfile();
		assertBasicProfileData(profile);
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals("http://www.facebook.com/habuma", profile.getLink());
		assertEquals("xyz123abc987", profile.getThirdPartyId());
		assertEquals(Integer.valueOf(-5), profile.getTimezone());  // should be -6 ???
		assertEquals(toDate("2010-08-22T00:01:59+0000"), profile.getUpdatedTime());
		assertTrue(profile.isVerified());
		assertEquals("Just some dude", profile.getAbout());
		assertEquals("I was born at a very early age.", profile.getBio());
		assertEquals("06/09/1971", profile.getBirthday());
		assertEquals("111762725508574", profile.getLocation().getId());
		assertEquals("Dallas, Texas", profile.getLocation().getName());
		assertEquals("107925612568471", profile.getHometown().getId());
		assertEquals("Plano, Texas", profile.getHometown().getName());
		assertEquals(1, profile.getInterestedIn().size());
		assertEquals("female", profile.getInterestedIn().get(0));
		assertEquals("Jedi", profile.getReligion());
		assertEquals("Galactic Republic", profile.getPolitical());
		assertEquals("\"May the force be with you.\" - Common Jedi greeting", profile.getQuotes());
		assertEquals("Married", profile.getRelationshipStatus());
		assertEquals("533477039", profile.getSignificantOther().getId());
		assertEquals("Raymie Walls", profile.getSignificantOther().getName());
		assertEquals("http://www.habuma.com", profile.getWebsite());
		assertEquals(3, profile.getInspirationalPeople().size());
		assertEquals("121966051173827", profile.getInspirationalPeople().get(0).getId());
		assertEquals("Homer Simpson", profile.getInspirationalPeople().get(0).getName());
		assertEquals("44596990399", profile.getInspirationalPeople().get(1).getId());
		assertEquals("Alice Cooper", profile.getInspirationalPeople().get(1).getName());
		assertEquals("56368119740", profile.getInspirationalPeople().get(2).getId());
		assertEquals("Captain Jack Sparrow", profile.getInspirationalPeople().get(2).getName());
		assertEquals(2, profile.getLanguages().size());
		assertEquals("106059522759137", profile.getLanguages().get(0).getId());
		assertEquals("English", profile.getLanguages().get(0).getName());
		assertEquals("113599388650247", profile.getLanguages().get(1).getId());
		assertEquals("Klingon", profile.getLanguages().get(1).getName());
		assertEquals(1, profile.getSports().size());
		assertEquals("114371035246890", profile.getSports().get(0).getId());
		assertEquals("Ping Pong", profile.getSports().get(0).getName());
		assertEquals(3, profile.getFavoriteTeams().size());
		assertEquals("37152881613", profile.getFavoriteTeams().get(0).getId());
		assertEquals("Chicago Bulls", profile.getFavoriteTeams().get(0).getName());
		assertEquals("159957123994", profile.getFavoriteTeams().get(1).getId());
		assertEquals("Oklahoma City Thunder", profile.getFavoriteTeams().get(1).getName());
		assertEquals("92774416228", profile.getFavoriteTeams().get(2).getId());
		assertEquals("Baltimore Ravens", profile.getFavoriteTeams().get(2).getName());
		assertEquals(3, profile.getFavoriteAtheletes().size());
		assertEquals("107670255929059", profile.getFavoriteAtheletes().get(0).getId());
		assertEquals("Emmitt Smith", profile.getFavoriteAtheletes().get(0).getName());
		assertEquals("108193242541968", profile.getFavoriteAtheletes().get(1).getId());
		assertEquals("Cal Ripken, Jr.", profile.getFavoriteAtheletes().get(1).getName());
		assertEquals("62975399193", profile.getFavoriteAtheletes().get(2).getId());
		assertEquals("Michael Jordan", profile.getFavoriteAtheletes().get(2).getName());
		assertWorkHistory(profile.getWork());
		assertEducationHistory(profile.getEducation());
	}

	@MediumTest
	public void testGetUserProfile_specificUserByUserId() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/minimal-profile.json", getClass()), responseHeaders));

		FacebookProfile profile = facebook.userOperations().getUserProfile("123456789");
		assertBasicProfileData(profile);
	}
	
	@MediumTest
	public void testGetUserProfileImage() {
		responseHeaders.setContentType(MediaType.IMAGE_JPEG);
		mockServer.expect(requestTo("https://graph.facebook.com/me/picture?type=normal"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/tinyrod.jpg", getClass()), responseHeaders));
		facebook.userOperations().getUserProfileImage();
		// TODO: Fix mock server handle binary data so we can test contents (or at least size) of image data.
		mockServer.verify();
	}
	
	@MediumTest
	public void testGetUserProfileImage_specificUserByUserId() {
		responseHeaders.setContentType(MediaType.IMAGE_JPEG);
		mockServer.expect(requestTo("https://graph.facebook.com/1234567/picture?type=normal"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/tinyrod.jpg", getClass()), responseHeaders));
		facebook.userOperations().getUserProfileImage("1234567");
		// TODO: Fix mock server handle binary data so we can test contents (or at least size) of image data.
		mockServer.verify();
	}
	
	private void assertBasicProfileData(FacebookProfile profile) {
		assertEquals("123456789", profile.getId());
		assertEquals("Craig", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertEquals("Craig Walls", profile.getName());
		assertEquals(Locale.US, profile.getLocale());
		assertEquals("male", profile.getGender());
	}

	private void assertEducationHistory(List<EducationEntry> educationHistory) {
		assertEquals(2, educationHistory.size());
		assertEquals("College", educationHistory.get(0).getType());
		assertEquals("103768553006294", educationHistory.get(0).getSchool().getId());
		assertEquals("New Mexico", educationHistory.get(0).getSchool().getName());
		assertEquals("117348274968344", educationHistory.get(0).getYear().getId());
		assertEquals("1994", educationHistory.get(0).getYear().getName());
		assertEquals("High School", educationHistory.get(1).getType());
		assertEquals("115157218496067", educationHistory.get(1).getSchool().getId());
		assertEquals("Jal High School", educationHistory.get(1).getSchool().getName());
		assertEquals("127132740657422", educationHistory.get(1).getYear().getId());
		assertEquals("1989", educationHistory.get(1).getYear().getName());
	}

	private void assertWorkHistory(List<WorkEntry> workHistory) {
		assertEquals(2, workHistory.size());
		assertEquals("119387448093014", workHistory.get(0).getEmployer().getId());
		assertEquals("SpringSource", workHistory.get(0).getEmployer().getName());
		assertEquals("0000-00", workHistory.get(0).getStartDate());
		assertEquals("0000-00", workHistory.get(0).getEndDate());
		assertEquals("298846151879", workHistory.get(1).getEmployer().getId());
		assertEquals("Improving", workHistory.get(1).getEmployer().getName());
		assertEquals("2009-03", workHistory.get(1).getStartDate());
		assertEquals("2010-05", workHistory.get(1).getEndDate());
	}

}
