package com.rwth.i10.exercisegroups.preferences;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.rwth.i10.exercisegroups.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class ManagePreferences {

	private Context context;
	
	public ManagePreferences(Context context){
		this.context = context;
	}
	
	
	public boolean savePreferences(String key, String value){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		SimpleCrypto crypto = new SimpleCrypto();
		
		editor.putString(key, crypto.toHex(value));
		
		return editor.commit();
	}
	
	public String getPreference(String key, String defaultValue){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SimpleCrypto crypto = new SimpleCrypto();
		String value = pref.getString(key, defaultValue);
		if(TextUtils.isEmpty(value) || value.equalsIgnoreCase(defaultValue))
			return defaultValue;
		else
			return crypto.fromHex(value);
	}
		
	public boolean putStringPreferences(String key, String value){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(key, value);		
		return editor.commit();
	}
	
	public boolean putIntPreferences(String key, int value){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();		
		editor.putInt(key, value);		
		return editor.commit();
	}
	public boolean putBoolPreferences(String key, boolean value){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();		
		editor.putBoolean(key, value);		
		return editor.commit();
	}
	
	public boolean removePreferences(String key){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();		
		editor.remove(key);		
		return editor.commit();
	}
	public boolean removePreferences(String ...values){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		for(int i=0; i<values.length; i++)
			editor.remove(values[i]);		
		return editor.commit();
	}
	
	public String getStringPreferences(String key, String defaultValue){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(key, defaultValue);
	}
	public int getIntPreferences(String key, int defaultValue){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getInt(key, defaultValue);
	}
	public boolean getBoolPreferences(String key, boolean defaultValue){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(key, defaultValue);
	}
		
		
	private class SimpleCrypto {

		public String encrypt(String seed, String cleartext) {
			byte[] result = null;
			try {
				byte[] rawKey = getRawKey(seed.getBytes());
				result = encrypt(rawKey, cleartext.getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return toHex(result);
		}

		public String decrypt(String seed, String encrypted) {
			byte[] enc = toByte(encrypted);
			byte[] result = null;
			try {
				byte[] rawKey = getRawKey(seed.getBytes());
				result = decrypt(rawKey, enc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new String(result);
		}

		private byte[] getRawKey(byte[] seed) throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(seed);
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			return raw;
		}


		private byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(clear);
			return encrypted;
		}

		private byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] decrypted = cipher.doFinal(encrypted);
			return decrypted;
		}

		public String toHex(String txt) {
			return toHex(txt.getBytes());
		}
		public String fromHex(String hex) {
			return new String(toByte(hex));
		}

		public byte[] toByte(String hexString) {
			int len = hexString.length()/2;
			byte[] result = new byte[len];
			for (int i = 0; i < len; i++)
				result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
			return result;
		}

		public String toHex(byte[] buf) {
			if (buf == null)
				return "";
			StringBuffer result = new StringBuffer(2*buf.length);
			for (int i = 0; i < buf.length; i++) {
				appendHex(result, buf[i]);
			}
			return result.toString();
		}
		private final static String HEX = "0123456789ABCDEF";
		private void appendHex(StringBuffer sb, byte b) {
			sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
		}

	}

	
}
