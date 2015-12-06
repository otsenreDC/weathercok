package io.bananalabs.weathercok;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.bananalabs.weathercok.broadcast.WindReceiver;
import io.bananalabs.weathercok.models.Vane;
import io.bananalabs.weathercok.service.ForecastService;
import io.bananalabs.weathercok.views.CompassView;
import io.bananalabs.weathercok.views.PointerView;


public class WindActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new VaneFragment())
                    .commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VaneFragment.REQUEST_RESOLVE_ERROR) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            VaneFragment placeholderFragment = (VaneFragment) fragmentManager.findFragmentById(R.id.container);
            placeholderFragment.mResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!placeholderFragment.mGoogleApiClient.isConnecting() &&
                        !placeholderFragment.mGoogleApiClient.isConnected()) {
                    placeholderFragment.mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wind, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class VaneFragment extends Fragment
            implements Vane.VaneListener, SensorEventListener, WindReceiver.WindReceiverListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final String LOG_TAG = this.getClass().getSimpleName();

        private final String MPS = "mps";
        private final String KPH = "kph";
        private final String FPS = "fps";
        private final String MPH = "mph";
        private final String KNOTS = "kno";

        private String mUnitStr = "";
        private double mMultiplier = 0;

        private static final int REQUEST_RESOLVE_ERROR = 1001;

        private Toolbar mToolbar;
        private TextView speedTextView;
        private ImageButton updateInfoButton;
        private Vane vane;

        private SensorManager sensorManager;
        private Sensor mOrientationSensor;

        private CompassView mCompassView;
        //        private ArrowView mArrowView;
        private PointerView mPointerView;

        public GoogleApiClient mGoogleApiClient;
        public boolean mResolvingError;
        private Location mLocation;

        private WindReceiver windReceiver;

        public VaneFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_wind, container, false);

            this.mCompassView = (CompassView) rootView.findViewById(R.id.compass_view_heading);
            this.mPointerView = (PointerView) rootView.findViewById(R.id.pointer_view_vane);

            this.vane = new Vane(this);
            this.speedTextView = (TextView) rootView.findViewById(R.id.text_view_speed);
            this.updateInfoButton = (ImageButton) rootView.findViewById(R.id.button_update_info);
            this.updateInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Location location = getLocation();
                    if (location != null) {
                        speedTextView.setText(null);
                        speedTextView.setHint(getResources().getString(R.string.wind_speed_refreshing));
                        vane.reset();
                        vane.fetchForecast(getActivity(), location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.msg_location_not_availble), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            this.updateInfoButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP: {
                            view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate));
                            break;
                        }
                    }
                    return false;
                }
            });


            this.sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            this.mOrientationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (mOrientationSensor == null) {
                Toast.makeText(getActivity(), "This app cannot run on this device.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }

            this.mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            if (this.mToolbar != null) {
                ((AppCompatActivity) getActivity()).setSupportActionBar(this.mToolbar);
            }

            this.mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();


            this.windReceiver = new WindReceiver(this);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();

            if (!mResolvingError) {
                this.mGoogleApiClient.connect();
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
            getActivity().registerReceiver(this.windReceiver, new IntentFilter(ForecastService.BROADCAST_ACTION_FORECAST));

            SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            this.mUnitStr = preferenceManager.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_default));
            setMultiplier(this.mUnitStr);

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

        @Override
        public void onPause() {
            super.onPause();
            sensorManager.unregisterListener(this);
            getActivity().unregisterReceiver(this.windReceiver);
        }

        @Override
        public void onStop() {
            this.mGoogleApiClient.disconnect();
            super.onStop();
        }

        @Override
        public void onWindFetched(Vane vane) {
            if (speedTextView != null) {
                speedTextView.setText(String.format(getActivity().getString(R.string.wind_speed_label), vane.getSpeed() * this.mMultiplier, this.mUnitStr, vane.getDirectionAsString()));
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            this.mCompassView.setHeading(360 - sensorEvent.values[0]);
            if (this.vane != null) {
                this.mPointerView.setRotation(-sensorEvent.values[0] + this.vane.getDirection().floatValue());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }


        // ConnectionCallbacks
        @Override
        public void onConnected(Bundle bundle) {
            this.setLocation(LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient));
        }

        @Override
        public void onConnectionSuspended(int i) {

        }


        // OnConnectionFailedListener
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (this.mResolvingError) {
                return;
            } else if (connectionResult.hasResolution()) {
                try {
                    this.mResolvingError = true;
                    connectionResult.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    this.mGoogleApiClient.connect();
                }
            } else {
                this.mResolvingError = false;
            }
        }

        @Override
        public void onWindDataReceived(Double speed, Double direction) {

            this.vane.setDirection(direction);
            this.vane.setSpeed(direction);

            if (speedTextView != null) {
                speedTextView.setText(String.format(getActivity().getString(R.string.wind_speed_label), speed * this.mMultiplier, this.mUnitStr, this.vane.getDirectionAsString()));
            }
        }

        // Accessors
        private void setLocation(Location mLocation) {
            this.mLocation = mLocation;
            if (this.mLocation != null)
                if (this.vane != null) {
                    this.vane.fetchForecast(getActivity(), this.mLocation.getLatitude(), this.mLocation.getLongitude());
                }
        }

        private Location getLocation() {
            return this.mLocation;
        }

    }


}
