/*package com.rwth.i10.exercisegroups.Activitys;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Adapters.ListViewMainAdapter;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.ContextData;

public class MainListActivity extends ActionBarActivity implements ContextData.Listener{

	private ActionBarDrawerToggle mActionBarDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private View LeftDrawer;
	private Activity context;
	private ListViewMainAdapter adapter;
	private ContextData contextData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		init();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		MainActivity.closeApplication = true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if(mActionBarDrawerToggle.onOptionsItemSelected(item))
			return true;
		return super.onOptionsItemSelected(item);
	}
	
	private void init(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		context = this;
		LeftDrawer = findViewById(R.id.list_left_drawer);
				
		ServerHandler.setListener(this);
		
		//setting nevigation drawer
		mDrawerLayout = (DrawerLayout)findViewById(R.id.list_view_drawer_layout);
		mActionBarDrawerToggle = 
				new ActionBarDrawerToggle(context, mDrawerLayout, R.drawable.ic_drawer_holo_light, 
						R.string.navigation_drawer_open_string, R.string.navigation_drawer_close_string);

		mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_holo_light, GravityCompat.START);

		adapter = new ListViewMainAdapter(context);
		ListView groupList = (ListView) findViewById(R.id.list_content_view);
		groupList.setAdapter(adapter);
		
		Button btn = (Button)findViewById(R.id.list_left_drawer_create_group_btn);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDrawerLayout.closeDrawer(GravityCompat.START);
				startActivity(new Intent(context, CreateGroupActivity.class));
			}
		});
		((Button)findViewById(R.id.list_left_drawer_view_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		
		contextData = ServerHandler.getContextData();
		contextData.registerGETListener(this);
		contextData.get("events/show", StaticUtilMethods.createFetchServerJSON());
		
	}

	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub
		new SetMarkers().execute(result);
	}

	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		
	}
	
	private class SetMarkers extends AsyncTask<String, JSONObject, Void>{

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			JSONArray events = null;
			try {
				JSONObject data = new JSONObject(params[0]);
				events = data.getJSONArray("events");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(events != null){
				HashMap<String, JSONObject> list = new HashMap<String, JSONObject>();
				for(int i=0; i< events.length(); i++){
					JSONObject eve = events.optJSONObject(i);
					if(eve != null){
						String tempname = "";
						JSONArray entities = eve.optJSONArray("entities");
						for(int k=0; k<entities.length(); k++){
							JSONObject temp = entities.optJSONObject(k);
							if(temp.optString("key").equalsIgnoreCase("group_id")){
								tempname = temp.optString("value");
								break;
							}
						}
						String tempstatus = eve.optString("action");
						if(tempstatus.equalsIgnoreCase("START"))
							list.put(tempname, eve);
						else if(list.containsKey(tempname))
							list.remove(tempname);
						else{
							JSONObject obj = list.get(tempname);
							if(obj != null)
							publishProgress(obj);
							list.remove(tempname);
						}
					}
				}
				if(!list.isEmpty()){
					for(JSONObject obj : list.values())
						if(obj != null)
							publishProgress(obj);
				}
				JSONObject obj = events.optJSONObject(events.length()-1);
				String status = obj.optString("group_status");
				//if(!TextUtils.isEmpty(status) && status.equalsIgnoreCase("START"))
					publishProgress(obj);
			}
			return null;
		}
	
		@Override
		protected void onProgressUpdate(JSONObject... values) {
			// TODO Auto-generated method stub
			String name = "", source = "", course = "", address = "", id = "";
			JSONArray entities = values[0].optJSONArray("entities");
			double lat = 0, lng = 0;
			if(entities != null){
				for(int i=0; i<entities.length(); i++){
					JSONObject entitiy = entities.optJSONObject(i);
					if(entitiy != null){
						if("app".equalsIgnoreCase(entitiy.optString("key")))
							name = entitiy.optString("value");
						if("group_activity".equalsIgnoreCase(entitiy.optString("key")))
							source = entitiy.optString("value");
						if("group_course".equalsIgnoreCase(entitiy.optString("key")))
							course = entitiy.optString("value");
						if("group_address".equalsIgnoreCase(entitiy.optString("key")))
							address = entitiy.optString("value");
						if("group_id".equalsIgnoreCase(entitiy.optString("key")))
							id = entitiy.optString("value");
						if("lat".equalsIgnoreCase(entitiy.optString("key")))
							try {
								lat = Double.parseDouble(String.valueOf(entitiy.opt("value")));
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								lat = 0;
							}
						if("lng".equalsIgnoreCase(entitiy.optString("key")))
							try {
								lng = Double.parseDouble(String.valueOf(entitiy.opt("value")));
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								lng = 0;
							}
					}
				}

				Log.d("name", name);
				GroupData data = new GroupData(source, course, address, 0, null);
				data.setGroupSession(id);
				adapter.addItem(data);
			}
		}
		
	}
	
}
*/