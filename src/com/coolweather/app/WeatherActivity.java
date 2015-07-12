package com.coolweather.app;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	/**
	 * ��ʾ������Ϣ����ͼ
	 */
	private LinearLayout mWeatherInfo;
	/**
	 * ������ʾ����
	 */
	private TextView mCountyName;
	/**
	 * ������ʾ����ʱ��
	 */
	private TextView mPublishText;
	/**
	 * ������ʾ��������
	 */
	private TextView mWeatherDesp;
	/**
	 * ������ʾ����1
	 */
	private TextView mTemp1;
	/**
	 * ������ʾ����2
	 */
	private TextView mTemp2;
	/**
	 * ������ʾ��ǰ������
	 */
	private TextView mCurrentDate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		
		mCountyName =(TextView) findViewById(R.id.county_name);
		mWeatherDesp =(TextView) findViewById(R.id.weather_desp);
		mTemp1 =(TextView) findViewById(R.id.temp1);
		mTemp2 =(TextView) findViewById(R.id.temp2);
		mCurrentDate =(TextView) findViewById(R.id.current_date);
		mPublishText =(TextView) findViewById(R.id.publish_text);
		
		mWeatherInfo = (LinearLayout) findViewById(R.id.weather_info_layout);
		
		String countyCode = getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode)){
			mPublishText.setText("ͬ����...");
			mWeatherInfo.setVisibility(View.INVISIBLE);
			mCountyName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			showWeather();
		}
	}
	/**
	 * ��ѯ�ؼ���������Ӧ������
	 */
	public void queryWeatherCode(String countyCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml?level=4";
		Log.d("queryWeatherCode", address);
		queryServer(address,"countyCode");
	}
	/**
	 * ��ѯ������������Ӧ������
	 */
	private void queryWeatherInfo(String weatherCode){
		String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		Log.d("queryWeatherInfo", address);
		queryServer(address,"weatherCode");
	}
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�����Ĵ��Ż���������Ϣ
	 */
	private void queryServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Log.d("onFinish", response);
				Log.d("onFinish","countyCode".equals(type)+"");
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//�ӷ��������ص������н�������������
						String[] array =response.split("\\|");
						Log.d("onFinish", array[1]);
						if(array!=null&&array.length==2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}else{
							Log.d("onFinish", "δ֪����1��");
						}
					}else{Log.d("onFinish", "δ֪����2��");};
				}else if("weatherCode".equals(type)){
					Log.d("onFinish type", type);
					//������������ص���Ϣ
					Utility.handleWeatherResponse(WeatherActivity.this,response);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
							
						}
					});
				}
				
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mPublishText.setText("ͬ��ʧ�ܣ�");
						
					}
				});
				
			}
		});
	}
	private void showWeather(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mCountyName.setText(prefs.getString("county_name", ""));
		mPublishText.setText("����"+prefs.getString("publish_time", "")+"����");
		mCurrentDate.setText(prefs.getString("current_date", ""));
		mWeatherDesp.setText(prefs.getString("weather_des", ""));
		mTemp1.setText(prefs.getString("temp1", ""));
		mTemp2.setText(prefs.getString("temp2", ""));
		mWeatherInfo.setVisibility(View.VISIBLE);
		mCountyName.setVisibility(View.VISIBLE);
	}

}
