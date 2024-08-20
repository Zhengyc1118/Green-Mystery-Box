package comp5216.sydney.edu.au.greenmysterybox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationManager;
import android.location.LocationListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 101;
    private CollectionReference mysteryBoxesRef;
    private double userLatitude = 0.0, userLongitude = 0.0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private HashMap<Marker, MysteryBox> mMarkerMysteryBoxMap = new HashMap<>();
    private static final long MIN_TIME_BW_UPDATES = 60000;  // 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;  // 100 meters

    LinearLayout customInfoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        customInfoWindow = findViewById(R.id.custom_info_window);
        View infoContentView = LayoutInflater.from(this).inflate(R.layout.mystery_box_item, null);
        customInfoWindow.addView(infoContentView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                LatLng userLocation = new LatLng(userLatitude, userLongitude);
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            }

        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_map);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_list) {
                Intent intent = new Intent(this, MysteryBoxList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_map) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent intent = new Intent(this, OrderActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });

        mysteryBoxesRef = FirebaseFirestore.getInstance().collection("mystery_boxes");

    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);


        mMap.setOnMarkerClickListener(marker -> {
            MysteryBox box = mMarkerMysteryBoxMap.get(marker);
            if (box != null) {
                // Populate customInfoWindow with MysteryBox details
                populateCustomInfoWindow(box);  // You'll implement this method next

                // Show the customInfoWindow
                customInfoWindow.setVisibility(View.VISIBLE);

                customInfoWindow.setOnClickListener(view -> {
                    Intent intent = new Intent(MapActivity.this, OrderConfirmationActivity.class); // Replace CurrentActivity and NextActivity with your actual activity names
                    intent.putExtra("box", box);
                    startActivity(intent);
                });
            }
            return true;
        });

        mMap.setOnMapClickListener(latLng -> customInfoWindow.setVisibility(View.GONE));


        LatLng userLocation = new LatLng(userLatitude, userLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        fetchMysteryBoxesAndAddMarkers();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                // Handle permission denied scenario
            }
        }
    }

    private void fetchMysteryBoxesAndAddMarkers() {
        mysteryBoxesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MysteryBox box = document.toObject(MysteryBox.class);
                    if (box.getStock() > 0) {
                        LatLng boxLocation = new LatLng(box.getLatitude(), box.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(boxLocation)
                                .title(box.getBoxName())
                                .snippet(box.getRestaurantName()));

                        // Add the marker and MysteryBox to the HashMap
                        mMarkerMysteryBoxMap.put(marker, box);
                    }

                }
            } else {
                Toast.makeText(MapActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // returns the distance in kilometers
    }

    private void populateCustomInfoWindow(MysteryBox box) {
        TextView boxName = customInfoWindow.findViewById(R.id.boxName);
        TextView restaurantName = customInfoWindow.findViewById(R.id.restaurantName);
        TextView distance = customInfoWindow.findViewById(R.id.distance);
        TextView originalPrice = customInfoWindow.findViewById(R.id.originalPrice);
        TextView currentPrice = customInfoWindow.findViewById(R.id.currentPrice);
        TextView contents = customInfoWindow.findViewById(R.id.contents);
        TextView pickupTime = customInfoWindow.findViewById(R.id.pickupTime);

        ImageView imageView = customInfoWindow.findViewById(R.id.imageView);
        Glide.with(this)
                .load(box.getImageURL())
                .into(imageView);

        boxName.setText(box.getBoxName());
        restaurantName.setText(box.getRestaurantName());

        double dist = calculateDistance(userLatitude, userLongitude, box.getLatitude(), box.getLongitude());
        distance.setText(String.format("%.2f km", dist));

        originalPrice.setText("$" + box.getOriginalPrice());
        currentPrice.setText("$" + box.getCurrentPrice());
        contents.setText("Contents: " + String.join(", ", box.getContents()));
        pickupTime.setText("Pickup Time: " + box.getPickupTime());
    }








}


