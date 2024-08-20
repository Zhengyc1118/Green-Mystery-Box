package comp5216.sydney.edu.au.greenmysterybox;

import static android.service.controls.ControlsProviderService.TAG;
import static comp5216.sydney.edu.au.greenmysterybox.MysteryBoxAdapter.calculateDistance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OrderConfirmationActivity extends AppCompatActivity {

    ImageView imageView;
    TextView restaurantName;
    TextView boxName;
    TextView distance;
    TextView originalPrice;
    TextView currentPrice;
    TextView contents;
    TextView pickupTime;
    Button confirmOrderButton;
    ImageButton backButton;
    int newValue;
    String orderId;
    TextView pickupAddress;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final long MIN_TIME_BW_UPDATES = 60000;  // 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;  // 100 meters
    private double userLatitude, userLongitude;
    private CollectionReference ordersRef;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageView = findViewById(R.id.offerImageView);
        restaurantName = findViewById(R.id.offerRestaurantName);
        distance = findViewById(R.id.offerDistance);
        boxName = findViewById(R.id.offerTitleTextView);
        originalPrice = findViewById(R.id.offerOriginalPrice);
        originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        currentPrice = findViewById(R.id.offerCurrentPrice);
        contents = findViewById(R.id.offerContents);
        pickupTime = findViewById(R.id.offerPickupTime);
        HorizontalNumberPicker horizontalNumberPicker = findViewById(R.id.numberPicker);
        confirmOrderButton = findViewById(R.id.orderButton);
        pickupAddress = findViewById(R.id.offerPickupAddress);

        MysteryBox thisItem = (MysteryBox) getIntent().getSerializableExtra("box");
        Glide.with(this).load(thisItem.getImageURL()).into(imageView);
        restaurantName.setText(thisItem.getRestaurantName());
        boxName.setText(thisItem.getBoxName());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                double d = calculateDistance(userLatitude, userLongitude, thisItem.getLatitude(), thisItem.getLongitude());
                distance.setText(String.format("%.2f km", d));
            }
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }

        originalPrice.setText("$" + thisItem.getOriginalPrice());
        currentPrice.setText("$" + thisItem.getCurrentPrice());
        contents.setText("Contents: " + String.join(", ", thisItem.getContents()));
        pickupTime.setText("Pickup Time: " + thisItem.getPickupTime());
        pickupAddress.setText("Pickup Address: " + thisItem.getPickupAddress());


        horizontalNumberPicker.setMax(thisItem.getStock());
        horizontalNumberPicker.setMin(0);
        horizontalNumberPicker.setOnValueChangedListener(new HorizontalNumberPicker.OnValueChangedListener() {
            @Override
            public void onValueChanged(int newVal) {
                newValue = newVal;
                confirmOrderButton.setText("PLACE ORDER  $"+ newValue*thisItem.getCurrentPrice());
            }
        });

        ordersRef = FirebaseFirestore.getInstance().collection("new_orders");

        // Set an OnClickListener for the confirmOrderButton
        confirmOrderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (newValue > 0) {
                    Random random = new Random();
                    int pickupId = random.nextInt(900000) + 100000;

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("uid", currentUser.getUid());
                    orderMap.put("imageURL", thisItem.getImageURL());
                    orderMap.put("restaurantName", thisItem.getRestaurantName());
                    orderMap.put("boxName", thisItem.getBoxName());
                    orderMap.put("contents", thisItem.getContents());
                    orderMap.put("latitude", thisItem.getLatitude());
                    orderMap.put("longitude", thisItem.getLongitude());
                    orderMap.put("pickupTime", thisItem.getPickupTime());
                    orderMap.put("pickupId", pickupId);
                    orderMap.put("quantity", newValue);
                    orderMap.put("originalPrice", thisItem.getOriginalPrice());
                    orderMap.put("currentPrice", thisItem.getCurrentPrice());
                    orderMap.put("totalPrice", newValue * thisItem.getCurrentPrice());
                    orderMap.put("status", "Awaiting pickup");
                    orderMap.put("pickupAddress", thisItem.getPickupAddress());


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference collectionRef = db.collection("mystery_boxes");
                    db.collection("new_orders").add(orderMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                    orderId = documentReference.getId();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                    String boxNameToUpdate = thisItem.getBoxName();
                    // Create a query to find the MysteryBox document by name
                    collectionRef.whereEqualTo("boxName", boxNameToUpdate)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        // Get the document ID of the matching MysteryBox
                                        String documentId = documentSnapshot.getId();

                                        int updatedStock = thisItem.getStock() - newValue;

                                        db.collection("mystery_boxes")
                                                .document(documentId)
                                                .update("stock", updatedStock)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Stock updated successfully.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "Error updating stock", e);
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error querying the Order by name", e);
                                }
                            });

                    // Create and show an AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderConfirmationActivity.this);
                    builder.setTitle("Thank you for your order!")
                            .setMessage("Your mystery box will be waiting for you in the restaurant.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle the "OK" button click (confirm order)
                                    dialog.dismiss(); // Dismiss the dialog
                                    Intent intent = new Intent(OrderConfirmationActivity.this, OrderActivity.class);
                                    intent.putExtra("orderId", orderId);
                                    startActivity(intent);
                                }
                            })
                            .show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderConfirmationActivity.this);
                    builder.setTitle("")
                            .setTitle("Invalid Quantity")
                            .setMessage("Please select a quantity greater than 0.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle the "OK" button click
                                    dialog.dismiss(); // Dismiss the dialog
                                }
                            })
                            .show();
                }
            }
        });


    }
}