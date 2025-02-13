package com.example.greenify_organic_food_app.ui.my_profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private TextView profileName, profileUsername, profileMobile;
    private EditText editEmail, editMobile, editUsername;
    private AutoCompleteTextView districtDropdown;
    private List<String> districtList;
    private ArrayAdapter<String> adapter;
    private ImageView profileImage, editProfileIcon;
    private Button updateButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String currentProfileImageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        profileMobile = view.findViewById(R.id.profile_mobile);
        editEmail = view.findViewById(R.id.edit_email);
        editMobile = view.findViewById(R.id.edit_mobile);
        editUsername = view.findViewById(R.id.edit_username);
        districtDropdown = view.findViewById(R.id.district_dropdown);
        profileImage = view.findViewById(R.id.profile_image);
        editProfileIcon = view.findViewById(R.id.edit_profile);
        updateButton = view.findViewById(R.id.btn_update_personal);

        districtList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, districtList);
        districtDropdown.setAdapter(adapter);

        // Using SharedPreferences to get the customer data
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail != null) {
            loadCustomerDetails(customerEmail);
        } else {
            Toast.makeText(requireContext(), "No customer email found!", Toast.LENGTH_SHORT).show();
        }

        loadDistrictsFromDb();
        districtDropdown.setOnClickListener(v -> districtDropdown.showDropDown());

        editProfileIcon.setOnClickListener(v -> openImageGallery());

        updateButton.setOnClickListener(v -> updateProfile());

        return view;
    }

    private void loadCustomerDetails(String email) {
        // Fetch user details from Firestore using the email stored in SharedPreferences
        db.collection("customer")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String name = document.getString("name");
                        String username = document.getString("username");
                        String mobile = document.getString("mobile");
                        currentProfileImageUrl = document.getString("profile_img");

                        profileName.setText(name != null ? name : "");
                        profileMobile.setText(mobile != null ? "Mobile: " + mobile : "");
                        editEmail.setText(email);
                        editMobile.setText(mobile);

                        if (username == null || username.isEmpty()) {
                            profileUsername.setText("");
                            editUsername.setHint("Enter your username");
                        } else {
                            profileUsername.setText("Username: " + username);
                            editUsername.setText(username);
                        }

                        if (currentProfileImageUrl != null) {
                            Glide.with(requireContext())
                                    .load(currentProfileImageUrl)
                                    .into(profileImage);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Customer data not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadDistrictsFromDb() {
        db.collection("district")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        districtList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String districtName = document.getString("name");
                            if (districtName != null) {
                                districtList.add(districtName);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "Failed to load districts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImageGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Glide.with(requireContext())
                        .load(imageUri)
                        .into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProfile() {
        String username = editUsername.getText().toString();

        // Check if username is empty
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve customer mobile number from SharedPreferences or Firestore
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(requireContext(), "Customer email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch customer mobile number from Firestore
        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerMobile = document.getString("mobile");

                        if (customerMobile == null || customerMobile.isEmpty()) {
                            Toast.makeText(requireContext(), "Customer mobile number not found!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update user data in Firestore
                        if (imageUri != null) {
                            // Upload the image to Firebase Storage using the customer's mobile number
                            StorageReference storageReference = firebaseStorage.getReference()
                                    .child("images/customer_profile_images/" + customerMobile + ".jpg");

                            storageReference.putFile(imageUri)
                                    .addOnCompleteListener(uploadTask -> {
                                        if (uploadTask.isSuccessful()) {
                                            storageReference.getDownloadUrl().addOnCompleteListener(urlTask -> {
                                                if (urlTask.isSuccessful()) {
                                                    String downloadUrl = urlTask.getResult().toString();
                                                    updateFirestore(username, downloadUrl);
                                                } else {
                                                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // If no new image, just update username
                            updateFirestore(username, currentProfileImageUrl);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Customer data not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestore(String username, String profileImageUrl) {
        // Using SharedPreferences to get the email
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail != null) {
            db.collection("customer")
                    .whereEqualTo("email", customerEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            // Update Firestore with new username and profile image URL
                            document.getReference().update("username", username, "profile_img", profileImageUrl)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                            loadCustomerDetails(customerEmail);  // Reload updated details
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        }
    }
}
