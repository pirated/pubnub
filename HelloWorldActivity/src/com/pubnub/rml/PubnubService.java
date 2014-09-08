package com.pubnub.rml;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import com.pubnub.rml.R;

public class PubnubService extends Service {

	String channel = "my_channel";
	Pubnub pubnub = new Pubnub("pub-c-c6933291-bc4b-4aa6-9de7-b2b19fcb9c66","sub-c-266ebc98-358c-11e4-9f47-02ee2ddab7fe"); 
	PowerManager.WakeLock wl = null;
	
	// notification
	Notification notification = new Notification();
	Handler innerHandler;
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	

	private final class ServiceHandler extends Handler {
		
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			
			String pnMsg = msg.obj.toString();
			
			if(pnMsg!=null){
				
				int startIndex = pnMsg.indexOf(":");
				int endIndex = pnMsg.lastIndexOf('"');
				String text = pnMsg.substring(startIndex + 2, endIndex);
	
				showNotification(text);
			}
		}
	};

	private void notifyUser(Object message) {

		Message msg = mServiceHandler.obtainMessage();

		try {
			
			final String obj = (String) message;
			msg.obj = obj;
			mServiceHandler.sendMessage(msg);

			Log.i("Received msg : ", obj.toString());

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	private void showNotification(String text) {

		Intent intent = new Intent();
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		Notification noti = new Notification.Builder(this)
				.setTicker("RML Services")
				.setContentTitle("RML Service Notification")
				.setContentText(text).setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent).getNotification();
		
		noti.flags = Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(0, noti);

	}

	public void onCreate() {
		
		super.onCreate();

		HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		
		mServiceHandler = new ServiceHandler(mServiceLooper);

		Toast.makeText(this, "PubnubService created...", Toast.LENGTH_LONG).show();
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PubNubService");//SubscribeAtBoot
		
		if (wl != null) {
			
			wl.acquire();
			
			Log.i("PUBNUB", "Partial Wake Lock : " + wl.isHeld());
		}

		Log.i("PUBNUB", "PubnubService created...");
		
		try {

			Toast.makeText(this, "PubNub Subscribed. " + wl.isHeld(),
					Toast.LENGTH_LONG).show();

			pubnub.subscribe(new String[] { channel }, new Callback() {
				
				public void connectCallback(String channel) {

					notifyUser("CONNECT on channel:" + channel);
				}

				public void disconnectCallback(String channel) {

					notifyUser("DISCONNECT on channel:" + channel);
				}

				public void reconnectCallback(String channel) {

					notifyUser("RECONNECT on channel:" + channel);
				}

				@Override
				public void successCallback(String channel, Object message) {

					notifyUser(channel + " " + message.toString());
				}

				@Override
				public void errorCallback(String channel, Object message) {

					notifyUser(channel + " " + message.toString());
				}
			});

		} catch (PubnubException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		if (wl != null) {
			
			wl.release();
			wl = null;
			
			Log.i("PUBNUB", "Partial Wake Lock : " + wl.isHeld());
		}
		
		Toast.makeText(this, "PubnubService destroyed...", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

}
