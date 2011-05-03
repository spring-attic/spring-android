/*
 * Copyright 2010 the original author or authors.
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

/**
 * @author Craig Walls
 */
public class CommentTemplateTest extends AbstractFacebookApiTest {
	
	@MediumTest
	public void testGetComments() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/comments"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/comments.json", getClass()), responseHeaders));
		
		List<Comment> comments = facebook.commentOperations().getComments("123456");
		assertEquals(2, comments.size());
		Comment comment1 = comments.get(0);
		assertEquals("1533260333", comment1.getFrom().getId());
		assertEquals("Art Names", comment1.getFrom().getName());
		assertEquals("Howdy!", comment1.getMessage());
		Comment comment2 = comments.get(1);
		assertEquals("638140578", comment2.getFrom().getId());
		assertEquals("Chuck Wagon", comment2.getFrom().getName());
		assertEquals("The world says hello back", comment2.getMessage());
	}
	
	@MediumTest
	public void testGetComment() {
		mockServer.expect(requestTo("https://graph.facebook.com/1533260333_122829644452184_587062"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/comment.json", getClass()), responseHeaders));
		Comment comment = facebook.commentOperations().getComment("1533260333_122829644452184_587062");
		assertEquals("1533260333", comment.getFrom().getId());
		assertEquals("Art Names", comment.getFrom().getName());
		assertEquals("Howdy!", comment.getMessage());
		assertNull(comment.getLikes());		
		assertEquals(4, comment.getLikesCount());
	}
	
	@MediumTest
	public void testPostComment() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/comments"))
			.andExpect(method(POST))
			.andExpect(body("message=Cool+beans"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"123456_543210\"}", responseHeaders));
		assertEquals("123456_543210", facebook.commentOperations().addComment("123456", "Cool beans"));
	}
	
	@MediumTest
	public void testDeleteComment() {
		mockServer.expect(requestTo("https://graph.facebook.com/1533260333_122829644452184_587062"))
			.andExpect(method(POST))
			.andExpect(body("method=delete"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{}", responseHeaders));
		facebook.commentOperations().deleteComment("1533260333_122829644452184_587062");
		mockServer.verify();
	}
	
	@MediumTest
	public void testGetLikes() {
		mockServer.expect(requestTo("https://graph.facebook.com/123456/likes")).andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withResponse(new ClassPathResource("testdata/likes.json", getClass()), responseHeaders));
		List<Reference> likes = facebook.commentOperations().getLikes("123456");
		assertEquals(3, likes.size());
		Reference like1 = likes.get(0);
		assertEquals("1122334455", like1.getId());
		assertEquals("Jack Bauer", like1.getName());
		Reference like2 = likes.get(1);
		assertEquals("5544332211", like2.getId());
		assertEquals("Chuck Norris", like2.getName());
		Reference like3 = likes.get(2);
		assertEquals("1324354657", like3.getId());
		assertEquals("Edmund Blackadder", like3.getName());
	}

}
