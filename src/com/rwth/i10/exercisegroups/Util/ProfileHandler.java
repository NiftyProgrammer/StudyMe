package com.rwth.i10.exercisegroups.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.en;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;
import de.contextdata.ContextData.Listener;
import de.contextdata.Entity;
import de.contextdata.Event;
import de.contextdata.RandomString;
import android.content.Context;
import android.text.TextUtils;

public class ProfileHandler implements Listener{

	private ContextData contextData;
	private Context context;
	
	private ProfileData profileData;
	private boolean deleteAfterwords;
	
	public ProfileHandler(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		fetschProfileData();
		deleteAfterwords = false;
	}
	public ProfileHandler(Context context, ContextData contextData){
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
	}
		
	@Override
	public void onGETResult(String result) {
		// TODO Auto-generated method stub
		try {
			JSONObject data = new JSONObject(result);
			
			if(data.optInt("result") == 0 || data.optInt("total_events") <= 0){
				uploadProfile(profileData);
			}
			ProfileData tempData = new ProfileData();
			JSONArray events = data.optJSONArray("events");
			if(events != null){
				for(int i=0; i<events.length(); i++){
					JSONObject event = events.optJSONObject(i);
					JSONArray entities = event.optJSONArray("entities");
					for(int j=0; j<entities.length(); j++){
						JSONObject obj = entities.optJSONObject(j);
						if(profileData.getUsername().equals( obj.optString( ProfileData.PROFILE_USERNAME ) ) ||
								profileData.getMsg_id().equals( obj.optString( ProfileData.PROFILE_MSG_ID ) )){
							tempData.setDisplayName( obj.optString( ProfileData.PROFILE_DISPLAY_NAME ) );
							tempData.setMsg_id( obj.optString( ProfileData.PROFILE_MSG_ID ) );
							tempData.setSession( event.optString( ProfileData.PROFILE_SESSION ) );
							tempData.setUsername( obj.optString( ProfileData.PROFILE_USERNAME ) );
							tempData.setEvent_id( event.optString( ProfileData.PROFILE_ID ) );
						}
					}
				}
				
				if(!TextUtils.isEmpty(tempData.getEvent_id())){
					setProfileData(tempData);
					if(deleteAfterwords){
						deleteAfterwords = false;
						deleteProfile(tempData.getEvent_id());
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void onPOSTResult(String result) {
		// TODO Auto-generated method stub
		try {
			JSONObject obj = new JSONObject(result);
			if("1".equalsIgnoreCase(obj.optString("result"))){
				uploadProfile(profileData);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setContexData(String username, String password){
		this.contextData = ServerHandler.createInstance(username, password);
		this.contextData.registerGETListener(this);
		this.contextData.registerPOSTListener(this);
	}
		
	public void deleteProfile(ContextData contextData, String id){
		try {
			contextData.post("events/delete", new JSONObject().put("id", id).toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
	
			e.printStackTrace();
		}
	}
	public void deleteProfile(String id){
		deleteProfile(this.contextData, id);
	}
	public void updateProfile(){
		deleteAfterwords = true;
		getPreviousProfile();
	}
	public void updateProfile(ContextData contextData){
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
		if(TextUtils.isEmpty(profileData.getEvent_id()))
			profileData.setEvent_id(data.getEvent_id());
		if(TextUtils.isEmpty(profileData.getMsg_id()))
			profileData.setMsg_id(data.getMsg_id());
		if(TextUtils.isEmpty(profileData.getSession()))
			profileData.setSession(data.getSession());
		if(TextUtils.isEmpty(profileData.getUsername()))
			profileData.setUsername(data.getUsername());
	}
	
	
	public void uploadProfile(ContextData contextData, ProfileData pData){
		
		Event event = new Event("UPDATE", "RELEVANCE", (int)System.currentTimeMillis());
		event.addEntity( new Entity<String>( ProfileData.PROFILE_USERNAME, pData.getUsername() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_DISPLAY_NAME, pData.getDisplayName() ) );
		event.addEntity( new Entity<String>( ProfileData.PROFILE_MSG_ID, pData.getMsg_id() ) );
		event.setSession(pData.getSession());
		contextData.post("events/show", StaticUtilMethods.eventToString(event));
	}
	public void uploadProfile(ProfileData pData){
		uploadProfile(contextData, pData);
	}
	
	public void getPreviousProfile(){
		contextData.get("events/show", createFetschProfileString());
	}
	
	public void fetschProfileData(){
		ManagePreferences pref = new ManagePreferences(context);
		profileData = new ProfileData();
		
		profileData.setSession(pref.getPreference(Constants.PROPERTY_PROFILE_SESSION, RandomString.randomString(20)));
		profileData.setMsg_id(pref.getStringPreferences(Constants.PROPERTY_REG_ID, ""));
		profileData.setUsername(pref.getPreference(context.getString(R.string.username_pref), ""));
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
			event.put("model", "LATEST");
			event.put("category", "ACTIVITY");
			event.put("source", "MOBILE");
			event.put("type", "RELEVANCE");
			
			JSONObject entity1 = new JSONObject();
			JSONObject entity2 = new JSONObject();
			entity1.put("key", "app");
			entity1.put("value", "study_me");
			
			entity2.put("key", "activity");
			entity2.put("value", "user_profile");
			
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
	
	
}
