package com.example.weatherforecast;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class WeatherDataDetails extends Activity {
	
	TextView cityName,temperature;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_data_details);
		
		cityName = (TextView)findViewById(R.id.textView1);
		temperature = (TextView)findViewById(R.id.textView2);
		 
		Intent i = getIntent();
		String cityname = i.getStringExtra("cityName");
		cityName.setText(cityname);
	    
		
		temperature.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//temperature.setText(text);
				
				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather_data_details, menu);
		return true;
	}

}
