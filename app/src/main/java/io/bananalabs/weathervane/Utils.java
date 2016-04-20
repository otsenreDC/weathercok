package io.bananalabs.weathervane;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by EDC on 1/29/15.
 */
public class Utils {

    public static void putDoubleInPreferences(Context context, Double value, String key)
    {
        SharedPreferences.Editor editor = Utils.getDefaultSharedPreferencesEditor(context);
        editor.putLong(key, value.longValue());
        editor.commit();
    }

    public static Double getDoubleInSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = Utils.getDefaultSharedPreferences(context);
        Long value = sharedPreferences.getLong(key, -1);

        return value.doubleValue();
    }

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    private static SharedPreferences.Editor getDefaultSharedPreferencesEditor(Context context) {
        return Utils.getDefaultSharedPreferences(context).edit();
    }

    private static final String MPS = "mps";
    private static final String KPH = "kph";
    private static final String FPS = "fps";
    private static final String MPH = "mph";
    private static final String KNOTS = "knots";

    public static double speedConversion (String unit, double speed) {
        double factor;
        switch (unit) {
            case MPS:
                factor = 1;
                break;
            case KPH:
                factor = 3.6;
                break;
            case FPS:
                factor = 3.28084;
                break;
            case MPH:
                factor = 2.23694;
                break;
            case KNOTS:
                factor = 1.94384;
                break;
            default:
                factor = 0;
                break;
        }
        return factor * speed;
    }

    public static String getUnit(Context context) {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferenceManager.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.pref_unit_default));
    }

}
