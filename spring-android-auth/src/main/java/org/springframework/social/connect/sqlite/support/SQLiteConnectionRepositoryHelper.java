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

package org.springframework.social.connect.sqlite.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Roy Clarkson
 * @since 1.0
 */
public final class SQLiteConnectionRepositoryHelper extends SQLiteOpenHelper {

	private static final String TAG = SQLiteConnectionRepositoryHelper.class.getSimpleName();

	private static final String DATABASE_NAME = "spring_social_connection_repository.sqlite";

	private static final int DATABASE_VERSION = 1;

	public SQLiteConnectionRepositoryHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table UserConnection (userId varchar not null,"
				+ "providerId varchar not null,"
				+ "providerUserId varchar,"
				+ "rank int not null,"
				+ "displayName varchar,"
				+ "profileUrl varchar,"
				+ "imageUrl varchar,"
				+ "accessToken varchar not null,"
				+ "secret varchar,"
				+ "refreshToken varchar,"
				+ "expireTime bigint,"
				+ "primary key (userId, providerId, providerUserId));"
				+ "create unique index UserConnectionRank on UserConnection(userId, providerId, rank);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading connection repository database from version "
				+ oldVersion + "to " + newVersion);
		// TODO: Upgrade database
	}
}