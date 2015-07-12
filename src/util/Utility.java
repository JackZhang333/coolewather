package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import modle.City;
import modle.County;
import modle.Province;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import db.CoolWeatherDB;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response){
		if(!TextUtils.isEmpty(response)){
			//把数据用中文“，”分开，得到一个字符串数组
			String[] provinces = response.split(",");
			Log.d("数据长度", provinces.length+"");
//			if(provinces!=null&&provinces.length>0){
//				
//				for(String province:provinces){
//					String[] array =province.split("\\|");
//					Province mProvince =new Province();
//					mProvince.setProvinceCode(array[0]);
//					Log.d("解析数据", array[1]);
//					mProvince.setProvinceCode(array[1]);
//					Log.d("解析数据", array[0]);
//					//将解析出的数据存储到Province表当中
//					coolWeatherDB.saveProvince(mProvince);
//					Log.d("解析后", "把数据存储到本地");
//				}
//				return true;
//			}
			for(int i=0;i<provinces.length;i++){
				String p =provinces[i];
				String[] array = p.split("\\|");
				Province province =new Province();
				province.setProvinceName(array[1]);
				province.setProvinceCode(array[0]);
				
				coolWeatherDB.saveProvince(province);
			
			}
			return true;
		}
		return false;
	}
	/**
	 * 解析和处理服务器返回的城市数据
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			//把数据用中文“，”分开，得到一个字符串数组
			String[] cities = response.split(",");
//			if(cities!=null&&cities.length>0){
//				
//				for(String city:cities){
//					String[] array =city.split("\\|");
//					City mCity =new City();
//					mCity.setCityCode(array[0]);
//					mCity.setCityCode(array[1]);
//					mCity.setProvinceId(provinceId);
//					//将解析出的数据存储到Province表当中
//					coolWeatherDB.saveCity(mCity);
//					
//				}
//				return true;
//			}
			for(int i=0;i<cities.length;i++){
				String c =cities[i];
				String[] array = c.split("\\|");
				City city =new City();
				city.setCityName(array[1]);
				
				city.setCityCode(array[0]);
				city.setProvinceId(provinceId);
				
				coolWeatherDB.saveCity(city);
				
				
			}
			return true;
		}
		return false;
	}
	/**
	 * 解析和处理服务器返回的县市数据
	 */
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId){
		if(!TextUtils.isEmpty(response)){
			//把数据用中文“，”分开，得到一个字符串数组
			String[] counties = response.split(",");
//			if(counties!=null&&counties.length>0){
//				
//				for(String county:counties){
//					String[] array =county.split("\\|");
//					County mCounty =new County();
//					mCounty.setCountyCode(array[0]);
//					mCounty.setCountyCode(array[1]);
//					mCounty.setCityId(cityId);
//					//将解析出的数据存储到Province表当中
//					coolWeatherDB.saveCounty(mCounty);
//					
//				}
//				return true;
//			}
			for(int i=0;i<counties.length;i++){
				String c =counties[i];
				String[] array = c.split("\\|");
				County county =new County();
				county.setCountyName(array[1]);
				county.setCountyCode(array[0]);
				county.setCityId(cityId);
				coolWeatherDB.saveCounty(county);
		
			}
			return true;
		}
		return false;
	}
	public static void handleWeatherResponse(Context context,String response){
		try{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String countyName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDes = weatherInfo.getString("weather");
			String publishTime =weatherInfo.getString("ptime");
			saveWeatherInfo(context,countyName,weatherCode,temp1,temp2,weatherDes,publishTime);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	public static void saveWeatherInfo(Context context,String countyName,String weatherCode,
			String temp1,String temp2,String weatherDes,String publishTime){
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor =PreferenceManager.
				getDefaultSharedPreferences(context).
				edit();
		editor.putBoolean("county_selected", true);
		editor.putString("county_name", countyName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_des", weatherDes);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
