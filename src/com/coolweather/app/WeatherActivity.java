package com.coolweather.app;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.Activity;
import android.app.SearchManager.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
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
	/**
	 * ����ѡ����еİ�ť
	 */
	private Button mBtnChooseCity;
	/**
	 * ˢ��ҳ��İ�ť
	 */
	private Button mBtnFresh;
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
		
		mBtnChooseCity =(Button) findViewById(R.id.switch_city);
		mBtnFresh = (Button) findViewById(R.id.fresh_info);
		
		mBtnChooseCity.setOnClickListener(this);
		mBtnFresh.setOnClickListener(this);
		
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
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent =new Intent(WeatherActivity.this,MainActivity.class);
			intent.putExtra("isFromWeatherActivity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.fresh_info:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode =prefs.getString("weather_code", "");
			queryWeatherInfo(weatherCode);
			break;

		default:
			break;
		}
		
	}

}
