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
import com.example.greenify_organic_food_app.CustomToast;
import com.example.greenify_organic_food_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private TextView profileName, profileUsername, profileMobile;
    private EditText editEmail, editMobile, editUsername, editAddressLine1, editAddressLine2, editContactNumber;
    private AutoCompleteTextView districtDropdown;
    private List<String> districtList;
    private ArrayAdapter<String> adapter;
    private ImageView profileImage, editProfileIcon;
    private Button updateButton, btnUpdateDelivery;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String currentProfileImageUrl;
    private SharedPreferences sharedPreferences;

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
        editAddressLine1 = view.findViewById(R.id.edit_address1);
        editAddressLine2 = view.findViewById(R.id.edit_address2);
        districtDropdown = view.findViewById(R.id.district_dropdown);
        profileImage = view.findViewById(R.id.profile_image);
        editProfileIcon = view.findViewById(R.id.edit_profile);
        updateButton = view.findViewById(R.id.btn_update_personal);
        btnUpdateDelivery = view.findViewById(R.id.btn_update_delivery);

        districtList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, districtList);
        districtDropdown.setAdapter(adapter);

        sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
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

        btnUpdateDelivery.setOnClickListener(v -> updateDeliveryAddress());

        return view;
    }

    private void loadCustomerDetails(String email) {
        db.collection("customer")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerId = document.getId();
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
                        loadCustomerDeliveryDetails(customerId);
                    } else {
                        Toast.makeText(requireContext(), "Customer data not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCustomerDeliveryDetails(String cus_id){
        db.collection("customer")
                .document(cus_id)
                .collection("customer_address")
                .document("delivery_details")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        String ads_line1 = document.getString("address_line1");
                        String ads_line2 = document.getString("address_line2");
                        String district = document.getString("district");

                        editAddressLine1.setText(ads_line1 != null ? ads_line1 : "");
                        editAddressLine2.setText(ads_line2 != null ? ads_line2 : "");
                        districtDropdown.setText(district != null ? district : "", false);
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

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(requireContext(), "Customer email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

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

                        if (imageUri != null) {
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
                            updateFirestore(username, currentProfileImageUrl);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Customer data not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDeliveryAddress() {
        String addressLine1 = editAddressLine1.getText().toString().trim();
        String addressLine2 = editAddressLine2.getText().toString().trim();
        String district = districtDropdown.getText().toString().trim();
        String contactNumber = sharedPreferences.getString("customerMobile",null);

        if (addressLine1.isEmpty() || district.isEmpty() || contactNumber.isEmpty()) {
            CustomToast.showToast(getContext(),"Please fill all required fields",false);
            return;
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            CustomToast.showToast(getContext(),"Customer email not found!",false);
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerId = document.getId();

                        Map<String, Object> deliveryDetails = new HashMap<>();
                        deliveryDetails.put("address_line1", addressLine1);
                        deliveryDetails.put("address_line2", addressLine2);
                        deliveryDetails.put("district", district);
                        deliveryDetails.put("contact_number", contactNumber);

                        db.collection("customer")
                                .document(customerId)
                                .collection("customer_address")
                                .document("delivery_details")
                                .set(deliveryDetails)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        CustomToast.showToast(getContext(),"Delivery address updated successfully!",true);
                                    } else {
                                        CustomToast.showToast(getContext(),"Failed to update delivery address",false);
                                    }
                                });
                    } else {
                       CustomToast.showToast(getContext(),"Customer data not found!",false);
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