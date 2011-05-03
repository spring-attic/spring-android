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

public class MediaTemplateTest extends AbstractFacebookApiTest {
	
	@MediumTest
	public void testGetAlbums() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/albums"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/albums.json", getClass()), responseHeaders));
		List<Album> albums = facebook.mediaOperations().getAlbums();
		assertAlbums(albums);
	}
	
	@MediumTest
	public void testGetAlbums_forSpecificUser() {
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/albums"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/albums.json", getClass()), responseHeaders));
		List<Album> albums = facebook.mediaOperations().getAlbums("192837465");
		assertAlbums(albums);
	}
	
	@MediumTest
	public void testGetAlbum() {
		mockServer.expect(requestTo("https://graph.facebook.com/10151447271460580"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/album.json", getClass()), responseHeaders));
		Album album = facebook.mediaOperations().getAlbum("10151447271460580");
		assertSingleAlbum(album);
	}
	
	@MediumTest
	public void testGetPhotos() {
		mockServer.expect(requestTo("https://graph.facebook.com/10151447271460580/photos"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/photos.json", getClass()), responseHeaders));
	
		List<Photo> photos = facebook.mediaOperations().getPhotos("10151447271460580");
		assertEquals(2, photos.size());
		assertSinglePhoto(photos.get(1));
		assertEquals("10150447271355580", photos.get(0).getId());
		assertEquals("738140579", photos.get(0).getFrom().getId());
		assertEquals("Craig Walls", photos.get(0).getFrom().getName());
		assertNull(photos.get(0).getName());
		assertNull(photos.get(0).getTags());
		assertEquals("http://a5.sphotos.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684114_n.jpg", photos.get(0).getSourceImage().getSource());
		assertEquals(400, photos.get(0).getSourceImage().getWidth());
		assertEquals(300, photos.get(0).getSourceImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684114_s.jpg", photos.get(0).getSmallImage().getSource());
		assertEquals(130, photos.get(0).getSmallImage().getWidth());
		assertEquals(97, photos.get(0).getSmallImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684114_a.jpg", photos.get(0).getAlbumImage().getSource());
		assertEquals(180, photos.get(0).getAlbumImage().getWidth());
		assertEquals(135, photos.get(0).getAlbumImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684114_t.jpg", photos.get(0).getTinyImage().getSource());
		assertEquals(75, photos.get(0).getTinyImage().getWidth());
		assertEquals(56, photos.get(0).getTinyImage().getHeight());
		assertEquals("http://www.facebook.com/photo.php?pid=17698198&id=738140578", photos.get(0).getLink());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjk.gif", photos.get(0).getIcon());
		assertEquals(1, (int) photos.get(0).getPosition());
		assertEquals(toDate("2011-03-24T21:36:06+0000"), photos.get(0).getCreatedTime());
		assertEquals(toDate("2011-03-24T21:37:43+0000"), photos.get(0).getUpdatedTime());
	}

	@MediumTest
	public void testGetPhoto() {
		mockServer.expect(requestTo("https://graph.facebook.com/10150447271355581"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/photo.json", getClass()), responseHeaders));
		assertSinglePhoto(facebook.mediaOperations().getPhoto("10150447271355581"));
	}

	@MediumTest
	public void testGetVideos() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/videos"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/videos.json", getClass()), responseHeaders));
		List<Video> videos = facebook.mediaOperations().getVideos();
		assertVideos(videos);
	}

	@MediumTest
	public void testGetVideos_forSpecificOwner() {
		mockServer.expect(requestTo("https://graph.facebook.com/100001387295207/videos"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/videos.json", getClass()), responseHeaders));
		List<Video> videos = facebook.mediaOperations().getVideos("100001387295207");
		assertVideos(videos);
	}

	private void assertVideos(List<Video> videos) {
		assertEquals(2, videos.size());
		Video video = videos.get(0);
		assertEquals("161503963905846", video.getId());
		assertEquals("100001387295207", video.getFrom().getId());
		assertEquals("Art Names", video.getFrom().getName());
		assertEquals("http://vthumb.ak.fbcdn.net/hvthumb-ak-ash2/50903_161504077239168_161503963905846_21174_1003_t.jpg", video.getPicture());
		assertEquals("<object width=\"400\" height=\"250\" ><param name=\"allowfullscreen\" value=\"true\" /><param name=\"movie\" value=\"http://www.facebook.com/v/161503963905846\" /><embed src=\"http://www.facebook.com/v/161503963905846\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\" width=\"400\" height=\"250\"></embed></object>", video.getEmbedHtml());
		assertEquals("http://b.static.ak.fbcdn.net/rsrc.php/v1/yD/r/DggDhA4z4tO.gif", video.getIcon());
		assertEquals("http://video.ak.fbcdn.net/cfs-ak-snc6/82226/704/161503963905846_41386.mp4?oh=131db79e0842f1c57940aa274b82d8fe&oe=4D95D900&__gda__=1301666048_11e66cf124ce537194b3f7b6ab86b579", video.getSource());
		assertEquals(toDate("2011-03-29T20:45:20+0000"), video.getCreatedTime());
		assertEquals(toDate("2011-03-29T20:45:20+0000"), video.getUpdatedTime());
		assertSingleVideo(videos.get(1));
	}
	
	@MediumTest
	public void testGetVideo() {
		mockServer.expect(requestTo("https://graph.facebook.com/161500020572907"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/video.json", getClass()), responseHeaders));
		Video video = facebook.mediaOperations().getVideo("161500020572907");
		assertSingleVideo(video);
	}

	private void assertSingleVideo(Video video) {
		assertEquals("161500020572907", video.getId());
		assertEquals("100001387295207", video.getFrom().getId());
		assertEquals("Art Names", video.getFrom().getName());
		assertEquals(1, video.getTags().size());
		assertEquals("100001387295207", video.getTags().get(0).getId());
		assertEquals("Art Names", video.getTags().get(0).getName());
		assertNull(video.getTags().get(0).getX());
		assertNull(video.getTags().get(0).getY());
		assertEquals("Just a test screen recording", video.getName());
		assertEquals("Nothing special...just for testing purposes.", video.getDescription());
		assertEquals("http://vthumb.ak.fbcdn.net/hvthumb-ak-ash2/158179_161500167239559_161500020572907_64114_872_t.jpg", video.getPicture());
		assertEquals("<object width=\"400\" height=\"250\" ><param name=\"allowfullscreen\" value=\"true\" /><param name=\"movie\" value=\"http://www.facebook.com/v/161500020572907\" /><embed src=\"http://www.facebook.com/v/161500020572907\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\" width=\"400\" height=\"250\"></embed></object>", video.getEmbedHtml());
		assertEquals("http://b.static.ak.fbcdn.net/rsrc.php/v1/yD/r/DggDhA4z4tO.gif", video.getIcon());
		assertEquals("http://video.ak.fbcdn.net/cfs-ak-snc6/80396/785/161500020572907_43024.mp4?oh=2d01ac0ffce931fecb8987ae02837fc6&oe=4D94E600&__gda__=1301603840_718156f2f2c257ebd7714b3b0ba5164e", video.getSource());
		assertEquals(toDate("2011-03-29T20:25:55+0000"), video.getCreatedTime());
		assertEquals(toDate("2011-03-29T20:25:55+0000"), video.getUpdatedTime());
	}
	
	private void assertSinglePhoto(Photo photo) {
		assertEquals("10150447271355581", photo.getId());
		assertEquals("738140579", photo.getFrom().getId());
		assertEquals("Craig Walls", photo.getFrom().getName());
		assertEquals("Cool picture", photo.getName());
		assertEquals(1, photo.getTags().size());
		assertEquals("738140579", photo.getTags().get(0).getId());
		assertEquals("Craig Walls", photo.getTags().get(0).getName());
		assertEquals((Integer) 47, photo.getTags().get(0).getX());
		assertEquals((Integer) 24, photo.getTags().get(0).getY());
		assertEquals("http://a5.sphotos.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684115_n.jpg", photo.getSourceImage().getSource());
		assertEquals(400, photo.getSourceImage().getWidth());
		assertEquals(300, photo.getSourceImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684115_s.jpg", photo.getSmallImage().getSource());
		assertEquals(130, photo.getSmallImage().getWidth());
		assertEquals(97, photo.getSmallImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684115_a.jpg", photo.getAlbumImage().getSource());
		assertEquals(180, photo.getAlbumImage().getWidth());
		assertEquals(135, photo.getAlbumImage().getHeight());
		assertEquals("http://photos-e.ak.fbcdn.net/hphotos-ak-snc6/200110_10150447271355580_738140579_17698198_7684115_t.jpg", photo.getTinyImage().getSource());
		assertEquals(75, photo.getTinyImage().getWidth());
		assertEquals(56, photo.getTinyImage().getHeight());
		assertEquals("http://www.facebook.com/photo.php?pid=17698198&id=738140579", photo.getLink());
		assertEquals("http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjl.gif", photo.getIcon());
		assertEquals(2, (int) photo.getPosition());
		assertEquals(toDate("2011-03-24T21:36:06+0000"), photo.getCreatedTime());
		assertEquals(toDate("2011-03-24T21:37:43+0000"), photo.getUpdatedTime());
	}
	

	private void assertAlbums(List<Album> albums) {
		assertEquals(3, albums.size());
		assertSingleAlbum(albums.get(0));
		assertEquals("10150694228040580", albums.get(1).getId());
		assertEquals("738140579", albums.get(1).getFrom().getId());
		assertEquals("Craig Walls", albums.get(1).getFrom().getName());
		assertEquals("http://www.facebook.com/album.php?aid=526031&id=738140579", albums.get(1).getLink());
		assertEquals("Profile Pictures", albums.get(1).getName());
		assertNull(albums.get(1).getDescription());
		assertNull(albums.get(1).getLocation());
		assertEquals(Album.Privacy.FRIENDS_OF_FRIENDS, albums.get(1).getPrivacy());
		assertEquals(Album.Type.PROFILE, albums.get(1).getType());
		assertEquals(5, albums.get(1).getCount());
		assertEquals(toDate("2010-10-22T20:22:51+0000"), albums.get(1).getCreatedTime());
		assertNull(albums.get(1).getUpdatedTime());

		assertEquals("247501695549", albums.get(2).getId());
		assertEquals("738140579", albums.get(2).getFrom().getId());
		assertEquals("Craig Walls", albums.get(2).getFrom().getName());
		assertEquals("http://www.facebook.com/album.php?aid=290408&id=738140579", albums.get(2).getLink());
		assertEquals("Photos on the go", albums.get(2).getName());
		assertNull(albums.get(2).getDescription());
		assertNull(albums.get(2).getLocation());
		assertEquals(Album.Privacy.EVERYONE, albums.get(2).getPrivacy());
		assertEquals(Album.Type.MOBILE, albums.get(2).getType());
		assertEquals(3, albums.get(2).getCount());
		assertEquals(toDate("2009-08-08T19:28:46+0000"), albums.get(2).getCreatedTime());
		assertEquals(toDate("2010-08-25T02:03:43+0000"), albums.get(2).getUpdatedTime());
	}

	private void assertSingleAlbum(Album album) {
		assertEquals("10151447271460580", album.getId());
		assertEquals("738140579", album.getFrom().getId());
		assertEquals("Craig Walls", album.getFrom().getName());
		assertEquals("http://www.facebook.com/album.php?aid=620722&id=738140579", album.getLink());
		assertEquals("Early Broncos", album.getName());
		assertNull(album.getDescription());
		assertEquals("Somewhere", album.getLocation());
		assertEquals(Album.Privacy.CUSTOM, album.getPrivacy());
		assertEquals(Album.Type.NORMAL, album.getType());
		assertEquals(1, album.getCount());
		assertEquals(toDate("2011-03-24T21:36:04+0000"), album.getCreatedTime());
		assertEquals(toDate("2011-03-24T22:00:12+0000"), album.getUpdatedTime());
	}

}
