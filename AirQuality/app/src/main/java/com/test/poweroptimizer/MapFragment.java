package com.test.poweroptimizer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentManager;
import android.graphics.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment  implements OnMapReadyCallback {

    private View mRootView;

    private SupportMapFragment fragment;
    private static GoogleMap map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.map, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.mapview);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.mapview, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
           fragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.addMarker(new MarkerOptions().position(new LatLng(31.235041, 121.509421)).title("Good"));
        this.map.addMarker(new MarkerOptions().position(new LatLng(31.233836, 121.513384)).title("Good"));
        this.map.addMarker(new MarkerOptions().position(new LatLng(31.232044, 121.518591)).title("Good"));
        MarkerOptions bad = new MarkerOptions().position(new LatLng(31.230117, 121.524717)).title("Bad");
        this.map.addMarker(bad);
        
        this.map.addMarker(new MarkerOptions()
                .position(new LatLng(31.240, 121.525)).title("Vending machine")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.m)));

        this.map.addMarker(new MarkerOptions()
                .position(new LatLng(31.22, 121.567)).title("Vending machine 2")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.m)));

        this.map.addMarker(new MarkerOptions()
                .position(new LatLng(31.22, 121.567)).title("Vending machine 3")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.m)));

        Polyline line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(31.235041, 121.509421), new LatLng(31.230067, 121.524819))
                .width(8)
                .color(Color.RED));

        Polyline line1 = map.addPolyline(new PolylineOptions()
                .add(new LatLng(31.230067, 121.524819), new LatLng(31.222489, 121.537944))
                .width(8)
                .color(Color.GREEN));


        LatLng chLocation = new LatLng(31.232044, 121.518591);
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(chLocation,14));
    }
}
