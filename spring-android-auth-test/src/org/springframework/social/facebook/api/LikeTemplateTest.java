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

import android.test.suitebuilder.annotation.MediumTest;

public class LikeTemplateTest extends AbstractFacebookApiTest {

	@MediumTest
	public void testGetLikes() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/likes")).andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/user-likes.json", getClass()), responseHeaders));
		List<UserLike> likes = facebook.likeOperations().getLikes();
		assertLikes(likes);
	}

	@MediumTest
	public void testGetLikes_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456789/likes")).andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/user-likes.json", getClass()), responseHeaders));
		List<UserLike> likes = facebook.likeOperations().getLikes("123456789");
		assertLikes(likes);
	}
	
	@MediumTest
	public void testLike() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/likes"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{}", responseHeaders));
		facebook.likeOperations().like("123456");
		mockServer.verify();
	}
	
	@MediumTest
	public void testUnlike() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/likes"))
			.andExpect(method(POST))
			.andExpect(body("method=delete"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{}", responseHeaders));
		facebook.likeOperations().unlike("123456");
		mockServer.verify();
	}

	private void assertLikes(List<UserLike> likes) {
		assertEquals(3, likes.size());
		UserLike like1 = likes.get(0);
		assertEquals("113294925350820", like1.getId());
		assertEquals("Pirates of the Caribbean", like1.getName());
		assertEquals("Movie", like1.getCategory());
		UserLike like2 = likes.get(1);
		assertEquals("38073733123", like2.getId());
		assertEquals("Dublin Dr Pepper", like2.getName());
		assertEquals("Company", like2.getCategory());
		UserLike like3 = likes.get(2);
		assertEquals("10264922373", like3.getId());
		assertEquals("Freebirds World Burrito", like3.getName());
		assertEquals("Restaurant/cafe", like3.getCategory());
	}

}
