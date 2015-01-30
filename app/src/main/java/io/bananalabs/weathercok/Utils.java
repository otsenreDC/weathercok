package io.bananalabs.weathercok;

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

}
