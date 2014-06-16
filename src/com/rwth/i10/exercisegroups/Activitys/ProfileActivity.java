package com.rwth.i10.exercisegroups.Activitys;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.R.layout;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.ContextData;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ProfileActivity extends Activity {

	private ServerHandler userContext = null;
	private Context context = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		
		init();
	}
	
	private void init(){
		
		context = this;
		
		String []credentials = StaticUtilMethods.getUserCredentials(context);
		userContext = new ServerHandler(credentials[0], credentials[1]);
		
	}
	
}
