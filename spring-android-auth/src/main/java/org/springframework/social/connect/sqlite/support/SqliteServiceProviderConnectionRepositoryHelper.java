package org.springframework.social.connect.sqlite.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteServiceProviderConnectionRepositoryHelper extends SQLiteOpenHelper {

	private static final String TAG = SqliteServiceProviderConnectionRepositoryHelper.class.getSimpleName();

	private static final String DATABASE_NAME = "spring_social_connection_repository.sqlite";

	private static final int DATABASE_VERSION = 1;

	public SqliteServiceProviderConnectionRepositoryHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table ServiceProviderConnection (localUserId varchar not null,"
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
				+ "create unique index ServiceProviderConnectionRank on ServiceProviderConnection(localUserId, providerId, rank);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading connection repository database from version "
				+ oldVersion + "to " + newVersion);
		// TODO: Upgrade database
	}
}