package comp5216.sydney.edu.au.greenmysterybox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.Manifest;


public class MysteryBoxList extends AppCompatActivity {

    private static final int REQUEST_CODE_FILTER = 1;

    private RecyclerView recyclerView;
    private MysteryBoxAdapter adapter;
    private List<MysteryBox> mysteryBoxList;
    private CollectionReference mysteryBoxesRef;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final long MIN_TIME_BW_UPDATES = 60000;  // 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;  // 100 meters
    private double userLatitude, userLongitude;
    private ArrayList<String> selectedCategories;
    private int maxPriceValue = 9999;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            MysteryBox thisItem = mysteryBoxList.get(position);
            Intent intent = new Intent(MysteryBoxList.this, OrderConfirmationActivity.class);
            intent.putExtra("box", thisItem);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mystery_box_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mysteryBoxList = new ArrayList<>();
        adapter = new MysteryBoxAdapter(this, mysteryBoxList, userLatitude, userLongitude);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(onItemClickListener);

        mysteryBoxesRef = FirebaseFirestore.getInstance().collection("mystery_boxes");
        fetchMysteryBoxes();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                // Update adapter
                adapter.setUserLocation(userLatitude, userLongitude);

                // Refresh list
                Collections.sort(mysteryBoxList, new MysteryBoxAdapter.DistanceComparator(userLatitude, userLongitude));
                adapter.notifyDataSetChanged();
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_list) {
                return true;
            } else if (itemId == R.id.nav_map) {
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
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

        FloatingActionButton fabFilter = findViewById(R.id.fab_filter);
        fabFilter.setOnClickListener(v -> {
            Intent intent = new Intent(MysteryBoxList.this, FilterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FILTER);
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILTER && resultCode == RESULT_OK) {
            selectedCategories = data.getStringArrayListExtra("selectedCategories");
            maxPriceValue = data.getIntExtra("maxPrice", 9999);
            fetchMysteryBoxes();
        }
    }

    private DocumentSnapshot lastVisible;  // Keeps track of the last document fetched
    private boolean isLoading = false;  // Indicates whether data is currently being fetched
    private int limit = 10;  // Number of documents to fetch per request

    private void fetchMysteryBoxes() {
        if (isLoading) return;  // Prevent overlapping requests
        isLoading = true;

        Query query = mysteryBoxesRef;
        if (selectedCategories != null && !selectedCategories.isEmpty() && !selectedCategories.contains("All")) {
            query = query.whereIn("category", selectedCategories);
        }

        query = query.whereLessThanOrEqualTo("currentPrice", maxPriceValue);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);  // Continue from where the last fetch left off
        }

        query.limit(limit).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<MysteryBox> newBoxes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MysteryBox box = document.toObject(MysteryBox.class);
                    if (box.getStock() > 0) {
                        newBoxes.add(box);
                    }
                }
                if (!newBoxes.isEmpty()) {
                    lastVisible = task.getResult().getDocuments().get(newBoxes.size() - 1);  // Update lastVisible for the next fetch
                }
                mysteryBoxList.addAll(newBoxes);
                Collections.sort(mysteryBoxList, new MysteryBoxAdapter.DistanceComparator(userLatitude, userLongitude));
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MysteryBoxList.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            isLoading = false;
        });
    }

//    private void fetchMysteryBoxes() {
//        Query query = mysteryBoxesRef;
//        if (selectedCategories != null && !selectedCategories.isEmpty() && !selectedCategories.contains("All")) {
//            query = query.whereIn("category", selectedCategories);
//        }
//
//        query = query.whereLessThanOrEqualTo("currentPrice", maxPriceValue);
//
//        query.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                mysteryBoxList.clear();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    MysteryBox box = document.toObject(MysteryBox.class);
//                    if (box.getStock() > 0) {  // client-side filtering for stock > 0
//                        mysteryBoxList.add(box);
//                    }
//                }
//                Collections.sort(mysteryBoxList, new MysteryBoxAdapter.DistanceComparator(userLatitude, userLongitude));
//                adapter.notifyDataSetChanged();
//            } else {
//                Toast.makeText(MysteryBoxList.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }


}
