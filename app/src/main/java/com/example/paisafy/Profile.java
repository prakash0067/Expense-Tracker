package com.example.paisafy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.paisafy.Model.User;

import java.io.File;

public class Profile extends Fragment {

    private ImageView profileImage;
    private TextView profileName;
    private LinearLayout usernameItem, emailItem, joinDateItem;
    DbHelper dbHelper;

    TextView tabPersonalInfo, tabSettings;
    LinearLayout infoContainer, settingsContainer, updateProfileBtn;
    Switch notificationSwitch, themeSwitch;
    Spinner currencySpinner;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Profile picture and name
        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);

        // Included info rows
        usernameItem = view.findViewById(R.id.usernameItem);
        emailItem = view.findViewById(R.id.emailItem);
        joinDateItem = view.findViewById(R.id.joinDateItem);
        updateProfileBtn = view.findViewById(R.id.updateProfile);

        // Get user ID from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        // Fetch user data
        dbHelper = new DbHelper(getContext());
        User user = dbHelper.getUserById(userId);

        if (user != null) {
            profileName.setText(user.getName());

            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                File imgFile = new File(user.getProfilePic());
                if (imgFile.exists()) {
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                }
            }

            setInfoItem(usernameItem, R.drawable.ic_user, "Name", user.getName());
            setInfoItem(emailItem, R.drawable.ic_email, "Email", user.getEmail());
            setInfoItem(joinDateItem, R.drawable.ic_calendar, "Join Date", user.getJoinDate());
        }

        // Tabs
        tabPersonalInfo = view.findViewById(R.id.tabPersonalInfo);
        tabSettings = view.findViewById(R.id.tabSettings);

        // Containers
        infoContainer = view.findViewById(R.id.infoContainer);
        settingsContainer = view.findViewById(R.id.settingsContainer);

        // Settings views
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        themeSwitch = view.findViewById(R.id.themeSwitch);
        currencySpinner = view.findViewById(R.id.currencySpinner);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"INR ₹", "USD $", "EUR €", "JPY ¥", "GBP £"}
        );
        currencySpinner.setAdapter(currencyAdapter);

        // Load saved settings
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = prefs.getBoolean("dark_mode_enabled", false);
        String selectedCurrency = prefs.getString("currency", "INR ₹");

        notificationSwitch.setChecked(notificationsEnabled);
        themeSwitch.setChecked(darkModeEnabled);
        currencySpinner.setSelection(currencyAdapter.getPosition(selectedCurrency));

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply();
        });

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                prefs.edit().putString("currency", selected).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // Tab click listeners
        tabPersonalInfo.setOnClickListener(v -> {
            tabPersonalInfo.setBackgroundResource(R.drawable.bg_tab_selected);
            tabPersonalInfo.setTextColor(getResources().getColor(R.color.white));
            tabSettings.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabSettings.setTextColor(getResources().getColor(R.color.gray));

            infoContainer.setVisibility(View.VISIBLE);
            settingsContainer.setVisibility(View.GONE);
        });

        tabSettings.setOnClickListener(v -> {
            tabSettings.setBackgroundResource(R.drawable.bg_tab_selected);
            tabSettings.setTextColor(getResources().getColor(R.color.white));
            tabPersonalInfo.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabPersonalInfo.setTextColor(getResources().getColor(R.color.gray));

            infoContainer.setVisibility(View.GONE);
            settingsContainer.setVisibility(View.VISIBLE);
        });

        // Check if "open_settings_tab" flag was passed
        boolean openSettingsTab = getArguments() != null && getArguments().getBoolean("open_settings_tab", false);
        if (openSettingsTab) {
            tabSettings.performClick(); // open Settings tab by default
        } else {
            tabPersonalInfo.performClick(); // open Personal Info by default
        }

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),EditProfile.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void setInfoItem(View itemView, int iconResId, String label, String value) {
        ImageView icon = itemView.findViewById(R.id.infoIcon);
        TextView labelView = itemView.findViewById(R.id.infoLabel);
        TextView valueView = itemView.findViewById(R.id.infoValue);

        icon.setImageResource(iconResId);
        labelView.setText(label);
        valueView.setText(value);
    }
}