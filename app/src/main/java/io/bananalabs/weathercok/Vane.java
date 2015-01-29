package io.bananalabs.weathercok;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by EDC on 1/24/15.
 */
public class Vane {

    private final String PROPERTY_WIND = "wind";
    private final String PROPERTY_SPEED = "speed";
    private final String PROPERTY_DIRECTION = "deg";

    private final String LOG_TAG = this.getClass().getSimpleName();

    interface VaneListener {
        public void onWindFetched(Vane vane);
    }

    /**
     * Vane's changes listener
     */
    private VaneListener vaneListener;

    /**
     * Speed in knots
     */
    private Double speed;
    /**
     * Direction in degrees
     */
    private Double direction;

    public Vane(VaneListener listener) {
        this.speed = Double.valueOf(0);
        this.direction = Double.valueOf(0);
        this.vaneListener = listener;
    }

    public Vane(Double speed, Double direction, VaneListener listener) {
        this.speed = speed;
        this.direction = direction;
        this.vaneListener = listener;
    }

    public void fetchForecast() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                String forecastJsonStr = null;

                String format = "json";
                String units = "metric";
                String longitude = "18.463806";
                String latitude = "-69.965299";

                try {
                    final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
                    final String FORMAT_PARAM = "mode";
                    final String UNITS_PARAM = "units";
                    final String LON_PARAM = "lon";
                    final String LAT_PARAM = "lat";

                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(LON_PARAM, longitude)
                            .appendQueryParameter(LAT_PARAM, latitude)
                            .build();

                    URL url = new URL(builtUri.toString());

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    if (inputStream == null) return null;

                    StringBuffer buffer = new StringBuffer();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                    }

                    if (buffer.length() == 0) return null;

                    forecastJsonStr = buffer.toString();

                } catch (IOException ioe) {
                    Log.getStackTraceString(ioe);
                    Log.e(LOG_TAG, ioe.getLocalizedMessage());
                    return null;
                }

                Log.d(LOG_TAG, forecastJsonStr);

                extractWindFromForecast(forecastJsonStr);

                return forecastJsonStr;
            }

            @Override
            protected void onPostExecute(String forecast) {
                super.onPostExecute(forecast);

                publishResults();
            }
        }.execute();
    }

    private void extractWindFromForecast(String json) {

        Double speed = new Double(0);
        Double direction = new Double(0);

        if (json != null) {
            try {
                JSONObject forecastObject = new JSONObject(json);
                if (forecastObject.has(PROPERTY_WIND)) {
                    JSONObject windObject = forecastObject.getJSONObject(PROPERTY_WIND);
                    if (windObject.has(PROPERTY_SPEED))
                        speed = windObject.getDouble(PROPERTY_SPEED);
                    if (windObject.has(PROPERTY_DIRECTION))
                        direction = windObject.getDouble(PROPERTY_DIRECTION);
                }
            } catch (JSONException je) {
                Log.getStackTraceString(je);
                Log.e(LOG_TAG, je.getLocalizedMessage());
            }
        }

        this.setDirection(direction);
        this.setSpeed(speed);

    }

    private void publishResults() {
        vaneListener.onWindFetched(this);
    }


    public Double getSpeed() {
        return this.speed;
    }

    public Double getDirection() {
        return this.direction;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void setDirection(Double direction) {
        this.direction = direction;
    }

}
