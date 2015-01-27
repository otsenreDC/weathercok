package io.bananalabs.weathercok;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class WindActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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
    public static class PlaceholderFragment extends Fragment implements Vane.VaneListener, SensorEventListener {

        private Toolbar mToolbar;
        private TextView speedTextView;
        private TextView directionTextView;
        private TextView orientationTextView;
        private Button updateInfoButton;
        private Vane vane;

        private SensorManager sensorManager;
        private Sensor mOrientationSensor;

        private CompassView mCompassView;
        private ArrowView mArrowView;
//        private ImageView mDuckImageView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_wind, container, false);

            this.mCompassView = (CompassView) rootView.findViewById(R.id.compass_view_heading);
            this.mArrowView = (ArrowView) rootView.findViewById(R.id.arrow_view_vane);

            this.vane = new Vane(this);
            this.vane.fetchForecast();
            this.speedTextView = (TextView) rootView.findViewById(R.id.text_view_speed);
            this.directionTextView = (TextView) rootView.findViewById(R.id.text_view_direction);
            this.orientationTextView = (TextView) rootView.findViewById(R.id.text_view_orientation);
            this.updateInfoButton = (Button) rootView.findViewById(R.id.button_update_info);
            this.updateInfoButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   vane.fetchForecast();
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
                ((ActionBarActivity)getActivity()).setSupportActionBar(this.mToolbar);
            }

            return rootView;
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
        public void onWindFetched(Vane vane) {
            if (speedTextView != null) {
                speedTextView.setText("" + vane.getSpeed());
            }
            if (directionTextView != null) {
                directionTextView.setText("" + vane.getDirection());
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            this.orientationTextView.setText("" + sensorEvent.values[0]);
            this.mCompassView.setHeading(sensorEvent.values[0]);
            if (this.vane != null) {
                this.mArrowView.setRotation( - sensorEvent.values[0] + this.vane.getDirection().floatValue());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    }
}
