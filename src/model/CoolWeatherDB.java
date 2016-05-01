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

    //���췽��˽�л�
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbhelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db=dbhelper.getWritableDatabase();
    }
    //��ȡ���ʵ��
    public synchronized static CoolWeatherDB getInstance(Context context){//CoolWeather����������Ϊһ�������ķ������͵�����
        if(coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
     //��ʡ����Ϣ�洢�����ݿ���
    public void saveProvince(Province province){
        if(province!=null){
            ContentValues values=new ContentValues();
            values.put("province_code",province.getProvinceCode());
            values.put("province_name",province.getProvinceName());
            db.insert("Province",null,values);
        }
    }
    //�����ݿ��ж�ȡʡ����Ϣ
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
    //���е�ʵ���洢�����ݿ���
    public void saveCity(City city){
        if(city!=null){
            ContentValues values=new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    //�����ݿ��ж�ȡĳʡ�µ������е���Ϣ
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

   //���ص���Ϣʵ���洢�����ݿ���
   public void saveCounty(County county){
       if(county!=null){
           ContentValues values=new ContentValues();
           values.put("county_name",county.getCountyName());
           values.put("county_code",county.getCountyNode());
           values.put("city_id",county.getCityId());
           db.insert("County",null,values);
       }
   }

    //�����ݿ������ȡĳ���µ������ص���Ϣ
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
