package comp5216.sydney.edu.au.greenmysterybox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText newEmail;
    EditText newPassword;
    Button registerBtn;
    TextView loginLink;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        newEmail = (EditText) findViewById(R.id.txtNewEmail);
        newPassword = (EditText) findViewById(R.id.txtNewPassword);
        registerBtn = (Button) findViewById(R.id.btnRegister);
        loginLink = (TextView) findViewById(R.id.linkLogin);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String email = newEmail.getText().toString();
                String password = newPassword.getText().toString();

                registerUser();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    };

    /*
    Check if the input email and address are valid
     */
    private boolean isValid(String email, String password) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.length() >= 6;
    }

    private void navigateToListActivity() {
        Intent intent = new Intent(RegisterActivity.this, MysteryBoxList.class);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String email = newEmail.getText().toString();
        String password = newPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Registration succeeded. Automatically logged in.",
                                Toast.LENGTH_SHORT).show();
                        // Registration success, you can do something here.
                        navigateToListActivity();
                    } else {
                        // Registration failed, handle the error.
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}