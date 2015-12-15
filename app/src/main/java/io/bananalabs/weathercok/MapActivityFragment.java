package io.bananalabs.weathercok;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import io.bananalabs.weathercok.models.Vane;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends Fragment {

    private final int DEFAULT_ZOOM_LEVEL = 1;

    private TextView mSpeedTextView;
    private TextView mDirectionTextView;
    private ImageView mArrow;
    private GoogleMap mMap;
    private LatLng mLatLng;

    private Vane mVane = new Vane();

    public MapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, null, false);

        mArrow = (ImageView) view.findViewById(R.id.image_arrow);
        mSpeedTextView = (TextView) view.findViewById(R.id.text_speed);
        mDirectionTextView = (TextView) view.findViewById(R.id.text_direction);

        SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        if (supportMapFragment != null)
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    mMap = googleMap;
                    if (mLatLng != null)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM_LEVEL));

                    mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {
                            if (mVane.getDirection() != null)
                                mArrow.setRotation(-mMap.getCameraPosition().bearing + mVane.getDirection().floatValue());
                            // TODO: GET NEW SPEED BASE ON NEW CAMERA LOCATION
                            if (getActivity() instanceof WindActivity) {
                                Double latitude = mMap.getCameraPosition().target.latitude;
                                Double longitude = mMap.getCameraPosition().target.longitude;
                                Location location = new Location("");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                ((WindActivity) getActivity()).fetchForecast(location);
                            }
                        }
                    });
                }
            });
        else
            Toast.makeText(getActivity(), "No map", Toast.LENGTH_SHORT).show();
        return view;
    }

    public void setLatLng(Double latitude, Double longitude) {
        mLatLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM_LEVEL));
        }
    }

    public void setWindSpeedDirection(Double speed, Double direction) {
        this.mVane.setSpeedDirection(speed, direction);
        if (mMap != null)
            mArrow.setRotation(-mMap.getCameraPosition().bearing + direction.floatValue());

        String unit = Utils.getUnit(getActivity());
        mSpeedTextView.setText("" + Utils.speedConversion(unit, mVane.getSpeed()) + " " + unit);

        mDirectionTextView.setText(String.format("%.2fº <%s>", mVane.getDirection(), mVane.getDirectionAsFullString()));
    }

}
