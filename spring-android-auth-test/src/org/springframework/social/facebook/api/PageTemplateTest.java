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

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.social.NotAuthorizedException;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Craig Walls
 * @author Roy Clarkson
 */
public class PageTemplateTest extends AbstractFacebookApiTest {
	
	@MediumTest
	public void testGetPage_organization() {
		mockServer.expect(requestTo("https://graph.facebook.com/140804655931206"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/organization-page"), responseHeaders));
	
		Page page = facebook.pageOperations().getPage("140804655931206");
		assertEquals("140804655931206", page.getId());
		assertEquals("SpringSource", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yr/r/fwJFrO5KjAQ.png", page.getPicture());
		assertEquals("http://www.facebook.com/pages/SpringSource/140804655931206", page.getLink());
		assertEquals(33, page.getLikes());
		assertEquals("Organization", page.getCategory());
		assertEquals("<p><b>SpringSource</b> is a division of <a href=\"http://en.wikipedia.org/wiki/VMware\" class=\"wikipedia\">VMware</a> that provides...</p>", page.getDescription());
	}

	@MediumTest
	public void testGetPage_product() {
		mockServer.expect(requestTo("https://graph.facebook.com/21278871488"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/product-page"), responseHeaders));

		Page page = facebook.pageOperations().getPage("21278871488");
		assertEquals("21278871488", page.getId());
		assertEquals("Mountain Dew", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/hprofile-ak-snc4/203494_21278871488_3106566_s.jpg", page.getPicture());
		assertEquals("http://www.facebook.com/mountaindew", page.getLink());
		assertEquals(5083988, page.getLikes());
		assertEquals("Food/beverages", page.getCategory());
		assertEquals("www.mountaindew.com\nwww.greenlabelsound.com\nwww.greenlabelart.com\nwww.honorthecode.com\nwww.dietdewchallenge.com\nwww.twitter.com/mtn_dew\nwww.youtube.com/mountaindew", page.getWebsite());
	}

	@MediumTest
	public void testGetPage_place() {
		mockServer.expect(requestTo("https://graph.facebook.com/150263434985489"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/place-page"), responseHeaders));

		Page page = facebook.pageOperations().getPage("150263434985489");
		assertEquals("150263434985489", page.getId());
		assertEquals("Denver International Airport", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yZ/r/u3l2nEuXNsK.png", page.getPicture());
		assertEquals("http://www.facebook.com/pages/Denver-International-Airport/150263434985489", page.getLink());
		assertEquals(1052, page.getLikes());
		assertEquals("Local business", page.getCategory());
		assertEquals("http://flydenver.com", page.getWebsite());
		assertEquals("Denver", page.getLocation().getCity());
		assertEquals("CO", page.getLocation().getState());
		assertEquals("United States", page.getLocation().getCountry());
		assertEquals(39.851693483111, page.getLocation().getLatitude(), 0.0001);
		assertEquals(-104.67384947947, page.getLocation().getLongitude(), 0.0001);
		assertEquals("(303) 342-2000", page.getPhone());
		assertEquals(121661, page.getCheckins());
	}

	@MediumTest
	public void testGetPage_application() {
		mockServer.expect(requestTo("https://graph.facebook.com/140372495981006"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(jsonResource("testdata/application-page"), responseHeaders));

		Page page = facebook.pageOperations().getPage("140372495981006");
		assertEquals("140372495981006", page.getId());
		assertEquals("Greenhouse", page.getName());
		assertEquals("http://www.facebook.com/apps/application.php?id=140372495981006", page.getLink());
		assertEquals("The social destination for Spring application developers.", page.getDescription());
	}
	
	@MediumTest
	public void testIsPageAdmin() {
		expectFetchAccounts();
		assertFalse(facebook.pageOperations().isPageAdmin("2468013579"));
		assertTrue(facebook.pageOperations().isPageAdmin("987654321"));
		assertTrue(facebook.pageOperations().isPageAdmin("1212121212"));
	}
	

	@MediumTest
	public void testIsPageAdmin_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.pageOperations().isPageAdmin("2468013579");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@SmallTest
	public void testGetAccounts() {
		expectFetchAccounts();
		List<Account> accounts = facebook.pageOperations().getAccounts();
		assertEquals(2, accounts.size());
		assertEquals("987654321", accounts.get(0).getId());
		assertEquals("Test Page", accounts.get(0).getName());
		assertEquals("Page", accounts.get(0).getCategory());
		assertEquals("pageAccessToken", accounts.get(0).getAccessToken());
		assertEquals("1212121212", accounts.get(1).getId());
		assertEquals("Test Page 2", accounts.get(1).getName());
		assertEquals("Page", accounts.get(1).getCategory());
		assertEquals("page2AccessToken", accounts.get(1).getAccessToken());
	}

	@MediumTest
	public void testPost_message() throws Exception {
		expectFetchAccounts();
		String requestBody = "message=Hello+Facebook+World&access_token=pageAccessToken";
		mockServer.expect(requestTo("https://graph.facebook.com/987654321/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		assertEquals("123456_78901234", facebook.pageOperations().post("987654321", "Hello Facebook World"));
		mockServer.verify();
	}

	@MediumTest
	public void testPostMessage_notAdmin() throws Exception {
		boolean success = false;
		try {
			expectFetchAccounts();
			facebook.pageOperations().post("2468013579", "Hello Facebook World");
		} catch (PageAdministrationException e) {
			success = true;
		}
		assertTrue("Expected PageAdministrationException", success);
	}

	@MediumTest
	public void testPostMessage_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.pageOperations().post("2468013579", "Hello Facebook World");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testPostLink() throws Exception {
		expectFetchAccounts();
		String requestBody = "link=someLink&name=some+name&caption=some+caption&description=some+description&message=Hello+Facebook+World&access_token=pageAccessToken";
		mockServer.expect(requestTo("https://graph.facebook.com/987654321/feed")).andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
		assertEquals("123456_78901234", facebook.pageOperations().post("987654321", "Hello Facebook World", link));
		mockServer.verify();
	}

	@MediumTest
	public void testPostLink_notAdmin() throws Exception {
		boolean success = false;
		try {
			expectFetchAccounts();
			FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
			facebook.pageOperations().post("2468013579", "Hello Facebook World", link);
		} catch (PageAdministrationException e) {
			success = true;
		}
		assertTrue("Expected PageAdministrationException", success);
	}

	@MediumTest
	public void testPostLink_unauthorized() {
		boolean success = false;
		try {
			FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
			unauthorizedFacebook.pageOperations().post("2468013579", "Hello Facebook World", link);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testPostPhoto_noCaption() {
		expectFetchAccounts();
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/photos"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"12345\"}", responseHeaders));
		// TODO: Match body content to ensure fields and photo are included
		Resource photo = getUploadResource("photo.jpg", "PHOTO DATA");
		String photoId = facebook.pageOperations().postPhoto("987654321", "192837465", photo);
		assertEquals("12345", photoId);
	}

	@MediumTest
	public void testPostPhoto_noCaption_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.pageOperations().postPhoto("987654321", "192837465", null);
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testPostPhoto_withCaption() {
		expectFetchAccounts();
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/photos"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"12345\"}", responseHeaders));
		// TODO: Match body content to ensure fields and photo are included
		Resource photo = getUploadResource("photo.jpg", "PHOTO DATA");
		String photoId = facebook.pageOperations().postPhoto("987654321", "192837465", photo, "Some caption");
		assertEquals("12345", photoId);
	}
	
	@MediumTest
	public void testPostPhoto_withCaption_unauthorized() {
		boolean success = false;
		try {
			unauthorizedFacebook.pageOperations().postPhoto("987654321", "192837465", null, "Some caption");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	// private helpers
	
	private void expectFetchAccounts() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/accounts"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
                .andRespond(withResponse(jsonResource("testdata/accounts"), responseHeaders));
	}

	private Resource getUploadResource(final String filename, String content) {
		Resource video = new ByteArrayResource(content.getBytes()) {
			public String getFilename() throws IllegalStateException {
				return filename;
			};
		};
		return video;
	}

}
