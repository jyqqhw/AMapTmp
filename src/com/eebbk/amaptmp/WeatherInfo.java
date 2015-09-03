package com.eebbk.amaptmp;

import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.LocationManagerProxy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherInfo extends Activity implements AMapLocalWeatherListener{

	private TextView mShowWeather;
	private LocationManagerProxy mLocationManagerProxy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);

		init();

	}

	private void init(){
		mShowWeather = (TextView) findViewById(R.id.tv_weather_show);

		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_LIVE, this);
	}

	@Override
	public void onWeatherForecaseSearched(AMapLocalWeatherForecast arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWeatherLiveSearched(AMapLocalWeatherLive arg0) {
		if(arg0!=null && arg0.getAMapException().getErrorCode() == 0){ 
			String city = arg0.getCity();
			//城市        
			String weather = arg0.getWeather();
			//天气情况        
			String windDir = arg0.getWindDir();
			//风向        
			String windPower = arg0.getWindPower();
			//风力        
			String humidity = arg0.getHumidity();
			//空气湿度        
			String reportTime = arg0.getReportTime();
			//数据发布时间
			mShowWeather.setText("城市："+city+"\n"
					+"天气情况 ："+weather+"\n"
					+"风向："+windDir+"\n"+"风力："+windPower+"\n"
					+"空气湿度："+humidity+"\n"
					+"数据发布时间："+reportTime+"\n");
		}
		else{        
			// 获取天气预报失败        
			Toast.makeText(this,"获取天气预报失败:"+ arg0.getAMapException().getErrorMessage(), Toast.LENGTH_SHORT).show();    }
	}

}



