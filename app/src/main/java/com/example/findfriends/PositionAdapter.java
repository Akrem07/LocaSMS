package com.example.findfriends;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findfriends.ui.dashboard.DashboardFragment;

import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private final List<Position> positions;
    private final Context context;
    private final DatabaseHelper databaseHelper;

    public PositionAdapter(Context context, List<Position> positions) {
        this.context = context;
        this.positions = positions;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position current = positions.get(position);

        // Fetch the name associated with the phone number from the database
        String realName = databaseHelper.getNameByPhone(current.getPhone());
        if (realName != null && !realName.isEmpty()) {
            holder.tvName.setText("Name: " + realName);
            current.setName(realName); // Update the name in the current position object
        } else {
            holder.tvName.setText("Name: Unknown");
        }

        holder.tvPhone.setText("Phone: " + current.getPhone());
        holder.tvLocation.setText("Location: " + current.getLatitude() + ", " + current.getLongitude());
        holder.tvTimestamp.setText("Timestamp: " + current.getTimestamp());

        // Delete Button Click
        holder.btnDelete.setOnClickListener(v -> {
            String positionId = String.valueOf(current.getId());
            boolean isDeleted = databaseHelper.deletePosition(positionId);
            if (isDeleted) {
                positions.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, positions.size());
                Toast.makeText(context, "Position deleted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete position!", Toast.LENGTH_SHORT).show();
            }
        });

        // View on Map Button Click
        holder.btnViewOnMap.setOnClickListener(v -> {
            Position currentPosition = positions.get(position);
            double latitude = currentPosition.getLatitude();
            double longitude = currentPosition.getLongitude();

            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);

            DashboardFragment dashboardFragment = new DashboardFragment();
            dashboardFragment.setArguments(bundle);

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, dashboardFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Edit Button Click
        holder.btnEdit.setOnClickListener(v -> showEditNameDialog(current, position));
    }



    private void showEditNameDialog(Position current, int position) {
        // Create a Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_name, null);
        builder.setView(view);

        // Set the custom rounded background
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);  // Apply the rounded background

        // Get references to the EditText and Buttons
        EditText etEditName = view.findViewById(R.id.etEditName);
        Button btnSaveName = view.findViewById(R.id.btnSaveName);
        Button btnCancel = view.findViewById(R.id.btnCancel); // Add cancel button reference

        // Set the current name in the EditText
        etEditName.setText(current.getName());

        // Set up the save button's click listener
        btnSaveName.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();

            if (!newName.isEmpty()) {
                // Assuming Position has a method `getId()`
                boolean isUpdated = databaseHelper.updatePositionName(current.getId(), newName);
                if (isUpdated) {
                    // Update the name in the position list and notify adapter
                    positions.get(position).setName(newName);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Close the dialog after saving the name
                } else {
                    Toast.makeText(context, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the cancel button's click listener to dismiss the dialog
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss the dialog when cancel is clicked
        });

        // Show the dialog
        dialog.show();
    }



    @Override
    public int getItemCount() {
        return positions.size();
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvLocation, tvTimestamp;
        Button btnDelete, btnViewOnMap, btnEdit;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFriendName);
            tvPhone = itemView.findViewById(R.id.tvFriendPhone);
            tvLocation = itemView.findViewById(R.id.tvFriendLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteFriend);
            btnViewOnMap = itemView.findViewById(R.id.btnViewOnMap);
            btnEdit = itemView.findViewById(R.id.btnEditFriend);
        }
    }
}
