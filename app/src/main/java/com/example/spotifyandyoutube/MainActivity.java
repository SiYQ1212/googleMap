package com.example.spotifyandyoutube;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";

    private EditText googleMapText;
    private Button googleMapButton;
    private ListView listViewPlaces;
    private PlacesClient placesClient;
    private ArrayList<String> searchResults = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private ArrayList<AutocompletePrediction> autocompletePredictions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDDE61q8QyEj_t_ZlXDWRHEOkypAWbuWqI");
        }
        placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        googleMapText = findViewById(R.id.googleMapText);
        googleMapButton = findViewById(R.id.googleMap);
        listViewPlaces = findViewById(R.id.listViewPlaces);

        // Set up the adapter for the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResults);
        listViewPlaces.setAdapter(adapter);

        googleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocationAndSearch();
            }
        });

        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompletePrediction prediction = autocompletePredictions.get(position);
                fetchPlaceDetails(prediction.getPlaceId());
            }
        });

        checkLocationPermission();
    }

    // 检查应用是否具有访问设备位置的权限，如果没有则请求权限
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "拥有定位权限", Toast.LENGTH_SHORT).show();
//            getCurrentLocationAndSearch();
        } else {  // 没有定位权限，申请
            ActivityCompat.requestPermissions(this,  // 申请权限
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

//     获取设备的当前位置并进行搜索。如果无法获取当前位置，则进行全局搜索
    private void getCurrentLocationAndSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        searchPlaceInNear();
                    } else {
                        Toast.makeText(MainActivity.this, "Unable to get current location. Performing global search.", Toast.LENGTH_SHORT).show();
                        searchPlaceWithoutLocation();
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to get current location. Performing global search.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to get current location", e);
                searchPlaceWithoutLocation();
            });
        } else {
            searchPlaceWithoutLocation();
        }
    }

    //     附近搜索
    private void searchPlaceInNear() {
        String query = googleMapText.getText().toString();
        if (TextUtils.isEmpty(query)) {
//            Toast.makeText(this, "Please enter a place to search", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationBias bias = RectangularBounds.newInstance(
                new com.google.android.gms.maps.model.LatLng(currentLocation.getLatitude() - 0.1, currentLocation.getLongitude() - 0.1),
                new com.google.android.gms.maps.model.LatLng(currentLocation.getLatitude() + 0.1, currentLocation.getLongitude() + 0.1)
        );

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bias)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            searchResults.clear();
            autocompletePredictions.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                searchResults.add(prediction.getFullText(null).toString());
                autocompletePredictions.add(prediction);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Found " + searchResults.size() + " results", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Place not found: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Place not found", exception);
        });
    }

    // 全局搜索
    private void searchPlaceWithoutLocation() {
        String query = googleMapText.getText().toString();
        if (TextUtils.isEmpty(query)) {
            // Toast.makeText(this, "Please enter a place to search", Toast.LENGTH_SHORT).show();
            return;
        }

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            searchResults.clear();
            autocompletePredictions.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                searchResults.add(prediction.getFullText(null).toString());
                autocompletePredictions.add(prediction);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Found " + searchResults.size() + " results", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Place not found: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Place not found", exception);
        });
    }

    // 根据地点ID获取详细信息（包括经纬度）
    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                // 启动导航
                navigation(latLng.latitude, latLng.longitude);
            } else {
                Log.e(TAG, "Location not found");
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Failed to fetch place details", exception);
        });
    }

    // 启动导航
    private void navigation(double latitude, double longitude) {
        // 创建导航的URI
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);

        // 创建Intent以启动Google地图
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // 启动Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
