package io.bananalabs.weathercok;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends Fragment {

    private final int DEFAULT_ZOOM_LEVEL = 1;

    private ImageView mArrow;
    private GoogleMap mMap;
    private LatLng mLatLng;

    private Double mSpeed;
    private Double mDirection;

    public MapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, null, false);
        mArrow = (ImageView) view.findViewById(R.id.image_arrow);
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
                            if (mDirection != null)
                                mArrow.setRotation(-mMap.getCameraPosition().bearing + mDirection.floatValue());
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
        this.mSpeed = speed;
        this.mDirection = direction;
        if (mMap != null)
            mArrow.setRotation(-mMap.getCameraPosition().bearing + direction.floatValue());
    }

}
