package com.example.findfriends.ui.dashboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private FragmentDashboardBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;
    private Marker currentLocationMarker;
    private Marker selectedMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Retrieve latitude and longitude from the arguments (passed bundle)
        Bundle bundle = getArguments();
        if (bundle != null) {
            double latitude = bundle.getDouble("latitude", 0.0);
            double longitude = bundle.getDouble("longitude", 0.0);

            // Add a marker on the map for the passed position
            LatLng position = new LatLng(latitude, longitude);
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions().position(position).title("Position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            }
        }

        // Initialize FusedLocationProviderClient and DatabaseHelper
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        dbHelper = new DatabaseHelper(requireContext());

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up location request and callback
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
        // Load all named positions
        Cursor namedCursor = dbHelper.getAllNamedPositions();
        if (namedCursor != null && namedCursor.getCount() > 0) { // Check if namedCursor is not null and contains data
            while (namedCursor.moveToNext()) {
                double latitude = namedCursor.getDouble(namedCursor.getColumnIndex("latitude"));
                double longitude = namedCursor.getDouble(namedCursor.getColumnIndex("longitude"));
                String name = namedCursor.getString(namedCursor.getColumnIndex("name"));

                // Check if the name is neither empty nor "null"
                if (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("null")) {
                    LatLng savedLocation = new LatLng(latitude, longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(savedLocation).title(name));
                    marker.setTag(namedCursor.getInt(namedCursor.getColumnIndex("id")));  // Store position id in the marker tag
                }
            }
            namedCursor.close();
        }

//        // Load last unnamed position
//        Cursor lastUnnamedCursor = dbHelper.getLastUnnamedPosition();
//        if (lastUnnamedCursor != null && lastUnnamedCursor.getCount() > 0 && lastUnnamedCursor.moveToFirst()) { // Check if lastUnnamedCursor is not null and has data
//            double latitude = lastUnnamedCursor.getDouble(lastUnnamedCursor.getColumnIndex("latitude"));
//            double longitude = lastUnnamedCursor.getDouble(lastUnnamedCursor.getColumnIndex("longitude"));
//
//            LatLng lastLocation = new LatLng(latitude, longitude);
//            Marker marker = mMap.addMarker(new MarkerOptions().position(lastLocation).title("Last Position"));
//            marker.setTag(lastUnnamedCursor.getInt(lastUnnamedCursor.getColumnIndex("id")));  // Store position id in the marker tag
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15)); // Center the map on the last position
//            lastUnnamedCursor.close();
//        }
    }



    private void showSavePositionDialog(LatLng latLng) {
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_name, null);  // Use dialog_edit_name.xml layout

        // Get references to the UI elements in the dialog layout
        EditText etEditName = dialogView.findViewById(R.id.etEditName);  // Reference to the EditText for name input
        MaterialButton btnSaveName = dialogView.findViewById(R.id.btnSaveName);  // Reference to the Save button
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);  // Reference to the Cancel button

        // Create the AlertDialog and set its view to the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);

        // Set up the Cancel button
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog when Cancel is clicked
        });

        // Set up the Save button
        btnSaveName.setOnClickListener(v -> {
            String positionName = etEditName.getText().toString().trim();

            // Check if the position name is not empty
            if (!positionName.isEmpty()) {
                try {
                    // Get the current timestamp
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Log the values being passed
                    Log.d("SavePosition", "Saving position: Name: " + positionName + ", Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude);

                    // Check if dbHelper is null
                    if (dbHelper == null) {
                        Log.e("SavePosition", "Database helper is not initialized!");
                        Toast.makeText(requireContext(), "Database helper not initialized!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Insert the position into the database
                    boolean isInserted = dbHelper.insertPositionWithName(latLng.latitude, latLng.longitude, timestamp, positionName);

                    if (isInserted) {
                        Toast.makeText(requireContext(), "Position saved!", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(positionName)); // Add marker to map
                    } else {
                        Toast.makeText(requireContext(), "Failed to save position!", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    // Catch any exceptions and log them
                    Log.e("SavePositionError", "Error while saving position: ", e);
                    Toast.makeText(requireContext(), "An error occurred while saving the position.", Toast.LENGTH_SHORT).show();
                }

                // Dismiss the dialog after saving
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Please enter a valid name for the position", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        dialog.show();
    }







    private void showPositionDetailsDialog(Marker marker) {
        int positionId = (int) marker.getTag();
        Cursor cursor = dbHelper.getPositionById(positionId);

        if (cursor != null && cursor.moveToFirst()) {
            String positionName = cursor.getString(cursor.getColumnIndex("name"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

            // Inflate the dialog_position_details.xml layout
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialog_position_details, null);

            // Find views within the inflated layout
            TextView tvPositionName = dialogView.findViewById(R.id.tvPositionName);
            TextView tvPositionLatitude = dialogView.findViewById(R.id.tvPositionLatitude);
            TextView tvPositionLongitude = dialogView.findViewById(R.id.tvPositionLongitude);
            TextView btnClose = dialogView.findViewById(R.id.btnClose);
            MaterialButton btnEdit = dialogView.findViewById(R.id.btnEdit);
            MaterialButton btnDelete = dialogView.findViewById(R.id.btnDelete);

            // Set data to views
            tvPositionName.setText("Name: " + positionName);
            tvPositionLatitude.setText("Latitude: " + latitude);
            tvPositionLongitude.setText("Longitude: " + longitude);

            // Build the dialog using the custom view
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);

            // Handle button clicks
            btnEdit.setOnClickListener(v -> {
                dialog.dismiss();
                showEditPositionDialog(marker, positionId, positionName);
            });

            btnDelete.setOnClickListener(v -> {
                boolean isDeleted = dbHelper.deletePosition(String.valueOf(positionId));
                if (isDeleted) {
                    marker.remove(); // Remove the marker from the map
                    Toast.makeText(requireContext(), "Position deleted", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            btnClose.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }




    private void showEditPositionDialog(Marker marker, int positionId, String oldName) {
        // Inflate the dialog_edit_position.xml layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_position, null);

        // Find views within the inflated layout
        EditText editText = dialogView.findViewById(R.id.editPositionName);
        editText.setText(oldName);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        MaterialButton btnSaveName = dialogView.findViewById(R.id.btnSaveName);


        // Build the dialog using the custom view
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Apply the custom rounded background
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);

        // Handle button clicks
        btnSaveName.setOnClickListener(v -> {
            String newName = editText.getText().toString().trim();
            if (!newName.isEmpty()) {
                boolean isUpdated = dbHelper.updatePositionName(positionId, newName);
                if (isUpdated) {
                    marker.setTitle(newName); // Update the marker title
                    Toast.makeText(requireContext(), "Position updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });



        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



}
