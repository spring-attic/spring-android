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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.DuplicateServiceProviderConnectionException;
import org.springframework.social.connect.NoSuchServiceProviderConnectionException;
import org.springframework.social.connect.ServiceProviderConnection;
import org.springframework.social.connect.ServiceProviderConnectionData;
import org.springframework.social.connect.ServiceProviderConnectionFactory;
import org.springframework.social.connect.ServiceProviderConnectionFactoryLocator;
import org.springframework.social.connect.ServiceProviderConnectionKey;
import org.springframework.social.connect.ServiceProviderConnectionRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Roy Clarkson
 */
public class SqliteServiceProviderConnectionRepository implements ServiceProviderConnectionRepository {
	
	private final String localUserId;
	
	private final SQLiteOpenHelper repositoryHelper;
	
	private final ServiceProviderConnectionFactoryLocator connectionFactoryLocator;
	
	private final TextEncryptor textEncryptor;
	
	public SqliteServiceProviderConnectionRepository(String localUserId, SQLiteOpenHelper repositoryHelper, ServiceProviderConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.localUserId = localUserId;
		this.repositoryHelper = repositoryHelper;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}
		
	public MultiValueMap<String, ServiceProviderConnection<?>> findConnections() {
		final String sql = SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? order by providerId, rank";
		final String[] selectionArgs = {localUserId};
		List<ServiceProviderConnection<?>> resultList = queryForConnections(sql, selectionArgs);
        MultiValueMap<String, ServiceProviderConnection<?>> connections = new LinkedMultiValueMap<String, ServiceProviderConnection<?>>();
        Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
        for (String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<ServiceProviderConnection<?>>emptyList());
        }
        for (ServiceProviderConnection<?> connection : resultList) {
            String providerId = connection.getKey().getProviderId();
            if (connections.get(providerId).size() == 0) {
                connections.put(providerId, new LinkedList<ServiceProviderConnection<?>>());
            }
            connections.add(providerId, connection);
        }
        return connections;
	}
		
	public List<ServiceProviderConnection<?>> findConnectionsToProvider(String providerId) {
		final String sql = SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? order by rank";
		final String[] selectionArgs = {localUserId, providerId};
		return queryForConnections(sql, selectionArgs);
	}	
	
	public MultiValueMap<String, ServiceProviderConnection<?>> findConnectionsForUsers(MultiValueMap<String, String> providerUsers) {
		if (providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}
		StringBuilder providerUsersCriteriaSql = new StringBuilder();
		List<String> args = new ArrayList<String>(1 + providerUsers.size() * 2);
		args.add(localUserId);
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
		
	    final String sql = SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and " + providerUsersCriteriaSql + " order by providerId, rank";
		final String[] selectionArgs = args.toArray(new String[0]);
	    List<ServiceProviderConnection<?>> resultList = queryForConnections(sql, selectionArgs);
		
		MultiValueMap<String, ServiceProviderConnection<?>> connectionsForUsers = new LinkedMultiValueMap<String, ServiceProviderConnection<?>>();
		for (ServiceProviderConnection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			List<String> userIds = providerUsers.get(providerId);
			List<ServiceProviderConnection<?>> connections = connectionsForUsers.get(providerId);
			if (connections == null) {
				connections = new ArrayList<ServiceProviderConnection<?>>(userIds.size());
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
	
	public ServiceProviderConnection<?> findConnection(ServiceProviderConnectionKey connectionKey) {		
		final String sql = SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? and providerUserId = ? order by rank";
		final String[] selectionArgs = {localUserId, connectionKey.getProviderId(), connectionKey.getProviderUserId()};
		ServiceProviderConnection<?> connection = queryForConnection(sql, selectionArgs);
		
		if (connection == null) {
			throw new NoSuchServiceProviderConnectionException(connectionKey);
		}
		
		return connection;
	}
	
	@SuppressWarnings("unchecked")
	public <S> ServiceProviderConnection<S> findConnectionByServiceApi(Class<S> serviceApiType) {
		final String sql = SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? and rank = 1";
		final String[] selectionArgs = {localUserId, getProviderId(serviceApiType)};		
		return (ServiceProviderConnection<S>) queryForConnection(sql, selectionArgs);
	}
	
    @SuppressWarnings("unchecked")
	public <S> List<ServiceProviderConnection<S>> findConnectionsByServiceApi(Class<S> serviceApiType) {
    	List<?> connections = findConnectionsToProvider(getProviderId(serviceApiType));
    	return (List<ServiceProviderConnection<S>>) connections;
    }
	
	@SuppressWarnings("unchecked")
	public <S> ServiceProviderConnection<S> findConnectionByServiceApiForUser(Class<S> serviceApiType, String providerUserId) {
		String providerId = getProviderId(serviceApiType);
		return (ServiceProviderConnection<S>) findConnection(new ServiceProviderConnectionKey(providerId, providerUserId));
	}
	
	public void addConnection(ServiceProviderConnection<?> connection) {
		try {
			ServiceProviderConnectionData data = connection.createData();
			SQLiteDatabase db = repositoryHelper.getWritableDatabase();		
			
			// generate rank
			final String sql = "select ifnull(max(rank) + 1, 1) from ServiceProviderConnection where localUserId = ? and providerId = ?";
			final String[] selectionArgs = {localUserId, data.getProviderId()};
			Cursor c = db.rawQuery(sql, selectionArgs);
			c.moveToFirst();
			int rank = c.getInt(0);
			c.close();
			
			// insert connection
			ContentValues values = new ContentValues();
			values.put("localUserId", localUserId);
			values.put("providerId", data.getProviderId());
			values.put("providerUserId", data.getProviderUserId());
			values.put("rank", rank);
			values.put("profileName", data.getProfileName());
			values.put("profileUrl", data.getProfileUrl());
			values.put("profilePictureUrl", data.getProfilePictureUrl());
			values.put("accessToken", encrypt(data.getAccessToken()));
			values.put("secret", encrypt(data.getSecret()));
			values.put("refreshToken", encrypt(data.getRefreshToken()));
			values.put("expireTime", data.getExpireTime());
			db.insertOrThrow("ServiceProviderConnection", null, values);
			db.close();
		} catch(SQLiteConstraintException e) {
			throw new DuplicateServiceProviderConnectionException(connection.getKey());
		}
	}
	
	public void updateConnection(ServiceProviderConnection<?> connection) {
		ServiceProviderConnectionData data = connection.createData();
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("profileName", data.getProfileName());
		values.put("profileUrl", data.getProfileUrl());
		values.put("profilePictureUrl", data.getProfilePictureUrl());
		values.put("accessToken", encrypt(data.getAccessToken()));
		values.put("secret", encrypt(data.getSecret()));
		values.put("refreshToken", encrypt(data.getRefreshToken()));
		values.put("expireTime", data.getExpireTime());
		final String whereClause = "localUserId = ? and providerId = ? and providerUserId = ?";
		final String[] whereArgs = {localUserId, data.getProviderId(), data.getProviderUserId()};
		db.update("ServiceProviderConnection", values, whereClause, whereArgs);
		db.close();
	}
	
	public void removeConnectionsToProvider(String providerId) {		
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		final String whereClause = "localUserId = ? and providerId = ?";
		final String[] whereArgs = {localUserId, providerId};
		db.delete("ServiceProviderConnection", whereClause, whereArgs);
		db.close();
	}

	public void removeConnection(ServiceProviderConnectionKey connectionKey) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		final String whereClause = "localUserId = ? and providerId = ? and providerUserId = ?";
		final String[] whereArgs = {localUserId, connectionKey.getProviderId(), connectionKey.getProviderUserId()};
		db.delete("ServiceProviderConnection", whereClause, whereArgs);
		db.close();
	}
	
	
	// internal helpers
	
	private static final String SELECT_FROM_SERVICE_PROVIDER_CONNECTION = "select localUserId, providerId, providerUserId, profileName, profileUrl, profilePictureUrl, accessToken, secret, refreshToken, expireTime from ServiceProviderConnection";
	
	private <S> String getProviderId(Class<S> serviceApiType) {
		return connectionFactoryLocator.getConnectionFactory(serviceApiType).getProviderId();
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
	
	private ServiceProviderConnection<?> queryForConnection(final String sql, final String[] selectionArgs) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, selectionArgs);
        ServiceProviderConnection<?> connection = null;
        if (c.getCount() > 0) {
			c.moveToFirst();
			connection = mapConnectionRow(c);
        }
		c.close();
		db.close();		
		return connection;
	}
	
	private List<ServiceProviderConnection<?>> queryForConnections(final String sql, final String[] selectionArgs) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, selectionArgs);		
		List<ServiceProviderConnection<?>> connections = new ArrayList<ServiceProviderConnection<?>>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			connections.add(mapConnectionRow(c));
			c.moveToNext();
		}
		c.close();
		db.close();
		return connections;
	}
	
	private ServiceProviderConnection<?> mapConnectionRow(Cursor c) {
		ServiceProviderConnectionData connectionData = mapConnectionData(c);
		ServiceProviderConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
		return connectionFactory.createConnection(connectionData);
	}
	
	private ServiceProviderConnectionData mapConnectionData(Cursor c) {
		return new ServiceProviderConnectionData(c.getString(c.getColumnIndex("providerId")), 
				c.getString(c.getColumnIndex("providerUserId")),
				c.getString(c.getColumnIndex("profileName")), 
				c.getString(c.getColumnIndex("profileUrl")), 
				c.getString(c.getColumnIndex("profilePictureUrl")),
				decrypt(c.getString(c.getColumnIndex("accessToken"))), 
				decrypt(c.getString(c.getColumnIndex("secret"))), 
				decrypt(c.getString(c.getColumnIndex("refreshToken"))), 
				expireTime(c.getLong(c.getColumnIndex("expireTime"))));
	}
	
}
