package com.rwth.i10.exercisegroups.gcm_config;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	public static String GCMBroadcastReceiverAction = "com.google.android.c2dm.intent.RECEIVE";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
	}

}
