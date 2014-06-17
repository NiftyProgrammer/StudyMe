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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends Activity implements OnClickListener{

	private Context _context = null;
	private ProfileData _newData;
	private ProfileHandler _mProfileHandler;
	
	private TextView _displayName, _email, _description;
	private View _progressLayout;
	private ScrollView _mainView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		
		init();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		new AsyncTask<Void, String, Boolean>(){
			private ManagePreferences pref;
			
			@Override
			protected void onPreExecute() {
				pref = new ManagePreferences(_context);
				_progressLayout.setVisibility(View.VISIBLE);
				_mainView.setVisibility(View.GONE);
			}			
			@Override
			protected Boolean doInBackground(Void... params) {
				// TODO Auto-generated method stub
				boolean sendData = false;
				if( !TextUtils.isEmpty( _displayName.getText() ) && !_displayName.getText().toString().equalsIgnoreCase(_newData.getDisplayName()) ){
					sendData = true;
					_newData.setDisplayName(_displayName.getText().toString());
					publishProgress(ProfileData.PROFILE_DISPLAY_NAME, _newData.getDisplayName());
				}
				if( !TextUtils.isEmpty( _email.getText() ) && !_email.getText().toString().equalsIgnoreCase(_newData.getEmail()) ){
					sendData = true;
					_newData.setEmail(_email.getText().toString());
					publishProgress(ProfileData.PROFILE_EMAIL, _newData.getEmail());
				}
				if( !TextUtils.isEmpty( _description.getText() ) && !_description.getText().toString().equalsIgnoreCase(_newData.getDesc()) ){
					sendData = true;
					_newData.setDesc(_description.getText().toString());
					publishProgress(ProfileData.PROFILE_DESC, _newData.getDesc());
				}
				
				if(sendData){
					_mProfileHandler.setProfileData(_newData);
					_mProfileHandler.updateProfile();
					
					while(!_mProfileHandler.getProcessFinished()){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(!TextUtils.isEmpty(_mProfileHandler.getError()))
						return true;
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
			
		}.execute();
		
	}
	
	private void init(){
		
		_context = this;
		_newData = MainActivity.mProfileHandler.getProfileData();
		
		_displayName = (TextView)findViewById(R.id.profile_display_name);
		_description = (TextView)findViewById(R.id.profile_desc);
		_email = (TextView)findViewById(R.id.profile_email_address);
		_mainView = (ScrollView)findViewById(R.id.profile_main_view);
		_progressLayout = findViewById(R.id.profile_progress_layout);
		
		String []credentials = StaticUtilMethods.getUserCredentials(_context);
		_mProfileHandler = new ProfileHandler(_context, credentials[0], credentials[1]);
		_mProfileHandler.setProfileData(_newData);
		_mProfileHandler.setUpdatedId();
		
		if(!TextUtils.isEmpty(_newData.getDisplayName()))
			_displayName.setText(_newData.getDisplayName());
		if(!TextUtils.isEmpty(_newData.getEmail()))
			_email.setText(_newData.getEmail());
		if(!TextUtils.isEmpty(_newData.getDesc()))
			_description.setText(_newData.getDesc());
		
		((Button)findViewById(R.id.profile_send_btn)).setOnClickListener(this);
	}
	
}
