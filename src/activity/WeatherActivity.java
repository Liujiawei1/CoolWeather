package activity;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
     
	private LinearLayout weatherInfoLayout;
	private TextView cityName;
	private TextView publishText;
	private TextView weatherDesp;
	private TextView temp1;
	private TextView temp2;
	private TextView currentDate;
	private Button switchCity;
	private Button refreshWeather;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.example.coolweather.R.layout.weather_layout);
		
		
		weatherInfoLayout=(LinearLayout)findViewById(com.example.coolweather.R.id.weather_info_layout);
		cityName=(TextView)findViewById(com.example.coolweather.R.id.city_name);
		publishText=(TextView)findViewById(com.example.coolweather.R.id.publish_text);
		weatherDesp=(TextView)findViewById(com.example.coolweather.R.id.weather_desp);
		temp1=(TextView)findViewById(com.example.coolweather.R.id.temp1);
		temp2=(TextView)findViewById(com.example.coolweather.R.id.temp2);
		currentDate=(TextView)findViewById(com.example.coolweather.R.id.current_date);
		
		String countyCode=getIntent().getStringExtra("county_code");//获取县级代号
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号就去查询天气
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}
		else{
			//没有县级代号就直接显示本地天气
			showWeather();
		}
		
		switchCity=(Button)findViewById(com.example.coolweather.R.id.switch_city);
		refreshWeather=(Button)findViewById(com.example.coolweather.R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	
	
	@Override
	public void onClick(View v){
		switch (v.getId()) {
		case com.example.coolweather.R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);//切换城市
			intent.putExtra("from_weather_activity",true);
			startActivity(intent);
			finish();
			break;

		case com.example.coolweather.R.id.refresh_weather:
			publishText.setText("同步中....");
			SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=pref.getString("weatherCode","");
			if(!TextUtils.isEmpty("weatherCode")){
				queryWeatherInfo(weatherCode);}//刷新天气
				break;
		default:
			break;
		}
		}
	
	
	private void queryWeatherCode(String countyCode){
		
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
		
	}
	
	
	
	private void queryWeatherInfo(String weatherCode){
		
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}
	
	
	//从服务器查询天气
	private void queryFromServer(final String address,final String type){
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			
			//解析服务器返回的天气代号
			public void onFinish(final String response) {
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						String[] array=response.split("\\|");
						if(array !=null && array.length==2){
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else  if("weatherCode".equals(type)){
					//调用handleWeatherResponse方法，解析服务器返回的JSON数据
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();//转到主线程显示天气信息，因为是UI操作，所以必须在主线程操作
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("同步失败");//转到主线程显示，因为是UI操作，所以必须在主线程操作
					}
				});
			}
			
			
		});
	}
	
	
	
	private void showWeather() {
		//从sharedPreference文件中读取存储的天气信息
		SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
		//将读取出来的信息都显示在界面
		cityName.setText(pref.getString("city_name",""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		weatherDesp.setText(pref.getString("weatherDesp", ""));
		publishText.setText("今天"+pref.getString("publish_time","")+"发布");
		currentDate.setText(pref.getString("current_time",""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);//启动后台更新服务
		startService(intent);
	}
	
	
}
