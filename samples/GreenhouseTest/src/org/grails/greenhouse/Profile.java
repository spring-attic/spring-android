package org.grails.greenhouse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.grails.greenhouse.util.ErrorHandler;
import org.grails.greenhouse.util.ErrorReporter;
import org.grails.greenhouse.util.Prefs;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Burt Beckwith
 */
public class Profile extends Activity {
	private static final String TAG = "Profile";

	private TextView _textFirstName;
	private TextView _textLastName;

	private OAuthConsumer _consumer;
	private SharedPreferences _settings;
	private HttpClient _client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ErrorReporter.INSTANCE.init(this);

		setContentView(R.layout.profile);

		createClient();

		_textFirstName = (TextView)findViewById(R.id.profileFirstName);
		_textLastName = (TextView)findViewById(R.id.profileLastName);

		findViewById(R.id.buttonSignOut).setOnClickListener(new OnClickListener() {
			public void onClick(final View view) {
				Prefs.resetAuthInformation(_settings);
				startActivity(new Intent(Profile.this, Splash.class));
				finish();
			}
		});

		findViewById(R.id.buttonRefresh).setOnClickListener(new OnClickListener() {
			public void onClick(final View view) {
				Toast.makeText(getBaseContext(),
						"REFRESH", Toast.LENGTH_LONG).show();
			}
		});

		_settings = getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE);
		_consumer = new CommonsHttpOAuthConsumer(Prefs.getConsumerKey(), Prefs.getConsumerSecret());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(new Intent(getBaseContext(), Preferences.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void createClient() {
		HttpParams parameters = new BasicHttpParams();
		HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(parameters, false);
//		HttpConnectionParams.setTcpNoDelay(parameters, Prefs.isTcpNoDelay());
		HttpConnectionParams.setTcpNoDelay(parameters, true);
//		HttpConnectionParams.setSocketBufferSize(parameters, Prefs.getSocketBufferSize());
		HttpConnectionParams.setSocketBufferSize(parameters, 8192);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//		schemeRegistry.register(new Scheme(Prefs.getScheme(), PlainSocketFactory.getSocketFactory(), Prefs.getPort()));
		_client = new DefaultHttpClient(new ThreadSafeClientConnManager(parameters, schemeRegistry), parameters);
	}

	@Override
	public void onResume() {
		super.onResume();

		String[] tokenAndSecret = Prefs.getUserTokenAndSecret(_settings);
		String token = tokenAndSecret[0];
		String tokenSecret = tokenAndSecret[1];
		if (token != null && tokenSecret != null) {
			_consumer.setTokenWithSecret(token, tokenSecret);
		}

		new GetCredentialsTask().execute();
	}

	protected void onFinish() {
		_client.getConnectionManager().shutdown();
	}

	private class GetCredentialsTask extends AsyncTask<Void, Void, JSONObject> {

		private ProgressDialog _authDialog;

		@Override
		protected void onPreExecute() {
			_authDialog = ProgressDialog.show(Profile.this,
					getText(R.string.auth_progress_title),
					getText(R.string.auth_progress_text), true, false);
		}

		@Override
		protected JSONObject doInBackground(Void... arg0) {
			HttpGet get = new HttpGet(Prefs.getProfileUrl());
			try {
				_consumer.sign(get);
				String response = _client.execute(get, new BasicResponseHandler());
				JSONObject json = new JSONObject(response);
				Log.d(TAG, "authenticatedQuery: " + json.toString(2));
				return json;
			}
			catch (Exception e) {
				ErrorHandler.handle(e, Profile.this);
				return null;
			}
		}

		@Override
		protected void onPostExecute(final JSONObject json) {
			_authDialog.dismiss();
			if (json == null) {
				_textFirstName.setText(getString(R.string.bad_value));
				_textLastName.setText(getString(R.string.bad_value));
			}
			else {
//				_textFirstName.setText(json.optString("name", getString(R.string.bad_value)));
//				String lastTweet;
//				try {
//					lastTweet = json.getJSONObject("status").optString("text", getString(R.string.bad_value));
//				}
//				catch (JSONException e) {
//					ErrorHandler.handle(e, Profile.this);
//					lastTweet = getString(R.string.tweet_error);
//				}
//				_textLastName.setText(lastTweet);
				_textFirstName.setText(json.optString("firstName", getString(R.string.bad_value)));
				_textLastName.setText(json.optString("lastName", getString(R.string.bad_value)));
			}
		}
	}
}
