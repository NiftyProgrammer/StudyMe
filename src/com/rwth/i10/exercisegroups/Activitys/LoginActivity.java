package com.rwth.i10.exercisegroups.Activitys;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.MyContextData;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ProfileHandler;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements MyContextData.Listener{


	/**
	 * The default email to populate the email field with.
	 */
	//public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	//private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		setTitle(getString(R.string.title_activity_login));

		context = this;
		
		// Set up the login form.
		//mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		//mUsernameView.setText(mUsername);

		
		ManagePreferences pref = new ManagePreferences(this);
		mUsername = pref.getPreference(getString(R.string.username_pref), null);
		mPassword = pref.getPreference(getString(R.string.password_pref), null);
		if(!TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mPassword)){			
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		
		
		mUsernameView = (EditText) findViewById(R.id.username);
		mPasswordView = (EditText) findViewById(R.id.password);

		mPasswordView.setImeActionLabel(getString(R.string.action_sign_in_short), R.id.login);
		mPasswordView.setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);

		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if ((id == R.id.login || id == EditorInfo.IME_NULL) && id != EditorInfo.IME_ACTION_NEXT) {
					attemptLogin();
					return true;
				}
				mPasswordView.setError(null);
				return false;
			}
		});/*
		mUsernameView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if(id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_NEXT)
				mUsernameView.setError(null);
				return false;
			}
		});*/


		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(context, RegisterActivity.class));
					}
				});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		/*if (mAuthTask != null) {
			return;
		}*/

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();


		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		


		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);


			MyContextData contextData = ServerHandler.createInstance(mUsername, mPassword);
			contextData.registerGETListener(this);
			contextData.setTimeout(60000);
			contextData.get("user/test", "");


			/*mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);*/
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}


	//registering new user
	private void requestToRegister(){
		/*AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("User not found.")
		.setMessage("Would you like to register as new user.")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mPasswordView.setImeActionLabel("Next", EditorInfo.IME_ACTION_NEXT);
				mPasswordView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				mEmailView.setVisibility(View.VISIBLE);
				mEmailView.requestFocus();
				dialog.dismiss();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mPasswordView.setImeActionLabel(getString(R.string.action_sign_in_short), R.id.login);
				mPasswordView.setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);
				mEmailView.setVisibility(View.GONE);
				dialog.dismiss();											
			}
		})
		.create();
		dialog.show();*/
	}


	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub
		showProgress(false);

		try {
			JSONObject object = new JSONObject(result);
			if (object.optInt("result") > 0) {
				saveCredentials();
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				intent.putExtra("com.rwth.i10.labproject.firstlogin", true);
				startActivity(intent);
				finish();
			} else {

				String reason = object.optString("reason");

				mPasswordView
				.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub

/*		showProgress(false);

		try {
			JSONObject object = new JSONObject(result);
			if(object.optInt("result") > 0){
				saveCredentials();
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			}
			else{
				String reason = object.optString("reason");
				if(reason.contains("User already exists")){
					mUsernameView.setError(getString(R.string.error_incorrect_username));
					mUsernameView.requestFocus();
				}
				else if(reason.contains("Wrong data")){
					mEmailView.setError(getString(R.string.error_invalid_email));
					mEmailView.requestFocus();
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	private void saveCredentials(){
		ManagePreferences managePreferences = new ManagePreferences(this);
		managePreferences.savePreferences(getString(R.string.username_pref), mUsername);
		managePreferences.savePreferences(getString(R.string.password_pref), mPassword);
		managePreferences.putBoolPreferences(ProfileData.PROFILE_PUBLIC, false);
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 *//*
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mUsername)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}


			ContextData contextData = new ContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 8, 
					"fqfc0pqm7rx8940wi40sa0wut6jmqp92vfodhxq1yo8b5vqqm0");



			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}*/
}
