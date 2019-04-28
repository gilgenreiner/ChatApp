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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;

public class popupDeleteDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_delete_dialog);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int) (width*1), (int) (heigth*0.3));

    }

    public void deleteProfile(View view) {
        final TextInputLayout txtPassword = findViewById(R.id.txtPassword);
        String password = txtPassword.getEditText().getText().toString();
        if( TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), R.string.emptyPasswordField, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(popupDeleteDialogActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }


    public void onCancel(View view) {
        this.onBackPressed();
    }
}
