package util;

import modle.City;
import modle.County;
import modle.Province;
import android.text.TextUtils;
import android.util.Log;
import db.CoolWeatherDB;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response){
		if(!TextUtils.isEmpty(response)){
			//�����������ġ������ֿ����õ�һ���ַ�������
			String[] provinces = response.split(",");
			Log.d("���ݳ���", provinces.length+"");
//			if(provinces!=null&&provinces.length>0){
//				
//				for(String province:provinces){
//					String[] array =province.split("\\|");
//					Province mProvince =new Province();
//					mProvince.setProvinceCode(array[0]);
//					Log.d("��������", array[1]);
//					mProvince.setProvinceCode(array[1]);
//					Log.d("��������", array[0]);
//					//�������������ݴ洢��Province����
//					coolWeatherDB.saveProvince(mProvince);
//					Log.d("������", "�����ݴ洢������");
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
	 * �����ʹ�����������صĳ�������
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			//�����������ġ������ֿ����õ�һ���ַ�������
			String[] cities = response.split(",");
//			if(cities!=null&&cities.length>0){
//				
//				for(String city:cities){
//					String[] array =city.split("\\|");
//					City mCity =new City();
//					mCity.setCityCode(array[0]);
//					mCity.setCityCode(array[1]);
//					mCity.setProvinceId(provinceId);
//					//�������������ݴ洢��Province����
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
	 * �����ʹ�����������ص���������
	 */
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId){
		if(!TextUtils.isEmpty(response)){
			//�����������ġ������ֿ����õ�һ���ַ�������
			String[] counties = response.split(",");
//			if(counties!=null&&counties.length>0){
//				
//				for(String county:counties){
//					String[] array =county.split("\\|");
//					County mCounty =new County();
//					mCounty.setCountyCode(array[0]);
//					mCounty.setCountyCode(array[1]);
//					mCounty.setCityId(cityId);
//					//�������������ݴ洢��Province����
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
}
