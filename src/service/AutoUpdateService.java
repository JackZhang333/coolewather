package service;

import receiver.AutoUpdateReceiver;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.util.Log;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Service", "已经启动服务");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				updateData();
				
			}
		}).start();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour =8*60*60*1000;
//		int anHour =1000*10;
		long trigerTime = SystemClock.elapsedRealtime() +anHour;
		Intent i =new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi =PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerTime, pi);
		
		return super.onStartCommand(intent, flags, startId);
	}
	private void updateData(){
		Log.d("Service", "循环启动后台更新服务");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}
