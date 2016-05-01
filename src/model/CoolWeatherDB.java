package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import db.CoolWeatherOpenHelper;

/**
 * Created by dell on 2016/4/21.
 */
public class CoolWeatherDB {
    public static final String DB_NAME="cool_weather";
    public static final int VERSION=1;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    //构造方法私有化
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbhelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db=dbhelper.getWritableDatabase();
    }
    //获取类的实例
    public synchronized static CoolWeatherDB getInstance(Context context){//CoolWeather体现了类作为一个方法的返回类型的作用
        if(coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
     //将省的信息存储在数据库中
    public void saveProvince(Province province){
        if(province!=null){
            ContentValues values=new ContentValues();
            values.put("province_code",province.getProvinceCode());
            values.put("province_name",province.getProvinceName());
            db.insert("Province",null,values);
        }
    }
    //从数据库中读取省的信息
    public List<Province> loadProvince(){
        List<Province> list=new ArrayList<Province>();
        Cursor cursor=db.query("Province",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                Province province=new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        return list;
    }
    //将市的实例存储在数据库中
    public void saveCity(City city){
        if(city!=null){
            ContentValues values=new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    //从数据库中读取某省下的所有市的信息
    public List<City> loadCity(int provinceId){
        List<City> list=new ArrayList<City>();
        Cursor cursor=db.query("City",null,"province_id=?",new String[]{String.valueOf(provinceId)},null,null,null);
        if(cursor.moveToFirst()){
            do {
                City city=new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while (cursor.moveToNext());
        }
        return list;
    }

   //将县的信息实例存储在数据库中
   public void saveCounty(County county){
       if(county!=null){
           ContentValues values=new ContentValues();
           values.put("county_name",county.getCountyName());
           values.put("county_code",county.getCountyNode());
           values.put("city_id",county.getCityId());
           db.insert("County",null,values);
       }
   }

    //从数据库遍历读取某市下的所有县的信息
    public List<County> loadCounty(int cityId){
        List<County> list=new ArrayList<County>();
        Cursor cursor=db.query("County",null,"city_id=?",new String[]{String.valueOf(cityId)},null,null,null);
        if (cursor.moveToFirst()){
            do {
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        return list;
    }
}
