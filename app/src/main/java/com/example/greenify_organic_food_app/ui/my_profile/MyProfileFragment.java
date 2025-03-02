package com.example.greenify_organic_food_app.ui.my_profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.CustomToast;
import com.example.greenify_organic_food_app.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final String LOCATIONIQ_API_KEY = "pk.7cffa57d7e32ea76bf5813a99f839548";

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

        editAddressLine1.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
                imageUri = data.getData();
                try {
                    Glide.with(requireContext())
                            .load(imageUri)
                            .into(profileImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                editAddressLine1.setText(place.getAddress());
                if (place.getLatLng() != null) {
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("latitude", (float) latitude);
                    editor.putFloat("longitude", (float) longitude);
                    editor.apply();
                }
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                if (data != null) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.e("Places", "Error: " + status.getStatusMessage());
                }
            }
            else if (resultCode == getActivity().RESULT_CANCELED) {
                Toast.makeText(getContext(), "Address selection cancelled", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void loadCustomerDeliveryDetails(String cus_id) {
        db.collection("customer")
                .document(cus_id)
                .collection("customer_address")
                .document("delivery_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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
        String contactNumber = sharedPreferences.getString("customerMobile", null);

        if (addressLine1.isEmpty() || district.isEmpty() || contactNumber.isEmpty()) {
            CustomToast.showToast(getContext(), "Please fill all required fields", false);
            return;
        }

        String fullAddress = addressLine1 + ", " + addressLine2 + ", " + district;

        fetchLatLongFromAddress(fullAddress, (latitude, longitude) -> {
            if (latitude != null && longitude != null) {
                saveAddressToFirestore(addressLine1, addressLine2, district, contactNumber, latitude, longitude);
            } else {
                CustomToast.showToast(getContext(), "Failed to fetch location. Please check the address.", false);
            }
        });
    }

    private void fetchLatLongFromAddress(String address, GeocodingCallback callback) {
        String url = "https://us1.locationiq.com/v1/search.php?key=" + LOCATIONIQ_API_KEY + "&q=" + Uri.encode(address) + "&format=json";

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            JSONObject firstResult = jsonArray.getJSONObject(0);
                            double latitude = firstResult.getDouble("lat");
                            double longitude = firstResult.getDouble("lon");
                            callback.onGeocodingResult(latitude, longitude);
                        } else {
                            callback.onGeocodingResult(null, null);
                        }
                    } catch (Exception e) {
                        Log.e("Geocoding", "Error parsing JSON", e);
                        callback.onGeocodingResult(null, null);
                    }
                },
                error -> {
                    Log.e("Geocoding", "API error: " + error.getMessage());
                    callback.onGeocodingResult(null, null);
                });

        queue.add(request);
    }

    private void saveAddressToFirestore(String addressLine1, String addressLine2, String district, String contactNumber, double latitude, double longitude) {
        String customerId = sharedPreferences.getString("customerId", null);
        if (customerId == null) {
            CustomToast.showToast(getContext(), "Customer ID not found!", false);
            return;
        }

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("address_line1", addressLine1);
        addressData.put("address_line2", addressLine2);
        addressData.put("district", district);
        addressData.put("contact_number", contactNumber);
        addressData.put("latitude", latitude);
        addressData.put("longitude", longitude);

        db.collection("customer")
                .document(customerId)
                .collection("customer_address")
                .document("delivery_details")
                .set(addressData)
                .addOnSuccessListener(aVoid -> {
                    CustomToast.showToast(getContext(), "Delivery address updated successfully!", true);
                })
                .addOnFailureListener(e -> {
                    CustomToast.showToast(getContext(), "Failed to update address: " + e.getMessage(), false);
                });
    }

    private void updateFirestore(String username, String profileImageUrl) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail != null) {
            db.collection("customer")
                    .whereEqualTo("email", customerEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

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

    private interface GeocodingCallback {
        void onGeocodingResult(Double latitude, Double longitude);
    }
}