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
import java.util.List;

import org.springframework.social.connect.support.Connection;
import org.springframework.social.connect.support.ConnectionRepository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Sqlite-based connection repository implementation.
 * @author Roy Clarkson
 */
public class SqliteConnectionRepository implements ConnectionRepository {
	
	private final SqliteConnectionRepositoryHelper repositoryHelper;
	
	public SqliteConnectionRepository(Context context) {
		repositoryHelper = new SqliteConnectionRepositoryHelper(context);
	}

	public boolean isConnected(Serializable accountId, String providerId) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();		
        String[] selectionArgs = {accountId.toString(), providerId};        
        Cursor c = db.rawQuery("select 1 from Connection where accountId = ? and providerId = ?", selectionArgs);
        int connectionCount = c.getCount();
        c.deactivate();
        db.close();
        
        return connectionCount > 0;
	}

	public List<Connection> findConnections(Serializable accountId, String providerId) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {accountId.toString(), providerId};        
        Cursor c = db.rawQuery("select id, accessToken, secret, refreshToken, providerAccountId from Connection where accountId = ? and providerId = ? order by id", selectionArgs);
        
		List<Connection> connections = new ArrayList<Connection>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			connections.add(assembleConnection(c));
			c.moveToNext();
		}
		c.deactivate();
		db.close();
        
		return connections;
	}


	public Serializable findAccountIdByConnectionAccessToken(String provider, String accessToken) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		// TODO: Encrypt accessToken
		String[] selectionArgs = {provider, accessToken};
		Cursor c = db.rawQuery("select accountId from Connection where providerId = ? and accessToken = ?", selectionArgs);
		
		List<Serializable> accountIds = new ArrayList<Serializable>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			accountIds.add(c.getString(c.getColumnIndex("accountId")));			
			c.moveToNext();
		}
		c.deactivate();
		db.close();
		
		return !accountIds.isEmpty() ? accountIds.get(0) : null;
	}

	public List<Serializable> findAccountIdsForProviderAccountIds(String providerId, List<String> providerAccountIds) {
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		String[] selectionArgs = {providerId};
		
		final String sql = "select accountId "
			+ "from Connection "
			+ "where providerId = ?"
			+ "and providerAccountId in ("
			+ createCommaSeparatedString(providerAccountIds)
			+ ")";
			
		Cursor c = db.rawQuery(sql, selectionArgs);
		
		List<Serializable> accountIds = new ArrayList<Serializable>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			accountIds.add(c.getString(c.getColumnIndex("accountId")));			
			c.moveToNext();
		}
		c.deactivate();
		db.close();
		
		return accountIds;
	}
	
	public Connection saveConnection(Serializable accountId, String providerId,	Connection connection) {
		try {
			SQLiteDatabase db = repositoryHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("accountId", accountId.toString());
			values.put("providerId", providerId);
			values.put("accessToken", connection.getAccessToken());
			values.put("secret", connection.getSecret());
			values.put("refreshToken", connection.getRefreshToken());
			values.put("providerAccountId", connection.getProviderAccountId());
			long connectionId = db.insertOrThrow("Connection", null, values);
			db.close();
			return new Connection(connectionId, connection.getAccessToken(), connection.getSecret(), connection.getRefreshToken(), connection.getProviderAccountId());
		} catch(SQLiteConstraintException e) {
			throw new IllegalArgumentException("Access token is not unique: a connection already exists!", e);
		}
	}

	public void removeConnection(Serializable accountId, String providerId, Long connectionId) {
		SQLiteDatabase db = repositoryHelper.getWritableDatabase();
		String[] bindArgs = {accountId.toString(), providerId, connectionId.toString()};
		db.execSQL("delete from Connection where accountId = ? and providerId = ? and id = ?", bindArgs);
		db.close();
	}
	
	
	// Helper methods
	
	private Connection assembleConnection(Cursor c) {
		// TODO: Decrypt accessToken, secret, and refreshToken
		return new Connection(c.getLong(c.getColumnIndex("id")),
				c.getString(c.getColumnIndex("accessToken")), 
				c.getString(c.getColumnIndex("secret")),
				c.getString(c.getColumnIndex("refreshToken")), 
				c.getString(c.getColumnIndex("providerAccountId")));
	}
	
	private static String createCommaSeparatedString(List<String> list) {
		StringBuilder sb = new StringBuilder();
		String separator = "";

		for (String s : list) {
			sb.append(separator);
			sb.append(s);
			separator = ",";
		}

		return sb.toString();
	}
	
	
	// private class for wiring up the database
	
	private class SqliteConnectionRepositoryHelper extends SQLiteOpenHelper {

		private static final String TAG = "SqliteConnectionRepositoryHelper";

		private static final String DATABASE_NAME = "connection_repository.sqlite";

		private static final int DATABASE_VERSION = 1;

		public SqliteConnectionRepositoryHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table Connection (id integer primary key,"
					+ "accountId varchar not null,"
					+ "providerId varchar not null,"
					+ "accessToken varchar not null,"
					+ "secret varchar,"
					+ "refreshToken varchar,"
					+ "providerAccountId varchar)");
			db.execSQL("create unique index AccessToken on Connection(accountId, providerId, accessToken)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading connection repository database from version "
					+ oldVersion + "to " + newVersion);
			// TODO: Upgrade database
		}
	}
}
