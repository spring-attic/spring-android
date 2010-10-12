package org.grails.greenhouse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.grails.greenhouse.util.ErrorHandler;
import org.grails.greenhouse.util.ErrorReporter;
import org.grails.greenhouse.util.Prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author Burt Beckwith
 */
public class OAuth extends Activity {

	private OAuthConsumer _consumer;
	private OAuthProvider _provider;
	private SharedPreferences _settings;

	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		ErrorReporter.INSTANCE.init(this);

		_consumer = new CommonsHttpOAuthConsumer(Prefs.getConsumerKey(), Prefs.getConsumerSecret());
		_provider = new CommonsHttpOAuthProvider(Prefs.getRequestTokenUrl(),
				Prefs.getAccessTokenUrl(), Prefs.getAuthorizeUrl());
		_provider.setOAuth10a(true);

		_settings = getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE);

		if (getIntent().getData() == null) {
			try {
				String authUrl = _provider.retrieveRequestToken(_consumer, Prefs.getCallbackUri().toString());
				Prefs.saveRequestInformation(_settings, _consumer.getToken(), _consumer.getTokenSecret());
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
				finish();
			}
			catch (Exception e) {
				ErrorHandler.handle(e, this);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Uri uri = getIntent().getData();
		if (uri == null || !Prefs.getCallbackUri().getScheme().equals(uri.getScheme())) {
			return;
		}

		String[] tokenAndSecret = Prefs.getRequestTokenAndSecret(_settings);
		String token = tokenAndSecret[0];
		String secret = tokenAndSecret[1];
		Intent intent = new Intent(this, Profile.class);

		try {
			if (token != null && secret != null) {
				_consumer.setTokenWithSecret(token, secret);
			}

			_provider.retrieveAccessToken(_consumer, uri.getQueryParameter(
					oauth.signpost.OAuth.OAUTH_VERIFIER));

			token = _consumer.getToken();
			secret = _consumer.getTokenSecret();
			Prefs.saveAuthInformation(_settings, token, secret);
			// Clear the request stuff, now that we have the real thing
			Prefs.resetRequestInformation(_settings);
			intent.putExtra(Prefs.USER_TOKEN, token); // TODO needed?
			intent.putExtra(Prefs.USER_SECRET, secret);
		}
		catch (Exception e) {
			ErrorHandler.handle(e, this);
		}
		finally {
			startActivity(intent);
			finish();
		}
	}
}
