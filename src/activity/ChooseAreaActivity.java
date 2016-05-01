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
    private List<Province> provinceList;//ʡ�б�
    private List<City> cityList;//���б�
    private List<County> countyList;//���б�
    private Province selectedProvince;//ѡ�е�ʡ��
    private City selectedCity;//ѡ�еĳ���
    private int currentLevel;//��ǰѡ�еļ���
    
    private boolean isFromWeatherActivity;//�����ж��Ƿ��������ʾ���������

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
        
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean("city_selected",false) && !isFromWeatherActivity){
        	//����ֵΪtrue,��ʾ�Ѿ�ѡ�����ж��Ҳ��Ǵ�������������ģ�����ת����ʾ�����Ľ���
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
                //��ǰΪ�ؼ�����ת��������ʾ����
                	String countyCode=countyList.get(index).getCountyNode();
                	Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                	//���Ұ��ؼ����Ŵ��ݹ�ȥ
                	intent.putExtra("county_code", countyCode);
                	startActivity(intent);
                	finish();
                }
            }
        });

        queryProvinces();
    }

//��ѯ���е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ��������ѯ

    private void queryProvinces(){
        provinceList=coolWeatherDB.loadProvince();
        if (provinceList.size()>0){
          dadaList.clear();
            for (Province province:provinceList){
                dadaList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("�й�");
            currentLevel=LEVEL_PROVINCE;
        }else queryFromServer(null,"province");
    }

    //��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ��������ѯ
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


    //��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ��������ѯ
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


    //�ӷ������˲�ѯ����
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
                	//�漰��UI���������Է��ص����߳̽���
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
            	//ͬ���漰��UI����
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     closeProgressDialog();
                     Toast.makeText(ChooseAreaActivity.this,"����ʧ��",Toast.LENGTH_SHORT).show();
                 }
             });
            }
        });
    }


   //��ʾ������
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("���ڼ���");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }
     //�رս�����
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

     //���ؼ�
    @Override
    public void onBackPressed(){
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else if(isFromWeatherActivity){
        	//����Ǵ���ʾ������������ģ�����back��ʱ��Ӧ�÷�����ʾ��������
        	Intent intent=new Intent(this,WeatherActivity.class);
        	startActivity(intent);
        	finish();
        }
        else{
            finish();
        }
    }


}

