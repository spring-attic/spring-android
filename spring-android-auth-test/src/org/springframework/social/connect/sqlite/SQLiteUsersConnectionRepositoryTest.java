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
package org.springframework.social.connect.sqlite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.AndroidKeyGenerators;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1ServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.oauth2.OAuth2ServiceProvider;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Roy Clarkson
 */
public class SQLiteUsersConnectionRepositoryTest extends AndroidTestCase {

	private ConnectionFactoryRegistry connectionFactoryRegistry;
	
	private TestFacebookConnectionFactory connectionFactory;
	
	private TextEncryptor textEncryptor;
	
	private SQLiteOpenHelper repositoryHelper;

	private SQLiteUsersConnectionRepository usersConnectionRepository;

	private ConnectionRepository connectionRepository; 

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        // creates the database, if it does not exist
		repositoryHelper = new SQLiteConnectionRepositoryHelper(getContext());
		
		// clear out any existing connections in the database
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		db.delete("UserConnection", null, null);
		db.close();
		
		connectionFactoryRegistry = new ConnectionFactoryRegistry();
		connectionFactory = new TestFacebookConnectionFactory();
		connectionFactoryRegistry.addConnectionFactory(connectionFactory);
		
		 // generates a random 8-byte salt that is then hex-encoded
		String salt = AndroidKeyGenerators.string().generateKey();
		String password = "Unit tests are cool!";
		textEncryptor = AndroidEncryptors.text(password, salt);
		usersConnectionRepository = new SQLiteUsersConnectionRepository(repositoryHelper, connectionFactoryRegistry, textEncryptor);
		connectionRepository = usersConnectionRepository.createConnectionRepository("1");
	}
	
	@Override
	public void tearDown() {
		connectionFactoryRegistry = null;
		connectionFactory = null;
		textEncryptor = null;
		repositoryHelper = null;
		usersConnectionRepository = null;
		
		if (repositoryHelper != null) {
			repositoryHelper.close();
		}
	}

	@MediumTest
	public void testFindUserIdConnectedTo() {
		insertFacebookConnection();
		String userId = usersConnectionRepository.findUserIdWithConnection(connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class));
		assertEquals("1", userId);
	}
	
	@MediumTest
	public void testFindUserIdConnectedToNoSuchConnection() {
		Connection<TestFacebookApi> connection = connectionFactory.createConnection(new AccessGrant("12345"));
		assertNull(usersConnectionRepository.findUserIdWithConnection(connection));
	}
	
	@MediumTest
	public void testFindUserIdMultipleConnectionsToSameProviderUser() {
		insertFacebookConnection();
		insertFacebookConnectionSameFacebookUser();
		assertNull(usersConnectionRepository.findUserIdWithConnection(connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class)));
	}
	
	@MediumTest
	public void testFindUserIdsConnectedTo() {
		insertFacebookConnection();
		insertFacebookConnection3();
		Set<String> userIds = usersConnectionRepository.findUserIdsConnectedTo("facebook", new HashSet<String>(Arrays.asList("9", "11")));
		assertEquals(2, userIds.size());
		assertTrue(userIds.contains("1"));
		assertTrue(userIds.contains("2"));        
	}
	
	@MediumTest
	@SuppressWarnings("unchecked")
	public void testFindConnectionsToProviders() {
		connectionFactoryRegistry.addConnectionFactory(new TestTwitterConnectionFactory());
		insertTwitterConnection();
		insertFacebookConnection();
		MultiValueMap<String, Connection<?>> connections = connectionRepository.findConnections();
		assertEquals(2, connections.size());
		Connection<TestFacebookApi> facebook = (Connection<TestFacebookApi>) connections.getFirst("facebook");
		assertFacebookConnection(facebook);
		Connection<TestTwitterApi> twitter = (Connection<TestTwitterApi>) connections.getFirst("twitter");
		assertTwitterConnection(twitter);
	}

	@MediumTest
	public void testFindAllConnectionsEmptyResult() {
		connectionFactoryRegistry.addConnectionFactory(new TestTwitterConnectionFactory());
		MultiValueMap<String, Connection<?>> connections = connectionRepository.findConnections();
		assertEquals(2, connections.size());
		assertEquals(0, connections.get("facebook").size());
		assertEquals(0, connections.get("twitter").size());		
	}

	@MediumTest
	public void testFindAllConnectionsNoProviderRegistered() {
		boolean success = false;
		try {
			insertTwitterConnection();
			connectionRepository.findConnections();
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue(success);
	}

	@MediumTest
	@SuppressWarnings("unchecked")
	public void testFindConnectionsToProvider() {
		connectionFactoryRegistry.addConnectionFactory(new TestTwitterConnectionFactory());
		insertTwitterConnection();
		List<Connection<?>> connections = connectionRepository.findConnectionsToProvider("twitter");
		assertEquals(1, connections.size());
		assertTwitterConnection((Connection<TestTwitterApi>) connections.get(0));
	}
	
	@MediumTest
	public void testFindConnectionsToProviderEmptyResult() {
		assertTrue(connectionRepository.findConnectionsToProvider("facebook").isEmpty());
	}

	@MediumTest
	@SuppressWarnings("unchecked")
	public void testFindConnectionsForUsersEmpty() {
		connectionFactoryRegistry.addConnectionFactory(new TestTwitterConnectionFactory());
		insertTwitterConnection();
		insertFacebookConnection();
		insertFacebookConnection2();
		MultiValueMap<String, String> providerUsers = new LinkedMultiValueMap<String, String>();
		providerUsers.add("facebook", "10");
		providerUsers.add("facebook", "9");
		providerUsers.add("twitter", "1");
		MultiValueMap<String, Connection<?>> connectionsForUsers = connectionRepository.findConnectionsForUsers(providerUsers);
		assertEquals(2, connectionsForUsers.size());
		assertEquals("10", connectionsForUsers.getFirst("facebook").getKey().getProviderUserId());
		assertFacebookConnection((Connection<TestFacebookApi>) connectionsForUsers.get("facebook").get(1));
		assertTwitterConnection((Connection<TestTwitterApi>) connectionsForUsers.getFirst("twitter"));
	}
	
	@MediumTest
	public void testFindConnectionsForUsersEmptyResult() {
		MultiValueMap<String, String> providerUsers = new LinkedMultiValueMap<String, String>();
		providerUsers.add("facebook", "1");
		assertTrue(connectionRepository.findConnectionsForUsers(providerUsers).isEmpty());
	}
	
	@MediumTest
	public void testFindConnectionsForUsersEmptyInput() {
		boolean success = false;
		try {
			MultiValueMap<String, String> providerUsers = new LinkedMultiValueMap<String, String>();
			connectionRepository.findConnectionsForUsers(providerUsers);
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue(success);
	}
	
	@MediumTest
	@SuppressWarnings("unchecked")
	public void testFindConnection() {
		insertFacebookConnection();
		assertFacebookConnection((Connection<TestFacebookApi>) connectionRepository.findConnection(new ConnectionKey("facebook", "9")));
	}
	
	@MediumTest
	public void testFindConnectionNoSuchConnection() {
		boolean success = false;
		try {
			connectionRepository.findConnection(new ConnectionKey("facebook", "bogus"));
		} catch (NoSuchConnectionException e) {
			success = true;
		}
		assertTrue(success);
	}

	@MediumTest
	public void testFindPrimaryConnectionToApi() {
		insertFacebookConnection();
		assertFacebookConnection(connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class));
	}

	@MediumTest
	public void testFindPrimaryConnectionToApiSelectFromMultipleByRank() {
		insertFacebookConnection2();
		insertFacebookConnection();
		assertFacebookConnection(connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class));
	}

	public void testFindPrimaryConnectionToApiNoSuchConnection() {
		assertNull(connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class));
	}

	@MediumTest
	public void testFindConnectionsToApi() {
		insertFacebookConnection();
		insertFacebookConnection2();
		List<Connection<TestFacebookApi>> connections = connectionRepository.findConnectionsToApi(TestFacebookApi.class);
		assertEquals(2, connections.size());
		assertFacebookConnection(connections.get(0));
	}
	
	@MediumTest
	public void testFindConnectionToApiForUser() {
		insertFacebookConnection();
		insertFacebookConnection2();	
		assertFacebookConnection(connectionRepository.findConnectionToApiForUser(TestFacebookApi.class, "9"));
		assertEquals("10", connectionRepository.findConnectionToApiForUser(TestFacebookApi.class, "10").getKey().getProviderUserId());
	}

	@MediumTest
	public void testFindConnectionToApiForUserNoSuchConnection() {
		boolean success = false;
		try {
			assertFacebookConnection(connectionRepository.findConnectionToApiForUser(TestFacebookApi.class, "9"));
		} catch (NoSuchConnectionException e) {
			success = true;
		}
		assertTrue(success);
	}
	
	@MediumTest
	public void testRemoveConnectionsToProvider() {
		insertFacebookConnection();
		insertFacebookConnection2();
		assertTrue(queryConnectionExists("facebook"));
		connectionRepository.removeConnectionsToProvider("facebook");
		assertFalse(queryConnectionExists("facebook"));
	}
	
	@MediumTest
	public void testRemoveConnectionsToProviderNoOp() {
		connectionRepository.removeConnectionsToProvider("twitter");
	}

	@MediumTest
	public void testRemoveConnection() {
		insertFacebookConnection();
		assertTrue(queryConnectionExists("facebook"));
		connectionRepository.removeConnection(new ConnectionKey("facebook", "9"));
		assertFalse(queryConnectionExists("facebook"));		
	}

	@MediumTest
	public void testRemoveConnectionNoOp() {
		connectionRepository.removeConnection(new ConnectionKey("facebook", "1"));
	}

	@MediumTest
	public void testAddConnection() {
		Connection<TestFacebookApi> connection = connectionFactory.createConnection(new AccessGrant("123456789", null, "987654321", 3600));
		connectionRepository.addConnection(connection);
		Connection<TestFacebookApi> restoredConnection = connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class);
		assertEquals(connection, restoredConnection);	
		assertNewConnection(restoredConnection);
	}
	
	@MediumTest
	public void testAddConnectionDuplicate() {
		boolean success = false;
		try {
			Connection<TestFacebookApi> connection = connectionFactory.createConnection(new AccessGrant("123456789", null, "987654321", 3600));
			connectionRepository.addConnection(connection);
			connectionRepository.addConnection(connection);
		} catch (DuplicateConnectionException e) {
			success = true;
		}
		assertTrue(success);
	}
	
	@MediumTest
	public void testUpdateConnectionProfileFields() {
		connectionFactoryRegistry.addConnectionFactory(new TestTwitterConnectionFactory());		
		insertTwitterConnection();
		Connection<TestTwitterApi> twitter = connectionRepository.findPrimaryConnectionToApi(TestTwitterApi.class);
		assertEquals("http://twitter.com/kdonald/picture", twitter.getImageUrl());
		twitter.sync();
		assertEquals("http://twitter.com/kdonald/a_new_picture", twitter.getImageUrl());
		connectionRepository.updateConnection(twitter);
		Connection<TestTwitterApi> twitter2 = connectionRepository.findPrimaryConnectionToApi(TestTwitterApi.class);
		assertEquals("http://twitter.com/kdonald/a_new_picture", twitter2.getImageUrl());
	}
	
	@MediumTest
	public void testUpdateConnectionAccessFields() {
		insertFacebookConnection();
		Connection<TestFacebookApi> facebook = connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class);
		assertEquals("234567890", facebook.getApi().getAccessToken());
		facebook.refresh();
		connectionRepository.updateConnection(facebook);
		Connection<TestFacebookApi> facebook2 = connectionRepository.findPrimaryConnectionToApi(TestFacebookApi.class);
		assertEquals("765432109", facebook2.getApi().getAccessToken());
		ConnectionData data = facebook.createData();
		assertEquals("654321098", data.getRefreshToken());
	}

		
	private void insertTwitterConnection() {
		ContentValues values = new ContentValues();
		values.put("userId", "1");
		values.put("providerId", "twitter");
		values.put("providerUserId", "1");
		values.put("rank", 1);
		values.put("displayName", "@kdonald");
		values.put("profileUrl", "http://twitter.com/kdonald");
		values.put("imageUrl", "http://twitter.com/kdonald/picture");
		values.put("accessToken", encrypt("123456789"));
		values.put("secret", encrypt("987654321"));
		values.putNull("refreshToken");
		values.putNull("expireTime");
		insertConnection(values);
	}
	
	private void insertFacebookConnection() {
		ContentValues values = new ContentValues();
		values.put("userId", "1");
		values.put("providerId", "facebook");
		values.put("providerUserId", "9");
		values.put("rank", 1);
		values.putNull("displayName");
		values.putNull("profileUrl");
		values.putNull("imageUrl");
		values.put("accessToken", encrypt("234567890"));
		values.putNull("secret");
		values.put("refreshToken", encrypt("345678901"));
		values.put("expireTime", System.currentTimeMillis() + 3600000);
		insertConnection(values);
	}
	
	private void insertFacebookConnection2() {
		ContentValues values = new ContentValues();
		values.put("userId", "1");
		values.put("providerId", "facebook");
		values.put("providerUserId", "10");
		values.put("rank", 2);
		values.putNull("displayName");
		values.putNull("profileUrl");
		values.putNull("imageUrl");
		values.put("accessToken", encrypt("456789012"));
		values.putNull("secret");
		values.put("refreshToken", encrypt("56789012"));
		values.put("expireTime", System.currentTimeMillis() + 3600000);
		insertConnection(values);
	}
	
	private void insertFacebookConnection3() {
		ContentValues values = new ContentValues();
		values.put("userId", "2");
		values.put("providerId", "facebook");
		values.put("providerUserId", "11");
		values.put("rank", 2);
		values.putNull("displayName");
		values.putNull("profileUrl");
		values.putNull("imageUrl");
		values.put("accessToken", encrypt("456789012"));
		values.putNull("secret");
		values.put("refreshToken", encrypt("56789012"));
		values.put("expireTime", System.currentTimeMillis() + 3600000);
		insertConnection(values);
	}
	
	private void insertFacebookConnectionSameFacebookUser() {
		ContentValues values = new ContentValues();
		values.put("userId", "2");
		values.put("providerId", "facebook");
		values.put("providerUserId", "9");
		values.put("rank", 1);
		values.putNull("displayName");
		values.putNull("profileUrl");
		values.putNull("imageUrl");
		values.put("accessToken", encrypt("234567890"));
		values.putNull("secret");
		values.put("refreshToken", encrypt("345678901"));
		values.put("expireTime", System.currentTimeMillis() + 3600000);
		insertConnection(values);
	}
	
	private String encrypt(String text) {
		return text != null ?  textEncryptor.encrypt(text): text;
	}
	
	private void insertConnection(final ContentValues values) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		db.insertOrThrow("UserConnection", null, values);
		db.close();
	}
	
	private boolean queryConnectionExists(String providerId) {
		final String sql = "select exists (select 1 from UserConnection where providerId = ?)";
		final String[] selectionArgs = {providerId};
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);
		c.moveToFirst();
		boolean b = (c.getInt(0) != 0);
		c.close();
		db.close();
		return b;
	}
	
	private void assertNewConnection(Connection<TestFacebookApi> connection) {
		assertEquals("facebook", connection.getKey().getProviderId());
		assertEquals("9", connection.getKey().getProviderUserId());
		assertEquals("Keith Donald", connection.getDisplayName());
		assertEquals("http://facebook.com/keith.donald", connection.getProfileUrl());
		assertEquals("http://facebook.com/keith.donald/picture", connection.getImageUrl());
		assertTrue(connection.test());
		TestFacebookApi api = connection.getApi();
		assertNotNull(api);
		assertEquals("123456789", api.getAccessToken());
		assertEquals("123456789", connection.createData().getAccessToken());
		assertEquals("987654321", connection.createData().getRefreshToken());
	}

	private void assertTwitterConnection(Connection<TestTwitterApi> twitter) {
		assertEquals(new ConnectionKey("twitter", "1"), twitter.getKey());
		assertEquals("@kdonald", twitter.getDisplayName());
		assertEquals("http://twitter.com/kdonald", twitter.getProfileUrl());
		assertEquals("http://twitter.com/kdonald/picture", twitter.getImageUrl());
		TestTwitterApi twitterApi = twitter.getApi();
		assertEquals("123456789", twitterApi.getAccessToken());		
		assertEquals("987654321", twitterApi.getSecret());
		twitter.sync();
		assertEquals("http://twitter.com/kdonald/a_new_picture", twitter.getImageUrl());
	}

	private void assertFacebookConnection(Connection<TestFacebookApi> facebook) {
		assertEquals(new ConnectionKey("facebook", "9"), facebook.getKey());
		assertEquals(null, facebook.getDisplayName());
		assertEquals(null, facebook.getProfileUrl());
		assertEquals(null, facebook.getImageUrl());
		TestFacebookApi facebookApi = facebook.getApi();
		assertEquals("234567890", facebookApi.getAccessToken());
		facebook.sync();
		assertEquals("Keith Donald", facebook.getDisplayName());
		assertEquals("http://facebook.com/keith.donald", facebook.getProfileUrl());
		assertEquals("http://facebook.com/keith.donald/picture", facebook.getImageUrl());		
	}
	
	// test facebook provider
	
	private static class TestFacebookConnectionFactory extends OAuth2ConnectionFactory<TestFacebookApi> {

		public TestFacebookConnectionFactory() {
			super("facebook", new TestFacebookServiceProvider(), new TestFacebookApiAdapter());
		}
		
	}

	private static class TestFacebookServiceProvider implements OAuth2ServiceProvider<TestFacebookApi> {

		public OAuth2Operations getOAuthOperations() {
			return new OAuth2Operations() {
				public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
					return null;
				}
				public String buildAuthenticateUrl(GrantType grantType, OAuth2Parameters parameters) {
					return null;
				}
				public AccessGrant exchangeForAccess(String authorizationGrant, String redirectUri, MultiValueMap<String, String> additionalParameters) {
					return null;
				}
				public AccessGrant refreshAccess(String refreshToken, String scope, MultiValueMap<String, String> additionalParameters) {
					return new AccessGrant("765432109", "read", "654321098", 3600);
				}								
			};
		}

		public TestFacebookApi getApi(final String accessToken) {
			return new TestFacebookApi() {
				public String getAccessToken() {
					return accessToken;
				}
			};
		}
		
	}
		
	public interface TestFacebookApi {
		
		String getAccessToken();
		
	}
	
	private static class TestFacebookApiAdapter implements ApiAdapter<TestFacebookApi> {

		private String accountId = "9";
		
		private String name = "Keith Donald";
		
		private String profileUrl = "http://facebook.com/keith.donald";
		
		private String profilePictureUrl = "http://facebook.com/keith.donald/picture";
		
		public boolean test(TestFacebookApi api) {
			return true;
		}

		public void setConnectionValues(TestFacebookApi api, ConnectionValues values) {
			values.setProviderUserId(accountId);
			values.setDisplayName(name);
			values.setProfileUrl(profileUrl);
			values.setImageUrl(profilePictureUrl);
		}

		public UserProfile fetchUserProfile(TestFacebookApi serviceApi) {
			return new UserProfileBuilder().setName(name).setEmail("keith@interface21.com").setUsername("Keith.Donald").build();
		}

		public void updateStatus(TestFacebookApi api, String message) {
			
		}
		
	}
	
	// test twitter provider
	
	private static class TestTwitterConnectionFactory extends OAuth1ConnectionFactory<TestTwitterApi> {

		public TestTwitterConnectionFactory() {
			super("twitter", new TestTwitterServiceProvider(), new TestTwitterApiAdapter());
		}
		
	}

	private static class TestTwitterServiceProvider implements OAuth1ServiceProvider<TestTwitterApi> {

		public OAuth1Operations getOAuthOperations() {
			return null;
		}

		public TestTwitterApi getApi(final String accessToken, final String secret) {
			return new TestTwitterApi() {
				public String getAccessToken() {
					return accessToken;
				}
				public String getSecret() {
					return secret;
				}
			};
		}
		
	}
		
	public interface TestTwitterApi {
		
		String getAccessToken();
		
		String getSecret();
		
	}
	
	private static class TestTwitterApiAdapter implements ApiAdapter<TestTwitterApi> {

		private String accountId = "1";
		
		private String name = "@kdonald";
		
		private String profileUrl = "http://twitter.com/kdonald";
		
		private String profilePictureUrl = "http://twitter.com/kdonald/a_new_picture";
		
		public boolean test(TestTwitterApi api) {
			return true;
		}

		public void setConnectionValues(TestTwitterApi api, ConnectionValues values) {
			values.setProviderUserId(accountId);
			values.setDisplayName(name);
			values.setProfileUrl(profileUrl);
			values.setImageUrl(profilePictureUrl);
		}

		public UserProfile fetchUserProfile(TestTwitterApi api) {
			return new UserProfileBuilder().setName(name).setUsername("kdonald").build();			
		}
		
		public void updateStatus(TestTwitterApi api, String message) {
		}
		
	}
	
}
