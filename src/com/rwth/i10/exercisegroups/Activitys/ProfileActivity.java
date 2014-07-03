package com.rwth.i10.exercisegroups.Activitys;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.R.layout;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.ProfileHandler;
import com.rwth.i10.exercisegroups.Util.ServerHandler;
import com.rwth.i10.exercisegroups.Util.StaticUtilMethods;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.ContextData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends ActionBarActivity implements OnClickListener{

	private Context _context = null;
	private ProfileData _newData;
	private ProfileHandler _mProfileHandler;
	private Bitmap _imageDrawable = null;

	private static final int CAMERA_REQUEST = 1888; 
	private static final int GALLERY_REQUEST = 1999;

	private MyAsyncTask sendTask;
	private TextView _displayName, _email, _description;
	private View _progressLayout;
	private ViewGroup _mainView;
	private ImageView _imgView;
	private ManagePreferences pref;
	private boolean sendData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);


		init();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if(sendTask != null && sendTask.getStatus() == Status.RUNNING){
				sendTask.cancel(false);
				sendTask = null;
			}
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch (id) {
		case R.id.profile_send_btn:
			pref = null;
			pref = new ManagePreferences(_context);
			_progressLayout.setVisibility(View.VISIBLE);
			_mainView.setVisibility(View.GONE);
			_newData = _mProfileHandler.getProfileData();

			sendData = false;
			if( !TextUtils.isEmpty( _displayName.getText() ) && !_displayName.getText().toString().equalsIgnoreCase(_newData.getDisplayName()) ){
				sendData = true;
				_newData.setDisplayName(_displayName.getText().toString());
			}
			if( !TextUtils.isEmpty( _email.getText() ) && !_email.getText().toString().equalsIgnoreCase(_newData.getEmail()) ){
				sendData = true;
				_newData.setEmail(_email.getText().toString());
			}
			if( !TextUtils.isEmpty( _description.getText() ) && !_description.getText().toString().equalsIgnoreCase(_newData.getDesc()) ){
				sendData = true;
				_newData.setDesc(_description.getText().toString());
			}

			if(sendData){
				_mProfileHandler.setProfileData(_newData);
				_mProfileHandler.updateProfile();
			}

			if(sendTask == null || sendTask.getStatus() != Status.RUNNING){
				sendTask = null;
				sendTask = new MyAsyncTask();
			}
			sendTask.execute();
			break;

		case R.id.profile_img:
			AlertDialog dialog = new AlertDialog.Builder(_context)
			.setTitle("Choose image option")
			.setAdapter(new ArrayAdapter<String>(_context, android.R.layout.simple_list_item_1, new String[]{"Camera", "Gallery"}), 
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
			break;
			
		default:
			break;
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
			_imageDrawable = (Bitmap) data.getExtras().get("data"); 
			_imgView.setImageBitmap(_imageDrawable);
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


			_imageDrawable = BitmapFactory.decodeFile(filePath);
			_imgView.setImageBitmap(_imageDrawable);
		}
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(sendTask != null && sendTask.getStatus() == Status.RUNNING){
			sendTask.cancel(false);
			sendTask = null;
			return;
		}
	}

	private void init(){

		_context = this;

		_displayName = (TextView)findViewById(R.id.profile_display_name);
		_description = (TextView)findViewById(R.id.profile_desc);
		_email = (TextView)findViewById(R.id.profile_email_address);
		_mainView = (ViewGroup)findViewById(R.id.profile_main_view);
		_progressLayout = findViewById(R.id.profile_progress_layout);
		_imgView = (ImageView) findViewById(R.id.profile_img);

		String []credentials = StaticUtilMethods.getUserCredentials(_context);
		_mProfileHandler = new ProfileHandler(_context, credentials[0], credentials[1]);
		if(_mProfileHandler.getProfileData().isPublicProfile()){
			_mProfileHandler = null;
			_mProfileHandler = new ProfileHandler(_context, getString(R.string.server_username), getString(R.string.server_password));
		}
		_mProfileHandler.getPreviousProfile();
		_newData = MainActivity.mProfileHandler.getProfileData();

		if(!TextUtils.isEmpty(_newData.getDisplayName()))
			_displayName.setText(_newData.getDisplayName());
		if(!TextUtils.isEmpty(_newData.getEmail()))
			_email.setText(_newData.getEmail());
		if(!TextUtils.isEmpty(_newData.getDesc()))
			_description.setText(_newData.getDesc());

		((Button)findViewById(R.id.profile_send_btn)).setOnClickListener(this);
		_imgView.setOnClickListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private class MyAsyncTask extends AsyncTask<Void, String, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			if(sendData){
				while(!_mProfileHandler.getProcessFinished() || isCancelled()){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(TextUtils.isEmpty(_mProfileHandler.getError())){
					if( !TextUtils.isEmpty( _displayName.getText() ) ){
						publishProgress(ProfileData.PROFILE_DISPLAY_NAME, _newData.getDisplayName());
					}
					if( !TextUtils.isEmpty( _email.getText() ) ){
						publishProgress(ProfileData.PROFILE_EMAIL, _newData.getEmail());
					}
					if( !TextUtils.isEmpty( _description.getText() ) ){
						publishProgress(ProfileData.PROFILE_DESC, _newData.getDesc());
					}

					return true;
				}
			}

			return false;
		}
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			pref.savePreferences(values[0], values[1]);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				finish();
			else{
				 MainActivity.showToast("Error while sending: " + MainActivity.mProfileHandler.getError());
				_progressLayout.setVisibility(View.GONE);
				_mainView.setVisibility(View.VISIBLE);
			}
			MainActivity.mProfileHandler.changeProcessFinished();
		}

	};
}
