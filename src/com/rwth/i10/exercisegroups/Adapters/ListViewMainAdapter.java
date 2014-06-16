package com.rwth.i10.exercisegroups.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.GroupData;

public class ListViewMainAdapter extends BaseAdapter {

	private ArrayList<GroupData> list;
	private Context context;
	
	public ListViewMainAdapter(Context context) {
		// TODO Auto-generated constructor stub
		list = new ArrayList<GroupData>();
		this.context = context;
	}
	
	public void addItem(GroupData group){
		this.list.add(group);
		this.notifyDataSetInvalidated();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View rootView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(rootView == null){
			LayoutInflater inflater = LayoutInflater.from(context);
			rootView = inflater.inflate(R.layout.adapter_main_list_view, null);
		}
		GroupData data = list.get(position);
		((TextView)rootView.findViewById(R.id.main_list_view_name)).setText(data.getName());
		((TextView)rootView.findViewById(R.id.main_list_view_course)).setText(data.getCourse());
		((TextView)rootView.findViewById(R.id.main_list_view_address)).setText(data.getAddress());
		if(data.getImage() != null)
			((ImageView)rootView.findViewById(R.id.main_list_view_img)).setBackgroundDrawable(new BitmapDrawable(context.getResources(), data.getImage()));
		else
			((ImageView)rootView.findViewById(R.id.main_list_view_img)).setBackgroundResource(R.drawable.group_img);
		
		return rootView;
	}

}
