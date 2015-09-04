package com.eebbk.amaptmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
	/////////�������
	private TextView mWeather;//���ֻ���ʾ
	//��ǰ����
	private TextView mTvCurrentCondition,mTvCurrentTmp;
	private ImageView mIvCurrentIcon;
	//δ�����������
	private TextView mTvForecOneTmp,mTvForecTwoTmp,mTvForecThreeTmp;//������¶���ʾ
	private TextView mTvForecOneWeek,mTvForecTwoWeek,mTvForecThreeWeek;//���������
	private ImageView mIvForecOneIcon,mIvForecTwoIcon,mIvForecThreeIcon;//���������ͼ��

	//���м���ID��Ӧ��
	private String mJsonTable;

	//���������
	private Calendar mCalendar;


	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				//�õ�������������
				SerializableMap data = (SerializableMap) msg.getData().getSerializable("maps");
				//����UI��������ֵ�����Ϳ��ӻ�����
				changeUIWithNewData(data.getMaps());

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
		//		AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.t100);
		//		Bitmap mMap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
		//		BitmapFactory.decodeResource(getResources(), R.raw.t100);

		init();
	}

	/**     * ��ʼ����λ     */    
	private void init() {

		//�����������ʾ����
		mCalendar = Calendar.getInstance();
		
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
				try {
					InputStream is = getResources().getAssets().open("100.png");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				//				Intent intent = new Intent(MainActivity.this, WeatherInfo.class);
				//				startActivity(intent);

			}
		});

		///////////////////���кͳ���ID��Ӧ��
		mJsonTable = getJsonTable();

		/*************************��ʼ������************************/
		//���ֻ���������
		mWeather = (TextView) findViewById(R.id.tv_weather_show);

		//��ǰ�������ӻ�����
		mTvCurrentCondition = (TextView) findViewById(R.id.tv_current_condition);
		mTvCurrentTmp = (TextView) findViewById(R.id.tv_current_tmp);
		mIvCurrentIcon = (ImageView) findViewById(R.id.iv_current_icon);
		//δ��������������ӻ�����
		mTvForecOneTmp = (TextView) findViewById(R.id.tv_forecast_one_tmp);
		mTvForecOneWeek = (TextView) findViewById(R.id.tv_forecast_one_week);
		mIvForecOneIcon = (ImageView) findViewById(R.id.iv_forecast_one_icon);

		mTvForecTwoTmp = (TextView) findViewById(R.id.tv_forecast_two_tmp);
		mTvForecTwoWeek = (TextView) findViewById(R.id.tv_forecast_two_week);
		mIvForecTwoIcon = (ImageView) findViewById(R.id.iv_forecast_two_icon);

		mTvForecThreeTmp = (TextView) findViewById(R.id.tv_forecast_three_tmp);
		mTvForecThreeWeek = (TextView) findViewById(R.id.tv_forecast_three_week);
		mIvForecThreeIcon = (ImageView) findViewById(R.id.iv_forecast_three_icon);

	}

	//�û�ȡ���������ݸ���UI
	private void changeUIWithNewData(Map<String,String> maps){
		//��ֵ��������
		mWeather.setText(maps.get("details"));
		//���ӻ�������
		mTvCurrentCondition.setText(maps.get("now_txt"));   //����
		mIvCurrentIcon.setImageBitmap(getPictureById(maps.get("now_code")));
		//mIvCurrentIcon.setImageResource(R.drawable.ic_launcher);
		mTvCurrentTmp.setText(maps.get("now_tmp")+"��");

		
		//��һ��
		mIvForecOneIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_1_cond_code_d")));
		mTvForecOneTmp.setText(maps.get("daily_forecast_1_tmp_min")+"�㡫"+maps.get("daily_forecast_1_tmp_max")+"��");
		
		mCalendar.setTimeInMillis(System.currentTimeMillis()+24*3600*1000);
		mTvForecOneWeek.setText(showWeekByNumber(mCalendar.get(Calendar.DAY_OF_WEEK)));

		//�ڶ���
		mIvForecTwoIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_2_cond_code_d")));
		mTvForecTwoTmp.setText(maps.get("daily_forecast_2_tmp_min")+"�㡫"+maps.get("daily_forecast_2_tmp_max")+"��");
		
		mCalendar.setTimeInMillis(System.currentTimeMillis()+48*3600*1000);
		mTvForecTwoWeek.setText(showWeekByNumber(mCalendar.get(Calendar.DAY_OF_WEEK)));

		//������
		mIvForecThreeIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_3_cond_code_d")));
		mTvForecThreeTmp.setText(maps.get("daily_forecast_3_tmp_min")+"�㡫"+maps.get("daily_forecast_3_tmp_max")+"��");
		
		mCalendar.setTimeInMillis(System.currentTimeMillis()+72*3600*1000);
		mTvForecThreeWeek.setText(showWeekByNumber(mCalendar.get(Calendar.DAY_OF_WEEK)));

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
				String temp = request(httpUrl);
				//Map<String,String> mMaps = parseJsonData(temp);

				SerializableMap maps = new SerializableMap(parseJsonData(temp));
				Message msg = new Message();
				msg.what = 100;
				Bundle bundle = new Bundle();
				bundle.putSerializable("maps", maps);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		}).start();

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

	private Map<String,String> parseJsonData(String jsonData){
		StringBuilder sb = new StringBuilder();
		Map<String,String> mWeatherMap = new HashMap<String, String>();
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

			mWeatherMap.put("now_tmp", now_tmp);//ʵʱ����״��
			mWeatherMap.put("now_txt", now_txt);
			mWeatherMap.put("now_code", now_code);
			mWeatherMap.put("daily_forecast_1_tmp_min", daily_forecast_1_tmp_min);//���������״��
			mWeatherMap.put("daily_forecast_1_tmp_max", daily_forecast_1_tmp_max);
			mWeatherMap.put("daily_forecast_1_cond_txt_d", daily_forecast_1_cond_txt_d);
			mWeatherMap.put("daily_forecast_1_cond_code_d", daily_forecast_1_cond_code_d);
			mWeatherMap.put("daily_forecast_2_tmp_min", daily_forecast_2_tmp_min);//���������״��
			mWeatherMap.put("daily_forecast_2_tmp_max", daily_forecast_2_tmp_max);
			mWeatherMap.put("daily_forecast_2_cond_txt_d", daily_forecast_2_cond_txt_d);
			mWeatherMap.put("daily_forecast_2_cond_code_d", daily_forecast_2_cond_code_d);
			mWeatherMap.put("daily_forecast_3_tmp_min", daily_forecast_3_tmp_min);//����������״��
			mWeatherMap.put("daily_forecast_3_tmp_max", daily_forecast_3_tmp_max);
			mWeatherMap.put("daily_forecast_3_cond_txt_d", daily_forecast_3_cond_txt_d);
			mWeatherMap.put("daily_forecast_3_cond_code_d", daily_forecast_3_cond_code_d);
			mWeatherMap.put("details", sb.toString());
			return mWeatherMap;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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

	///����������õ���Ӧ����ͼƬ
	private Bitmap getPictureById(String code){
		Bitmap mBitmap = null;
		try {
			InputStream is = getResources().getAssets().open(code+".png");
			mBitmap = BitmapFactory.decodeStream(is);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mBitmap;
	}

	//����������ʾ����
	private String showWeekByNumber(int num){
		String result = null;
		switch(num){
		case 1:
			result = "������";
			break;
		case 2:
			result = "����һ";
			break;
		case 3:
			result = "���ڶ�";
			break;
		case 4:
			result = "������";
			break;
		case 5:
			result = "������";
			break;
		case 6:
			result = "������";
			break;
		case 7:
			result = "������";
			break;
		default:
			break;
		}

		return result;
	}


}




