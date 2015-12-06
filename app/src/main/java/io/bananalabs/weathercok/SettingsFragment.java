package io.bananalabs.weathercok;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by EDC on 12/6/15.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
