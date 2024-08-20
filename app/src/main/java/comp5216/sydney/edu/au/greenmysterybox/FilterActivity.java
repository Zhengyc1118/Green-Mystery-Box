package comp5216.sydney.edu.au.greenmysterybox;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private CheckBox checkBoxAll, checkBoxCategory1, checkBoxCategory2, checkBoxCategory3,checkBoxCategory4; // ... other categories
    private SeekBar seekBarPrice;
    private TextView textViewMaxPrice;
    private Button buttonFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        checkBoxAll = findViewById(R.id.checkBox_all);
        checkBoxCategory1 = findViewById(R.id.checkBox_category1);
        checkBoxCategory2 = findViewById(R.id.checkBox_category2);
        checkBoxCategory3 = findViewById(R.id.checkBox_category3);
        checkBoxCategory4 = findViewById(R.id.checkBox_category4);
        // ... initialize other categories ...

        textViewMaxPrice = findViewById(R.id.textView_maxPrice);
        seekBarPrice = findViewById(R.id.seekBar_price);
        buttonFilter = findViewById(R.id.button_filter);

        checkBoxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxCategory1.setChecked(false);
                checkBoxCategory2.setChecked(false);
                checkBoxCategory3.setChecked(false);
                checkBoxCategory4.setChecked(false);
                // ... uncheck other categories
            }
        });

        checkBoxCategory1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxAll.setChecked(false);
            }
        });

        checkBoxCategory2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxAll.setChecked(false);
            }
        });

        // Update max price text when SeekBar changes
        seekBarPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewMaxPrice.setText("Max Price: $" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        buttonFilter.setOnClickListener(v -> {
            ArrayList<String> selectedCategories = new ArrayList<>();
            if (checkBoxCategory1.isChecked()) selectedCategories.add("Burger");
            if (checkBoxCategory2.isChecked()) selectedCategories.add("Seafood");
            if (checkBoxCategory3.isChecked()) selectedCategories.add("Bakery");
            if (checkBoxCategory4.isChecked()) selectedCategories.add("Pizza");
            // ... check other categories

            int maxPriceValue = seekBarPrice.getProgress();

            Intent intent = new Intent();
            intent.putStringArrayListExtra("selectedCategories", selectedCategories);
            intent.putExtra("maxPrice", maxPriceValue);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
