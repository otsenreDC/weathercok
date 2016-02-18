package io.bananalabs.weathercok.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import io.bananalabs.weathercok.broadcast.WindReceiver;

/**
 * Created by EDC on 1/23/16.
 */
public class ForecastWearService
        extends WearableListenerService
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WindReceiver.WindReceiverListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mConnectionError;

    private Location mLocation;
    private WindReceiver mForecastReceiver;


    public ForecastWearService() {
        super();
    }

    public static void startActionFetchForecast(Context context) {
        Intent intent = new Intent(context, ForecastWearService.class);
        context.startService(intent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(this, "Data changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        this.mForecastReceiver = new WindReceiver(this);

        if (!mConnectionError)
            mGoogleApiClient.connect();


        registerReceiver(this.mForecastReceiver, new IntentFilter(ForecastService.BROADCAST_ACTION_FORECAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()))
            mGoogleApiClient.disconnect();

        unregisterReceiver(this.mForecastReceiver);
    }

    /*
    Google API Client Interfaces
     */
    @Override
    public void onConnected(Bundle bundle) {
        this.mLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        if (mLocation != null) {
            ForecastService.startActionFetchForecast(this, mLocation.getLatitude(), mLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mConnectionError = true;
    }

    /*
    Wind Receiver
     */
    @Override
    public void onWindDataReceived(Double speed, Double direction) {
        Toast.makeText(this, "CD " + speed, Toast.LENGTH_SHORT).show();
//        stopSelf();
    }
}
