package org.springframework.social.connect.sqlite.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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