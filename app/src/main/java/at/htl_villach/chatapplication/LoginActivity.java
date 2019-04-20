package at.htl_villach.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    FirebaseFirestore database;
    FirebaseAuth firebaseAuth;
    boolean allowBack = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            allowBack = intent.getExtras().getBoolean("allowBack");
        }
        final TextInputLayout txtUsername = findViewById(R.id.txtUsername);
        final TextInputLayout txtPassword = findViewById(R.id.txtPassword);
        final Button btnLogin = findViewById(R.id.btnLogin);
        final Button btnRegister = findViewById(R.id.btnRegister);


        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = txtUsername.getEditText().getText().toString();
                final String password = txtPassword.getEditText().getText().toString();

                DocumentReference userRef = database.collection("users").document(username);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String asdf = "test";

                        if(task.isSuccessful()) {
                            DocumentSnapshot user = task.getResult();
                            if(user.exists()) {
                                String email = (String) user.getData().get("email");
                                String realPassword = (String) user.getData().get("password");
                                if(password.equals(realPassword) ) {
                                    firebaseAuth.signInWithEmailAndPassword(email, password);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Wrong password.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong username.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(allowBack) {
            super.onBackPressed();
        }
    }
}

