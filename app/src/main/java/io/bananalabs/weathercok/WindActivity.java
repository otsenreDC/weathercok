package io.bananalabs.weathercok;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.bananalabs.weathercok.broadcast.WindReceiver;
import io.bananalabs.weathercok.models.Vane;
import io.bananalabs.weathercok.service.ForecastService;


public class WindActivity
        extends
        AppCompatActivity
        implements
        SensorEventListener,
        Vane.VaneListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WindReceiver.WindReceiverListener {

    public final int REQUEST_RESOLVE_ERROR = 1001;


    private ImageButton updateInfoButton;
    private Toolbar mToolbar;

    private SensorManager sensorManager;
    private Sensor mOrientationSensor;
    public GoogleApiClient mGoogleApiClient;
    public boolean mResolvingError;
    private Location mLocation;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private Vane vane;

    private WindReceiver windReceiver;

    private WindFragment windFragment;
    private MapActivityFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);

        this.vane = new Vane(this);
        this.windReceiver = new WindReceiver(this);

        this.windFragment = new WindFragment();
        this.mapFragment = new MapActivityFragment();

        this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.mOrientationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mOrientationSensor == null) {
            Toast.makeText(this, "This app cannot run on this device.", Toast.LENGTH_LONG).show();
            this.finish();
        }

        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        this.mPager = (ViewPager) findViewById(R.id.pager);
        this.mPagerAdapter = new ScreeSlidePagerAdapter(getSupportFragmentManager());
        this.mPager.setAdapter(this.mPagerAdapter);
        this.updateInfoButton = (ImageButton) findViewById(R.id.button_update_info);
        this.updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = mLocation;
                if (location != null) {
                    vane.fetchForecast(WindActivity.this, location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(WindActivity.this, WindActivity.this.getString(R.string.msg_location_not_availble), Toast.LENGTH_SHORT).show();
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

        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (this.mToolbar != null) {
            this.setSupportActionBar(this.mToolbar);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            this.mResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!this.mGoogleApiClient.isConnecting() &&
                        !this.mGoogleApiClient.isConnected()) {
                    this.mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!this.mResolvingError) {
            this.mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
        this.registerReceiver(this.windReceiver, new IntentFilter(ForecastService.BROADCAST_ACTION_FORECAST));
    }


    @Override
    public void onPause() {
        super.onPause();

        this.sensorManager.unregisterListener(this);
        this.unregisterReceiver(this.windReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();

        this.mGoogleApiClient.disconnect();
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (windFragment != null)
            windFragment.updateHeading(-sensorEvent.values[0]);
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
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                this.mGoogleApiClient.connect();
            }
        } else {
            this.mResolvingError = false;
        }
    }

    // Accessors
    private void setLocation(Location mLocation) {
        this.mLocation = mLocation;
        this.vane.fetchForecast(this, mLocation.getLatitude(), mLocation.getLatitude());
    }

    private Location getLocation() {
        return this.mLocation;
    }

    @Override
    public void onWindDataReceived(Double speed, Double direction) {
        if (windFragment != null) {
            windFragment.updateFragment(speed, direction);
        }
    }

    @Override
    public void onWindFetched(Vane vane) {

    }

    private class ScreeSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreeSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return windFragment;
                case 1:
                    return mapFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
