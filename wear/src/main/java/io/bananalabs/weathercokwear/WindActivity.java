package io.bananalabs.weathercokwear;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.Toast;

import io.bananalabs.common.views.CompassView;

public class WindActivity extends Activity implements
        SensorEventListener {

    private CompassView mCompassView;
    private SensorManager mSensorManager;
    private Sensor mRotationVector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) {
            Toast.makeText(this, "THIS APP CAN'T RUN ON THIS DEVICE", Toast.LENGTH_SHORT).show();
            finish();
        }

        setContentView(R.layout.activity_wind);

        configureSensor();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mCompassView = (CompassView) stub.findViewById(R.id.compass_view_heading);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }


    private boolean hasSystemFeature(String systemFeature) {
        PackageManager pkgManager = getPackageManager();
        return pkgManager.hasSystemFeature(systemFeature);
    }

    private void configureSensor() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
    }

    /**
     * Sensor Event Listener
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            if (mCompassView != null) {
                mCompassView.setRotation((float)(Math.asin(sensorEvent.values[2])*2.0 * 180 / Math.PI));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
