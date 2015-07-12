package com.coolweather.app;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import db.CoolWeatherDB;
import modle.City;
import modle.County;
import modle.Province;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private TextView mTitle;
	private ListView mListView;
	
	private List<String> datalist=new ArrayList<String>();
	private List<Province> provincesList;
	private List<City> citiesList;
	private List<County> countiesList;
	
	private ArrayAdapter<String> adapter;
	
	private static final int LEVEL_PROVINCE =1;
	private static final int LEVEL_CITY =2;
	private static final int LEVEL_COUNTY =3;
	
	private int currentLevel;
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	private CoolWeatherDB coolWeatherDB;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		//根据SharedPreference中的标识，决定是否要跳转到天气页面
		SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("county_selected", false)){
			Intent intent =new Intent(MainActivity.this,WeatherActivity.class);
			startActivity(intent);
			finish();
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mTitle =(TextView) findViewById(R.id.title_text);
		mListView = (ListView) findViewById(R.id.list_view);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		adapter =new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		
		mListView.setAdapter(adapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel == LEVEL_PROVINCE){
					//根据点击的位置从当前的数据集中取出相应的数据
					selectedProvince = provincesList.get(position);
					queryCity();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity =citiesList.get(position);
					queryCounty();
				}else{
					selectedCounty =countiesList.get(position);
					String countyCode =selectedCounty.getCountyCode();
					Intent intent =new Intent(MainActivity.this,WeatherActivity.class);
					intent.putExtra("county_code",countyCode);
					
					startActivity(intent);
					finish();
				}
				
			}
		});
		queryProvince();
	}
	/**
	 * 查询全国所有的省，优先从数据库中查询，如果没有数据在到服务器上去查询
	 */
	private void queryProvince(){
		//先从数据库中查询数据
		
		provincesList = coolWeatherDB.loadProvinces();
		if(provincesList!=null){
			datalist.clear();
			for(Province province:provincesList){
//				Log.d("数据集赋值", province.getProvinceName()+"");
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitle.setText("中国");
//			Log.d("中国","设置标题");
			currentLevel =LEVEL_PROVINCE;
		}else{
			//数据库中没有查到就从网络服务器上查询，第一次查询
			queryFromServer(null,"province");
		}
	}
	/**
	 * 查询所选省的城市，优先从数据库中查询，如果没有数据在到服务器上去查询
	 */
	private void queryCity(){
		
		//先从数据库中查询数据
		citiesList = coolWeatherDB.loadCities(selectedProvince.getId());
		
		if(citiesList!=null){
			datalist.clear();
			for(City city:citiesList){
//				Log.d("数据集赋值", city.getCityName()+"");
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitle.setText(selectedProvince.getProvinceName());
			currentLevel =LEVEL_CITY;
		}else{
			//数据库中没有查到就从网络服务器上查询，第一次查询
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/**
	 * 查询所选城市的县市，优先从数据库中查询，如果没有数据在到服务器上去查询
	 */
	private void queryCounty(){
		Log.d("query县市","进入查询语句");
		//先从数据库中查询数据
		countiesList = coolWeatherDB.loadCounties(selectedCity.getId());
//		Log.d("县市", "当前选择的城市"+selectedCity.getId());
		if(countiesList.size()>0){
			datalist.clear();
			for(County county:countiesList){
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitle.setText(selectedCity.getCityName());
			currentLevel =LEVEL_COUNTY;
		}else{
			//数据库中没有查到就从网络服务器上查询，第一次查询
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	/**
	 * 根据传入的代号和类型从网络服务器上查询省、城市、县市的数据
	 */
	private void queryFromServer(final String code,final String type){
		String address=null;
		if(!TextUtils.isEmpty(code)){
			if(type.equals("city")){
				address = "http://www.weather.com.cn/data/list3/city"+code+".xml?level=2";
			}else if(type.equals("county")){
//				Log.d("查询县市","从服务器上查询县市");
				address = "http://www.weather.com.cn/data/list3/city"+code+".xml?level=3";
			}
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml?level=1";
		}
		
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result =false;
				
				if("province".equals(type)){
					
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
					
				}else if("city".equals(type)){
					
					result = Utility.handleCitiesResponse(coolWeatherDB, response,selectedProvince.getId());
				}else if("county".equals(type)){
					Log.d("第一次查询", "把数据存储到本地！");
					result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//通过RunOnUiThread()返回主线程处理
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvince();
							}else if("city".equals(type)){
								queryCity();
							}else if("county".equals(type)){
								queryCounty();
							}
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
						closeProgressDialog();
						Toast.makeText(MainActivity.this, "加载失败！", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
		
	}
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog =new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 捕获Back按键，根据当前的级别来判断，返回哪一个成层级
	 */
	@Override
	public void onBackPressed(){
		if(currentLevel==LEVEL_COUNTY){
			queryCity();
		}else if(currentLevel==LEVEL_CITY){
			queryProvince();
		}
		else{
			finish();
		}
	}
}
