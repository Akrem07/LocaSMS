package com.example.findfriends.ui.notifications;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findfriends.DatabaseHelper;
import com.example.findfriends.Position;
import com.example.findfriends.PositionAdapter;
import com.example.findfriends.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PositionAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DatabaseHelper(getContext());
        List<Position> positions = getPositionsFromDatabase();

        adapter = new PositionAdapter(getContext(), positions);
        recyclerView.setAdapter(adapter);

        return root;
    }

    private List<Position> getPositionsFromDatabase() {
        List<Position> positions = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = databaseHelper.getAllPositions();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Get values from the cursor
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id")); // Get id from database
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                    // Only add the position if the phone is not null or empty
                    if (phone != null && !phone.isEmpty()) {
                        positions.add(new Position(id, latitude, longitude, timestamp, name, phone));
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions gracefully
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor in the finally block
            }
        }
        return positions;
    }

}
