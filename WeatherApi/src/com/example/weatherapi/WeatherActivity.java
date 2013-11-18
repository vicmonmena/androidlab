package com.example.weatherapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Este ejemplo muestra como realizar una llamada a un API en Internet,
 * parsear el resultado en JSon,
 * y usar una AsyncTask para realizar dichas tareas sin bloquear el hilo de ejecucion
 * principal de la aplicacion.
 *
 */
public class WeatherActivity extends Activity {

	private static final String TAG = WeatherActivity.class.getName();
	
	private static final String WEATHER_URL = "http://weather.yahooapis.com/forecastjson?w=";
	private static final String MADRID_CODE = "766273";

	private static final String LOCATION_NAME = "location";
	private static final String CITY_NAME = "city";
	private static final String CONDITION_NAME = "condition";
	private static final String TEMPERATURE_NAME = "temperature";
	private static final String FORECAST_NAME = "forecast";
	private static final String DAY_NAME = "day";
	private static final String HIGH_TEMPERATURE_NAME = "high_temperature";
	private static final String LOW_TEMPERATURE_NAME = "low_temperature";

	// --------------- For XML parsing ---------------
	private static final String WEATHER_RSS = "http://weather.yahooapis.com/forecastrss?w=";
	
	private static final String TEMP_NAME = "temp";
	private static final String DATE_NAME = "date";
	
	private static final String HIGH_TEMP_NAME = "high";
	private static final String LOW_TEMP_NAME = "low";
	private static final String ANDALUCIA_WOEID_CODES[] = {"752212", "755404",
		"758007", "761766", "762399", "762761", "766356", "774508"};
	
	// --------------- --------------- ---------------
	
	private static final String TODAY = "Today";
	private static final String TOMORROW = "Tomorrow";

	private TextView mCity;
	private TextView mToday;
	private TextView mTomorrow;

	/**
	 * Guarda la informacion relevante del servicio
	 *
	 */
	private class WeatherInfo {
		String city;
		int temperatureNow;
		int lowTemperature;
		int highTemperature;
		int lowTemperatureTomorrow;
		int highTemperatureTomorrow;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCity = (TextView) findViewById(R.id.city);
		mToday = (TextView) findViewById(R.id.today);
		mTomorrow = (TextView) findViewById(R.id.tomorrow);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_launch:
				int itemPos = ((Spinner) findViewById(R.id.spinner_cities)).getSelectedItemPosition();
				launch(ANDALUCIA_WOEID_CODES[itemPos]);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void launch(String woeid_code){
		try {
			new WeatherAsyncTask().execute(woeid_code);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Toast.makeText(WeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * AsyncTask que se encarga de hacer la llamada HTTP al API de Yahoo Weather
	 * y parsear el resultado
	 * 
	 * Recibe un codigo de la ciudad de la que se va a solicitar el tiempo y devuelve un 
	 * objeto WeatherInfo con la informacion relevante.
	 *
	 */
	private class WeatherAsyncTask extends AsyncTask<String, Void, WeatherInfo>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// Muestra la progressbar en vez del spinner mientras recupera la información del servidor.
			findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
			findViewById(R.id.spinner_cities).setVisibility(View.GONE);
			findViewById(R.id.linear_info).setVisibility(View.GONE);
		}
		
		@Override
		protected WeatherInfo doInBackground(String... params) {
			String code = params[0];
			if (TextUtils.isEmpty(code))
				throw new IllegalArgumentException("Code cannot be empty");
			
			URL url = null;
			HttpURLConnection connection = null;
			
			try {

				//Construimos la URL y realizamos la llamada
				url = new URL(WEATHER_RSS + code);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				InputStream is = connection.getInputStream();
				
				//Parseamos la respuesta Json
				// -- WeatherInfo info = readWeatherInfo(is);
				
				//Parseamos la respuesta XML
				WeatherInfo info = readXMLWeatherInfo(is);
				
				return info;

			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(WeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				if (connection != null)
					connection.disconnect();
			}
			
			// Retardo para que se muestre la progressbar
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(WeatherInfo result) {
			super.onPostExecute(result);
			// Oculta la barra y vuelve a habilitar el spinner
			findViewById(R.id.progressBar1).setVisibility(View.GONE);
			findViewById(R.id.spinner_cities).setVisibility(View.VISIBLE);
			findViewById(R.id.linear_info).setVisibility(View.VISIBLE);
			showResult(result);
		}
		

		/**
		 * Parsea la respuesta en JSon a partir de la informacion del servicio
		 * 
		 * @param is
		 * @return
		 */
		private WeatherInfo readXMLWeatherInfo(InputStream is){
			
			WeatherInfo info = new WeatherInfo();
			
			try {
				
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(is,null);
				
				int eventType = parser.getEventType();
				
				while (eventType != XmlPullParser.END_DOCUMENT) {
					
					if (eventType == XmlPullParser.START_TAG) {
						
						if (parser.getName().equals(LOCATION_NAME)) {
							info.city = parser.getAttributeValue(null, CITY_NAME);
						}
						
						if (parser.getName().equals(CONDITION_NAME)) {
							info.temperatureNow = 
								Integer.parseInt(parser.getAttributeValue(null, TEMP_NAME));
						}
						
						if (parser.getName().equals(FORECAST_NAME)) {
							
							// Today
							info.lowTemperature = Integer.parseInt(
								parser.getAttributeValue(null, LOW_TEMP_NAME));
							info.highTemperature = Integer.parseInt(
								parser.getAttributeValue(null, HIGH_TEMP_NAME));
							
							// Tomorrow
							parser.nextTag();
							
							if (parser.getName().equals(FORECAST_NAME)) {
								info.lowTemperatureTomorrow = Integer.parseInt(
									parser.getAttributeValue(null, LOW_TEMP_NAME));
								info.highTemperatureTomorrow = Integer.parseInt(
									parser.getAttributeValue(null, HIGH_TEMP_NAME));
							}
						}
					}
					
					eventType = parser.next();
				}
				
			} catch (Exception e) {
				Log.i(TAG, "Exception parsing XML RSS...");
			}
			
			return info;
		}

		/**
		 * Parsea la respuesta en JSon a partir de la informacion del servicio
		 * 
		 * @param is
		 * @return
		 */
		private WeatherInfo readWeatherInfo(InputStream is){
			if (is == null)
				return null;

			WeatherInfo info = new WeatherInfo();
			JsonReader reader = null;

			try {

				reader = new JsonReader(new InputStreamReader(is));
				reader.beginObject();

				while (reader.hasNext()){

					if (isCancelled()) break;		//Comprobacion de si ha sido cancelada

					String name = reader.nextName();
					if (name.equals(LOCATION_NAME)){			//Location

						reader.beginObject();
						while (reader.hasNext()){
							String name2 = reader.nextName();
							if (name2.equals(CITY_NAME)){
								info.city = reader.nextString();
							} else reader.skipValue();
						}
						reader.endObject();

					} else if (name.equals(CONDITION_NAME)){	//Condition

						reader.beginObject();
						while (reader.hasNext()){
							String name2 = reader.nextName();
							if (name2.equals(TEMPERATURE_NAME)){
								info.temperatureNow = reader.nextInt();
							} else reader.skipValue();
						}
						reader.endObject();

					} else if (name.equals(FORECAST_NAME)){		//Forecast

						reader.beginArray();
						while (reader.hasNext()){

							String day = null;
							int high = -111;
							int low = -111;


							reader.beginObject();
							while (reader.hasNext()){
								String name2 = reader.nextName();
								if (name2.equals(DAY_NAME)){
									day = reader.nextString();
								} else if (name2.equals(HIGH_TEMPERATURE_NAME)){
									high = reader.nextInt();
								}  else if (name2.equals(LOW_TEMPERATURE_NAME)){
									low = reader.nextInt();
								} else reader.skipValue();
							}
							reader.endObject();
							
							if (day.equals(TODAY)){
								info.highTemperature = high;
								info.lowTemperature = low;
							} else if (day.equals(TOMORROW)){
								info.highTemperatureTomorrow = high;
								info.lowTemperatureTomorrow = low;
							}
						}
						reader.endArray();

					} else reader.skipValue();
				}
				reader.endObject();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return info;
		}
	}

	private void showResult(WeatherInfo info){
		mCity.setText("Temperature in " + info.city);
		mToday.setText("Today: " + info.temperatureNow + " F (min: " + info.lowTemperature + " F / max: " + info.highTemperature + " F).");
		mTomorrow.setText("Tomorrow: min: " + info.lowTemperatureTomorrow + " F / max: " + info.highTemperatureTomorrow + " F.");
	}


}
