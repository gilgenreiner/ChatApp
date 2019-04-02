package com.htl_villach.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity  {
    Button btnLogin,btnCancel;
    EditText username,password;

    int counter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        username = (EditText)findViewById(R.id.txtUsername);
        password = (EditText)findViewById(R.id.txtPassword);

        btnCancel = (Button)findViewById(R.id.btnCancel);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().equals("admin") && password.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "Login successful...",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
                    if(TextUtils.isEmpty(username.getText().toString())){
                        username.setError("Field is empty");
                    }
                    if(TextUtils.isEmpty(password.getText().toString())){
                        password.setError("Field is empty");
                    }
                }else  if(username.getText().toString().equals("admin") && !password.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "Wrong Password", Toast.LENGTH_SHORT).show();
                }else  if(!username.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "User with this username not registered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}