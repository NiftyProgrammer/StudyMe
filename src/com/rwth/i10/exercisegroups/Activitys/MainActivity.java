package com.rwth.i10.exercisegroups.Activitys;

import java.io.IOException;
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import com.rwth.i10.exercisegroups.database.GroupsDataSource;
import com.rwth.i10.exercisegroups.database.SQLiteHelper;
import com.rwth.i10.exercisegroups.gcm_config.GcmServer;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

public class MainActivity extends ActionBarActivity implements MyContextData.Listener,
LocationListener, View.OnClickListener{


	public static MainListViewAdapter groupListView;
	public static FragmentManager mFragmentManager;
	public static GroupsDataSource databaseSourse;
	public static boolean closeApplication;
	public static MyContextData serverHandler = null;
	/*public static ContextData userHandler = null;*/
	public static Location mLocation;
	public static ProfileHandler mProfileHandler;
	public static String regId;


	private static Activity context;
	private static MainActivity mainInstance;

	private static GoogleMap map;	
	private static SupportMapFragment mapFragment;
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private static String allGroupsString;


	private static GetRequest getRequest;
	private static ActionBarDrawerToggle mActionBarDrawerToggle;
	private static DrawerLayout mDrawerLayout;
	private static View LeftDrawer;
	private static TextView filterView;
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
			setProgressBarIndeterminateVisibility(true);
			fetschGroups();
		}
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
		if(location.distanceTo(mLocation) > 50){
			//stop group if running
		}
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

	private void storeRegistrationId(Context context, String regId) {
		ManagePreferences pref = new ManagePreferences(context);
		int appVersion = getAppVersion(context);
		pref.putStringPreferences(Constants.PROPERTY_REG_ID, regId);
		pref.putIntPreferences(Constants.PROPERTY_APP_VERSION, appVersion);
	}

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
						data.setGroup_id(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ID)));
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


	private static void sendMessageToServer(Map<String, String> params, List<String> ids, MessagesTypes type){
		GcmServer server = new GcmServer();
		server.sendMessage(params, ids, type, context);
	}

	public static void deleteGroupFromServer(String id){
		getRequest = GetRequest.DELETE_GROUP_REQUEST;

		JSONObject data = new JSONObject();

		try {
			data.put("model", "COMPLETE");
			data.put("category", "ACTIVITY");
			data.put("source", "MOBILE");
			data.put("type", "ANNOUNCEMENT");

			JSONObject entity1 = new JSONObject();
			entity1.put("key", "group_id");
			entity1.put("value", id);
			JSONArray entities = new JSONArray();
			entities.put(entity1);

			data.put("entities", entities);			
		} catch (Exception e) {}


		serverHandler.get("events/show", data.toString());

	}

	public static void fetschGroups(){
		getRequest = GetRequest.GROUP_REQUEST;
		serverHandler.get("events/show", StaticUtilMethods.createFetchServerJSON());
	}
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


	private void setFilteredGroups(ArrayList<JSONObject> array){
		SetMarkers marks = new SetMarkers();
		if(array == null || array.isEmpty()){
			Toast.makeText(context, "No group found", Toast.LENGTH_SHORT).show();
			return;
		}
		for(int i=0; i<array.size(); i++)
			marks.onProgressUpdate(array.get(i));
		marks.onPostExecute(null);
	}


	public static void addUserRequest(ProfileData data){
		JSONObject event = new JSONObject();
		try {
			event.put("model", "COMPLETE");
			event.put("category", "ACTIVITY");
			event.put("source", "MOBILE");
			event.put("type", "PROFILE");

			JSONObject entity1 = new JSONObject();
			JSONObject entity2 = new JSONObject();
			JSONObject entity3 = new JSONObject();

			entity1.put("key", "app");
			entity1.put("value", "study_me");

			entity2.put("key", "activity");
			entity2.put("value", "profile_data");

			entity3.put("key", ProfileData.PROFILE_USERNAME);
			entity3.put("value", data.getUsername());

			JSONArray array = new JSONArray();
			array.put(entity1);
			array.put(entity2);
			array.put(entity3);

			event.put("entities", array);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getRequest = GetRequest.USER_PROFILE_REQUEST;
		serverHandler.get("event/show", event.toString());

	}

	public static void addGroupMessage(GroupData data, String msg){
		
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
			filterView = (TextView)rootView.findViewById(R.id.left_drawer_filter);
			slidingLayout = rootView.findViewById(R.id.sliding_up_panal);
			slidingPanel = (SlidingUpPanelLayout)rootView.findViewById(R.id.left_drawer_sliding_up_panel);


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
			Button profileBtn = (Button)rootView.findViewById(R.id.left_drawer_profile_change_status);
			profileBtn.setBackgroundColor(mProfileHandler.getProfileData().isPublicProfile() ? Color.GREEN : Color.RED);
			profileBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mProfileHandler.getProfileData().isPublicProfile()){
						new ManagePreferences(context).putBoolPreferences(ProfileData.PROFILE_PUBLIC, false);
						mProfileHandler.getProfileData().setPublicProfile(false);
						hideUserProfile();
						((Button)arg0).setBackgroundColor(Color.RED);
					}
					else{
						new ManagePreferences(context).putBoolPreferences(ProfileData.PROFILE_PUBLIC, true);
						mProfileHandler.getProfileData().setPublicProfile(true);
						mProfileHandler.uploadProfile(serverHandler);
						((Button)arg0).setBackgroundColor(Color.GREEN);
					}					
				}
			});
			((Button)rootView.findViewById(R.id.left_drawer_filter_btn)).setOnClickListener(mainInstance);

			//slidingPanel.collapsePanel();
			slidingPanel.setPanelHeight(0);
			slidingPanel.hidePanel();
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
					if(allMarkers.isEmpty())
						return;
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Marker marker : allMarkers.keySet()) {
						builder.include(marker.getPosition());
					}
					LatLngBounds bounds = builder.build();
					int padding = 0; // offset from edges of the map in pixels
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

					map.moveCamera(cu);
					map.animateCamera(cu);
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


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch(id){

		case R.id.left_drawer_filter_btn:
			mDrawerLayout.closeDrawer(LeftDrawer);
			String getString = filterView.getText().toString();
			if(!TextUtils.isEmpty(getString)){
				Log.d("String", getString);
				try {
					ArrayList<JSONObject> sendObject = new ArrayList<JSONObject>();
					JSONObject data = new JSONObject(allGroupsString);
					JSONArray events = data.getJSONArray("events");
					for(int j=0; j<events.length(); j++){
						JSONObject event = events.optJSONObject(j);
						JSONArray entities = event.optJSONArray("entities");
						if(entities != null){
							String course = "";
							for(int i=0; i<entities.length(); i++){
								JSONObject entitiy = entities.optJSONObject(i);
								if(entitiy != null){
									if("group_course".equalsIgnoreCase(entitiy.optString("key"))){
										course = entitiy.optString("value");
										break;
									}
								}
							}
							Log.d("course", course);
							if(Pattern.compile(Pattern.quote(course), Pattern.CASE_INSENSITIVE).matcher(getString).find()){
								sendObject.add(event);
							}
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
	}

	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub

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

		case USER_PROFILE_REQUEST:
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
						databaseSourse.createUser(tempData);
					}
				}
			} catch (JSONException e) {}
			break;
		}

		}


		getRequest = null;
	}


	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		Log.d("POST Data", result);
	}


	private class SetMarkers extends AsyncTask<String, JSONObject, Void>{

		public SetMarkers() {
			// TODO Auto-generated constructor stub
			if(map != null){
				map.clear();
				allMarkers.clear();
			}
			setProgressBarIndeterminateVisibility(true);
		}

		private boolean isWithinDistance(double groupLat, double groupLng){
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
								groups.put(groupId, eve);
							else
								endedGroups.add(groupId);
						}

					}					
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
			GroupData data = new GroupData();
			JSONArray entities = values[0].optJSONArray("entities");
			if(entities != null){
				data.setGroup_id(values[0].optString("session"));
				data.setStatus(values[0].optString("action"));
				data.setTimestamp(values[0].optLong("timestamp"));
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
			setProgressBarIndeterminateVisibility(false);

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
					((TextView)slidingLayout.findViewById(R.id.main_view_group_name)).setText(data.getName());
					((TextView)slidingLayout.findViewById(R.id.main_view_group_address)).setText(Html.fromHtml(data.getAddress()));
					((TextView)slidingLayout.findViewById(R.id.main_view_group_course)).setText(data.getCourse());
					((TextView)slidingLayout.findViewById(R.id.main_view_group_date)).setText( StaticUtilMethods.getDate( data.getTimestamp() ) );
					((TextView)slidingLayout.findViewById(R.id.main_view_group_desc)).setText( data.getDescription() );

					Bitmap image = data.getImage();
					if(data.getImage() == null){
						image = BitmapFactory.decodeResource(getResources(), R.drawable.group_img);
					}
					((ImageView)slidingLayout.findViewById(R.id.main_view_group_img)).setImageBitmap(
							StaticUtilMethods.getRoundedShape(image));

					Button join = (Button) slidingLayout.findViewById(R.id.main_view_join_btn);
					if(regId.equalsIgnoreCase(data.getAdmin()))
						join.setVisibility(View.GONE);
					else{
						join.setVisibility(View.VISIBLE);
						join.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								if(!regId.equalsIgnoreCase(data.getAdmin()))
									sendMessage(new String("username" + Constants.VALUE_SEPRATOR 
											+ mUsername + Constants.KEY_SEPRATOR +
											"msgId" + Constants.VALUE_SEPRATOR +
											regId),
											new String[]{data.getAdmin()},
											MessagesTypes.GROUP_JOIN_REQUEST);
							}
						});
					}
					
					((Button)slidingLayout.findViewById(R.id.main_view_message_send_btn)).setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							// send messages
						}
					});

					slidingLayout.setVisibility(View.VISIBLE);
					//slidingPanel.setSlidingEnabled(true);
					if(slidingPanel.isPanelAnchored() || slidingPanel.isPanelExpanded() || 
							!slidingPanel.isPanelHidden())
						slidingPanel.showPanel();

					slidingPanel.setAnchorPoint(0.7f);
					slidingPanel.expandPanel(0.7f);
				}
			});
			//map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));
		}

	}

	private enum GetRequest{
		GROUP_REQUEST, USER_PROFILE_REQUEST, DELETE_GROUP_REQUEST;
	}

}
