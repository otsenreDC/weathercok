package io.bananalabs.weathercok.models;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import io.bananalabs.weathercok.service.ForecastService;

/**
 * Created by EDC on 1/24/15.
 */
public class Vane {

    private final String PROPERTY_WIND = "wind";
    private final String PROPERTY_SPEED = "speed";
    private final String PROPERTY_DIRECTION = "deg";

    private final String LOG_TAG = this.getClass().getSimpleName();

    public interface VaneListener {
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

    public void fetchForecast(Context context, Double latitude, Double longitude) {

        ForecastService.startActionFetchForecast(context, latitude, longitude);

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

    public void reset() {
        this.setSpeed((double)0);
        this.setDirection((double)0);
    }


    public String getDirectionAsString() {

        Double direction = getDirection();
        String directionStr = "";

        if (direction != null) {
            if (direction ==  0 || direction == 360) { // Nortrh
                directionStr = "N";
            } else if (direction > 0 && direction < 90) { // North - East
                directionStr = "NE";
            } else if (direction == 90) { // East
                directionStr = "E";
            } else if (direction > 90 && direction < 180) { // South - East
                directionStr = "SE";
            } else if (direction == 180) { // South
                directionStr = "S";
            } else if (direction > 180 && direction < 200) { // South - West
                directionStr = "SW";
            } else if (direction == 270) { // West
                directionStr = "W";
            } else if (direction > 270 && direction < 360) { // North - West
                directionStr = "NW";
            }
        }

        return directionStr;
    }

}
