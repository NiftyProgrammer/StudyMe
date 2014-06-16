package com.rwth.i10.exercisegroups.Activitys;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.R.layout;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.ContextData;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends Activity implements OnClickListener{

	private Context _context = null;
	private ProfileData _newData;
	
	private TextView _displayName, _email, _description;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		
		init();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if( !TextUtils.isEmpty( _displayName.getText() ) ){}
		
		finish();
	}
	
	private void init(){
		
		_context = this;
		_newData = MainActivity.mProfileHandler.getProfileData();
		
		_displayName = (TextView)findViewById(R.id.profile_display_name);
		_description = (TextView)findViewById(R.id.profile_desc);
		_email = (TextView)findViewById(R.id.profile_email_address);
		
		if(!TextUtils.isEmpty(_newData.getDisplayName()))
			_displayName.setText(_newData.getDisplayName());
		if(!TextUtils.isEmpty(_newData.getEmail()))
			_email.setText(_newData.getEmail());
		if(!TextUtils.isEmpty(_newData.getDesc()))
			_description.setText(_newData.getDesc());
		
		((Button)findViewById(R.id.profile_send_btn)).setOnClickListener(this);
	}
	
}
