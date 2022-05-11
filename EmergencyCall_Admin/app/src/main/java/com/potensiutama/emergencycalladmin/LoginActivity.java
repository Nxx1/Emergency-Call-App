package com.potensiutama.emergencycalladmin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {


    DatabaseReference dbUser;
    SharedPreferences preferences;
    EditText edtUsername,edtPassword;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbUser = FirebaseDatabase.getInstance().getReference("User");

        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);

        Button btnLogin = findViewById(R.id.btn_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dbUsername = preferences.getString("username", "");
        String dbPassword = preferences.getString("password", "");

        if(dbUsername.equals("admin") && dbPassword.equals("admin")){
            startActivity(new Intent(LoginActivity.this, MenuAwalActivity.class));
            finish();
        }


        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();

            if(username.equals("admin") && password.equals("admin")){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username","admin");
                editor.putString("password","admin");
                editor.apply();
                Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MenuAwalActivity.class));
                finish();
            }else{
                Toast.makeText(LoginActivity.this, edtUsername.getText().toString() + "\n"+edtPassword.getText().toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, "Username atau Password yang anda masukkan salah!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}