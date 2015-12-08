package io.bananalabs.weathercok;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    private Toolbar mToolbar;
    private TextView speedTextView;
    private ImageButton updateInfoButton;
    private CompassView mCompassView;
    private PointerView mPointerView;

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
        
//        this.updateInfoButton = (ImageButton) rootView.findViewById(R.id.button_update_info);
//        this.updateInfoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Location location = new Location("");
//                if (location != null) {
//                    speedTextView.setText(null);
//                    speedTextView.setHint(getResources().getString(R.string.wind_speed_refreshing));
//                    vane.reset();
//                    vane.fetchForecast(getActivity(), location.getLatitude(), location.getLongitude());
//                } else {
//                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_location_not_availble), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        this.updateInfoButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_UP: {
//                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate));
//                        break;
//                    }
//                }
//                return false;
//            }
//        });

        this.mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (this.mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(this.mToolbar);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        this.mUnitStr = preferenceManager.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
        setMultiplier(this.mUnitStr);

        updateTextView();

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
        this.mCompassView.setHeading(heading);
    }

    public void updateFragment(Double speed, Double direction) {
        this.speed = speed;
        this.direction = direction;

        updateTextView();
    }

    private void updateTextView() {
        Vane vane  = new Vane(speed, direction, null);
        this.speedTextView.setText(String.format(getActivity().getString(R.string.wind_speed_label), vane.getSpeed() * this.mMultiplier, this.mUnitStr, vane.getDirectionAsString()));
    }
}