package io.bananalabs.weathercok;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class WindActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new VaneFragment())
                    .commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VaneFragment.REQUEST_RESOLVE_ERROR) {
            FragmentManager fragmentManager = getFragmentManager();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wind, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class VaneFragment extends Fragment implements Vane.VaneListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final String LOG_TAG = this.getClass().getSimpleName();

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
                        vane.fetchForecast(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.msg_location_not_availble), Toast.LENGTH_SHORT).show();
                    }
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
                ((ActionBarActivity) getActivity()).setSupportActionBar(this.mToolbar);
            }

            this.mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

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
        }

        @Override
        public void onPause() {
            super.onPause();
            sensorManager.unregisterListener(this);
        }

        @Override
        public void onStop() {
            this.mGoogleApiClient.disconnect();
            super.onStop();
        }

        @Override
        public void onWindFetched(Vane vane) {
            if (speedTextView != null) {
                speedTextView.setText(String.format(getActivity().getString(R.string.wind_speed_label), vane.getSpeed()));
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
                Log.e(LOG_TAG, "GooglePlay service error");
            }
        }


        // Accessors
        private void setLocation(Location mLocation) {
            this.mLocation = mLocation;
            if (this.vane != null) {
                this.vane.fetchForecast(this.mLocation.getLatitude(), this.mLocation.getLongitude());
            }
        }

        private Location getLocation() {
            return this.mLocation;
        }

    }
}
