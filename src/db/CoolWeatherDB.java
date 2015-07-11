package db;

import java.util.ArrayList;
import java.util.List;

import modle.City;
import modle.County;
import modle.Province;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CoolWeatherDB
{
	/**
	 * 数据库名称
	 */
	public static final String DB_NAME ="cool_weather";
	
	/**
	 * 数据库版本
	 */
	public static int VERSION =1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	private CoolWeatherDB(Context context){
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	/**
	 * 获取CoolWeatherDB的实例
	 */
	public synchronized static CoolWeatherDB getInstance (Context context){
		if(coolWeatherDB ==null){
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	/**
	 * 将Province实例储存到数据库
	 */
	public void saveProvince(Province province){
		if(province!=null){
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			Log.d("存储数据---", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			Log.d("存储数据---", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	/**
	 * 从数据库读取全国所有的省份信息
	 */
	public List<Province> loadProvinces(){
		
		List<Province> list = new ArrayList<Province>();
		Cursor cursor =db.query("Province", null, null, null, null, null, null);
		if(cursor==null){
			Log.d("加载数据", "从数据库查询数据失败！");
			return null;
		}
		if(cursor.moveToFirst()){
			do{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				
				list.add(province);
			} while (cursor.moveToNext());
			if(cursor!=null){
				cursor.close();
			}
			return list;
		}
		return null;
	}
	
	//保存City信息到本地数据库的方法
	public void saveCity(City city){
		
		if(city!=null){
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	//根据Province_id取得某个省所有城市的方法
	public  List<City> loadCities(int provinceId){
		
		List<City> list =new ArrayList<City>();
		Cursor cursor =db.query(
				"City", null, "province_id = ?",
				new String[]{String.valueOf(provinceId)},
				null, null, null);
		if(cursor.moveToFirst()){
			do{
				City city = new City();
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				
				list.add(city);
			}while(cursor.moveToNext());
			
			if(cursor!=null){
				cursor.close();
			}
			return list;
		}
		return null;
	}
	//储存County的信息到本地数据库
	public void saveCounty(County county){
		if(county!=null){
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
			
		}
	}
	//根据city_id提取某个城市所有县市的方法
	public List<County> loadCounties(int cityId){
		List<County> list = new ArrayList<County>();
		
		Cursor cursor = db.query("County", null, "city_id = ?",
				new String[]{String.valueOf(cityId)}, null, null, null);
		
		Log.d("loadCounty ", "进入加载语句,城市号为："+cityId);
		if(cursor.moveToFirst()){
			do{
				County county = new County();
				county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				Log.d("加载数据",cursor.getString(cursor.getColumnIndex("county_name"))+"");
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				
				list.add(county);
			}while(cursor.moveToNext());
		}
		if(cursor!=null){
			cursor.close();
		}
		return list;
		
		
	}
}
