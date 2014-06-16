package com.rwth.i10.exercisegroups.gcm_config;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rwth.i10.exercisegroups.Activitys.MainActivity;
import com.rwth.i10.exercisegroups.Util.MessageCategories;
import com.rwth.i10.exercisegroups.Util.MessagesTypes;

public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM
			 * will be extended in the future with new message types, just ignore
			 * any message types you're not interested in, or that you don't
			 * recognize.
			 */
			if (GoogleCloudMessaging.
					MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Log.d("Message error", extras.toString());
				//sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.
					MESSAGE_TYPE_DELETED.equals(messageType)) {
				Log.d("deleted Message on server", extras.toString());
				//sendNotification("Deleted messages on server: " +
				//extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.
					MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// Post notification of received message.
				sendNotification("Received: " + extras.toString());

				Log.i("Message", "Received: " + extras.toString());

				int msgType = Integer.parseInt(
						extras.getString(MessageCategories.TYPE.getString()));
				MessagesTypes type = MessagesTypes.convert(msgType);

				switch(type){

				case RECEIVE_MESSAGE:
				{
					break;
				}

				case UPDATE_GROUPS:
				{
					MainActivity.fetschGroups();
					break;
				}

				default:
				{
					MainActivity.fetschGroups();
				}

				}

			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.common_signin_btn_icon_light)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

	
}
