package com.example.greenify_organic_food_app.ui.my_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.fragment.app.Fragment;
import com.example.greenify_organic_food_app.R;

public class MyProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        // List of Districts
        String[] districts = {"Colombo", "Gampaha", "Kandy", "Galle", "Matara", "Kurunegala", "Jaffna"};

        // Get reference to AutoCompleteTextView
        AutoCompleteTextView districtDropdown = view.findViewById(R.id.district_dropdown);

        // Set up adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, districts);
        districtDropdown.setAdapter(adapter);

        // Show dropdown when clicked
        districtDropdown.setOnClickListener(v -> districtDropdown.showDropDown());

        return view;
    }
}
