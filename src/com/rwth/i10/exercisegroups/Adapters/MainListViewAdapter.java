package com.rwth.i10.exercisegroups.Adapters;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Activitys.MainActivity;
import com.rwth.i10.exercisegroups.Util.Constants;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.MessagesTypes;
import com.rwth.i10.exercisegroups.Util.MyContextData;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.ContextData;
import de.contextdata.Entity;
import de.contextdata.Event;
import de.contextdata.RandomString;

public class MainListViewAdapter extends BaseAdapter {

	private ArrayList<GroupData> groupsList;
	private Context context;
	private static String runningGroup;
	
	public MainListViewAdapter(Context context) {
		// TODO Auto-generated constructor stub
		groupsList = new ArrayList<GroupData>();
		this.context = context;
		runningGroup = null;
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
	
	public void addItem(GroupData item){
		groupsList.add(item);
		this.notifyDataSetInvalidated();
	}
	
	public void addItem(ArrayList<GroupData> items){
		groupsList.addAll(items);
		this.notifyDataSetInvalidated();
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
		if(rootView == null){
			LayoutInflater inflator = LayoutInflater.from(this.context);
			rootView = inflator.inflate(R.layout.adapter_group_list_item, null);
		}
		final GroupData data = groupsList.get(arg0);
		((TextView)rootView.findViewById(R.id.adapter_group_list_name)).setText(data.getName());
		if(data.getImage() == null)
			((ImageView)rootView.findViewById(R.id.adapter_group_list_img)).setBackgroundResource(R.drawable.group_img);
		else
			((ImageView)rootView.findViewById(R.id.adapter_group_list_img)).setBackgroundDrawable(new BitmapDrawable(context.getResources(), data.getImage()));
		
		((Button)rootView.findViewById(R.id.adapter_group_list_del_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(runningGroup != null && runningGroup.equalsIgnoreCase(data.getName()))
					Toast.makeText(context, "Group still running", Toast.LENGTH_SHORT).show();
				else{
					removeItem(data);
					MainActivity.databaseSourse.deleteGroup(data.getGroup_id());
				}
			}
		});
		Button status_btn = (Button)rootView.findViewById(R.id.adapter_group_list_status_btn);
		if(!TextUtils.isEmpty(data.getStatus())){
			runningGroup = data.getName();
			status_btn.setText("Stop");
		}
		status_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(runningGroup == null){
					sendEvent("START");
					runningGroup = data.getName();
					((Button)v).setText("Stop");
				}
				else if(runningGroup.equalsIgnoreCase(data.getName())){
					sendEvent("END");
					runningGroup = null;
					((Button)v).setText("Start");
					//MainActivity.fetschGroups();
				}
				else{
					Toast.makeText(context, "Another group is running.", Toast.LENGTH_SHORT).show();					
				}
			}
			private void sendEvent(String action){
				Location location = MainActivity.mLocation;
				Address add = StaticUtilMethods.getAddressForLocation(context, location);
				StringBuilder address = new StringBuilder();
				if(add != null)
				for(int i=0; i<add.getMaxAddressLineIndex(); i++)
					address.append(add.getAddressLine(i));
				
				MyContextData mContextData = ServerHandler.createInstance(
						context.getString(R.string.server_username), 
						context.getString(R.string.server_password));
				Event event = new Event(action, "ANNOUNCEMENT", (int)System.currentTimeMillis());
				event.setSession(data.getGroup_id());
				event.addEntity(new Entity<String>("app", "study_me"));
				event.addEntity(new Entity<Integer>(Constants.PROPERTY_TIMESTAMP, StaticUtilMethods.timestamp()));
				event.addEntity(new Entity<String>("group_activity", data.getName()));
				event.addEntity(new Entity<String>("group_course", data.getCourse()));
				event.addEntity(new Entity<String>("group_address", address.toString()));
				event.addEntity(new Entity<Integer>("group_max_particiepent", data.getMaxNumber()));
				event.addEntity(new Entity<Double>("lat", location.getLatitude()));
				event.addEntity(new Entity<Double>("lng", location.getLongitude()));
				Gson g = new Gson();
				String json = "[" + g.toJson(event) + "]";
				Toast.makeText(context, data.getName() + " group started.", Toast.LENGTH_SHORT).show();
				mContextData.post("events/update", json);
				
				MainActivity.sendMessage("", MessagesTypes.UPDATE_GROUPS);
				
				data.setStatus(action);
				data.setLat(location.getLatitude());
				data.setLng(location.getLongitude());
				data.setAddress(address.toString());
				MainActivity.databaseSourse.createGroup(data);
			}
		});
		
		return rootView;
	}

}
