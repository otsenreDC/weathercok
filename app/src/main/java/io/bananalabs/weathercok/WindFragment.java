package io.bananalabs.weathercok;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.bananalabs.weathercok.models.Vane;
import io.bananalabs.weathercok.views.CompassView;
import io.bananalabs.weathercok.views.PointerView;

/**
 * Created by EDC on 12/8/15.
 */
public class WindFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private final String MPS = "mps";
    private final String KPH = "kph";
    private final String FPS = "fps";
    private final String MPH = "mph";
    private final String KNOTS = "knots";

    private String mUnitStr = "";
    private double mMultiplier = 0;

    private TextView speedTextView;
    private CompassView mCompassView;
    private PointerView mPointerView;

    private Float heading = (float)0;
    private Double speed = (double)0;
    private Double direction = (double)0;

    public WindFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wind, container, false);

        this.mCompassView = (CompassView) rootView.findViewById(R.id.compass_view_heading);
        this.mPointerView = (PointerView) rootView.findViewById(R.id.pointer_view_vane);

        this.speedTextView = (TextView) rootView.findViewById(R.id.text_view_speed);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        this.mUnitStr = preferenceManager.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
        setMultiplier(this.mUnitStr);

        updateViews();

    }

    private void setMultiplier(String unit) {
        switch (unit) {
            case MPS:
                this.mMultiplier = 1;
                break;
            case KPH:
                this.mMultiplier = 3.6;
                break;
            case FPS:
                this.mMultiplier = 3.28084;
                break;
            case MPH:
                this.mMultiplier = 2.23694;
                break;
            case KNOTS:
                this.mMultiplier = 1.94384;
                break;
            default:
                this.mMultiplier = 0;
                break;
        }
    }

    public void updateHeading(Float heading) {
        this.heading = heading;
        this.mCompassView.setHeading(heading);
        this.mPointerView.setRotation(heading + direction.floatValue());
    }

    public void updateFragment(Double speed, Double direction) {
        this.speed = speed;
        this.direction = direction;

        updateViews();
    }

    private void updateViews() {
        Vane vane  = new Vane(speed, direction, null);
        this.speedTextView.setText(String.format(getActivity().getString(R.string.wind_speed_label), vane.getSpeed() * this.mMultiplier, this.mUnitStr, vane.getDirectionAsString()));
    }
}