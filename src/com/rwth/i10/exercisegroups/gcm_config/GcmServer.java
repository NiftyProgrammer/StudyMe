package com.rwth.i10.exercisegroups.gcm_config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import com.rwth.i10.exercisegroups.R;
import com.rwth.i10.exercisegroups.Util.MessagesTypes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


/**
 * 
 * Server class to send message to other device(s)
 * 
 * */

public class GcmServer {

	private Context context;
	private int status;

	
	/**
	 * This method is used from other classes to send message with msg parameter (like message itself),
	 * user ids which will receive this message and message type
	 * */
	public void sendMessage(final Map<String, String> msgParams,
			final List<String> regIds, final MessagesTypes type,
			Context context){

		this.context = context;

		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				try {
					post(msgParams, regIds, type);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d("Server_error", e.getMessage());
				}
				return null;
			}

		}.execute();
	}

	
	/**
	 * Creates html header and body to send message to device(s) 
	 * */
	private void post(Map<String, String> msgParams, List<String> regIds, MessagesTypes type)
			throws IOException{
		// TODO Auto-generated method stub
		URL url;
		String baseUrl = context.getString(R.string.gcm_server_base_url);
		try { 
			url = new URL(baseUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + baseUrl);
		}

		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Map.Entry<String, String>> iterator = msgParams.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Map.Entry<String, String> param = iterator.next();
			bodyBuilder.append("data." + param.getKey()).append('=')
			.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}


		// add the regId to the end
		String body = bodyBuilder.toString();
		Iterator<String> regIdIterator = regIds.iterator();
		while (regIdIterator.hasNext()){
			body += "&registration_id="+regIdIterator.next();
		}
		body += "&restricted_package_name=com.rwth.i10.exercisegroups";

		/**
		 * If 'collapse' parameter is set, then on receiver end if it receive more then one message if this collapse type then only one will be handled
		 * */
		if(type == MessagesTypes.UPDATE_GROUPS)
			body += "&collapse_key=1";
		if(type == MessagesTypes.NEW_USER_JOINED)
			body += "&collapse_key=2";

		byte[] bytes = body.getBytes();
		HttpsURLConnection conn = null;

		try {
			Log.e("URL", "> " + url);

			conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(SSLContext.getInstance("Default").getSocketFactory());
			conn.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			});

			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "key=AIzaSyAALG1bJYhdQkJgomnMc_uhwFbeSnPcbns");
			conn.setRequestMethod("POST");

			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();

			// handle the response
			status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("GCM Post failed with error code " + status);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
