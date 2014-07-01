package com.rwth.i10.exercisegroups.Fragments;

import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Activitys.MainActivity;
import com.rwth.i10.exercisegroups.Util.GroupData;

public class CreateGroup extends Fragment {

	private TextView activity;
	private TextView course;	
	
	public CreateGroup() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);
		
		activity = (TextView)rootView.findViewById(R.id.create_group_activity_text);
		course = (TextView) rootView.findViewById(R.id.create_group_course_text);
		
		
		((Button) rootView.findViewById(R.id.create_group_btn))
				.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Event event = new Event("END", "ANNOUNCEMENT", (int)System.currentTimeMillis());
				event.setSession(RandomString.randomString(20));
				event.addEntity(new Entity<String>("app", "study_me"));
				event.addEntity(new Entity<String>("group_activity", activity.getText().toString()));
				event.addEntity(new Entity<String>("group_course", course.getText().toString()));
				Gson g = new Gson();
				String json = "[" + g.toJson(event) + "]";
				Log.d("Json", json);
				MainActivity.contextData.post("events/update", json);
				MainActivity.mFragmentManager.popBackStack();*/
				GroupData item = new GroupData(
						activity.getText().toString(), 
						course.getText().toString(), 
						"", 0, null
					);
				MainActivity.groupListView.addItem(item);
				MainActivity.mFragmentManager.popBackStack();
			}
		});
		
		return rootView;
	}
}
