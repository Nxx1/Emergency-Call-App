package com.potensiutama.emergencycallclient.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.potensiutama.emergencycallclient.MenuAwalActivity;
import com.potensiutama.emergencycallclient.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    TextView bRegister;
    Button bLogin;
    EditText edtEmail,edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            finish();
            startActivity(new Intent(LoginActivity.this, MenuAwalActivity.class));
        }

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);

        bLogin = findViewById(R.id.btn_login);
        bRegister = findViewById(R.id.btn_register);

        bRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this,RegisterActivity.class)));

        bLogin.setOnClickListener(v -> LoginAkun(edtEmail.getText().toString(),edtPassword.getText().toString()));
    }

    private void LoginAkun(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        finish();
                        startActivity(new Intent(LoginActivity.this, MenuAwalActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Email/Password Salah!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }
}