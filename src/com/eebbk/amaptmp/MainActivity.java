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
	//位置相关
	private LocationManagerProxy mLocationManagerProxy;
	private Button mToWeather;
	private TextView mShowLocation;
	private TextView mShowStatus;
	private String mLocationCity = "长沙";
	private String mLocationID = "CN101281601";
	/////////天气相关
	private TextView mWeather;//数字化显示
	//当前天气
	private TextView mTvCurrentCondition,mTvCurrentTmp;
	private ImageView mIvCurrentIcon;
	//未来三天的天气
	private TextView mTvForecOneTmp,mTvForecTwoTmp,mTvForecThreeTmp;//三天的温度显示
	private TextView mTvForecOneWeek,mTvForecTwoWeek,mTvForecThreeWeek;//三天的星期
	private ImageView mIvForecOneIcon,mIvForecTwoIcon,mIvForecThreeIcon;//三天的天气图标

	//城市及其ID对应表
	private String mJsonTable;

	//获得日历项
	private Calendar mCalendar;


	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				//得到传过来的数据
				SerializableMap data = (SerializableMap) msg.getData().getSerializable("maps");
				//更改UI，包括数值天气和可视化天气
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

	/**     * 初始化定位     */    
	private void init() {

		//日历项，用来显示星期
		mCalendar = Calendar.getInstance();
		
		/*************************初始化定位************************/

		mShowLocation = (TextView) findViewById(R.id.tv_location_show);
		mShowStatus = (TextView) findViewById(R.id.tv_stutus_show);

		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		//此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，        
		//注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求        
		//在定位结束后，在合适的生命周期调用destroy()方法              
		//其中如果间隔时间为-1，则定位只定一次        
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

		///////////////////城市和城市ID对应表
		mJsonTable = getJsonTable();

		/*************************初始化天气************************/
		//数字化天气数据
		mWeather = (TextView) findViewById(R.id.tv_weather_show);

		//当前天气可视化数据
		mTvCurrentCondition = (TextView) findViewById(R.id.tv_current_condition);
		mTvCurrentTmp = (TextView) findViewById(R.id.tv_current_tmp);
		mIvCurrentIcon = (ImageView) findViewById(R.id.iv_current_icon);
		//未来三天的天气可视化数据
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

	//用获取的天气数据更新UI
	private void changeUIWithNewData(Map<String,String> maps){
		//数值化的天气
		mWeather.setText(maps.get("details"));
		//可视化的天气
		mTvCurrentCondition.setText(maps.get("now_txt"));   //今天
		mIvCurrentIcon.setImageBitmap(getPictureById(maps.get("now_code")));
		//mIvCurrentIcon.setImageResource(R.drawable.ic_launcher);
		mTvCurrentTmp.setText(maps.get("now_tmp")+"°");

		
		//第一天
		mIvForecOneIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_1_cond_code_d")));
		mTvForecOneTmp.setText(maps.get("daily_forecast_1_tmp_min")+"°～"+maps.get("daily_forecast_1_tmp_max")+"°");
		
		mCalendar.setTimeInMillis(System.currentTimeMillis()+24*3600*1000);
		mTvForecOneWeek.setText(showWeekByNumber(mCalendar.get(Calendar.DAY_OF_WEEK)));

		//第二天
		mIvForecTwoIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_2_cond_code_d")));
		mTvForecTwoTmp.setText(maps.get("daily_forecast_2_tmp_min")+"°～"+maps.get("daily_forecast_2_tmp_max")+"°");
		
		mCalendar.setTimeInMillis(System.currentTimeMillis()+48*3600*1000);
		mTvForecTwoWeek.setText(showWeekByNumber(mCalendar.get(Calendar.DAY_OF_WEEK)));

		//第三天
		mIvForecThreeIcon.setImageBitmap(getPictureById(maps.get("daily_forecast_3_cond_code_d")));
		mTvForecThreeTmp.setText(maps.get("daily_forecast_3_tmp_min")+"°～"+maps.get("daily_forecast_3_tmp_max")+"°");
		
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
			//获取位置信息            
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

	/********************获取天气相关的方法***************************/
	//开启一个线程来获取信息
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


	//根据url地址得到网页内容
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
			//整段数据就是一个json对象a
			JSONObject mJsonObject = new JSONObject(jsonData);
			//a对象中内含一个json数组b，b数组中内含一个json对象c
			JSONObject mJsonObject2 = mJsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0);
			/*实况天气*/
			JSONObject now = mJsonObject2.getJSONObject("now");
			String now_txt = now.getJSONObject("cond").getString("txt");//当前天气状况
			String now_code = now.getJSONObject("cond").getString("code");//当前天气状况代码
			String now_tmp = now.getString("tmp");//当前温度
			/*天气预报*/
			JSONArray daily_forecast = mJsonObject2.getJSONArray("daily_forecast");
			//明天的天气情况
			JSONObject daily_forecast_1_tmp = daily_forecast.getJSONObject(1).getJSONObject("tmp");//获取明天的温度
			String daily_forecast_1_tmp_min = daily_forecast_1_tmp.getString("min");//最高气温
			String daily_forecast_1_tmp_max = daily_forecast_1_tmp.getString("max");//最低气温
			JSONObject daily_forecast_1_cond = daily_forecast.getJSONObject(1).getJSONObject("cond");//获取明天的天气状况
			String daily_forecast_1_cond_txt_d = daily_forecast_1_cond.getString("txt_d");//白天的天气状况
			String daily_forecast_1_cond_code_d = daily_forecast_1_cond.getString("code_d");//白天的天气状况代码
			//后天的天气情况
			JSONObject daily_forecast_2_tmp = daily_forecast.getJSONObject(2).getJSONObject("tmp");//获取后天的温度
			String daily_forecast_2_tmp_min = daily_forecast_2_tmp.getString("min");//最高气温
			String daily_forecast_2_tmp_max = daily_forecast_2_tmp.getString("max");//最低气温
			JSONObject daily_forecast_2_cond = daily_forecast.getJSONObject(2).getJSONObject("cond");//获取后天的天气状况
			String daily_forecast_2_cond_txt_d = daily_forecast_2_cond.getString("txt_d");//白天的天气状况
			String daily_forecast_2_cond_code_d = daily_forecast_2_cond.getString("code_d");//白天的天气状况代码
			//大后天的天气情况
			JSONObject daily_forecast_3_tmp = daily_forecast.getJSONObject(3).getJSONObject("tmp");//获取大后天的温度
			String daily_forecast_3_tmp_min = daily_forecast_3_tmp.getString("min");//最高气温
			String daily_forecast_3_tmp_max = daily_forecast_3_tmp.getString("max");//最低气温
			JSONObject daily_forecast_3_cond = daily_forecast.getJSONObject(3).getJSONObject("cond");//获取大后天的天气状况
			String daily_forecast_3_cond_txt_d = daily_forecast_3_cond.getString("txt_d");//白天的天气状况
			String daily_forecast_3_cond_code_d = daily_forecast_3_cond.getString("code_d");//白天的天气状况代码
			sb.append("当前温度："+now_tmp+" 当前天气状况："+now_txt+" 天气状况代码："+now_code+"\n"
					+"未来三天的天气情况为："+"\n"
					+"明天  最高温度："+daily_forecast_1_tmp_min+" 最高温度："+daily_forecast_1_tmp_max+" 天气状况："+daily_forecast_1_cond_txt_d+" 天气状况代码："+daily_forecast_1_cond_code_d+"\n"
					+"后天  最高温度："+daily_forecast_2_tmp_min+" 最高温度："+daily_forecast_2_tmp_max+" 天气状况："+daily_forecast_2_cond_txt_d+" 天气状况代码："+daily_forecast_2_cond_code_d+"\n"
					+"大后天  最高温度："+daily_forecast_3_tmp_min+" 最高温度："+daily_forecast_3_tmp_max+" 天气状况："+daily_forecast_3_cond_txt_d+" 天气状况代码："+daily_forecast_3_cond_code_d+"\n");

			mWeatherMap.put("now_tmp", now_tmp);//实时天气状况
			mWeatherMap.put("now_txt", now_txt);
			mWeatherMap.put("now_code", now_code);
			mWeatherMap.put("daily_forecast_1_tmp_min", daily_forecast_1_tmp_min);//明天的天气状况
			mWeatherMap.put("daily_forecast_1_tmp_max", daily_forecast_1_tmp_max);
			mWeatherMap.put("daily_forecast_1_cond_txt_d", daily_forecast_1_cond_txt_d);
			mWeatherMap.put("daily_forecast_1_cond_code_d", daily_forecast_1_cond_code_d);
			mWeatherMap.put("daily_forecast_2_tmp_min", daily_forecast_2_tmp_min);//后天的天气状况
			mWeatherMap.put("daily_forecast_2_tmp_max", daily_forecast_2_tmp_max);
			mWeatherMap.put("daily_forecast_2_cond_txt_d", daily_forecast_2_cond_txt_d);
			mWeatherMap.put("daily_forecast_2_cond_code_d", daily_forecast_2_cond_code_d);
			mWeatherMap.put("daily_forecast_3_tmp_min", daily_forecast_3_tmp_min);//大后天的天气状况
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


	//解析json数据，得到城市ID
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

	//从raw文件夹中获取json数据
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

	///根据天气码得到相应天气图片
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

	//根据数字显示星期
	private String showWeekByNumber(int num){
		String result = null;
		switch(num){
		case 1:
			result = "星期日";
			break;
		case 2:
			result = "星期一";
			break;
		case 3:
			result = "星期二";
			break;
		case 4:
			result = "星期三";
			break;
		case 5:
			result = "星期四";
			break;
		case 6:
			result = "星期五";
			break;
		case 7:
			result = "星期六";
			break;
		default:
			break;
		}

		return result;
	}


}




