package com.rwth.i10.exercisegroups.Activitys;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.R.layout;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ProfileHandler;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends ActionBarActivity implements OnClickListener{

	private Context _context = null;
	private ProfileData _newData;
	private ProfileHandler _mProfileHandler;


	private MyAsyncTask sendTask;
	private TextView _displayName, _email, _description;
	private View _progressLayout;
	private ScrollView _mainView;
	private ManagePreferences pref;
	private boolean sendData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);


		init();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if(sendTask != null && sendTask.getStatus() == Status.RUNNING){
				sendTask.cancel(false);
				sendTask = null;
			}
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		pref = null;
		pref = new ManagePreferences(_context);
		_progressLayout.setVisibility(View.VISIBLE);
		_mainView.setVisibility(View.GONE);

		sendData = false;
		if( !TextUtils.isEmpty( _displayName.getText() ) && !_displayName.getText().toString().equalsIgnoreCase(_newData.getDisplayName()) ){
			sendData = true;
			_newData.setDisplayName(_displayName.getText().toString());
		}
		if( !TextUtils.isEmpty( _email.getText() ) && !_email.getText().toString().equalsIgnoreCase(_newData.getEmail()) ){
			sendData = true;
			_newData.setEmail(_email.getText().toString());
		}
		if( !TextUtils.isEmpty( _description.getText() ) && !_description.getText().toString().equalsIgnoreCase(_newData.getDesc()) ){
			sendData = true;
			_newData.setDesc(_description.getText().toString());
		}

		if(sendData){
			_mProfileHandler.setProfileData(_newData);
			_mProfileHandler.updateProfile();
		}
		
		if(sendTask == null || sendTask.getStatus() != Status.RUNNING){
			sendTask = null;
			sendTask = new MyAsyncTask();
		}
		sendTask.execute();
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(sendTask != null && sendTask.getStatus() == Status.RUNNING){
			sendTask.cancel(false);
			sendTask = null;
			return;
		}
	}

	private void init(){

		_context = this;

		_displayName = (TextView)findViewById(R.id.profile_display_name);
		_description = (TextView)findViewById(R.id.profile_desc);
		_email = (TextView)findViewById(R.id.profile_email_address);
		_mainView = (ScrollView)findViewById(R.id.profile_main_view);
		_progressLayout = findViewById(R.id.profile_progress_layout);

		String []credentials = StaticUtilMethods.getUserCredentials(_context);
		_mProfileHandler = new ProfileHandler(_context, credentials[0], credentials[1]);
		_mProfileHandler.fetschProfileData();
		_mProfileHandler.setUpdatedId();
		_newData = MainActivity.mProfileHandler.getProfileData();

		if(!TextUtils.isEmpty(_newData.getDisplayName()))
			_displayName.setText(_newData.getDisplayName());
		if(!TextUtils.isEmpty(_newData.getEmail()))
			_email.setText(_newData.getEmail());
		if(!TextUtils.isEmpty(_newData.getDesc()))
			_description.setText(_newData.getDesc());

		((Button)findViewById(R.id.profile_send_btn)).setOnClickListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private class MyAsyncTask extends AsyncTask<Void, String, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			if(sendData){
				while(!_mProfileHandler.getProcessFinished() || isCancelled()){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(TextUtils.isEmpty(_mProfileHandler.getError())){
					if( !TextUtils.isEmpty( _displayName.getText() ) && !_displayName.getText().toString().equalsIgnoreCase(_newData.getDisplayName()) ){
						publishProgress(ProfileData.PROFILE_DISPLAY_NAME, _newData.getDisplayName());
					}
					if( !TextUtils.isEmpty( _email.getText() ) && !_email.getText().toString().equalsIgnoreCase(_newData.getEmail()) ){
						publishProgress(ProfileData.PROFILE_EMAIL, _newData.getEmail());
					}
					if( !TextUtils.isEmpty( _description.getText() ) && !_description.getText().toString().equalsIgnoreCase(_newData.getDesc()) ){
						publishProgress(ProfileData.PROFILE_DESC, _newData.getDesc());
					}

					return true;
				}
			}

			return false;
		}
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			pref.savePreferences(values[0], values[1]);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				finish();
			else{
				Toast.makeText(_context, "Error while sending: " + MainActivity.mProfileHandler.getError(), 0).show();
				_progressLayout.setVisibility(View.GONE);
				_mainView.setVisibility(View.VISIBLE);
			}
			MainActivity.mProfileHandler.changeProcessFinished();
		}

	};
}
