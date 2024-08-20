package comp5216.sydney.edu.au.greenmysterybox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {

    private CollectionReference ordersCollectionRef;
    private TextView personNameText; // using email at present
    private TextView savedTimesText;

    private Button logoutButton;
    String savedFoodTimesTemplate = "Saved food %d times";
    private TextView savedDollarsText;
    String savedDollarsTextTemplate = "Saved $ %,.2f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        personNameText = findViewById(R.id.textPersonName);
        savedTimesText = findViewById(R.id.textSavedFoodTimes);
        savedDollarsText = findViewById(R.id.textSavedDollarAmount);

        personNameText.setText(ProfileInfo.loggedInUserEmail == null? "" :
                ProfileInfo.loggedInUserEmail);

        ordersCollectionRef = FirebaseFirestore.getInstance().collection("new_orders");
        summariseOrders();

        logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle logout logic here
                logoutUser();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
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
                Intent intent = new Intent(this, OrderActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Start the Profile activity
                return true;
            } else {
                return false;
            }
        });

    }

    private void summariseOrders() {
        CollectionReference ordersRef = FirebaseFirestore.getInstance().collection("new_orders");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String currentUid = currentUser.getUid();
        Query query = ordersRef.whereEqualTo("uid", currentUid);
        Task<QuerySnapshot> querySnapshotTask = query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        int orderCount = 0;
                        double originalTotalDollars = 0.0;
                        double currentTotalDollars = 0.0;
                        for (QueryDocumentSnapshot document : result) {
                            ++ orderCount;
                            Double currentPrice = Double.parseDouble(document.get("currentPrice").toString());
                            Double originalPrice = Double.parseDouble(document.get("originalPrice").toString());
                            Integer quantity = Integer.parseInt(document.get("quantity").toString());
                            originalTotalDollars += (originalPrice * quantity);
                            currentTotalDollars += (currentPrice * quantity);
                            Log.i("TT",
                                    String.format("currentPrice %,.2f, originalPrice %,.2f, qty %d",
                                    currentPrice, originalPrice, quantity));
                        }
                        double savingDollars = originalTotalDollars - currentTotalDollars;
                        savedDollarsText.setText(String.format(savedDollarsTextTemplate, savingDollars));
                        savedTimesText.setText(String.format(savedFoodTimesTemplate, orderCount));
                    }
                });
    }

    private void logoutUser() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }

}