package com.example.findfriends.ui.dashboard;



import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.findfriends.R;
import com.example.findfriends.DatabaseHelper;
import com.example.findfriends.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private FragmentDashboardBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;
    private Marker currentLocationMarker;
    private Marker selectedMarker; // Track the currently selected marker

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize FusedLocationProviderClient and DatabaseHelper
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        dbHelper = new DatabaseHelper(requireContext());

        // Load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the location request
        createLocationRequest();

        return root;
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(60000)
                .setFastestInterval(30000)
                .setSmallestDisplacement(10)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        loadSavedPositions();

        mMap.setOnMapClickListener(latLng -> {
            showSavePositionDialog(latLng);
        });

        mMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;  // Store the selected marker
            showPositionDetailsDialog(marker);
            return true; // Return true to indicate the click was handled
        });

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void updateLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        boolean isInserted = dbHelper.insertPosition(latitude, longitude, timestamp);

        if (isInserted) {
            LatLng newLocation = new LatLng(latitude, longitude);
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }
            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
        }
    }

    private void loadSavedPositions() {
        Cursor cursor = dbHelper.getAllPositions();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                String name = cursor.getString(cursor.getColumnIndex("name"));

                LatLng savedLocation = new LatLng(latitude, longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(savedLocation).title(name));
                marker.setTag(cursor.getInt(cursor.getColumnIndex("id")));  // Store position id in the marker tag
            }
            cursor.close();
        }
    }

    private void showSavePositionDialog(LatLng latLng) {
        EditText editText = new EditText(requireContext());
        editText.setHint("Enter position name");

        new AlertDialog.Builder(requireContext())
                .setTitle("Save Position")
                .setMessage("Do you want to save this position?")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String positionName = editText.getText().toString().trim();
                    if (!positionName.isEmpty()) {
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        boolean isInserted = dbHelper.insertPositionWithName(latLng.latitude, latLng.longitude, timestamp, positionName);
                        if (isInserted) {
                            Toast.makeText(requireContext(), "Position saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to save position!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please enter a name for the position", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showPositionDetailsDialog(Marker marker) {
        int positionId = (int) marker.getTag();
        Cursor cursor = dbHelper.getPositionById(positionId);

        if (cursor != null && cursor.moveToFirst()) {
            String positionName = cursor.getString(cursor.getColumnIndex("name"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

            new AlertDialog.Builder(requireContext())
                    .setTitle("Position Details")
                    .setMessage("Name: " + positionName + "\nLatitude: " + latitude + "\nLongitude: " + longitude)
                    .setPositiveButton("Edit", (dialog, which) -> showEditPositionDialog(marker, positionId, positionName))
                    .setNegativeButton("Delete", (dialog, which) -> {
                        boolean isDeleted = dbHelper.deletePosition(positionId);
                        if (isDeleted) {
                            marker.remove();
                            Toast.makeText(requireContext(), "Position deleted!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete position!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();

            cursor.close();
        }
    }

    private void showEditPositionDialog(Marker marker, int positionId, String currentName) {
        EditText editText = new EditText(requireContext());
        editText.setText(currentName);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Position")
                .setMessage("Edit the position name")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    boolean isUpdated = dbHelper.updatePositionName(positionId, newName);
                    if (isUpdated) {
                        marker.setTitle(newName);
                        Toast.makeText(requireContext(), "Position updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update position!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
