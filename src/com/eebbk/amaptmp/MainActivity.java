package com.eebbk.amaptmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

public class MainActivity extends Activity implements AMapLocationListener{
	//λ�����
	private LocationManagerProxy mLocationManagerProxy;
	private Button mToWeather;
	private TextView mShowLocation;
	private TextView mShowStatus;
	private String mLocationCity = "��ɳ";
	private String mLocationID = "CN101281601";
	//�������
	private String mJsonResult;
	private TextView mWeather;
	
	//���м���ID��Ӧ��
	private String mJsonTable;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				mWeather.setText(mJsonResult);
				break;
			case 200:
				getWeatherInfo();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.t100);
		Bitmap mMap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
		BitmapFactory.decodeResource(getResources(), R.raw.t100);
		init();
	}

	/**     * ��ʼ����λ     */    
	private void init() {
		/*************************��ʼ����λ************************/

		mShowLocation = (TextView) findViewById(R.id.tv_location_show);
		mShowStatus = (TextView) findViewById(R.id.tv_stutus_show);

		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		//�˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�        
		//ע�����ú��ʵĶ�λʱ��ļ���������ں���ʱ�����removeUpdates()������ȡ����λ����        
		//�ڶ�λ�������ں��ʵ��������ڵ���destroy()����              
		//����������ʱ��Ϊ-1����λֻ��һ��        
		mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, 60*1000, 15, this);
		List<String> lists = mLocationManagerProxy.getAllProviders();
		StringBuilder sb = new StringBuilder();
		for(String str:lists){
			sb.append(str+"\n");
		}
		mShowStatus.setText(sb.toString());

		mToWeather = (Button) findViewById(R.id.btn_weather_go);
		mToWeather.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, WeatherInfo.class);
				startActivity(intent);

			}
		});
		
		///////////////////���кͳ���ID��Ӧ��
		mJsonTable = getJsonTable();
		
		/*************************��ʼ������************************/
		mWeather = (TextView) findViewById(R.id.tv_weather_show);


	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation arg0) {
		if(arg0 != null && arg0.getAMapException().getErrorCode() == 0){
			//��ȡλ����Ϣ            
			Double geoLat = arg0.getLatitude();            
			Double geoLng = arg0.getLongitude();
			String datas = arg0.getExtras().getString("desc");
			String city = arg0.getCity();
			mLocationCity = city.substring(0, city.length()-1);
			String district = arg0.getDistrict();
			handler.sendEmptyMessage(200);
			mShowLocation.setText(mLocationCity+"\n"+datas);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mLocationManagerProxy.destroy();

		if (mLocationManagerProxy != null) {   
			mLocationManagerProxy.removeUpdates(this);    
			mLocationManagerProxy.destory();    
		}   
		mLocationManagerProxy = null;
	}

	/********************��ȡ������صķ���***************************/
	//����һ���߳�����ȡ��Ϣ
	public void getWeatherInfo(){

		new Thread(new Runnable() {

			@Override
			public void run() {
				mLocationID = parseCityID(mJsonTable, mLocationCity);
				String httpUrl = "https://api.heweather.com/x3/weather?cityid="+mLocationID+
						"&key=59cc19b8b8ab45cca41ae89df97370df";
				mJsonResult = requestJson(httpUrl);


				System.out.println(mJsonResult);
				Log.i("aaa", mJsonResult);
				handler.sendEmptyMessage(100);
			}
		}).start();

	}

	public String requestJson(String httpUrl) {
		String result = null;
		String temp = request(httpUrl);
		result = parseJsonData(temp);

		return result;
	}

	//����url��ַ�õ���ҳ����
	public static String request(String httpUrl) {
		String result = null;
		try {
			HttpClient mClient = new DefaultHttpClient();
			HttpGet mHttpGet = new HttpGet(httpUrl);
			HttpResponse mResponse = mClient.execute(mHttpGet);
			if(mResponse.getStatusLine().getStatusCode() == 200){
				HttpEntity mHttpEntity = mResponse.getEntity();
				result = EntityUtils.toString(mHttpEntity, "utf-8");
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private String parseJsonData(String jsonData){
		StringBuilder sb = new StringBuilder();

		try {
			//�������ݾ���һ��json����a
			JSONObject mJsonObject = new JSONObject(jsonData);
			//a�������ں�һ��json����b��b�������ں�һ��json����c
			JSONObject mJsonObject2 = mJsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0);
			/*ʵ������*/
			JSONObject now = mJsonObject2.getJSONObject("now");
			String now_txt = now.getJSONObject("cond").getString("txt");//��ǰ����״��
			String now_code = now.getJSONObject("cond").getString("code");//��ǰ����״������
			String now_tmp = now.getString("tmp");//��ǰ�¶�
			/*����Ԥ��*/
			JSONArray daily_forecast = mJsonObject2.getJSONArray("daily_forecast");
			//������������
			JSONObject daily_forecast_1_tmp = daily_forecast.getJSONObject(1).getJSONObject("tmp");//��ȡ������¶�
			String daily_forecast_1_tmp_min = daily_forecast_1_tmp.getString("min");//�������
			String daily_forecast_1_tmp_max = daily_forecast_1_tmp.getString("max");//�������
			JSONObject daily_forecast_1_cond = daily_forecast.getJSONObject(1).getJSONObject("cond");//��ȡ���������״��
			String daily_forecast_1_cond_txt_d = daily_forecast_1_cond.getString("txt_d");//���������״��
			String daily_forecast_1_cond_code_d = daily_forecast_1_cond.getString("code_d");//���������״������
			//������������
			JSONObject daily_forecast_2_tmp = daily_forecast.getJSONObject(2).getJSONObject("tmp");//��ȡ������¶�
			String daily_forecast_2_tmp_min = daily_forecast_2_tmp.getString("min");//�������
			String daily_forecast_2_tmp_max = daily_forecast_2_tmp.getString("max");//�������
			JSONObject daily_forecast_2_cond = daily_forecast.getJSONObject(2).getJSONObject("cond");//��ȡ���������״��
			String daily_forecast_2_cond_txt_d = daily_forecast_2_cond.getString("txt_d");//���������״��
			String daily_forecast_2_cond_code_d = daily_forecast_2_cond.getString("code_d");//���������״������
			//�������������
			JSONObject daily_forecast_3_tmp = daily_forecast.getJSONObject(3).getJSONObject("tmp");//��ȡ�������¶�
			String daily_forecast_3_tmp_min = daily_forecast_3_tmp.getString("min");//�������
			String daily_forecast_3_tmp_max = daily_forecast_3_tmp.getString("max");//�������
			JSONObject daily_forecast_3_cond = daily_forecast.getJSONObject(3).getJSONObject("cond");//��ȡ����������״��
			String daily_forecast_3_cond_txt_d = daily_forecast_3_cond.getString("txt_d");//���������״��
			String daily_forecast_3_cond_code_d = daily_forecast_3_cond.getString("code_d");//���������״������
			sb.append("��ǰ�¶ȣ�"+now_tmp+" ��ǰ����״����"+now_txt+" ����״�����룺"+now_code+"\n"
					+"δ��������������Ϊ��"+"\n"
					+"����  ����¶ȣ�"+daily_forecast_1_tmp_min+" ����¶ȣ�"+daily_forecast_1_tmp_max+" ����״����"+daily_forecast_1_cond_txt_d+" ����״�����룺"+daily_forecast_1_cond_code_d+"\n"
					+"����  ����¶ȣ�"+daily_forecast_2_tmp_min+" ����¶ȣ�"+daily_forecast_2_tmp_max+" ����״����"+daily_forecast_2_cond_txt_d+" ����״�����룺"+daily_forecast_2_cond_code_d+"\n"
					+"�����  ����¶ȣ�"+daily_forecast_3_tmp_min+" ����¶ȣ�"+daily_forecast_3_tmp_max+" ����״����"+daily_forecast_3_cond_txt_d+" ����״�����룺"+daily_forecast_3_cond_code_d+"\n");
			return sb.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}


	//����json���ݣ��õ�����ID
	private String parseCityID(String jsonTable,String city){
		String cityID = "CN101281601";
		if(jsonTable != null){
			try {
				JSONObject mJsonObject = new JSONObject(jsonTable);
				cityID = mJsonObject.getString(city);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return cityID;
	}

	//��raw�ļ����л�ȡjson����
	private String getJsonTable(){
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			InputStream is = getResources().openRawResource(R.raw.city_code);
			br = new BufferedReader(new InputStreamReader(is,"gbk"));

			String str = null;

			while((str = br.readLine()) != null){
				sb.append(str);
			}
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			try {

				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}




}




