package com.rwth.i10.exercisegroups.Activitys;

import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.GroupData;

import de.contextdata.RandomString;

public class CreateGroupActivity extends Activity {

	private TextView activity;
	private TextView course;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_create_group);
		
		activity = (TextView)findViewById(R.id.create_group_activity_text);
		course = (TextView)findViewById(R.id.create_group_course_text);
		
		
		((Button)findViewById(R.id.create_group_btn))
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
				GroupData item = new GroupData(activity.getText().toString(), course.getText().toString(), "", 0, null);
				item.setGroup_id(RandomString.randomString(20));
				Log.d("group_id", item.getGroup_id() + " id");
				MainActivity.groupListView.addItem(item);
				finish();
				//MainActivity.mFragmentManager.popBackStack();
			}
		});
	}
}
