package com.example.findfriends.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.findfriends.MyLocationService;
import com.example.findfriends.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Action when button is clicked
        binding.btnsendsms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numero = binding.ednumero.getText().toString().trim();
                String name = binding.edname.getText().toString().trim();

                // Send SMS with name
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(
                        numero,
                        null,
                        "FindFriends: envoyer moi votre position - " + name,  // Include name in the message
                        null,
                        null
                );

                // Start service with the phone number and name
                Intent serviceIntent = new Intent(getContext(), MyLocationService.class);
                serviceIntent.putExtra("phone", numero);
                serviceIntent.putExtra("name", name);  // Pass the name to the service
                getActivity().startService(serviceIntent);  // Start the service
            }
        });

        final TextView textView = binding.textHome;
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
