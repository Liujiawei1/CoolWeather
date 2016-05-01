package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;

/**
 * Created by dell on 2016/4/22.
 */
public class Utility {
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if (!TextUtils.isEmpty(response)){
            String[] allProvinces=response.split(",");
            if (allProvinces!=null&&allProvinces.length>0){
                for (String p:allProvinces){
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceName(array[1]);
                    province.setProvinceCode(array[0]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }


    public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities=response.split(",");
            if (allCities!=null&&allCities.length>0){
                for (String p:allCities){
                    String[] array=p.split("\\|");
                    City city=new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties=response.split(",");
            if (allCounties!=null&&allCounties.length>0){
                for (String p:allCounties){
                    String[] array=p.split("\\|");
                    County county=new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
             return true;
            }
        }
        return false;
    }
    
    //解析服务器返回的json数据
    public static void handleWeatherResponse(Context context,String response){
    	try{
    		JSONObject jsonObject=new JSONObject(response);
    		JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
    		String cityName=weatherInfo.getString("city");
    		String weatherCode=weatherInfo.getString("cityid");
    		String temp1=weatherInfo.getString("temp1");
    		String temp2=weatherInfo.getString("temp2");
    		String weatherDesp=weatherInfo.getString("weather");
    		String publishTime=weatherInfo.getString("ptime");
    		saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
    	}catch(JSONException e){
    		e.printStackTrace();
    	}
    	}
    	
    	
    	
    	//存储到sharedpreferences文件中
   
	private static void saveWeatherInfo(Context context,String cityName,String weatherCode,
    		String temp1,String temp2,String weatherDesp,String publishTime) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
    	SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putBoolean("city_selected", true);
    	editor.putString("city_name", cityName);
    	editor.putString("weatherCode", weatherCode);
    	editor.putString("temp1",temp1);
    	editor.putString("temp2", temp2);
    	editor.putString("weatherDesp", weatherDesp);
    	editor.putString("publish_time", publishTime);
    	editor.putString("current_time",sdf.format(new Date()));
    	editor.commit();
    	
	}

}
