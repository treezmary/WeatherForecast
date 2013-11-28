package com.example.weatherforecast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

import com.example.weatherforecast.WeatherDataXmlParser.WeatherData;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String URL ="http://api.openweathermap.org/data/2.5/forecast/daily?mode=xml&q=";
    
    
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    // The user's current network preference setting.
    public static String sPref = null;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

	EditText city;
	Button result;
	ImageView bgImageView;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 // Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

		city = (EditText) findViewById(R.id.cityEditText);
		result = (Button) findViewById(R.id.okButton);
		bgImageView = (ImageView) findViewById(R.id.imageView);

		result.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			

				String tempStr = city.getText().toString();
				if (city.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(),
							"Enter a city name", Toast.LENGTH_LONG).show();
					return;
				} else {

					loadPage(tempStr);
				}
			}
		});
		

	}
	
	 // Refreshes the display if the network connection and the
    // pref settings allow it.
    @Override
    public void onStart() {
        super.onStart();

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Wi-Fi");

        updateConnectedFlags();

        // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
        // display. For example, if the user has set "Wi-Fi only" in prefs and the
        // device loses its Wi-Fi connection midway through the user using the app,
        // you don't want to refresh the display--this would force the display of
        // an error page instead of weather data.
        if (refreshDisplay) {
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }
    
 // Uses AsyncTask subclass to download the XML weatherdata.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
    private void loadPage(String cityName) {
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected || mobileConnected))) {
        	String URLWithCityName = URL+cityName;
            // AsyncTask subclass
            new DownloadXmlTask().execute(URLWithCityName);
        } else {
           Toast.makeText(getApplicationContext(), "Unable to download XML", Toast.LENGTH_LONG).show();
        }
    }

    
    
    
    // Implementation of AsyncTask used to download XML weatherdata.
    private class DownloadXmlTask extends AsyncTask<String, Void, List<WeatherData>> {

        @Override
        protected List<WeatherData> doInBackground(String... urls) {
        	List<WeatherData> weatherData = null;
            try {
            	weatherData = loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "IO Exception", Toast.LENGTH_SHORT).show();
            } catch (XmlPullParserException e) {
            	Toast.makeText(MainActivity.this, "XmlPullParserException", Toast.LENGTH_SHORT).show();
            }
            return weatherData;
        }

        @Override
        protected void onPostExecute(List<WeatherData> weatherData) {
 
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private List<WeatherData> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        WeatherDataXmlParser weatherDataXmlParser = new WeatherDataXmlParser();
        List<WeatherData> weatherData = null;
     
        try {
            stream = downloadUrl(urlString);
            weatherData = weatherDataXmlParser.parse(stream);
        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return weatherData;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }
    
    /**
    *
    * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
    * which indicates a connection change. It checks whether the type is TYPE_WIFI.
    * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
    * main activity accordingly.
    *
    */
   public class NetworkReceiver extends BroadcastReceiver {

       @Override
       public void onReceive(Context context, Intent intent) {
           ConnectivityManager connMgr =
                   (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

           // Checks the user prefs and the network connection. Based on the result, decides
           // whether
           // to refresh the display or keep the current display.
           // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
           if (WIFI.equals(sPref) && networkInfo != null
                   && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
               // If device has its Wi-Fi connection, sets refreshDisplay
               // to true. This causes the display to be refreshed when the user
               // returns to the app.
               refreshDisplay = true;
               Toast.makeText(context, "wifi conected", Toast.LENGTH_SHORT).show();

               // If the setting is ANY network and there is a network connection
               // (which by process of elimination would be mobile), sets refreshDisplay to true.
           } else if (ANY.equals(sPref) && networkInfo != null) {
               refreshDisplay = true;

               // Otherwise, the app can't download content--either because there is no network
               // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
               // is no Wi-Fi connection.
               // Sets refreshDisplay to false.
           } else {
               refreshDisplay = false;
               Toast.makeText(context, "lost connection", Toast.LENGTH_SHORT).show();
           }
       }
   }    


}
