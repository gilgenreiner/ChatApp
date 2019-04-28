package at.htl_villach.chatapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class popupDeleteDialogActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String userId ;
    String email ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_delete_dialog);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int) (width*1), (int) (heigth*0.3));
        firebaseAuth = FirebaseAuth.getInstance().getInstance();
    }

    public void deleteProfile(View view) {

        final TextInputLayout txtPassword = findViewById(R.id.txtPassword);
        String password = txtPassword.getEditText().getText().toString();
        if( TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), R.string.emptyPasswordField, Toast.LENGTH_SHORT).show();
        } else {
            user = firebaseAuth.getCurrentUser();
            userId = user.getUid();
            email = user.getEmail();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference drfUser = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                        drfUser.removeValue();
                                        Toast.makeText(popupDeleteDialogActivity.this, "User account deleted!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(popupDeleteDialogActivity.this, LoginActivity.class);
                                        intent.putExtra("allowBack", false);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(popupDeleteDialogActivity.this, "Something went wrong!",
                                                Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }
    }


    public void onCancel(View view) {
        this.onBackPressed();
    }
}
