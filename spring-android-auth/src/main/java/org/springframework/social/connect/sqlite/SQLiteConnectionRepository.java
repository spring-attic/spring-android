/*
 * Copyright 2002-2014 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * {@link ConnectionRepository} that uses SQLite to persist connection data to a relational database.
 * 
 * @author Roy Clarkson
 * @since 1.0
 */
public class SQLiteConnectionRepository implements ConnectionRepository {

	private final String userId;

	private final SQLiteOpenHelper repositoryHelper;

	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	public SQLiteConnectionRepository(SQLiteOpenHelper repositoryHelper, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this("1", repositoryHelper, connectionFactoryLocator, textEncryptor);
	}

	public SQLiteConnectionRepository(String userId, SQLiteOpenHelper repositoryHelper, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.userId = userId;
		this.repositoryHelper = repositoryHelper;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public MultiValueMap<String, Connection<?>> findAllConnections() {
		final String sql = selectFromUserConnection() + " where userId = ? order by providerId, rank";
		final String[] selectionArgs = { userId };
		List<Connection<?>> resultList = queryForConnections(sql, selectionArgs);
		MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
		Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
		for (String registeredProviderId : registeredProviderIds) {
			connections.put(registeredProviderId, Collections.<Connection<?>> emptyList());
		}
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			if (connections.get(providerId).size() == 0) {
				connections.put(providerId, new LinkedList<Connection<?>>());
			}
			connections.add(providerId, connection);
		}
		return connections;
	}

	public List<Connection<?>> findConnections(String providerId) {
		final String sql = selectFromUserConnection() + " where userId = ? and providerId = ? order by rank";
		final String[] selectionArgs = { userId, providerId };
		return queryForConnections(sql, selectionArgs);
	}

	@SuppressWarnings("unchecked")
	public <A> List<Connection<A>> findConnections(Class<A> apiType) {
		List<?> connections = findConnections(getProviderId(apiType));
		return (List<Connection<A>>) connections;
	}

	public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUsers) {
		if (providerUsers == null || providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}
		StringBuilder providerUsersCriteriaSql = new StringBuilder();
		List<String> args = new ArrayList<String>(1 + providerUsers.size() * 2);
		args.add(userId);
		for (Iterator<Entry<String, List<String>>> entries = providerUsers.entrySet().iterator(); entries.hasNext();) {
			Entry<String, List<String>> entry = entries.next();
			providerUsersCriteriaSql.append("providerId = ? and providerUserId in (?");
			args.add(entry.getKey());

			for (Iterator<String> values = entry.getValue().iterator(); values.hasNext();) {
				String value = values.next();
				args.add(value);

				if (values.hasNext()) {
					providerUsersCriteriaSql.append(", ?");
				}
			}

			providerUsersCriteriaSql.append(")");

			if (entries.hasNext()) {
				providerUsersCriteriaSql.append(" or ");
			}
		}

		final String sql = selectFromUserConnection() + " where userId = ? and " + providerUsersCriteriaSql + " order by providerId, rank";
		final String[] selectionArgs = args.toArray(new String[0]);
		List<Connection<?>> resultList = queryForConnections(sql, selectionArgs);

		MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			List<String> userIds = providerUsers.get(providerId);
			List<Connection<?>> connections = connectionsForUsers.get(providerId);
			if (connections == null) {
				connections = new ArrayList<Connection<?>>(userIds.size());
				for (int i = 0; i < userIds.size(); i++) {
					connections.add(null);
				}
				connectionsForUsers.put(providerId, connections);
			}
			String providerUserId = connection.getKey().getProviderUserId();
			int connectionIndex = userIds.indexOf(providerUserId);
			connections.set(connectionIndex, connection);
		}
		return connectionsForUsers;
	}

	public Connection<?> getConnection(ConnectionKey connectionKey) {
		final String sql = selectFromUserConnection() + " where userId = ? and providerId = ? and providerUserId = ? order by rank";
		final String[] selectionArgs = { userId, connectionKey.getProviderId(), connectionKey.getProviderUserId() };
		Connection<?> connection = queryForConnection(sql, selectionArgs);

		if (connection == null) {
			throw new NoSuchConnectionException(connectionKey);
		}

		return connection;
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
		if (connection == null) {
			throw new NotConnectedException(providerId);
		}
		return connection;
	}

	@SuppressWarnings("unchecked")
	public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) findPrimaryConnection(providerId);
	}

	public void addConnection(Connection<?> connection) {
		try {
			ConnectionData data = connection.createData();
			SQLiteDatabase db = repositoryHelper.getWritableDatabase();

			// generate rank
			final String sql = "select coalesce(max(rank) + 1, 1) as rank from UserConnection where userId = ? and providerId = ?";
			final String[] selectionArgs = { userId, data.getProviderId() };
			Cursor c = db.rawQuery(sql, selectionArgs);
			c.moveToFirst();
			int rank = c.getInt(c.getColumnIndex("rank"));
			c.close();

			// insert connection
			ContentValues values = new ContentValues();
			values.put("userId", userId);
			values.put("providerId", data.getProviderId());
			values.put("providerUserId", data.getProviderUserId());
			values.put("rank", rank);
			values.put("displayName", data.getDisplayName());
			values.put("profileUrl", data.getProfileUrl());
			values.put("imageUrl", data.getImageUrl());
			values.put("accessToken", encrypt(data.getAccessToken()));
			values.put("secret", encrypt(data.getSecret()));
			values.put("refreshToken", encrypt(data.getRefreshToken()));
			values.put("expireTime", data.getExpireTime());
			db.insertOrThrow("UserConnection", null, values);
			db.close();
		} catch (SQLiteConstraintException e) {
			throw new DuplicateConnectionException(connection.getKey());
		}
	}

	public void updateConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("displayName", data.getDisplayName());
		values.put("profileUrl", data.getProfileUrl());
		values.put("imageUrl", data.getImageUrl());
		values.put("accessToken", encrypt(data.getAccessToken()));
		values.put("secret", encrypt(data.getSecret()));
		values.put("refreshToken", encrypt(data.getRefreshToken()));
		values.put("expireTime", data.getExpireTime());
		final String whereClause = "userId = ? and providerId = ? and providerUserId = ?";
		final String[] whereArgs = { userId, data.getProviderId(), data.getProviderUserId() };
		db.update("UserConnection", values, whereClause, whereArgs);
		db.close();
	}

	public void removeConnections(String providerId) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		final String whereClause = "userId = ? and providerId = ?";
		final String[] whereArgs = { userId, providerId };
		db.delete("UserConnection", whereClause, whereArgs);
		db.close();
	}

	public void removeConnection(ConnectionKey connectionKey) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		final String whereClause = "userId = ? and providerId = ? and providerUserId = ?";
		final String[] whereArgs = { userId, connectionKey.getProviderId(), connectionKey.getProviderUserId() };
		db.delete("UserConnection", whereClause, whereArgs);
		db.close();
	}


	// internal helpers

	private String selectFromUserConnection() {
		return "select userId, providerId, providerUserId, displayName, profileUrl, imageUrl, accessToken, secret, refreshToken, expireTime from UserConnection";
	}

	private Connection<?> findPrimaryConnection(String providerId) {
		final String sql = selectFromUserConnection() + " where userId = ? and providerId = ? and rank = 1";
		final String[] selectionArgs = { userId, providerId };
		List<Connection<?>> connections = queryForConnections(sql, selectionArgs);
		if (connections.size() > 0) {
			return connections.get(0);
		} else {
			return null;
		}
	}

	private <A> String getProviderId(Class<A> apiType) {
		return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
	}

	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}

	private String decrypt(String encryptedText) {
		return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
	}

	private Long expireTime(long expireTime) {
		return expireTime == 0 ? null : expireTime;
	}

	private Connection<?> queryForConnection(final String sql, final String[] selectionArgs) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = null;
		Connection<?> connection = null;
		try {
			c = db.rawQuery(sql, selectionArgs);
			if (c.getCount() > 0) {
				c.moveToFirst();
				connection = mapConnectionRow(c);
			}
		} finally {
			c.close();
			db.close();
		}
		return connection;
	}

	private List<Connection<?>> queryForConnections(final String sql, final String[] selectionArgs) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = null;
		List<Connection<?>> connections = new ArrayList<Connection<?>>();
		try {
			c = db.rawQuery(sql, selectionArgs);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				connections.add(mapConnectionRow(c));
				c.moveToNext();
			}
		} finally {
			c.close();
			db.close();
		}
		return connections;
	}

	private Connection<?> mapConnectionRow(Cursor c) {
		ConnectionData connectionData = mapConnectionData(c);
		ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
		return connectionFactory.createConnection(connectionData);
	}

	private ConnectionData mapConnectionData(Cursor c) {
		return new ConnectionData(c.getString(c.getColumnIndex("providerId")), c.getString(c.getColumnIndex("providerUserId")), c.getString(c.getColumnIndex("displayName")), c.getString(c.getColumnIndex("profileUrl")), c.getString(c
				.getColumnIndex("imageUrl")), decrypt(c.getString(c.getColumnIndex("accessToken"))), decrypt(c.getString(c.getColumnIndex("secret"))), decrypt(c.getString(c.getColumnIndex("refreshToken"))), expireTime(c.getLong(c
				.getColumnIndex("expireTime"))));
	}

}
