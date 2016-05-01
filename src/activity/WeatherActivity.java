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
		
		String countyCode=getIntent().getStringExtra("county_code");//��ȡ�ؼ�����
		if(!TextUtils.isEmpty(countyCode)){
			//���ؼ����ž�ȥ��ѯ����
			publishText.setText("ͬ����");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}
		else{
			//û���ؼ����ž�ֱ����ʾ��������
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
			Intent intent=new Intent(this,ChooseAreaActivity.class);//�л�����
			intent.putExtra("from_weather_activity",true);
			startActivity(intent);
			finish();
			break;

		case com.example.coolweather.R.id.refresh_weather:
			publishText.setText("ͬ����....");
			SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=pref.getString("weatherCode","");
			if(!TextUtils.isEmpty("weatherCode")){
				queryWeatherInfo(weatherCode);}//ˢ������
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
	
	
	//�ӷ�������ѯ����
	private void queryFromServer(final String address,final String type){
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			
			//�������������ص���������
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
					//����handleWeatherResponse�������������������ص�JSON����
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();//ת�����߳���ʾ������Ϣ����Ϊ��UI���������Ա��������̲߳���
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");//ת�����߳���ʾ����Ϊ��UI���������Ա��������̲߳���
					}
				});
			}
			
			
		});
	}
	
	
	
	private void showWeather() {
		//��sharedPreference�ļ��ж�ȡ�洢��������Ϣ
		SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
		//����ȡ��������Ϣ����ʾ�ڽ���
		cityName.setText(pref.getString("city_name",""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		weatherDesp.setText(pref.getString("weatherDesp", ""));
		publishText.setText("����"+pref.getString("publish_time","")+"����");
		currentDate.setText(pref.getString("current_time",""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);//������̨���·���
		startService(intent);
	}
	
	
}
