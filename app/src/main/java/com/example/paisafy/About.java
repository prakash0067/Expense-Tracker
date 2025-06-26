package com.example.paisafy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        LinearLayout linkedinText = view.findViewById(R.id.linkedinRow);
        LinearLayout githubText = view.findViewById(R.id.githubRow);

        linkedinText.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/prakashsirvi"));
            startActivity(intent);
        });

        githubText.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/prakash0067"));
            startActivity(intent);
        });


        return view;
    }
}