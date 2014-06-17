package com.rwth.i10.exercisegroups.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.en;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;
import de.contextdata.Entity;
import de.contextdata.Event;
import de.contextdata.RandomString;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class ProfileHandler implements MyContextData.Listener{

	private MyContextData contextData;
	private Context context;
	
	private ProfileData profileData;
	private String error;
	
	private boolean deleteAfterwords;
	private boolean uploadProfile;
	private boolean processFinished;
	private boolean updatedId;
	
	private int totalPosts;
	
	public ProfileHandler(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		//fetschProfileData();
		deleteAfterwords = uploadProfile = processFinished = false;
		updatedId = true;
		error = null;
		totalPosts = 0;
	}
	public ProfileHandler(Context context, MyContextData contextData){
		this(context);
		this.contextData = contextData;
		this.contextData.registerGETListener(this);
		this.contextData.registerPOSTListener(this);
	}
	public ProfileHandler(Context context, String username, String password){
		this(context);
		this.contextData = ServerHandler.createInstance(username, password);
		this.contextData.registerGETListener(this);
		this.contextData.registerPOSTListener(this);
		this.contextData.setTimeout(60000 * 2);
	}
		
	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub
		Log.d("Get Result", result);
		try {
			JSONObject data = new JSONObject(result);
			
			if(data.optInt("result") == 0 || data.optInt("total_events") <= 0){
				uploadProfile(profileData);
				return;
			}
			ProfileData tempData = new ProfileData();
			JSONArray events = data.optJSONArray("events");
			if(events != null){
				for(int i=0; i<events.length(); i++){
					JSONObject event = events.optJSONObject(i);
					JSONArray entities = event.optJSONArray("entities");
					String app = "", activity = "", disName = "", msgId = "", uname = "";
					String email = "", description = "";
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
						if(ProfileData.PROFILE_DISPLAY_NAME.equalsIgnoreCase(obj.optString( "key" )))
							disName = obj.optString("value");
						if(ProfileData.PROFILE_EMAIL.equalsIgnoreCase(obj.optString( "key" )))
							email = obj.optString("value");
						if(ProfileData.PROFILE_DESC.equalsIgnoreCase(obj.optString( "key" )))
							description = obj.optString("value");
					}
					if( "study_me".equalsIgnoreCase(app) &&
							"profile_data".equalsIgnoreCase(activity) ){
						tempData.setDisplayName( disName );
						tempData.setMsg_id( msgId );
						profileData.setSession( event.optString( ProfileData.PROFILE_SESSION ) );
						tempData.setUsername( uname );
						profileData.setEvent_id( event.optInt( ProfileData.PROFILE_ID ) );
						tempData.setEmail( email );
						tempData.setDesc( description );
						
						setProfileData(tempData);
					}
				}
				
				if(profileData.getEvent_id() > 0){
					if(deleteAfterwords){
						deleteAfterwords = false;
						deleteProfile(profileData.getEvent_id());
					}
					updatedId = false;
					//setProfileData(tempData);
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = e.getMessage();
		}
	}
	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		Log.d("Post Result", result);
		try {
			JSONObject obj = new JSONObject(result);
			if("1".equalsIgnoreCase(obj.optString("result")) && ++totalPosts > 1 /*&& uploadProfile*/){
				/*uploadProfile = false;
				uploadProfile(profileData);*/
				processFinished = true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = e.getMessage();
			processFinished = true;
		}
	}
	
	public boolean getProcessFinished(){
		return processFinished;
	}
	public String getError(){
		return error;
	}
	public void changeProcessFinished(){
		processFinished = false;
		error = null;
	}
	public void setUpdatedId(){
		updatedId = false;
	}
	
	public void setContexData(String username, String password){
		this.contextData = ServerHandler.createInstance(username, password);
		this.contextData.registerGETListener(this);
		this.contextData.registerPOSTListener(this);
	}
		
	public void deleteProfile(MyContextData contextData, int id){
		//uploadProfile = true;
		try {
			String value = new JSONObject().put("id", id).toString();
			Log.d("Delete profile post value", value);
			contextData.post("events/delete", value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
	
			e.printStackTrace();
		}
	}
	public void deleteProfile(int id){
		deleteProfile(this.contextData, id);
	}
	public void updateProfile(){
		if(updatedId || profileData.getEvent_id() < 1){
			deleteAfterwords = true;
			getPreviousProfile();
			uploadProfile(profileData);
		}
		else{
			deleteAfterwords = false;
			deleteProfile(profileData.getEvent_id());
			uploadProfile(profileData);
		}
			
	}
	public void updateProfile(MyContextData contextData){
		this.contextData = contextData;
		this.contextData.registerGETListener(this);
		this.contextData.registerPOSTListener(this);
		updateProfile();
	}
	public void deleteProfile(String mUsername, String mPassword){
		setContexData(mUsername, mPassword);
		updateProfile();
	}
	
	public void setProfileData(ProfileData data){
		if(TextUtils.isEmpty(profileData.getDisplayName()))
			profileData.setDisplayName(data.getDisplayName());
		if(TextUtils.isEmpty(profileData.getMsg_id()))
			profileData.setMsg_id(data.getMsg_id());
		if(TextUtils.isEmpty(profileData.getSession()))
			profileData.setSession(data.getSession());
		if(TextUtils.isEmpty(profileData.getUsername()))
			profileData.setUsername(data.getUsername());
		if(TextUtils.isEmpty(profileData.getDesc()))
			profileData.setDesc(data.getDesc());
		if(TextUtils.isEmpty(profileData.getEmail()))
			profileData.setEmail(data.getEmail());
	}
	
	public void setProfileDataId(int id){
		profileData.setEvent_id(id);
	}
	
	
	public void uploadProfile(MyContextData contextData, ProfileData pData){
		
		Event event = new Event("UPDATE", "RELEVANCE", (int)System.currentTimeMillis());
		event.addEntity(new Entity<String>("app", "study_me"));
		event.addEntity(new Entity<String>("activity", "profile_data"));
		event.addEntity( new Entity<String>( ProfileData.PROFILE_USERNAME, pData.getUsername() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_DISPLAY_NAME, pData.getDisplayName() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_EMAIL, pData.getEmail() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_DESC, pData.getDesc() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_MSG_ID, pData.getMsg_id() ) );
		
		event.setSession(pData.getSession());
		String value = StaticUtilMethods.eventToString(event);
		Log.d("Upload Profile post Value", value);
		contextData.post("events/update", value);
	}
	public void uploadProfile(ProfileData pData){
		uploadProfile(contextData, pData);
	}
	
	public void getPreviousProfile(){
		String value = createFetschProfileString();
		Log.d("Get Previous Value", value);
		contextData.get("events/show", value);
	}
	
	public void fetschProfileData(){
		ManagePreferences pref = new ManagePreferences(context);
		profileData = new ProfileData();
		
		profileData.setSession(pref.getPreference(Constants.PROPERTY_PROFILE_SESSION, RandomString.randomString(20)));
		profileData.setMsg_id(pref.getStringPreferences(Constants.PROPERTY_REG_ID, ""));
		profileData.setUsername(pref.getPreference(context.getString(R.string.username_pref), ""));
		profileData.setDisplayName(pref.getPreference(ProfileData.PROFILE_DISPLAY_NAME, ""));
		profileData.setEmail(pref.getPreference(ProfileData.PROFILE_EMAIL, ""));
		profileData.setDesc(pref.getPreference(ProfileData.PROFILE_DESC, ""));
	}
	
	public ProfileData getProfileData(){
		return this.profileData;
	}
	public ProfileData setMessageId(String msg_id){
		this.profileData.setMsg_id(msg_id);
		return this.profileData;
	}
	
	private String createFetschProfileString(){
		JSONObject event = new JSONObject();
		try {
			event.put("model", "COMPLETE");
			event.put("category", "ACTIVITY");
			event.put("source", "MOBILE");
			event.put("type", "RELEVANCE");
			
			JSONObject entity1 = new JSONObject();
			JSONObject entity2 = new JSONObject();
			entity1.put("key", "app");
			entity1.put("value", "study_me");
			
			entity2.put("key", "activity");
			entity2.put("value", "profile_data");
			
			JSONArray array = new JSONArray();
			array.put(entity1);
			array.put(entity2);
			
			event.put("entities", array);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return event.toString();
	}
	
	public void profileDataFromString(String data){
		String []datas = data.split(SAPERATOR);
		int index = 0;
		if(profileData == null)
			profileData = new ProfileData();
		
		profileData.setDesc(datas[index++]);
		profileData.setDisplayName(datas[index++]);
		profileData.setEmail(datas[index++]);
		try {
			profileData.setEvent_id(Integer.parseInt(datas[index++]));
		} catch (NumberFormatException e) {}
		profileData.setMsg_id(datas[index++]);
		profileData.setSession(datas[index++]);
		profileData.setUsername(datas[index++]);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return profileData.getDesc() + SAPERATOR + 
				profileData.getDisplayName() + SAPERATOR +
				profileData.getEmail() + SAPERATOR +
				profileData.getEvent_id() + SAPERATOR +
				profileData.getMsg_id() + SAPERATOR +
				profileData.getSession() + SAPERATOR +
				profileData.getUsername();
	}
	
	private static final String SAPERATOR = "#@#";
}
