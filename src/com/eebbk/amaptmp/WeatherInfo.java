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
			//����        
			String weather = arg0.getWeather();
			//�������        
			String windDir = arg0.getWindDir();
			//����        
			String windPower = arg0.getWindPower();
			//����        
			String humidity = arg0.getHumidity();
			//����ʪ��        
			String reportTime = arg0.getReportTime();
			//���ݷ���ʱ��
			mShowWeather.setText("���У�"+city+"\n"
					+"������� ��"+weather+"\n"
					+"����"+windDir+"\n"+"������"+windPower+"\n"
					+"����ʪ�ȣ�"+humidity+"\n"
					+"���ݷ���ʱ�䣺"+reportTime+"\n");
		}
		else{        
			// ��ȡ����Ԥ��ʧ��        
			Toast.makeText(this,"��ȡ����Ԥ��ʧ��:"+ arg0.getAMapException().getErrorMessage(), Toast.LENGTH_SHORT).show();    }
	}

}



