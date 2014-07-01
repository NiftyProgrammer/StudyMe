package com.rwth.i10.exercisegroups.Activitys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;

import de.contextdata.RandomString;

public class CreateGroupActivity extends ActionBarActivity {

	private TextView activity;
	private TextView course;
	private TextView desc;
	private ImageView image;

	private Bitmap imageDrawable = null;
	private Context context;

	private static final int CAMERA_REQUEST = 1888; 
	private static final int GALLERY_REQUEST = 1999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_create_group);

		context = this;

		activity = (TextView)findViewById(R.id.create_group_activity_text);
		course = (TextView)findViewById(R.id.create_group_course_text);
		desc = (TextView)findViewById(R.id.create_group_desc);
		image = (ImageView)findViewById(R.id.create_group_img);

		activity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				// TODO Auto-generated method stub
				if (id == EditorInfo.IME_ACTION_NEXT) {
					if(!TextUtils.isEmpty(activity.getText()))
						activity.setError(null);
					else
						activity.setError("Group Name required");						
				}
				return false;
			}
		});
		
		course.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				// TODO Auto-generated method stub
				if (id == EditorInfo.IME_ACTION_NEXT) {
					if(!TextUtils.isEmpty(activity.getText()))
						course.setError(null);
					else
						course.setError("Group Course required");						
				}
				return false;
			}
		});

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
				if(TextUtils.isEmpty(activity.getText())){
					activity.setError("Group Name required");
					return;
				}
				else if(TextUtils.isEmpty(course.getText())){
					course.setText("Group Course required");
					return;
				}
				
				GroupData item = new GroupData(activity.getText().toString(), course.getText().toString(), "", 0, null);
				item.setDescription(desc.getText().toString());
				item.setAdmin(MainActivity.regId);
				item.setGroupId(RandomString.randomString(20));
				item.setStatus("END");

				//compress image
				if(imageDrawable != null){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					imageDrawable.compress(Bitmap.CompressFormat.JPEG, 100, out);
					Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
					item.setImage(StaticUtilMethods.getRoundedShape(decoded));
				}
				
				MainActivity.groupListView.addItem(item);
				MainActivity.databaseSourse.createGroup(item);
				MainActivity.databaseSourse.addNewGroupMessage(item.getGroupId(), "");
				finish();
				//MainActivity.mFragmentManager.popBackStack();
			}
		});

		image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle("Choose image option")
				.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[]{"Camera", "Gallery"}), 
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						// TODO Auto-generated method stub
						switch(position){
						case 0:
							Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
							startActivityForResult(cameraIntent, CAMERA_REQUEST); 
							break;

						case 1:
							Intent intent = new Intent(Intent.ACTION_PICK);
							intent.setType("image/*");
							startActivityForResult(intent, GALLERY_REQUEST);
							break;
						}
					}
				})
				.create();
				dialog.show();
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
			imageDrawable = (Bitmap) data.getExtras().get("data"); 
			image.setImageBitmap(imageDrawable);
		}
		else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(
					selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();


			imageDrawable = BitmapFactory.decodeFile(filePath);
			image.setImageBitmap(imageDrawable);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}	
}
