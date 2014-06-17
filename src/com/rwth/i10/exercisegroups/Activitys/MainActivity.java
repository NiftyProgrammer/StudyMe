package com.rwth.i10.exercisegroups.Activitys;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.internal.by;
import com.google.android.gms.internal.cu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Adapters.MainListViewAdapter;
import com.rwth.i10.exercisegroups.Util.Constants;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.MessageCategories;
import com.rwth.i10.exercisegroups.Util.MessagesTypes;
import com.rwth.i10.exercisegroups.Util.MyContextData;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ProfileHandler;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;
import com.rwth.i10.exercisegroups.database.GroupsDataSource;
import com.rwth.i10.exercisegroups.database.SQLiteHelper;
import com.rwth.i10.exercisegroups.gcm_config.GcmServer;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;
import de.contextdata.Entity;
import de.contextdata.Event;
import de.contextdata.RandomString;

public class MainActivity extends ActionBarActivity implements MyContextData.Listener,
LocationListener{


	public static FragmentManager mFragmentManager;
	public static GroupsDataSource databaseSourse;
	public static boolean closeApplication;
	public static MyContextData serverHandler = null;
	/*public static ContextData userHandler = null;*/
	public static Location mLocation;
	public static ProfileHandler mProfileHandler;


	private static Activity context;

	private static GoogleMap map;	
	private static SupportMapFragment mapFragment;
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private static String regId;


	private static ActionBarDrawerToggle mActionBarDrawerToggle;
	private static DrawerLayout mDrawerLayout;
	private static View LeftDrawer;

	private LocationManager mLocationManager = null;
	private PlaceholderFragment mFragment;
	private String 	mUsername,
	mPassword;


	public static MainListViewAdapter groupListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = this;

		boolean firstLogin = false;
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey("com.rwth.i10.labproject.firstlogin")){
			firstLogin = bundle.getBoolean("com.rwth.i10.labproject.firstlogin", false);
		}
		
		if(mapFragment == null)
		{
			GoogleMapOptions mapOptions = new GoogleMapOptions()
			.mapType(GoogleMap.MAP_TYPE_NORMAL)
			.zoomControlsEnabled(true)
			.compassEnabled(true)
			.camera(new CameraPosition(new LatLng(0, 0), 0, 0, 0));
			mapFragment = SupportMapFragment.newInstance(mapOptions);
		}
		map = mapFragment.getMap();
		mFragment = new PlaceholderFragment();		
		mFragmentManager = getSupportFragmentManager();
		if(savedInstanceState == null)
			mFragmentManager.beginTransaction()
			.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
			.add(R.id.container, mFragment)
			.add(R.id.content_frame, mapFragment)
			.commit();

		init(firstLogin);
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(closeApplication)
			finish();
		/*mFragmentManager.beginTransaction()
		.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
		.add(R.id.container, mFragment)
		.add(R.id.content_frame, mapFragment)
		.commit();*/
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		databaseSourse.close();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if(StaticUtilMethods.isNetworkAvailable(context))
			fetschGroups();
		else
			Toast.makeText(context, "Network not connected", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		mActionBarDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		mActionBarDrawerToggle.syncState();
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

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(StaticUtilMethods.isBetterLocation(mLocation, location))
			mLocation = location;
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		mLocationManager.removeUpdates(this);
		mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(new Criteria(), true), 
				StaticUtilMethods.TWO_MINUTES, 0, this);
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		mLocationManager.removeUpdates(this);
		mLocationManager.requestLocationUpdates(provider, StaticUtilMethods.TWO_MINUTES, 0, this);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.d("GCM", "Error" + resultCode);
				/*finish();*/
			}
			return false;
		}
		return true;
	}

	private void init(boolean firstLogin){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		databaseSourse = new GroupsDataSource(context);
		databaseSourse.open();

		closeApplication = false;


		String []credentials = StaticUtilMethods.getUserCredentials(context);
		mUsername = credentials[0];
		mPassword = credentials[1];

		serverHandler = ServerHandler.createInstance(getString(R.string.server_username), getString(R.string.server_password));
		serverHandler.registerGETListener(this);
		serverHandler.registerPOSTListener(this);
		serverHandler.setTimeout(60000 * 2);

		/*userHandler = ServerHandler.createInstance(mUsername, mPassword);
		userHandler.registerGETListener(this);
		userHandler.registerPOSTListener(this);*/

		mProfileHandler = new ProfileHandler(context, mUsername, mPassword);
		if(firstLogin)
			mProfileHandler.getPreviousProfile();


		groupListView = new MainListViewAdapter(context);

		if(checkPlayServices()){
			gcm = GoogleCloudMessaging.getInstance(context);
			regId = getRegistrationId(context);
			Log.d("RegId", "prev Id: " + regId);
			if (TextUtils.isEmpty(regId)) {
				registerInBackground();
			}
		}

		getDatabaseGroups();

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = mLocationManager.getBestProvider(new Criteria(), true);
		mLocation = mLocationManager.getLastKnownLocation(provider);
		mLocationManager.requestLocationUpdates(provider, StaticUtilMethods.TWO_MINUTES, 0, this);
	}
	private String getRegistrationId(Context context) {
		final ManagePreferences prefs = new ManagePreferences(context);
		String registrationId = prefs.getStringPreferences(Constants.PROPERTY_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getIntPreferences(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);

		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationId;
	}
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	private void registerInBackground() {
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}
				try {
					regId = gcm.register(getString(R.string.gcm_project_code_number));
					storeRegistrationId(context, regId);

					mProfileHandler.setMessageId(regId);
					mProfileHandler.updateProfile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	private void storeRegistrationId(Context context, String regId) {
		ManagePreferences pref = new ManagePreferences(context);
		int appVersion = getAppVersion(context);
		pref.putStringPreferences(Constants.PROPERTY_REG_ID, regId);
		pref.putIntPreferences(Constants.PROPERTY_APP_VERSION, appVersion);
	}

	private void getDatabaseGroups(){
		new AsyncTask<Void, Void, ArrayList<GroupData>>(){
			@Override
			protected ArrayList<GroupData> doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Cursor cursor = databaseSourse.getAllGroups();
				ArrayList<GroupData> groups = new ArrayList<GroupData>();
				if(cursor != null && cursor.moveToFirst()){
					do{
						GroupData data = new GroupData(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_AVTIVITY)), 
								cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_COURSE)),
								cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ADDRESS)),
								cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TABLE_MAX_PART)), null);
						data.setGroup_id(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ID)));
						data.setLat(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LAT)));
						data.setLng(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LNG)));
						byte []bytes = cursor.getBlob(cursor.getColumnIndex(SQLiteHelper.TABLE_IMAGE));
						if(bytes != null && bytes.length > 0)
							data.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
					}while(cursor.moveToNext());
				}
				return groups;
			}
			@Override
			protected void onPostExecute(ArrayList<GroupData> result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				groupListView.addItem(result);
			}
		}.execute();
	}


	public static void fetschGroups(){
		serverHandler.get("events/show", StaticUtilMethods.createFetchServerJSON());
	}
	public static void sendMessage(String message, MessagesTypes type){

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put(MessageCategories.TYPE.getString(), String.valueOf(type.ordinal()));
		params.put(MessageCategories.MESSAGE.toString(), message);

		JSONObject retrive = new JSONObject();
		try {
			retrive.put("model", "COMPLETE");
			retrive.put("type", "RELEVANCE");

			JSONObject entity1 = new JSONObject();
			entity1.put("key", "app");
			entity1.put("value", "study_me");

			JSONObject entity2 = new JSONObject();
			entity2.put("key", "activity");
			entity2.put("value", "profile_data");

			retrive.put("entities", new JSONArray().put(entity1).put(entity2));
		} catch (JSONException e) {}

		MyContextData myContext = ServerHandler.createInstance(context.getString(R.string.server_username), context.getString(R.string.server_password));
		myContext.setTimeout(60000 *2);
		myContext.registerGETListener(new MyContextData.Listener() {

			@Override
			public void onPOSTResult(String result) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGETResult(String result) {
				// TODO Auto-generated method stub
				Log.d("send ids", result);
				try {
					List<String> ids = new ArrayList<String>();
					JSONObject data = new JSONObject(result);

					if(data.optInt("result") != 0 || data.optInt("total_events") > 0){
						JSONArray events = data.optJSONArray("events");
						if(events != null){
							for(int i=0; i<events.length(); i++){
								JSONObject event = events.optJSONObject(i);
								JSONArray entities = event.optJSONArray("entities");
								String app = "", activity = "", msgId = "", uname = "";
								for(int j=0; j<entities.length(); j++){
									JSONObject obj = entities.optJSONObject(j);
									if("app".equalsIgnoreCase(obj.optString( "key" )))
										app = obj.optString("value");
									if("activity".equalsIgnoreCase(obj.optString( "key" )))
										activity = obj.optString("value");
									if(ProfileData.PROFILE_USERNAME.equalsIgnoreCase(obj.optString( "key" )))
										uname = obj.optString("value");
									if(ProfileData.PROFILE_MSG_ID.equalsIgnoreCase(obj.optString( "key" )))
										msgId = obj.optString("value");
								}
								if("study_me".equalsIgnoreCase(app) && "profile_data".equalsIgnoreCase(activity)){
									ids.add(msgId);
								}
							}

							GcmServer server = new GcmServer();
							server.sendMessage(params, ids, context);
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});


	}

	public static void hideUserProfile(){
		new AsyncTask<Void, Void, Integer>(){
			private ProfileHandler mServerProfile;
			protected void onPreExecute() {
				mServerProfile = new ProfileHandler(context, context.getString(R.string.server_username),
						context.getString(R.string.server_password));
				mServerProfile.getPreviousProfile();
			}

			@Override
			protected Integer doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				while(mServerProfile.getUploadedId()){
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
				}
				return mServerProfile.getProfileData().getEvent_id();
			}
			protected void onPostExecute(Integer result) {
				if(result > 0)
					mServerProfile.deleteProfile(result);
			}
		}.execute();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			LeftDrawer = rootView.findViewById(R.id.left_drawer);

			//setting nevigation drawer
			mDrawerLayout = (DrawerLayout)rootView.findViewById(R.id.drawer_layout);
			mActionBarDrawerToggle = 
					new ActionBarDrawerToggle(context, mDrawerLayout, R.drawable.ic_drawer_holo_light, 
							R.string.navigation_drawer_open_string, R.string.navigation_drawer_close_string);

			mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_holo_light, GravityCompat.START);

			ListView groupList = (ListView) rootView.findViewById(R.id.left_drawer_group_list);
			groupList.setAdapter(groupListView);

			Button btn = (Button)rootView.findViewById(R.id.left_drawer_create_group_btn);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, CreateGroupActivity.class));
				}
			});
			((Button)rootView.findViewById(R.id.left_drawer_view_btn)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, MainListActivity.class));										
				}
			});
			((Button)rootView.findViewById(R.id.left_drawer_profile_btn)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, ProfileActivity.class));
				}
			});
			((Button)rootView.findViewById(R.id.left_drawer_profile_change_status)).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mProfileHandler.getProfileData().isPublicProfile()){
						
					}
					else{
						mProfileHandler.uploadProfile(serverHandler);
						((Button)arg0).
					}					
				}
			});

			/* MapFragment fragment =  (MapFragment)MapFragment.instantiate(context, "com.google.android.gms.maps.MapFragment");
            map = fragment.getMap();

            map.setMyLocationEnabled(true);*/

			return rootView;
		}
	}

	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub
		Log.d("GET Data", result);/*
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resultCode == ConnectionResult.SUCCESS)*/
		new SetMarkers().execute(result);
	}


	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		Log.d("POST Data", result);
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

			while(map == null){
				map = mapFragment.getMap();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
			}

			if(events != null){
				LinkedHashMap<String, JSONObject> groups = new LinkedHashMap<String, JSONObject>();
				ArrayList<String> endedGroups = new ArrayList<String>();
				for(int i=0; i< events.length(); i++){
					JSONObject eve = events.optJSONObject(i);
					String groupId = eve.optString("session");

					if(eve.optString("action").equalsIgnoreCase("START"))
						groups.put(groupId, eve);
					else
						endedGroups.add(groupId);
				}

				if(!endedGroups.isEmpty()){
					for(int i=0; i<endedGroups.size(); i++){
						groups.remove(endedGroups.get(i));
					}
				}
				if(!groups.isEmpty())
					for(JSONObject obj : groups.values())
						publishProgress(obj);
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(JSONObject... values) {
			// TODO Auto-generated method stub
			String activity = "", course = "", address = "";
			JSONArray entities = values[0].optJSONArray("entities");
			double lat = 0, lng = 0;
			if(entities != null){
				for(int i=0; i<entities.length(); i++){
					JSONObject entitiy = entities.optJSONObject(i);
					if(entitiy != null){
						if("group_activity".equalsIgnoreCase(entitiy.optString("key")))
							activity = entitiy.optString("value");
						if("group_course".equalsIgnoreCase(entitiy.optString("key")))
							course = entitiy.optString("value");
						if("group_address".equalsIgnoreCase(entitiy.optString("key")))
							address = entitiy.optString("value");
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

					LatLng position = new LatLng(lat, lng);

					map.addMarker(new MarkerOptions()
					.title(activity)
					.snippet(course + "\nAddress: " + address)
					.position(position));
				}

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));

			}



		}

	}
}
