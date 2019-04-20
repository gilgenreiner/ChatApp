package at.htl_villach.chatapplication;

import at.htl_villach.chatapplication.bll.User;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final TextInputLayout txtUsername = findViewById(R.id.txtUsername);
        final TextInputLayout txtEmail = findViewById(R.id.txtEmail);
        final TextInputLayout txtPassword = findViewById(R.id.txtPassword);
        final TextInputLayout txtRepeatPassword = findViewById(R.id.txtRepeatPassword);
        final Button btnRegister = findViewById(R.id.btnRegister);


        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String email = txtEmail.getEditText().getText().toString();
                final String username = txtUsername.getEditText().getText().toString();
                final String password = txtPassword.getEditText().getText().toString();
                String repeatPassword = txtRepeatPassword.getEditText().getText().toString();

                if (TextUtils.isEmpty(username)) {
                    txtUsername.setError("Please fill in your username.");
                } else if (TextUtils.isEmpty(email)) {
                    txtEmail.setError("Please fill in your email address.");
                } else if (TextUtils.isEmpty(password)) {
                    txtPassword.setError("Please fill in your password.");
                } else if (password.length() < 7) {
                    txtPassword.setError("Your password must be at least 7 characters.");
                } else if (!TextUtils.equals(password, repeatPassword)) {
                    txtRepeatPassword.setError("The passwords do not match.");
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                UserProfileChangeRequest setUsername = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                firebaseAuth.getCurrentUser().updateProfile(setUsername);
                                createUserDatabaseAndContinue(username, email, password);
                            } else {
                                Toast.makeText(getApplicationContext(), "Register not successful.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }


    }


    private void createUserDatabaseAndContinue(String username, String email, String password) {
        CollectionReference users = database.collection("users");
        final User u = new User(null, email, 0, username, null, password);
        final DocumentReference userRef = users.document(username);
        userRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)  {
                        if(task.getResult().exists()) {
                            Toast.makeText(getApplicationContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            userRef.set(u);
                            startNewIntent();
                        }
                    }
                });
    }

    private void startNewIntent() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }
}