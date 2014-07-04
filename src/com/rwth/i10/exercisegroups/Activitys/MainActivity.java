package com.rwth.i10.exercisegroups.Activitys;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.rwth.i10.exercisegroups.Util.SlidingUpPanelLayout;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;
import com.rwth.i10.exercisegroups.Util.UserStatus;
import com.rwth.i10.exercisegroups.database.GroupsDataSource;
import com.rwth.i10.exercisegroups.database.SQLiteHelper;
import com.rwth.i10.exercisegroups.gcm_config.GcmServer;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.Entity;

public class MainActivity extends ActionBarActivity implements MyContextData.Listener,
LocationListener, View.OnClickListener{


	/**
	 * Main list to be displayed in sliding nevigational bar */
	public static MainListViewAdapter groupListView;
	
	/**Main view fragment to be used to display all the main activity*/
	public static FragmentManager mFragmentManager;
	
	/**
	 * Database connection sourse*/
	public static GroupsDataSource databaseSourse;
	
	/**
	 * flag set from other applications to close application*/
	public static boolean closeApplication;
	
	/**
	 * Server connection handler to connecet to ContextData server*/
	public static MyContextData serverHandler = null;

	/**
	 * Used to store users location and later uses it*/
	public static Location mLocation;
	
	/**
	 * Used to store user editdata and later use it to send data to other users*/
	public static ProfileHandler mProfileHandler;
	
	/**
	 * Main gcm registration id*/
	public static String regId;


	private static Activity context;
	private static MainActivity mainInstance;
	private static boolean isRefresh;

	private static GoogleMap map;	
	private static SupportMapFragment mapFragment;
	private GoogleCloudMessaging gcm;


	/**
	 * used to store string got from server about groups*/
	private static String allGroupsString;
	
	/**
	 * used to store status of get request send to server*/
	private static GetRequest getRequest;
	
	/**
	 * varialbes used to create and update LeftSliding drawer*/
	private static ActionBarDrawerToggle mActionBarDrawerToggle;
	private static DrawerLayout mDrawerLayout;
	private static View LeftDrawer;
	private static TextView filterView;
	private static TextView usersCounter;
	private static View slidingLayout;
	private static SlidingUpPanelLayout slidingPanel;
	private static HashMap<Marker, GroupData> allMarkers = new HashMap<Marker, GroupData>();

	private LocationManager mLocationManager = null;
	private PlaceholderFragment mFragment;

	private static String 	mUsername,
	mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		context = this;
		mainInstance = this;

		/**
		 * for first time there is no data, os just used to get updated server data.*/
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
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(slidingPanel.isPanelAnchored() || slidingPanel.isPanelExpanded())
			slidingPanel.collapsePanel();
		else{
			databaseSourse.close();
			super.onBackPressed();
		}
		//return;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if(StaticUtilMethods.isNetworkAvailable(context)){
			fetschGroups();
		}
		else
			showToast("Network not connected");
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			fetschGroups();
			return true;
		}
		else if(mActionBarDrawerToggle.onOptionsItemSelected(item))
			return true;
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		/*if(location.distanceTo(mLocation) > 50){
			//stop group if running
		}*/
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

	
	/**
	 * Used to check play services version for map to play*/
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

	
	/**
	 * Initialize user all main views*/
	private void init(boolean firstLogin){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		//database resourses
		databaseSourse = new GroupsDataSource(context);
		databaseSourse.open();

		closeApplication = false;


		//getting user credentials from pref
		String []credentials = StaticUtilMethods.getUserCredentials(context);
		mUsername = credentials[0];
		mPassword = credentials[1];

		//server handler to handle server main account.
		serverHandler = ServerHandler.createInstance(getString(R.string.server_username), getString(R.string.server_password));
		serverHandler.registerGETListener(this);
		serverHandler.registerPOSTListener(this);
		serverHandler.setTimeout(60000 * 2);


		mProfileHandler = new ProfileHandler(context, mUsername, mPassword);
		if(firstLogin)
			mProfileHandler.getPreviousProfile();


		groupListView = new MainListViewAdapter(context);

		if(checkPlayServices()){
			gcm = GoogleCloudMessaging.getInstance(context);
			regId = getRegistrationId(context);
			int prevTime = getPreviousRevisedVersion(context);
			int currentTime = (int)(System.currentTimeMillis() / 1000.0f);
			Log.d("RegId", "prev Id: " + regId);
			if (firstLogin || (TextUtils.isEmpty(regId) || Math.abs(currentTime - prevTime) > 86400000)) {		//if id is older then day
				registerInBackground();
			}
		}

		getDatabaseGroups();
		//getDatabaseUsersJoinReq();

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = mLocationManager.getBestProvider(new Criteria(), true);
		mLocation = mLocationManager.getLastKnownLocation(provider);
		mLocationManager.requestLocationUpdates(provider, StaticUtilMethods.TWO_MINUTES, 0, this);
	}
	
	
	/**
	 * Used to get msg registration id from preferences*/
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
	
	/**
	 * get previous msg id version number */
	private int getPreviousRevisedVersion(Context context){
		ManagePreferences prefs = new ManagePreferences(context);
		return prefs.getIntPreferences(Constants.PROPERTY_REG_ID_DATE, (int)(System.currentTimeMillis() / 1000.0f));
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
	
	/**
	 * Get msg id from server in background */
	private void registerInBackground() {
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}

				while(true){
					try {
						regId = gcm.register(getString(R.string.gcm_project_code_number));
						storeRegistrationId(context, regId);

						mProfileHandler.setMessageId(regId);
						mProfileHandler.updateProfile();
						if(!TextUtils.isEmpty(regId))
							break;
						try {
							Thread.sleep(10000 * 5);
						} catch (InterruptedException e) {}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
				return null;
			}
		}.execute();
	}

	/**
	 * Store msg registration id in preferences. */
	private void storeRegistrationId(Context context, String regId) {
		ManagePreferences pref = new ManagePreferences(context);
		int appVersion = getAppVersion(context);
		pref.putStringPreferences(Constants.PROPERTY_REG_ID, regId);
		pref.putIntPreferences(Constants.PROPERTY_REG_ID_DATE, (int)(System.currentTimeMillis() / 1000.0f));
		pref.putIntPreferences(Constants.PROPERTY_APP_VERSION, appVersion);
	}

	/**
	 * used to get previously created groups that the user have created */
	private void getDatabaseGroups(){
		new AsyncTask<Void, Void, ArrayList<GroupData>>(){
			private Cursor cursor = null;
			protected void onPreExecute() {
			}

			@Override
			protected ArrayList<GroupData> doInBackground(Void... params) {
				// TODO Auto-generated method stub
				cursor = databaseSourse.getAllGroups();
				ArrayList<GroupData> groups = new ArrayList<GroupData>();
				if(cursor != null && cursor.moveToFirst()){
					do{
						GroupData data = new GroupData(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_AVTIVITY)), 
								cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_COURSE)),
								cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ADDRESS)),
								cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TABLE_MAX_PART)), null);
						data.setGroupId(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ID)));
						data.setGroupSession(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_SESSION)));
						data.setLat(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LAT)));
						data.setLng(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LNG)));
						data.setStatus(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_STATUS)));
						byte []bytes = cursor.getBlob(cursor.getColumnIndex(SQLiteHelper.TABLE_IMAGE));
						if(bytes != null && bytes.length > 0)
							data.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
						groups.add(data);
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

	/**
	 * checks if new location is within our desired group data */
	private boolean isWithinDistance(double groupLat, double groupLng){
		if(mLocation == null)
			return false;
		double latDistance = Math.toRadians(mLocation.getLatitude() - groupLat);
		double lngDistance = Math.toRadians(mLocation.getLongitude() - groupLng);
		double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
				(Math.cos(Math.toRadians(mLocation.getLatitude()))) *
				(Math.cos(Math.toRadians(groupLat))) *
				(Math.sin(lngDistance / 2)) *
				(Math.sin(lngDistance / 2));

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double dist = 6371 * c;             
		return dist < 50;
	}

	/**
	 * get users joined requests from database. */
	private static void getDatabaseUsersJoinReq(){
		new AsyncTask<Void, Void, Integer>(){
			@Override
			protected Integer doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return databaseSourse.getUsersCount(UserStatus.JOIN_REQUEST.ordinal());
			}
			@Override
			protected void onPostExecute(Integer result) {
				// TODO Auto-generated method stub
				if(result > 0){
					usersCounter.setVisibility(View.VISIBLE);
					usersCounter.setText(String.valueOf(result));
				}					
			}
		}.execute();
	}	

	/**
	 * send message to server class and forward it to user. */
	private static void sendMessageToServer(Map<String, String> params, List<String> ids, MessagesTypes type){
		GcmServer server = new GcmServer();
		server.sendMessage(params, ids, type, context);
	}

	
	/**
	 * to delete specific group from server */
	public static void deleteGroupFromServer(GroupData group){
		getRequest = GetRequest.DELETE_GROUP_REQUEST;

		JSONObject data = new JSONObject();

		try {
			data.put("model", "COMPLETE");
			data.put("category", "ACTIVITY");
			data.put("source", "MOBILE");
			data.put("type", "ANNOUNCEMENT");

			JSONObject entity1 = new JSONObject();
			JSONObject entity2 = new JSONObject();
			JSONObject entity3 = new JSONObject();
			JSONObject entity4 = new JSONObject();
			JSONObject entity5 = new JSONObject();

			entity1.put("key", "app");
			entity1.put("value", "study_me");

			entity2.put("key", "group_activity");
			entity2.put("value", group.getName());

			entity3.put("key", "group_admin");
			entity3.put("value", group.getAdmin());

			entity4.put("key", "group_id");
			entity4.put("value", group.getGroupId());

			entity5.put("key", "joined_users");
			entity5.put("value", group.getUsers_joined().toString());

			JSONArray entities = new JSONArray();
			entities.put(entity1);
			entities.put(entity2);
			entities.put(entity3);
			entities.put(entity4);

			data.put("entities", entities);			
		} catch (Exception e) {}


		serverHandler.get("events/show", data.toString());

	}

	/**
	 * fetsch latest groups from server for map update. */
	public static void fetschGroups(){
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mainInstance.setProgressBarIndeterminateVisibility(true);
			}
		});
		
		getRequest = GetRequest.GROUP_REQUEST;
		isRefresh = true;
		serverHandler.get("events/show", StaticUtilMethods.createFetchServerJSON());
	}
	
	
	/**
	 * user call funtion for sending message*/
	public static void sendMessage(String message, String []ids, final MessagesTypes type){

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put(MessageCategories.MESSAGE.toString(), message);
		params.put(MessageCategories.TYPE.getString(), String.valueOf(type.ordinal()));

		if(ids != null){
			sendMessageToServer(params, Arrays.asList(ids), type);
			return;
		}

		JSONObject retrive = new JSONObject();
		try {
			retrive.put("model", "COMPLETE");
			retrive.put("type", "PROFILE");

			JSONObject entity1 = new JSONObject();
			entity1.put("key", "app");
			entity1.put("value", "study_me");

			JSONObject entity2 = new JSONObject();
			entity2.put("key", "activity");
			entity2.put("value", "profile_data");

			retrive.put("entities", new JSONArray().put(entity1).put(entity2));
		} catch (JSONException e) {}

		// if ids are null then get its from server to send it 
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
								if("study_me".equalsIgnoreCase(app) && "profile_data".equalsIgnoreCase(activity) && !mUsername.equalsIgnoreCase(uname)){
									ids.add(msgId);
								}
							}
							sendMessageToServer(params, ids, type);							
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		myContext.get("events/show", retrive.toString());

	}

	/**
	 * Hides/deletes user profile from main server account */
	public static void hideUserProfile(){
		new AsyncTask<Void, Void, Long>(){
			private ProfileHandler mServerProfile;
			protected void onPreExecute() {
				mServerProfile = new ProfileHandler(context, context.getString(R.string.server_username),
						context.getString(R.string.server_password));
				mServerProfile.getPreviousProfile();
				mServerProfile.setUpdatedId(true);
			}

			@Override
			protected Long doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				while(mServerProfile.getUploadedId()){
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
				}
				return mServerProfile.getProfileData().getEvent_id();
			}
			protected void onPostExecute(Long result) {
				if(result > 0)
					mServerProfile.deleteProfile(result);
			}
		}.execute();
	}

	/**
	 * put markers of filtered group on map */
	private void setFilteredGroups(ArrayList<JSONObject> array){

		SetMarkers marks = new SetMarkers();		
		marks.onPreExecute();

		if(array == null || array.isEmpty()){
			showToast("No group found");
			return;
		}
		for(int i=0; i<array.size(); i++)
			marks.onProgressUpdate(array.get(i));
		marks.onPostExecute(null);
	}

	/**
	 * get group data from previously downloaded data*/
	private static long getPreviousGroupData(GroupData group){
		JSONArray events = null;
		try {
			JSONObject data = new JSONObject(allGroupsString);
			events = data.getJSONArray("events");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long groupId = 0;
		if(events != null){
			for(int i=0; i< events.length(); i++){
				JSONObject eve = events.optJSONObject(i);
				groupId = eve.optLong("id");

				GroupData tempGroup = new GroupData();

				JSONArray entities = eve.optJSONArray("entities");
				if(entities != null){
					for(int j=0; j<entities.length(); j++){
						JSONObject entity = entities.optJSONObject(j);
						if(entity != null){
							if("group_activity".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setName(entity.optString("value"));
							if("group_course".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setCourse(entity.optString("value"));
							if("group_address".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setAddress(entity.optString("value"));
							if("group_desc".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setDescription(entity.optString("value"));
							if("group_id".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setGroupId(entity.optString("value"));
							if("group_admin".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setAdmin(entity.optString("value"));
							if("lat".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setLat(entity.optDouble("value"));
							if("lng".equalsIgnoreCase(entity.optString("key")))
								tempGroup.setLng(entity.optDouble("value"));
						}
					}
					if(tempGroup.equals(group))
						break;
				}
			}
		}
		return groupId;
	}

	/**
	 * adds user in group list */
	private static void addUserInGroup(String group_id, ProfileData data){

		data.setStatus(UserStatus.JOIN_REQUEST.ordinal());
		data.setPublicProfile(false);
		databaseSourse.createUser(data, group_id);			//add user in database

	}

	/**
	 * adds user in list and updates markers to it */
	public static void addUserRequest(ProfileData data, String group_id){


		addUserInGroup(group_id, data);

		getDatabaseUsersJoinReq();

	}

	/**
	 * get username for other classes*/
	public static String getUsername(){
		return mUsername;
	}
	
	
	/**
	 * show toast on screen even from other classes. */
	public static void showToast(String msg){
		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.toast_layout, null);
		((TextView)view.findViewById(R.id.toast_text)).setText(msg);

		Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "gabriola.ttf");
		((TextView)view.findViewById(R.id.toast_text)).setTypeface(typeFace);

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.setMargin(0, 0.05f);
		toast.setView(view);
		toast.show();
		Log.i("Message", msg);
	}

	/**
	 * gets the group data from current downloaded data. */
	public static GroupData getGroupCurrentData(String id){
		return groupListView.getGroupItem(id);
	}

	/**
	 * A placeholder fragment containing a for all mainview intilizations.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			LeftDrawer = rootView.findViewById(R.id.left_drawer);
			filterView = (TextView)rootView.findViewById(R.id.left_drawer_filter);
			slidingLayout = rootView.findViewById(R.id.sliding_up_panal);
			slidingPanel = (SlidingUpPanelLayout)rootView.findViewById(R.id.left_drawer_sliding_up_panel);
			usersCounter = (TextView) rootView.findViewById(R.id.left_drawer_users_count);

			usersCounter.setVisibility(View.GONE);

			//setting nevigation drawer
			mDrawerLayout = (DrawerLayout)rootView.findViewById(R.id.drawer_layout);
			mActionBarDrawerToggle = 
					new ActionBarDrawerToggle(context, mDrawerLayout, R.drawable.ic_drawer_holo_light, 
							R.string.navigation_drawer_open_string, R.string.navigation_drawer_close_string);

			mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_holo_light, GravityCompat.START);

			ViewGroup groupList = (ViewGroup) rootView.findViewById(R.id.left_drawer_group_list);
			groupListView.setMainView(groupList);
			groupListView.notifyDataSetInvalidated();

			ImageButton btn = (ImageButton)rootView.findViewById(R.id.left_drawer_create_group_btn);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, CreateGroupActivity.class));
				}
			});
			/*((Button)rootView.findViewById(R.id.left_drawer_view_btn)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, MainListActivity.class));										
				}
			});*/
			((ViewGroup)rootView.findViewById(R.id.left_drawer_profile_btn)).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDrawerLayout.closeDrawer(LeftDrawer);
					startActivity(new Intent(context, ProfileActivity.class));
				}
			});

			final TextView profileStatus = (TextView)rootView.findViewById(R.id.left_drawer_profile_change_status_txt);
			ViewGroup profileBtn = (ViewGroup)rootView.findViewById(R.id.left_drawer_profile_change_status);
			profileStatus.setTextColor(	mProfileHandler.getProfileData().isPublicProfile() ? Color.GREEN : Color.RED);
			profileStatus.setText(mProfileHandler.getProfileData().isPublicProfile() ? "Public" : "Invisible");
			
			((TextView)rootView.findViewById(R.id.left_drawer_profile_txt)).setText(
					TextUtils.isEmpty(mProfileHandler.getProfileData().getDisplayName()) ? 
							mUsername : mProfileHandler.getProfileData().getDisplayName());
			
			profileBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mProfileHandler.getProfileData().isPublicProfile()){
						new ManagePreferences(context).putBoolPreferences(ProfileData.PROFILE_PUBLIC, false);
						mProfileHandler.getProfileData().setPublicProfile(false);
						hideUserProfile();
					}
					else{
						new ManagePreferences(context).putBoolPreferences(ProfileData.PROFILE_PUBLIC, true);
						mProfileHandler.getProfileData().setPublicProfile(true);
						mProfileHandler.uploadProfile(serverHandler);
					}					

					profileStatus.setTextColor(	mProfileHandler.getProfileData().isPublicProfile() ? Color.GREEN : Color.RED);
					profileStatus.setText(mProfileHandler.getProfileData().isPublicProfile() ? "Public" : "Invisible");
				}
			});
			((ImageButton)rootView.findViewById(R.id.left_drawer_filter_btn)).setOnClickListener(mainInstance);
			((TextView)rootView.findViewById(R.id.left_drawer_logout)).setOnClickListener(mainInstance);
			((LinearLayout)rootView.findViewById(R.id.left_drawer_users_layout)).setOnClickListener(mainInstance);
			//((TextView)rootView.findViewById(R.id.left_drawer_stats)).setOnClickListener(mainInstance);
			
			slidingPanel.collapsePanel();
			slidingPanel.setPanelHeight(0);
			//slidingPanel.hidePanel();
			slidingPanel.setEnableDragViewTouchEvents(true);
			//slidingPanel.setSlidingEnabled(false);
			slidingLayout.setVisibility(View.GONE);
			slidingPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

				@Override
				public void onPanelSlide(View panel, float slideOffset) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPanelExpanded(View panel) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPanelCollapsed(View panel) {
					// TODO Auto-generated method stub
					slidingLayout.setVisibility(View.GONE);
					slidingPanel.setPanelHeight(0);
					slidingPanel.hidePanel();
					//slidingPanel.setSlidingEnabled(false);
				}

				@Override
				public void onPanelAnchored(View panel) {
					// TODO Auto-generated method stub

				}
			});


			/* MapFragment fragment =  (MapFragment)MapFragment.instantiate(context, "com.google.android.gms.maps.MapFragment");
            map = fragment.getMap();

            map.setMyLocationEnabled(true);*/

			return rootView;
		}
	}


	/**
	 * some of on click listeners. */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch(id){

		case R.id.left_drawer_filter_btn:
		{
			mDrawerLayout.closeDrawer(LeftDrawer);
			String getString = filterView.getText().toString();
			if(!TextUtils.isEmpty(getString)){


				Log.d("String", getString);
				try {
					ArrayList<JSONObject> sendObject = new ArrayList<JSONObject>();
					JSONObject data = new JSONObject(allGroupsString);
					JSONArray events = data.getJSONArray("events");

					if(events != null){
						LinkedHashMap<String, JSONObject> startedGroups = new LinkedHashMap<String, JSONObject>();
						LinkedHashMap<String, JSONObject> updatedGroups = new LinkedHashMap<String, JSONObject>();
						ArrayList<String> endedGroups = new ArrayList<String>();
						for(int i=0; i< events.length(); i++){
							JSONObject eve = events.optJSONObject(i);
							String groupId = eve.optString("session");

							JSONArray entities = eve.optJSONArray("entities");
							if(entities != null){
								String course = "";
								double lat = 0, lng = 0;
								for(int j=0; j<entities.length(); j++){
									JSONObject entity = entities.optJSONObject(j);
									if(entity != null){
										if("lat".equalsIgnoreCase(entity.optString("key")))
											lat = entity.optDouble("value");
										if("lng".equalsIgnoreCase(entity.optString("key")))
											lng = entity.optDouble("value");
										if("group_course".equalsIgnoreCase(entity.optString("key")))
											course = entity.optString("value");
									}
								}

								if(isWithinDistance(lat, lng) && (Pattern.compile(Pattern.quote(course), Pattern.CASE_INSENSITIVE).matcher(getString).find() || 
										course.toLowerCase().contains(getString.toLowerCase()))){
									if(eve.optString("action").equalsIgnoreCase("START"))
										startedGroups.put(groupId, eve);
									else if(eve.optString("action").equalsIgnoreCase("UPDATE"))
										updatedGroups.put(groupId, eve);
									else
										endedGroups.add(groupId);
								}

							}					
						}

						if(!endedGroups.isEmpty()){
							for(int i=0; i<endedGroups.size(); i++){
								startedGroups.remove(endedGroups.get(i));
							}
						}
						if(!startedGroups.isEmpty())
							for(String key : startedGroups.keySet()){
								if(updatedGroups.containsKey(key))
									sendObject.add(updatedGroups.get(key));
								else
									sendObject.add(startedGroups.get(key));
							}
					}

					setFilteredGroups(sendObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				new SetMarkers().execute(allGroupsString);
			}
			break;
		}

		case R.id.left_drawer_logout:
		{
			AlertDialog dialog = new AlertDialog.Builder(context)
									.setTitle("Logout")
									.setMessage("Do you want to logout this account.")
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub
											ManagePreferences prefs = new ManagePreferences(context);
											prefs.removePreferences();
											prefs.removePreferences(
													getString(R.string.password_pref), 
													getString(R.string.username_pref),
													ProfileData.PROFILE_PUBLIC, 
													Constants.PROPERTY_REG_ID, 
													Constants.PROPERTY_PROFILE_SESSION, 
													Constants.PROPERTY_REG_ID_DATE);
											context.deleteDatabase(SQLiteHelper.DATABASE_NAME);

											startActivity(new Intent(context, LoginActivity.class));
											finish();
										}
									})
									.setNegativeButton("No", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									})
									.setCancelable(true)
									.create();
			dialog.show();
			break;
		}

		case R.id.left_drawer_users_layout:
		{
			mDrawerLayout.closeDrawer(LeftDrawer);
			startActivity(new Intent(context, UserActivity.class));
			break;
		}
		
		/*case R.id.left_drawer_stats:
		{
			mDrawerLayout.closeDrawer(LeftDrawer);
			startActivity(new Intent(context, StatsActivity.class));
			break;
		}
*/
		}
	}

	/**
	 * get results from server like groups, profiles etc. */
	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub

		if(getRequest != null)
			switch(getRequest){

			case DELETE_GROUP_REQUEST:
			{
				try {
					JSONObject data = new JSONObject(result);
					JSONArray events = data.getJSONArray("events");
					for(int i=0; i<events.length(); i++){
						JSONObject event = events.optJSONObject(i);
						if(event != null)
							serverHandler.post("events/delete", new JSONObject().put("id", event.optString("id")).toString());
					}
				} catch (JSONException e) {}
				break;
			}

			case GROUP_REQUEST:
			{
				allGroupsString = result;
				new SetMarkers().execute(result);
				break;
			}

			/*case USER_PROFILE_REQUEST:
		{
			try {
				JSONObject data = new JSONObject(result);

				ProfileData tempData = new ProfileData();
				JSONArray events = data.optJSONArray("events");
				if(events != null){
					for(int i=0; i<events.length(); i++){
						JSONObject event = events.optJSONObject(i);
						JSONArray entities = event.optJSONArray("entities");
						for(int j=0; j<entities.length(); j++){
							JSONObject obj = entities.optJSONObject(j);
							if(ProfileData.PROFILE_USERNAME.equalsIgnoreCase(obj.optString( "key" )))
								tempData.setUsername(obj.optString("value"));
							if(ProfileData.PROFILE_MSG_ID.equalsIgnoreCase(obj.optString( "key" )))
								tempData.setMsg_id(obj.optString("value"));
							if(ProfileData.PROFILE_DISPLAY_NAME.equalsIgnoreCase(obj.optString( "key" )))
								tempData.setDisplayName(obj.optString("value"));
							if(ProfileData.PROFILE_EMAIL.equalsIgnoreCase(obj.optString( "key" )))
								tempData.setEmail(obj.optString("value"));
							if(ProfileData.PROFILE_DESC.equalsIgnoreCase(obj.optString( "key" )))
								tempData.setDesc(obj.optString("value"));
							try {
								if(ProfileData.PROFILE_PUBLIC.equalsIgnoreCase(obj.optString( "key" )))
									tempData.setPublicProfile(Boolean.parseBoolean(obj.getString("value")));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								tempData.setPublicProfile(false);
							}						
						}
						tempData.setStatus(UserStatus.JOIN_REQUEST.ordinal());
						databaseSourse.createUser(tempData);
					}
				}
			} catch (JSONException e) {}
			break;
		}*/

			}

		else{
			allGroupsString = result;
			new SetMarkers().execute(result);
		}			

		getRequest = null;
	}


	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		Log.d("POST Data", result);
	}


	/**
	 * Marker class to read downloaded string and displays the markers on map */
	private class SetMarkers extends AsyncTask<String, JSONObject, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if(map != null){
				map.clear();
				allMarkers.clear();
			}
		}



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

			//read all groups and 
			if(events != null){
				LinkedHashMap<String, JSONObject> statedGroups = new LinkedHashMap<String, JSONObject>();
				LinkedHashMap<String, JSONObject> updatedGroups = new LinkedHashMap<String, JSONObject>();
				ArrayList<String> endedGroups = new ArrayList<String>();
				for(int i=0; i< events.length(); i++){
					JSONObject eve = events.optJSONObject(i);
					String groupId = eve.optString("session");

					JSONArray entities = eve.optJSONArray("entities");
					if(entities != null){
						double lat = 0, lng = 0;
						for(int j=0; j<entities.length(); j++){
							JSONObject entity = entities.optJSONObject(j);
							if(entity != null){
								if("lat".equalsIgnoreCase(entity.optString("key")))
									lat = entity.optDouble("value");
								if("lng".equalsIgnoreCase(entity.optString("key")))
									lng = entity.optDouble("value");
							}
						}

						if(isWithinDistance(lat, lng)){
							if(eve.optString("action").equalsIgnoreCase("START"))
								statedGroups.put(groupId, eve);
							else if(eve.optString("action").equalsIgnoreCase("UPDATE"))
								updatedGroups.put(groupId, eve);
							else
								endedGroups.add(groupId);
						}

					}					
				}

				if(!endedGroups.isEmpty()){
					for(int i=0; i<endedGroups.size(); i++){
						statedGroups.remove(endedGroups.get(i));
					}
				}
				if(!statedGroups.isEmpty())
					for(String key : statedGroups.keySet()){
						if(updatedGroups.containsKey(key))
							publishProgress(updatedGroups.get(key));
						else
							publishProgress(statedGroups.get(key));
					}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(JSONObject... values) {
			// TODO Auto-generated method stub
			GroupData data = new GroupData();
			JSONArray entities = values[0].optJSONArray("entities");
			if(entities != null){
				data.setGroupSession(values[0].optString("session"));
				data.setStatus(values[0].optString("action"));
				try {
					data.setTimestamp(Long.valueOf(values[0].optString("timestamp")) * 1000);
				} catch (NumberFormatException e1) {}
				for(int i=0; i<entities.length(); i++){
					JSONObject entitiy = entities.optJSONObject(i);
					if(entitiy != null){
						if("group_activity".equalsIgnoreCase(entitiy.optString("key")))
							data.setName(entitiy.optString("value"));
						else if("group_course".equalsIgnoreCase(entitiy.optString("key")))
							data.setCourse(entitiy.optString("value"));
						else if("group_address".equalsIgnoreCase(entitiy.optString("key")))
							data.setAddress(entitiy.optString("value"));
						else if("group_desc".equalsIgnoreCase(entitiy.optString("key")))
							data.setDescription(entitiy.optString("value"));
						else if("group_admin".equalsIgnoreCase(entitiy.optString("key")))
							data.setAdmin(entitiy.optString("value"));
						else if("group_id".equalsIgnoreCase(entitiy.optString("key")))
							data.setGroupId(entitiy.optString("value"));
						else if("joined_users".equalsIgnoreCase(entitiy.optString("key"))){
							String listString = entitiy.optString("value");
							listString = listString.replace("[", "");
							listString = listString.replace("]", "");
							if(!TextUtils.isEmpty(listString)){
								ArrayList<String> val = new ArrayList<String>(Arrays.asList(listString.substring(0, listString.length()-1).split(", \\s*")));
								data.setUsers_joined(val);
							}
						}
						else if("lat".equalsIgnoreCase(entitiy.optString("key")))
							try {
								data.setLat(Double.parseDouble(String.valueOf(entitiy.opt("value"))));
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								data.setLat(0);
							}
						else if("lng".equalsIgnoreCase(entitiy.optString("key")))
							try {
								data.setLng(Double.parseDouble(String.valueOf(entitiy.opt("value"))));
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								data.setLng(0);
							}
						else if("group_img".equalsIgnoreCase(entitiy.optString("key"))){
							String img = entitiy.optString("value");
							if(!TextUtils.isEmpty(img) && !img.equalsIgnoreCase("null")){
								byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
								Bitmap imageBitmap = BitmapFactory.decodeByteArray(
										decodedString, 0, decodedString.length);
								data.setImage(imageBitmap);
							}
						}
					}
				}

				Log.d("Inserted Group", data.getName() + " - " + data.getStatus());

				LatLng position = new LatLng(data.getLat(), data.getLng());
				MarkerOptions marker = new MarkerOptions()
				.title(data.getName())
				.snippet(data.getCourse())
				.position(position);

				Marker mark = map.addMarker(marker);
				allMarkers.put(mark, data);

			}
		}


		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mainInstance.setProgressBarIndeterminateVisibility(false);


			if(allMarkers.isEmpty())
				return;
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (Marker marker : allMarkers.keySet()) {
				builder.include(marker.getPosition());
			}
			LatLngBounds bounds = builder.build();
			int padding = 50; // offset from edges of the map in pixels
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

			map.moveCamera(cu);
			map.animateCamera(cu);


			map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

				@Override
				public void onInfoWindowClick(Marker marker) {
					// TODO Auto-generated method stub
					final GroupData data = allMarkers.get(marker);
					

					final ViewGroup msgLayout = (ViewGroup) slidingLayout.findViewById(R.id.main_view_message_layout);
					boolean isGroupJoined = databaseSourse.isGroupMsgExists(data.getGroupId());

					Bitmap image = data.getImage();
					if(data.getImage() == null){
						image = BitmapFactory.decodeResource(getResources(), R.drawable.group_img);
					}
					
					
					
					((ImageView)slidingLayout.findViewById(R.id.main_view_group_img)).setImageBitmap(
							StaticUtilMethods.getRoundedShape(image));

					ViewGroup join = (ViewGroup) slidingLayout.findViewById(R.id.main_view_join_btn);
					if(!isGroupJoined && !regId.equalsIgnoreCase(data.getAdmin())){
							join.setVisibility(View.VISIBLE);
							join.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if(!regId.equalsIgnoreCase(data.getAdmin())){
										showToast("Join request send.");
										sendMessage(new String( 
												mUsername + Constants.VALUE_SEPRATOR +
												data.getGroupId() + Constants.VALUE_SEPRATOR +
												regId),
												new String[]{data.getAdmin()},
												MessagesTypes.GROUP_JOIN_REQUEST);
									}
								}
							});
						
					}
					else{
						new SetGroupMessages((ViewGroup) slidingLayout.findViewById(R.id.main_view_messages_list)).execute(data.getGroupId());
						join.setVisibility(View.GONE);
					}

					if(isGroupJoined){
						Button sendbutton = (Button)slidingLayout.findViewById(R.id.main_view_message_send_btn);
						sendbutton.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								// send messages
								TextView msgView = (TextView) msgLayout.findViewById(R.id.main_view_message_text);
								String msg = msgView.getText().toString();
								if(!TextUtils.isEmpty(msg)){
									ArrayList<String> list = data.getUsers_joined();
									ArrayList<String> array = new ArrayList<String>();
									for(int i=0; i<list.size(); i++){
										String []vals = list.get(i).split(Constants.VALUE_SEPRATOR);
										if(vals != null && !vals[0].equalsIgnoreCase(mUsername))
											array.add(vals[1]);
									}
									if(!data.getAdmin().equalsIgnoreCase(regId))
										array.add(data.getAdmin());
									
									sendMessage(data.getGroupId() + Constants.KEY_SEPRATOR + mUsername + Constants.VALUE_SEPRATOR + msg, array.toArray(new String[]{}), MessagesTypes.RECEIVE_MESSAGE);
									databaseSourse.addNewGroupMessages(data.getGroupId(), mUsername + Constants.VALUE_SEPRATOR + msg);
									new SetGroupMessages((ViewGroup) slidingLayout.findViewById(R.id.main_view_messages_list)).execute(data.getGroupId());
									msgView.setText("");
									showToast("Message send...");
								}
							}
						});
						msgLayout.setVisibility(View.VISIBLE);
						((ViewGroup) slidingLayout.findViewById(R.id.main_view_group_users)).setVisibility(View.VISIBLE);
					}
					else{
						((ViewGroup) slidingLayout.findViewById(R.id.main_view_group_users)).setVisibility(View.GONE);
						msgLayout.setVisibility(View.GONE);
					}
					
					ArrayAdapter<String> users = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1){
						@Override
						public void notifyDataSetInvalidated() {
							// TODO Auto-generated method stub
							ViewGroup viewgroup = (ViewGroup) slidingLayout.findViewById(R.id.main_view_group_users);
							viewgroup.removeAllViews();
							for(int i=0; i<getCount(); i++)
								viewgroup.addView(getView(i, null, null));
							viewgroup.invalidate();
						}
					};
					ArrayList<String> jusers = data.getUsers_joined();
					for(int u=0; u<jusers.size(); u++){
						String []vals = jusers.get(u).split(Constants.VALUE_SEPRATOR);
						if(vals != null && vals.length > 0)
							users.add(vals[0]);
					}
					//users.notifyDataSetInvalidated();

					try {
						((TextView)slidingLayout.findViewById(R.id.main_view_group_address)).setText(new String(data.getAddress().getBytes("ISO-8859-1"), "UTF-8"));
					} catch (UnsupportedEncodingException e) {}
					((TextView)slidingLayout.findViewById(R.id.main_view_group_name)).setText(data.getName());
					((TextView)slidingLayout.findViewById(R.id.main_view_group_course)).setText(data.getCourse());
					((TextView)slidingLayout.findViewById(R.id.main_view_group_date)).setText( StaticUtilMethods.getDate( data.getTimestamp() ) );
					((TextView)slidingLayout.findViewById(R.id.main_view_group_desc)).setText( data.getDescription() );
					
					
					slidingLayout.setVisibility(View.VISIBLE);
					
					slidingPanel.setAnchorPoint(0.7f);
					slidingPanel.expandPanel(0.7f);
				}
			});
			//map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));
		}

		public void updateMarkerData(GroupData data){
			for(Marker marker : allMarkers.keySet()){
				GroupData group = allMarkers.get(marker);
				if(group.equals(data)){
					allMarkers.put(marker, data);
					return;
				}
			}
		}

	}

	private class SetGroupMessages extends AsyncTask<String, String, Void>{
		private MessageAdapter adapter;
		private ViewGroup mainView;
		private String msg = "";
		
		public SetGroupMessages(ViewGroup mainView) {
			// TODO Auto-generated constructor stub
			this.mainView = mainView; 
			this.adapter = new MessageAdapter(context, android.R.layout.simple_list_item_1, mainView); 
		}
		
		
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			msg = params[0];
			String messages = databaseSourse.getGroupMsg(msg);
			if(TextUtils.isEmpty(messages))
				return null;
			
			String []msgs = messages.split(Constants.KEY_SEPRATOR);
			for(int i=0; i<msgs.length; i++){
				String []vals = msgs[i].split(Constants.VALUE_SEPRATOR);
				if(vals != null && vals.length > 1){
					String msg = vals[0] + ": " + vals[1];
					publishProgress(msg);
				}
			}
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			adapter.add(values[0]);			
			adapter.notifyDataSetInvalidated();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(slidingPanel.isPanelAnchored() || slidingPanel.isPanelExpanded()){
				this.cancel(true);
				new SetGroupMessages(mainView).execute(msg);
			}
			else
				return;
		}
		
	}

	private class MessageAdapter extends ArrayAdapter<String>{

		private ViewGroup mainView;
		
		public MessageAdapter(Context context, int resource, ViewGroup mainView) {
			super(context, resource);
			// TODO Auto-generated constructor stub
			this.mainView = mainView; 
		}

		@Override
		public void notifyDataSetInvalidated() {
			// TODO Auto-generated method stub
			mainView.removeAllViews();
			for(int i=0; i<getCount(); i++){
				mainView.addView(getView(i, null, null));
			}
			mainView.invalidate();
		}
	}
	
	private enum GetRequest{
		GROUP_REQUEST, DELETE_GROUP_REQUEST;
	}

}
