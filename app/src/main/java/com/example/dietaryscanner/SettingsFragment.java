package com.example.dietaryscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView; // Make sure to import CardView
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Find the CardViews from the inflated view using their new IDs
        CardView privacyCard = view.findViewById(R.id.card_privacy);
        CardView preferencesCard = view.findViewById(R.id.card_preferences);
        CardView donateCard = view.findViewById(R.id.card_donate);

        // Make the Privacy Policy card functional
        privacyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), privacyscreen.class);
                startActivity(intent);
            }
        });

        // Make the Preferences card functional
        preferencesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), allergyselectionscreen.class);
                startActivity(intent);
            }
        });

        // Make the Donate card functional
        donateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://humanconcernusa.org/palestine-emergency-relief-website/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        return view;
    }
}