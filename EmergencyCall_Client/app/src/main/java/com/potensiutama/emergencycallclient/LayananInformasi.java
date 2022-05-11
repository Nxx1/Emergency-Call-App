package com.potensiutama.emergencycallclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.potensiutama.emergencycallclient.Adapter.MyLayananInformasiAdapter;
import com.potensiutama.emergencycallclient.Auth.LoginActivity;
import com.potensiutama.emergencycallclient.Model.LayananInformasiModel;

import java.util.ArrayList;

public class LayananInformasi extends AppCompatActivity {

    private ListView listView;

    private MyLayananInformasiAdapter adapter;
    private ArrayList<LayananInformasiModel> layananInformasiModelArrayList;
    DatabaseReference dbInformasi;

    private ProgressDialog progressDialog;

    FirebaseStorage storage;
    StorageReference storageReference;

    ImageButton btnSOS,bLayananInformasi,bDaftarLokasi,bHome,bLogout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layanan_informasi);
        dbInformasi = FirebaseDatabase.getInstance().getReference("Informasi");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        listView = findViewById(R.id.lv_list);

        progressDialog = new ProgressDialog(LayananInformasi.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Mohon tunggu...");

        layananInformasiModelArrayList = new ArrayList<>();

        bLayananInformasi = findViewById(R.id.home_btn_informasi);
        bDaftarLokasi = findViewById(R.id.home_btn_maps);
        bHome = findViewById(R.id.home_btn_dashboard);
        btnSOS = findViewById(R.id.home_btn_sos);
        bLogout = findViewById(R.id.home_btn_logout);

        bHome.setOnClickListener(v -> finish());

        bDaftarLokasi.setOnClickListener(view -> {
            Intent intent = new Intent(LayananInformasi.this, DaftarLokasiActivity.class);
            startActivity(intent);
            finish();
        });

        btnSOS.setOnClickListener(v -> startActivity(new Intent(LayananInformasi.this, SOSActivity.class)));

        bLogout.setOnClickListener(v -> {

            DialogLogout();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        progressDialog.setTitle("Data layanan informasi");
        progressDialog.show();

        dbInformasi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                layananInformasiModelArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    LayananInformasiModel layananInformasiModel = dataSnapshot1.getValue(LayananInformasiModel.class);
                    layananInformasiModelArrayList.add(layananInformasiModel);
                }

                progressDialog.dismiss();

                adapter = new MyLayananInformasiAdapter(LayananInformasi.this);
                adapter.setLayananInformasiModelArrayList(layananInformasiModelArrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LayananInformasi.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DialogLogout() {
        AlertDialog.Builder dialog;
        LayoutInflater inflater;
        View dialogView;
        dialog = new AlertDialog.Builder(LayananInformasi.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_logout, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        final AlertDialog  alertDialog=dialog.create();
        Button bYa = dialogView.findViewById(R.id.btn_logout_ya);
        Button bTidak = dialogView.findViewById(R.id.btn_logout_tidak);

        bTidak.setOnClickListener(v -> alertDialog.dismiss());

        bYa.setOnClickListener(v -> {
            mAuth = FirebaseAuth.getInstance();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                mAuth.signOut();
                finish();
                startActivity(new Intent(LayananInformasi.this,LoginActivity.class));
            }else{
                Toast.makeText(LayananInformasi.this, "Logout gagal!", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

}