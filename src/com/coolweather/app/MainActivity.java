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
		//����SharedPreference�еı�ʶ�������Ƿ�Ҫ��ת������ҳ��
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
					//���ݵ����λ�ôӵ�ǰ�����ݼ���ȡ����Ӧ������
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
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��в�ѯ�����û�������ڵ���������ȥ��ѯ
	 */
	private void queryProvince(){
		//�ȴ����ݿ��в�ѯ����
		
		provincesList = coolWeatherDB.loadProvinces();
		if(provincesList!=null){
			datalist.clear();
			for(Province province:provincesList){
//				Log.d("���ݼ���ֵ", province.getProvinceName()+"");
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitle.setText("�й�");
//			Log.d("�й�","���ñ���");
			currentLevel =LEVEL_PROVINCE;
		}else{
			//���ݿ���û�в鵽�ʹ�����������ϲ�ѯ����һ�β�ѯ
			queryFromServer(null,"province");
		}
	}
	/**
	 * ��ѯ��ѡʡ�ĳ��У����ȴ����ݿ��в�ѯ�����û�������ڵ���������ȥ��ѯ
	 */
	private void queryCity(){
		
		//�ȴ����ݿ��в�ѯ����
		citiesList = coolWeatherDB.loadCities(selectedProvince.getId());
		
		if(citiesList!=null){
			datalist.clear();
			for(City city:citiesList){
//				Log.d("���ݼ���ֵ", city.getCityName()+"");
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTitle.setText(selectedProvince.getProvinceName());
			currentLevel =LEVEL_CITY;
		}else{
			//���ݿ���û�в鵽�ʹ�����������ϲ�ѯ����һ�β�ѯ
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/**
	 * ��ѯ��ѡ���е����У����ȴ����ݿ��в�ѯ�����û�������ڵ���������ȥ��ѯ
	 */
	private void queryCounty(){
		Log.d("query����","�����ѯ���");
		//�ȴ����ݿ��в�ѯ����
		countiesList = coolWeatherDB.loadCounties(selectedCity.getId());
//		Log.d("����", "��ǰѡ��ĳ���"+selectedCity.getId());
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
			//���ݿ���û�в鵽�ʹ�����������ϲ�ѯ����һ�β�ѯ
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	/**
	 * ���ݴ���Ĵ��ź����ʹ�����������ϲ�ѯʡ�����С����е�����
	 */
	private void queryFromServer(final String code,final String type){
		String address=null;
		if(!TextUtils.isEmpty(code)){
			if(type.equals("city")){
				address = "http://www.weather.com.cn/data/list3/city"+code+".xml?level=2";
			}else if(type.equals("county")){
//				Log.d("��ѯ����","�ӷ������ϲ�ѯ����");
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
					Log.d("��һ�β�ѯ", "�����ݴ洢�����أ�");
					result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//ͨ��RunOnUiThread()�������̴߳���
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
						Toast.makeText(MainActivity.this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
		
	}
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog =new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * �رնԻ���
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/**
	 * ����Back���������ݵ�ǰ�ļ������жϣ�������һ���ɲ㼶
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
