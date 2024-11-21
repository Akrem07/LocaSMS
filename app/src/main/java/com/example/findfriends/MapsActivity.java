package com.example.findfriends;


import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.findfriends.databinding.FragmentDashboardBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentDashboardBinding binding;
    double longitude,latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        longitude=Double.parseDouble(this.getIntent().getStringExtra("longitude"));
        latitude=Double.parseDouble(this.getIntent().getStringExtra("latitude"));


        // Add a marker in Sydney and move the camera
        LatLng position = new LatLng(latitude, longitude);


        mMap.addMarker(new MarkerOptions().position(position).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }
}