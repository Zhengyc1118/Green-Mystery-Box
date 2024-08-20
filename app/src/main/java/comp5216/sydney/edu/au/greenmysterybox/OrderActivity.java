package comp5216.sydney.edu.au.greenmysterybox;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> ordersList;
    private CollectionReference ordersRef;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double userLatitude, userLongitude;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            Order thisOrder = ordersList.get(position);
            if (thisOrder.getStatus().equals("Awaiting pickup")) {
                showConfirmationDialog(thisOrder);
            } else {
                // Handle the case where the order is already picked up
                // You can show a message to the user or perform a different action
                Toast.makeText(OrderActivity.this, "This order is already picked up", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void showConfirmationDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Pick Up")
                .setMessage("Do you want to confirm the pick up for this order?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle order pick up confirmation logic here
                        // Update the order status

                        order.setStatus("Picked up");
                        int pickupId = order.getPickupId();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference collectionRef = db.collection("new_orders");

                        collectionRef.whereEqualTo("pickupId", pickupId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {

                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        // Update the status in Firestore
                                        collectionRef.document(document.getId())
                                                .update("status", "Picked up")
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Order status updated successfully");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error updating order status", e);
                                                });
                                    }


                                    // Notify the RecyclerView adapter to update the UI
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error querying the Order by name", e);
                                    }
                                });


                        // Close the dialog
                        dialog.dismiss();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ordersList = new ArrayList<>();
        adapter = new OrderAdapter(this, ordersList, userLatitude, userLongitude);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(onItemClickListener);

        ordersRef = FirebaseFirestore.getInstance().collection("new_orders");
        fetchOrders();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                // Update adapter
                adapter.setUserLocation(userLatitude, userLongitude);
                // Refresh list
                adapter.notifyDataSetChanged();
            }
        };
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_list) {
                Intent intent = new Intent(this, MysteryBoxList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_map) {
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });

    }

    private void fetchOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String currentUid = currentUser.getUid();

        Query query = ordersRef.whereEqualTo("uid", currentUid);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Order order = document.toObject(Order.class);
                    ordersList.add(order);
                }
                Collections.sort(ordersList, new OrderStatusComparator());
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(OrderActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class OrderStatusComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            if (o1.getStatus().equals("Awaiting pickup") && !o2.getStatus().equals("Awaiting pickup")) {
                return -1;  // o1 comes before o2
            } else if (!o1.getStatus().equals("Awaiting pickup") && o2.getStatus().equals("Awaiting pickup")) {
                return 1;  // o2 comes before o1
            } else {
                return 0;  // no change in order
            }
        }
    }
}