package com.rwth.i10.exercisegroups.Adapters;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Activitys.MainActivity;
import com.rwth.i10.exercisegroups.Util.Constants;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.MessagesTypes;
import com.rwth.i10.exercisegroups.Util.MyContextData;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.Entity;
import de.contextdata.Event;
import de.contextdata.RandomString;


/**
 * Adapter for main view groups diplay
 * */
public class MainListViewAdapter extends BaseAdapter {

	private ArrayList<GroupData> groupsList;
	private static Context context;
	private static String runningGroup;
	private static ViewGroup mainView;
	
	private static final String ACTIVE = "Active";
	private static final String INACTIVE = "In-Active";

	public MainListViewAdapter(Context context) {
		// TODO Auto-generated constructor stub
		groupsList = new ArrayList<GroupData>();
		this.context = context;
		runningGroup = null;
	}

	public void setMainView(ViewGroup mainView){
		this.mainView = mainView;
	}

	public boolean removeItem(GroupData item){
		int i=0;
		for (GroupData temp : groupsList) {
			if(temp.getName().equalsIgnoreCase(item.getName()) &&
					temp.getCourse().equalsIgnoreCase(item.getCourse()) &&
					temp.getMaxNumber() == item.getMaxNumber()){
				groupsList.remove(i);
				this.notifyDataSetInvalidated();
				return true;
			}
			i++;
		}
		return false;
	}

	public GroupData getGroupItem(String id){
		for(GroupData data : groupsList){
			if(data.getGroupSession().equals(id) || data.getGroupId().equals(id)){
				Location location = MainActivity.mLocation;
				Address add = StaticUtilMethods.getAddressForLocation(context, location);
				StringBuilder address = new StringBuilder();
				if(add != null)
					for(int i=0; i<add.getMaxAddressLineIndex(); i++)
						address.append(add.getAddressLine(i) + " ");


				if(TextUtils.isEmpty(data.getAdmin()))
					data.setAdmin(MainActivity.regId);
				data.setLat(location.getLatitude());
				data.setLng(location.getLongitude());
				data.setAddress(address.toString());

				return data;
			}
		}
		return null;
	}

	public void addItem(GroupData item){
		groupsList.add(item);
		this.notifyDataSetInvalidated();
	}

	@Override
	public void notifyDataSetInvalidated() {
		// TODO Auto-generated method stub
		if(mainView != null){
			mainView.removeAllViews();
			for(int i=0; i<getCount(); i++){
				View view = getView(i, null, null);
				mainView.addView(view);
			}
			mainView.invalidate();
		}
	}

	public void addItem(ArrayList<GroupData> items){
		groupsList.addAll(items);
		this.notifyDataSetInvalidated();
	}
	public void updateGroup(GroupData data){
		for(int i=0; i<groupsList.size(); i++){
			if(data.getGroupSession().equals(groupsList.get(i).getGroupSession())){
				groupsList.set(i, data);
				return;
			}
		}
	}
	public static String getCurrentGroup(){
		return runningGroup;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return groupsList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return groupsList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View rootView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LayoutInflater inflator = LayoutInflater.from(this.context);

		if(rootView == null){
			rootView = inflator.inflate(R.layout.adapter_group_list_item, null);
		}
		final GroupData data = groupsList.get(arg0);
		((TextView)rootView.findViewById(R.id.adapter_group_list_name)).setText(data.getName());
		if(data.getImage() == null)
			((ImageView)rootView.findViewById(R.id.adapter_group_list_img)).setBackgroundResource(R.drawable.group_img);
		else
			((ImageView)rootView.findViewById(R.id.adapter_group_list_img)).setBackgroundDrawable(new BitmapDrawable(context.getResources(), data.getImage()));

		
		/*
		 * Handle group delete button 
		 * **/
		((ImageButton)rootView.findViewById(R.id.adapter_group_list_del_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(runningGroup != null && runningGroup.equalsIgnoreCase(data.getName()))
					MainActivity.showToast("Group still running");
				else{
					removeItem(data);
					MainActivity.databaseSourse.deleteGroup(data.getGroupId());
					MainActivity.databaseSourse.removeGroupMsg(data.getGroupId());
					MainActivity.sendMessage(data.getGroupId() + Constants.VALUE_SEPRATOR + data.getName(), data.getUsers_joined().toArray(new String[]{}), MessagesTypes.GROUP_DISBAND_ACK);
					
					//MainActivity.deleteGroupFromServer(data);
				}
			}
		});
		
		TextView statusView = (TextView)rootView.findViewById(R.id.adapter_group_list_status);
		setStatus(INACTIVE, statusView);
		
		/*
		 * Handle group status change button
		 * **/
		LinearLayout status_btn = (LinearLayout)rootView.findViewById(R.id.adapter_group_list_status_btn);
		if(runningGroup == null && !TextUtils.isEmpty(data.getGroupSession()) && 
				!TextUtils.isEmpty(data.getStatus()) && data.getStatus().equalsIgnoreCase("START")){
			runningGroup = data.getGroupSession();
			setStatus(ACTIVE, statusView);
		}
		status_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView statusView = (TextView)v.findViewById(R.id.adapter_group_list_status);
				if(runningGroup == null){
					data.setGroupSession(RandomString.randomString(20));
					
					sendEvent("START");
					
					MainActivity.databaseSourse.updateGroup(data);
					
					runningGroup = data.getGroupSession();
					setStatus(ACTIVE, statusView);
					if(!MainActivity.mProfileHandler.getProfileData().isPublicProfile())
						MainActivity.showToast("Profile not public.");
				}
				else if(runningGroup.equalsIgnoreCase(data.getGroupSession())){
					sendEvent("END");
					MainActivity.databaseSourse.updateGroup(data);
					runningGroup = null;
					setStatus(INACTIVE, statusView);
				}
				else{
					MainActivity.showToast("Another group is running.");					
				}
			}
			private void sendEvent(String action){
				Location location = MainActivity.mLocation;
				Address add = StaticUtilMethods.getAddressForLocation(context, location);
				StringBuilder address = new StringBuilder();
				if(add != null)
					for(int i=0; i<add.getMaxAddressLineIndex(); i++)
						address.append(add.getAddressLine(i) + " ");


				if(TextUtils.isEmpty(data.getAdmin()))
					data.setAdmin(MainActivity.regId);
				data.setStatus(action);
				data.setLat(location.getLatitude());
				data.setLng(location.getLongitude());
				data.setAddress(address.toString());

				sendGroupData(data);

				MainActivity.fetschGroups();
				MainActivity.sendMessage("", null, MessagesTypes.UPDATE_GROUPS);
			}
		});


		return rootView;
	}

	
	/**
	 * Change group status and its color with respect to it
	 * */
	private void setStatus(String status, TextView view){
		if(status.equalsIgnoreCase(INACTIVE))
			view.setTextColor(Color.parseColor("#FF6969"));
		else
			view.setTextColor(Color.GREEN);//Color.parseColor("#B7FAAA"));
		view.setText(status);
	}
	
	
	/**
	 * Send group data to server as an event
	 * */
	public static void sendGroupData(GroupData data){
		MyContextData mContextData = ServerHandler.createInstance(
				context.getString(R.string.server_username), 
				context.getString(R.string.server_password));
		Event event = new Event(data.getStatus(), "ANNOUNCEMENT", (data.getTimestamp() < 1 ? (int)(System.currentTimeMillis() / 1000.0f)
				: (int)data.getTimestamp()));
		event.setSession(data.getGroupSession());
		event.addEntity(new Entity<String>("app", "study_me"));
		event.addEntity(new Entity<String>("group_activity", data.getName()));
		event.addEntity(new Entity<String>("group_course", data.getCourse()));
		event.addEntity(new Entity<String>("group_address", data.getAddress()));
		event.addEntity(new Entity<String>("group_admin", data.getAdmin()));
		event.addEntity(new Entity<String>("group_desc", data.getDescription()));
		event.addEntity(new Entity<String>("group_id", data.getGroupId()));
		event.addEntity(new Entity<Double>("lat", data.getLat()));
		event.addEntity(new Entity<Double>("lng", data.getLng()));
		event.addEntity(new Entity<String>("joined_users", data.getUsers_joined().toString()));

		String imageData = null;
		if(data.getImage() != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			data.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);    
			byte[] byteArrayImage = baos.toByteArray();
			imageData = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
		}
		event.addEntity(new Entity<String>("group_img", imageData));

		Gson g = new Gson();
		String json = "[" + g.toJson(event) + "]";
		MainActivity.showToast("\"" + data.getName() + "\" group " + (data.getStatus().equalsIgnoreCase("START") 
				? "active" : "inactive") +  " .");
		mContextData.post("events/update", json);

	}

}
