package io.bananalabs.weathercok.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ForecastService extends IntentService {

    public static final String BROADCAST_ACTION_FORECAST = "io.bananalabs.weathercok.broadcast.action.FORECAST";

    public static final String PROPERTY_WIND = "wind";
    public static final String PROPERTY_SPEED = "speed";
    public static final String PROPERTY_DIRECTION = "deg";

    private static final String LOG_TAG = ForecastService.class.getSimpleName();

    private static final String ACTION_FETCH_FORECAST = "io.bananalabs.weathercok.service.action.FETCH_FORECAST";

    // TODO: Rename parameters
    private static final String EXTRA_LATITUDE = "io.bananalabs.weathercok.service.extra.LATITUDE";
    private static final String EXTRA_LONGITUDE = "io.bananalabs.weathercok.service.extra.LONGITUDE";

    // TODO: Customize helper method
    public static void startActionFetchForecast(Context context, Double latitude, Double longitude) {
        Intent intent = new Intent(context, ForecastService.class);
        intent.setAction(ACTION_FETCH_FORECAST);
        intent.putExtra(EXTRA_LATITUDE, latitude);
        intent.putExtra(EXTRA_LONGITUDE, longitude);
        context.startService(intent);
    }

    public ForecastService() {
        super("ForecastService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_FORECAST.equals(action)) {
                final Double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
                final Double longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);
                handleActionFoo(latitude, longitude);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(Double latitude, Double longitude) {
        // TODO: Handle action Foo
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        String longitudeStr = longitude.toString();
        String latitudeStr = latitude.toString();

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String LON_PARAM = "lon";
            final String LAT_PARAM = "lat";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(LON_PARAM, longitudeStr)
                    .appendQueryParameter(LAT_PARAM, latitudeStr)
                    .build();

            URL url = new URL(builtUri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) return;

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) return;

            forecastJsonStr = buffer.toString();

        } catch (IOException ioe) {
//            Log.getStackTraceString(ioe);
            return;
        }

        extractWindFromForecast(forecastJsonStr);

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
//                Log.getStackTraceString(je);
            }
        }

        broadcastIntent(speed, direction);
    }

    private Intent broadcastIntent(Double speed, Double direction) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_FORECAST);
        intent.putExtra(PROPERTY_SPEED, speed);
        intent.putExtra(PROPERTY_DIRECTION, direction);

        sendBroadcast(intent);

        return intent;
    }
}
