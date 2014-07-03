package com.rwth.i10.exercisegroups.Activitys;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.MyContextData;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;

public class RegisterActivity extends ActionBarActivity {
	private String mUsername;
	private String mPassword;
	private String mEmail;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mEmailView;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;

	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		setTitle(getString(R.string.title_activity_register));

		init();

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void init(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		context = this;

		mUsernameView = (EditText) findViewById(R.id.reg_username);
		mPasswordView = (EditText) findViewById(R.id.reg_password);
		mEmailView = (EditText) findViewById(R.id.reg_email);

		mRegisterFormView = findViewById(R.id.register_form);
		mRegisterStatusView = findViewById(R.id.register_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);

		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if ((id == EditorInfo.IME_NULL)) {
					attemptRegister();
					return true;
				}
				mPasswordView.setError(null);
				return false;
			}
		});

		mEmailView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if ((id == R.id.register || id == EditorInfo.IME_NULL)) {
					attemptRegister();
					return true;
				}
				mPasswordView.setError(null);
				return false;
			}
		});
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						attemptRegister();
					}
				});
	}

	private void attemptRegister(){
		mUsernameView.setError(null);
		mPasswordView.setError(null);
		mEmailView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mEmail = mEmailView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

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

		// Check for a valid Email.
		if(!TextUtils.isEmpty(mEmail)){
			if (!mEmail.contains("@")) {
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mRegisterStatusMessageView.setText(R.string.register_progress);
			showProgress(true);


			MyContextData contextData = ServerHandler.createInstance("", "");
			contextData.registerPOSTListener(new MyContextData.Listener() {

				@Override
				public void onPOSTResult(String result) {
					// TODO Auto-generated method stub
					check(result);
				}

				@Override
				public void onGETResult(String result) {
					// TODO Auto-generated method stub
				}
				
				private void check(String result){
					showProgress(false);
					Log.d("data", result);
					try {
						JSONObject object = new JSONObject(result);
						if(object.optInt("result") > 0){
							requestToLogin();
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
					}
				}
			});
			JSONObject data = new JSONObject();
			try {
				data.put("name", mUsername);
				data.put("pass", mPassword);
				data.putOpt("email", mEmail);
			} catch (JSONException e) {
			}
			contextData.post("user/new", data.toString());


			/*mAuthTask = new UserLoginTask();
					mAuthTask.execute((Void) null);*/
		}
	}

	//registering new user
	private void requestToLogin(){
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("Registration Completed.")
		.setMessage("Would you like to Login with this user?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				saveCredentials();
				Intent intent = new Intent(context, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				dialog.dismiss();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();					
				finish();
			}
		})
		.create();
		dialog.show();
	}

	private void saveCredentials(){
		ManagePreferences managePreferences = new ManagePreferences(this);
		managePreferences.savePreferences(getString(R.string.username_pref), mUsername);
		managePreferences.savePreferences(getString(R.string.password_pref), mPassword);
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

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mRegisterStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mRegisterFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

}
