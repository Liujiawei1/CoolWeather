package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;
import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

/**
 * Created by dell on 2016/4/22.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dadaList=new ArrayList<String>();
    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表
    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private int currentLevel;//当前选中的级别
    
    private boolean isFromWeatherActivity;//用来判断是否从天气显示界面过来的

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
        
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean("city_selected",false) && !isFromWeatherActivity){
        	//返回值为true,表示已经选过城市而且不是从天气界面过来的，就跳转到显示天气的界面
        	Intent intent=new Intent(this,WeatherActivity.class);
        	startActivity(intent);
        	finish();
        	return;
        }
        
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dadaList);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(index);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(index);
                    queryCounties();
                }
                else if(currentLevel==LEVEL_COUNTY){
                //当前为县级就跳转到天气显示界面
                	String countyCode=countyList.get(index).getCountyNode();
                	Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                	//而且把县级代号传递过去
                	intent.putExtra("county_code", countyCode);
                	startActivity(intent);
                	finish();
                }
            }
        });

        queryProvinces();
    }

//查询所有的省，优先从数据库查询，如果没有查询到再去服务器查询

    private void queryProvinces(){
        provinceList=coolWeatherDB.loadProvince();
        if (provinceList.size()>0){
          dadaList.clear();
            for (Province province:provinceList){
                dadaList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else queryFromServer(null,"province");
    }

    //查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器查询
    private void queryCities(){
        cityList=coolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size()>0){
            dadaList.clear();
            for (City city:cityList){
                dadaList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else {
        	queryFromServer(selectedProvince.getProvinceCode(),"city");
        	}
    }


    //查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器查询
    private void queryCounties(){
        countyList=coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size()>0){
            dadaList.clear();
            for (County county:countyList){
                dadaList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else {
        	queryFromServer(selectedCity.getCityCode(),"county");
        }
    }


    //从服务器端查询数据
    private void queryFromServer(final String code,final String type){
     String address;
        if (!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else address="http://www.weather.com.cn/data/list3/city.xml";
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                	//涉及到UI操作，所以返回到主线程进行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
            	//同样涉及到UI操作
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     closeProgressDialog();
                     Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                 }
             });
            }
        });
    }


   //显示进度条
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }
     //关闭进度条
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

     //返回键
    @Override
    public void onBackPressed(){
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else if(isFromWeatherActivity){
        	//如果是从显示天气界面过来的，当按back键时，应该返回显示天气界面
        	Intent intent=new Intent(this,WeatherActivity.class);
        	startActivity(intent);
        	finish();
        }
        else{
            finish();
        }
    }


}

