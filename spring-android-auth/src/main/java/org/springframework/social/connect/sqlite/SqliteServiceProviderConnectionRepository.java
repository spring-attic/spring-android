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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ServiceProviderConnection;
import org.springframework.social.connect.ServiceProviderConnectionData;
import org.springframework.social.connect.ServiceProviderConnectionFactory;
import org.springframework.social.connect.ServiceProviderConnectionFactoryLocator;
import org.springframework.social.connect.ServiceProviderConnectionKey;
import org.springframework.social.connect.ServiceProviderConnectionRepository;
import org.springframework.social.connect.support.LocalUserIdLocator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Roy Clarkson
 */
public class SqliteServiceProviderConnectionRepository implements ServiceProviderConnectionRepository {
	
	private final SqliteRepositoryHelper repositoryHelper;
	
	private final ServiceProviderConnectionFactoryLocator connectionFactoryLocator;
	
	private final LocalUserIdLocator localUserIdLocator;
	
	private final TextEncryptor textEncryptor;
	
	/**
	 * Creates a SQLite-based connection repository.
	 * @param context the Android Context to execute within
	 * @param textEncryptor the encryptor to use when storing oauth keys
	 */
	public SqliteServiceProviderConnectionRepository(Context context, ServiceProviderConnectionFactoryLocator connectionFactoryLocator, LocalUserIdLocator localUserIdLocator,  TextEncryptor textEncryptor) {
		this.repositoryHelper = new SqliteRepositoryHelper(context);
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.localUserIdLocator = localUserIdLocator;
		this.textEncryptor = textEncryptor;
	}
	
	public List<ServiceProviderConnection<?>> findAllConnections() {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {getLocalUserId().toString()};        
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? order by providerId, rank", selectionArgs);
        
		List<ServiceProviderConnection<?>> connections = new ArrayList<ServiceProviderConnection<?>>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			connections.add(mapConnectionRow(c));
			c.moveToNext();
		}
		c.deactivate();
		db.close();
        
		return connections;
	}
	
	public List<ServiceProviderConnection<?>> findConnectionsToProvider(String providerId) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {getLocalUserId().toString(), providerId};        
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? order by rank", selectionArgs);
        
		List<ServiceProviderConnection<?>> connections = new ArrayList<ServiceProviderConnection<?>>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			connections.add(mapConnectionRow(c));
			c.moveToNext();
		}
		c.deactivate();
		db.close();
        
		return connections;
	}	
	
	public List<ServiceProviderConnection<?>> findConnectionsForUsers(Map<String, List<String>> providerUsers) {
		if (providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}
		StringBuilder providerUsersCriteriaSql = new StringBuilder();
		List<Object> args = new ArrayList<Object>(1 + providerUsers.size() * 2);
		args.add(getLocalUserId().toString());
		for (Iterator<Entry<String, List<String>>> it = providerUsers.entrySet().iterator(); it.hasNext();) {
			Entry<String, List<String>> entry = it.next();
			providerUsersCriteriaSql.append("providerId = ? and providerUserId in (?)");
			args.add(entry.getKey());
			args.add(entry.getValue());
			if (it.hasNext()) {
				providerUsersCriteriaSql.append(" or " );
			}
		}
		
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = args.toArray(new String[0]);
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and " + providerUsersCriteriaSql, selectionArgs);
        
		List<ServiceProviderConnection<?>> connections = new ArrayList<ServiceProviderConnection<?>>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			connections.add(mapConnectionRow(c));
			c.moveToNext();
		}
		c.deactivate();
		db.close();
        
		return connections;
	}
	
	public ServiceProviderConnection<?> findConnection(ServiceProviderConnectionKey connectionKey) {		
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {getLocalUserId().toString(), connectionKey.getProviderId(), connectionKey.getProviderUserId()};
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? and providerUserId = ? order by rank", selectionArgs);
        
        ServiceProviderConnection<?> connection = null;
        if (c.getCount() > 0) {
			c.moveToFirst();
			connection = mapConnectionRow(c);
        }
        
		c.deactivate();
		db.close();
        
		return connection;
	}
	
	@SuppressWarnings("unchecked")
	public <S> ServiceProviderConnection<S> findConnectionByServiceApi(Class<S> serviceApiType) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {getLocalUserId().toString(), getProviderId(serviceApiType)};
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? and rank = 1", selectionArgs);
        
		ServiceProviderConnection<?> connection = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			connection = mapConnectionRow(c);
		}
		c.deactivate();
		db.close();
        
		return (ServiceProviderConnection<S>) connection;
	}
	
	@SuppressWarnings("unchecked")
	public <S> ServiceProviderConnection<S> findConnectionByServiceApiForUser(Class<S> serviceApiType, String providerUserId) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {getLocalUserId().toString(), getProviderId(serviceApiType), providerUserId};
        Cursor c = db.rawQuery(SELECT_FROM_SERVICE_PROVIDER_CONNECTION + " where localUserId = ? and providerId = ? and providerUserId = ?", selectionArgs);
        
		ServiceProviderConnection<?> connection = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			connection = mapConnectionRow(c);
		}
		c.deactivate();
		db.close();
        
		return (ServiceProviderConnection<S>) connection;
	}
	
	public void addConnection(ServiceProviderConnection<?> connection) {
		ServiceProviderConnectionData data = connection.createData();
		Serializable localUserId = getLocalUserId();
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		String[] bindArgs = {localUserId.toString(), data.getProviderId(), data.getProviderUserId(), localUserId.toString(), data.getProviderId(), data.getProfileName(), data.getProfileUrl(), data.getProfilePictureUrl(), encrypt(data.getAccessToken()), encrypt(data.getSecret()), encrypt(data.getRefreshToken()), data.getExpireTime().toString()};
		db.execSQL("insert into ServiceProviderConnection (localUserId, providerId, providerUserId, rank, profileName, profileUrl, profilePictureUrl, accessToken, secret, refreshToken, expireTime) values (?, ?, ?, (select ifnull(max(rank) + 1, 1) from ServiceProviderConnection where localUserId = ? and providerId = ?), ?, ?, ?, ?, ?, ?, ?)", bindArgs);
		db.close();
	}
	
	public void removeConnectionsToProvider(String providerId) {		
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		String[] bindArgs = {getLocalUserId().toString(), providerId};
		db.execSQL("delete from ServiceProviderConnection where localUserId = ? and providerId = ?", bindArgs);
		db.close();
	}

	public void removeConnection(ServiceProviderConnectionKey connectionKey) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		String[] bindArgs = {getLocalUserId().toString(), connectionKey.getProviderId(), connectionKey.getProviderUserId()};
		db.execSQL("delete from ServiceProviderConnection where localUserId = ? and providerId = ? and providerUserId = ?", bindArgs);
		db.close();
	}
	
	
	// internal helpers
	
	private static final String SELECT_FROM_SERVICE_PROVIDER_CONNECTION = "select localUserId, providerId, providerUserId, profileName, profileUrl, profilePictureUrl, accessToken, secret, refreshToken, expireTime from ServiceProviderConnection";
	
	private Serializable getLocalUserId() {
		return localUserIdLocator.getLocalUserId();
	}
	
	private <S> String getProviderId(Class<S> serviceApiType) {
		return connectionFactoryLocator.getConnectionFactory(serviceApiType).getProviderId();
	}
	
	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}
	
	private String decrypt(String encryptedText) {
		return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
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
				c.getLong(c.getColumnIndex("expireTime")));
	}
	
	
	// private class for wiring up the database
	
	private class SqliteRepositoryHelper extends SQLiteOpenHelper {

		private static final String TAG = "SqliteServiceProviderConnectionRepository";

		private static final String DATABASE_NAME = "spring_social_connection_repository.sqlite";

		private static final int DATABASE_VERSION = 1;

		public SqliteRepositoryHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table ServiceProviderConnection (localUserId bigint not null,"
					+ "providerId varchar not null,"
					+ "providerUserId varchar,"
					+ "rank int not null,"
					+ "profileName varchar,"
					+ "profileUrl varchar,"
					+ "profilePictureUrl varchar,"
					+ "accessToken varchar not null,"					
					+ "secret varchar,"
					+ "refreshToken varchar,"
					+ "expireTime bigint,"
					+ "primary key (localUserId, providerId, providerUserId));"
					+ "create index ConnectionsToProviderUser on ServiceProviderConnection(providerId, providerUserId);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading connection repository database from version "
					+ oldVersion + "to " + newVersion);
			// TODO: Upgrade database
		}
	}
}
