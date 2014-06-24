package com.rwth.i10.exercisegroups.Util;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.preferences.ManagePreferences;

import de.contextdata.Event;
import de.contextdata.RandomString;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

public class StaticUtilMethods {

	public static final int TWO_MINUTES = 1000 * 60 * 2;
	
	public static String createFetchServerJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("model", "COMPLETE");
			json.put("category", "ACTIVITY");
			json.put("source", "MOBILE");
			json.put("type", "ANNOUNCEMENT");
			JSONObject entity1 = new JSONObject();
			entity1.put("key", "app");
			entity1.put("value", "study_me");
			JSONArray entities = new JSONArray();
			entities.put(entity1);
			json.put("entities", entities);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("Json String", json.toString());
		return json.toString();
	}

	public static Address getAddressForLocation(Context context, Location location){
		Geocoder geocoder;
		List<Address> addresses = null;
		geocoder = new Geocoder(context, Locale.getDefault());
		try {
			addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(addresses != null)
			return addresses.get(0);
		else
			return null;
	}
	
	public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/** Checks whether two providers are the same */
	public static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	public static void storeProfileSessionId(String session, Context context){
		ManagePreferences pref = new ManagePreferences(context);
		pref.savePreferences(Constants.PROPERTY_PROFILE_SESSION, session);
	}
	
	public static String getProfileSessionId(Context context){
		ManagePreferences pref = new ManagePreferences(context);
		return pref.getPreference(Constants.PROPERTY_PROFILE_SESSION, RandomString.randomString(20));
	}
	
	public static String[] getUserCredentials(Context context){
		String []credentials = new String[2];
		ManagePreferences pref = new ManagePreferences(context);
		credentials[0] = pref.getPreference(context.getString(R.string.username_pref), "");
		credentials[1] = pref.getPreference(context.getString(R.string.password_pref), "");
		return credentials;
	}
	
	public static String eventToString(Event obj){
		Gson g = new Gson();
		String json = "[" + g.toJson(obj) + "]";
		return json;
	}
	
	public static int timestamp(){
		return (int)Calendar.getInstance().getTime().getTime();
	}
	
	public static String getDate(long time) {
	    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
	    cal.setTimeInMillis(time);
	    String date = DateFormat.format("dd-MM-yyyy", cal).toString();
	    return date;
	}
}
