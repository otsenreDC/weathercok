package io.bananalabs.weathercok.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.bananalabs.weathercok.service.ForecastService;

public class WindReceiver extends BroadcastReceiver {

    private WindReceiverListener listener;

    public WindReceiver(WindReceiverListener listener) {
        this.listener = listener;
    }

    public interface WindReceiverListener {
        public void onWindDataReceived(Double speed, Double direction);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ForecastService.BROADCAST_ACTION_FORECAST)) {
            Double speed = intent.getDoubleExtra(ForecastService.PROPERTY_SPEED, 0);
            Double direction = intent.getDoubleExtra(ForecastService.PROPERTY_DIRECTION, 0);
            listener.onWindDataReceived(speed, direction);
        }
    }
}
