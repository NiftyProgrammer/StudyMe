package com.rwth.i10.exercisegroups.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;

import android.os.AsyncTask;
import android.util.Log;
import de.contextdata.RandomString;

public class MyContextData {


	private String server;
	private int version;
	private String username;
	private String password;
	private int appID;
	private int timeout;
	private String appSecret;
	private Listener mGETListener = null;
	private Listener mPOSTListener = null;

	private String TAG = "LearningContext";

	/**
	 * Constructor for the ContextData class
	 * 
	 * @param server
	 *            The URL of the server with the API
	 * @param version
	 *            The version of the API
	 * @param username
	 *            The name of the user
	 * @param password
	 *            The password of the user
	 * @param appID
	 *            The ID of your application, is provided when this app is
	 *            registered
	 * @param appSecret
	 *            The secret string of your application, is provided when this
	 *            app is registered
	 */
	public MyContextData(String server, int version, String username,
			String password, int appID, String appSecret) {
		super();
		if (server.endsWith("/"))
			this.server = server;
		else
			this.server = server + "/";
		this.version = version;
		this.username = username;
		this.password = password;
		this.appID = appID;
		this.appSecret = appSecret;
	}

	public interface Listener {
		public void onGETResult(String result);

		public void onPOSTResult(String result);
	}

	/**
	 * Listener for the result of a GET request
	 * 
	 * @param listener
	 */
	public void registerGETListener(Listener listener) {
		mGETListener = listener;
	}

	/**
	 * Listener for the result of a POST request
	 * 
	 * @param listener
	 */
	public void registerPOSTListener(Listener listener) {
		mPOSTListener = listener;
	}

	/**
	 * Sends a POST request to the API
	 * 
	 * @param api
	 *            The interface to communicate with, e.g. "events"
	 * @param json
	 *            The JSON string that should be sent to the interface
	 */
	public void post(String api, String json) {
		if (api.startsWith("/"))
			api.replaceFirst("/", "");

		new PostDataTask().execute(new String[] { api, json });
	}

	/**
	 * Sends a GET request to the API
	 * 
	 * @param api
	 *            The interface to communicate with, e.g. "events"
	 * @param json
	 *            The JSON string that should be sent to the interface
	 */
	public void get(String api, String json) {
		if (api.startsWith("/"))
			api.replaceFirst("/", "");

		new GetDataTask().execute(new String[] { api, json });
	}

	public void setTimeout(int timeout){
		this.timeout = timeout;
	}
	
	private ArrayList<NameValuePair> getPostData(String data) {
		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

		String nonce = RandomString.randomString(55);
		nvp.add(new BasicNameValuePair("nonce", convertToUTF8(nonce)));
		nvp.add(new BasicNameValuePair("aid", convertToUTF8(String
				.valueOf(appID))));
		nvp.add(new BasicNameValuePair("user", convertToUTF8(username)));
		nvp.add(new BasicNameValuePair("data", convertToUTF8(data)));

		String hash;
		try {
			hash = sha1(urlEncode(data) + appID + urlEncode(username)
					+ urlEncode(nonce) + appSecret + sha1(password));
			nvp.add(new BasicNameValuePair("h", convertToUTF8(hash)));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return nvp;
	}

	private String getGetData(String data) {
		String nonce = RandomString.randomString(55);
		String hash = "";
		try {
			hash = sha1(urlEncode(data) + appID + urlEncode(username)
					+ urlEncode(nonce) + appSecret + sha1(password));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String getString = "?nonce=" + urlEncode(nonce) + "&aid="
				+ urlEncode(String.valueOf(appID)) + "&user="
				+ urlEncode(username) + "&data=" + urlEncode(data) + "&h="
				+ urlEncode(hash);

		return getString;
	}

	private String convertToUTF8(String s) {
		String out = null;
		try {
			out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
		return out;
	}

	private String urlEncode(String string) {
		try {
			string = URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return string;
	}

	private String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}

	private class GetDataTask extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... data) {

			String params = getGetData(data[1]);

			try {
				HttpParams param = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(param, timeout);
				HttpConnectionParams.setSoTimeout(param, timeout);
				
				HttpClient httpclient = new DefaultHttpClient(param);
				HttpGet httpget = new HttpGet(server + version + "/" + data[0]
						+ params);
				Log.d("test", params);
				HttpResponse response = httpclient.execute(httpget);

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					BufferedInputStream bis = new BufferedInputStream(instream);
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					}
					String html = new String(baf.toByteArray());
					Log.d(TAG, html);

					return html;
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "There was a protocol based error");
			} catch (IOException e) {
				Log.e(TAG, "There was an IO Stream related error");
			}

			return "";

		}

		protected void onPostExecute(String result) {
			if (mGETListener != null)
				mGETListener.onGETResult(result);
		}
	}

	private class PostDataTask extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... data) {
			ArrayList<NameValuePair> nvp = getPostData(data[1]);

			try {
				HttpParams param = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(param, timeout);
				HttpConnectionParams.setSoTimeout(param, timeout);
				
				HttpClient httpclient = new DefaultHttpClient(param);
				HttpPost httppost = new HttpPost(server + version + "/"
						+ data[0]);
				httppost.setEntity(new UrlEncodedFormEntity(nvp));
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					BufferedInputStream bis = new BufferedInputStream(instream);
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					}
					String html = new String(baf.toByteArray());
					Log.d(TAG, html);

					return html;
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "There was a protocol based error");
			} catch (IOException e) {
				Log.e(TAG, "There was an IO Stream related error");
			}
			return "";
		}

		protected void onPostExecute(String result) {
			if (mPOSTListener != null)
				mPOSTListener.onPOSTResult(result);
		}
	}

	
}
