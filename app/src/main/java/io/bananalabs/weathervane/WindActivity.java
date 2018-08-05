package io.bananalabs.weathervane;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.bananalabs.weathervane.broadcast.WindReceiver;
import io.bananalabs.weathervane.models.Vane;
import io.bananalabs.weathervane.service.ForecastService;


public class WindActivity
        extends
        AppCompatActivity
        implements
        SensorEventListener,
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
    private LocationManager locationManager;
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

        this.vane = new Vane();
        this.windReceiver = new WindReceiver(this);

        this.windFragment = new WindFragment();
        this.mapFragment = new MapActivityFragment();

        this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.mOrientationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mOrientationSensor == null) {
            Toast.makeText(this, "This app cannot run on this device.", Toast.LENGTH_LONG).show();
            this.finish();
        }

        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        this.mPager = (ViewPager) findViewById(R.id.pager);
        this.mPager.setPageTransformer(true, new DepthPageTransformer());
        this.mPagerAdapter = new ScreeSlidePagerAdapter(getSupportFragmentManager());
        this.mPager.setAdapter(this.mPagerAdapter);
        this.mPager.addOnPageChangeListener(onPageChangeListener);
        this.updateInfoButton = (ImageButton) findViewById(R.id.button_update_info);
        this.updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = mLocation;
                if (location != null) {
                    fetchForecast(location);
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

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.msg_gps_off))
                    .setPositiveButton(getString(R.string.btn_yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(getString(R.string.btn_no), null)
                    .show();
        }

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

    public void fetchForecast(Location location) {
        this.vane.fetchForecast(this, location.getLatitude(), location.getLatitude());
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

    // On Page Changed Listener
    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    updateInfoButton.animate().setInterpolator(new DecelerateInterpolator());
                    updateInfoButton.animate().translationY(0);
                    updateInfoButton.animate().setDuration(200L);
                    break;
                case 1:
                    updateInfoButton.animate().setInterpolator(new AccelerateDecelerateInterpolator());
                    updateInfoButton.animate().translationY(2510);
                    updateInfoButton.animate().setDuration(300L);
                    break;
            }
        }
    };

    // Accessors
    private void setLocation(Location mLocation) {
        this.mLocation = mLocation;
        if (mLocation != null) {
            fetchForecast(mLocation);
            if (mapFragment != null)
                mapFragment.setLatLng(mLocation.getLatitude(), mLocation.getLongitude());
        }
    }

    private Location getLocation() {
        return this.mLocation;
    }

    @Override
    public void onWindDataReceived(Double speed, Double direction) {
        if (windFragment != null) {
            windFragment.updateFragment(speed, direction);
        }
        if (mapFragment != null)
            mapFragment.setWindSpeedDirection(speed, direction);
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

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
