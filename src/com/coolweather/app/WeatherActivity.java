package com.coolweather.app;

import receiver.AutoUpdateReceiver;
import service.AutoUpdateService;
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
	 * 显示天气信息的视图
	 */
	private LinearLayout mWeatherInfo;
	/**
	 * 用于显示城市
	 */
	private TextView mCountyName;
	/**
	 * 用于显示发布时间
	 */
	private TextView mPublishText;
	/**
	 * 用于显示天气描述
	 */
	private TextView mWeatherDesp;
	/**
	 * 用于显示气温1
	 */
	private TextView mTemp1;
	/**
	 * 用于显示气温2
	 */
	private TextView mTemp2;
	/**
	 * 用于显示当前的天气
	 */
	private TextView mCurrentDate;
	/**
	 * 重新选择城市的按钮
	 */
	private Button mBtnChooseCity;
	/**
	 * 刷新页面的按钮
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
			mPublishText.setText("同步中...");
			mWeatherInfo.setVisibility(View.INVISIBLE);
			mCountyName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			showWeather();
		}
	}
	/**
	 * 查询县级代号所对应的天气
	 */
	public void queryWeatherCode(String countyCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml?level=4";
		Log.d("queryWeatherCode", address);
		queryServer(address,"countyCode");
	}
	/**
	 * 查询天气代号所对应的天气
	 */
	private void queryWeatherInfo(String weatherCode){
		String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		Log.d("queryWeatherInfo", address);
		queryServer(address,"weatherCode");
	}
	/**
	 * 根据传入的地址和类型去向服务器查询天气的代号或者天气信息
	 */
	private void queryServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Log.d("onFinish", response);
				Log.d("onFinish","countyCode".equals(type)+"");
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据中解析出天气代号
						String[] array =response.split("\\|");
						Log.d("onFinish", array[1]);
						if(array!=null&&array.length==2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}else{
							Log.d("onFinish", "未知错误1！");
						}
					}else{Log.d("onFinish", "未知错误2！");};
				}else if("weatherCode".equals(type)){
					Log.d("onFinish type", type);
					//处理服务器返回的信息
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
						mPublishText.setText("同步失败！");
						
					}
				});
				
			}
		});
	}
	private void showWeather(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mCountyName.setText(prefs.getString("county_name", ""));
		mPublishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		mCurrentDate.setText(prefs.getString("current_date", ""));
		mWeatherDesp.setText(prefs.getString("weather_des", ""));
		mTemp1.setText(prefs.getString("temp2", ""));
		mTemp2.setText(prefs.getString("temp1", ""));
		mWeatherInfo.setVisibility(View.VISIBLE);
		mCountyName.setVisibility(View.VISIBLE);
		
		Intent intent =new Intent(this,AutoUpdateService.class);
		Log.d("weatherActivity", "准备启动服务");
		startService(intent);
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
