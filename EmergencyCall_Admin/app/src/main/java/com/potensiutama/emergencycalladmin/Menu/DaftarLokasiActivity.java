package com.potensiutama.emergencycalladmin.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.emergencycalladmin.Adapter.MyDaftarLokasiAdapter;
import com.potensiutama.emergencycalladmin.Common.Common;
import com.potensiutama.emergencycalladmin.LocationPickerActivity;
import com.potensiutama.emergencycalladmin.LoginActivity;
import com.potensiutama.emergencycalladmin.MapEmergencyCall;
import com.potensiutama.emergencycalladmin.MenuAwalActivity;
import com.potensiutama.emergencycalladmin.Model.DaftarLokasiModel;
import com.potensiutama.emergencycalladmin.R;

import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;

public class DaftarLokasiActivity extends AppCompatActivity implements View.OnClickListener {

    private int PLACE_PICKER_REQUEST = 1;

    FloatingActionButton fab;
    AlertDialog.Builder dialog;
    LayoutInflater inflater;
    View dialogView;
    EditText txt_nama,txt_alamat,txt_lat,txt_long;

    private ListView listView;

    private MyDaftarLokasiAdapter adapter;
    private ArrayList<DaftarLokasiModel> daftarLokasiModelArrayList;
    DatabaseReference dbLokasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_lokasi);

        dbLokasi = FirebaseDatabase.getInstance().getReference("DaftarLokasi");

        listView = findViewById(R.id.lv_list);

        daftarLokasiModelArrayList = new ArrayList<>();

        fab = (FloatingActionButton) findViewById(R.id.fab_tambah_daftar_lokasi);
        fab.setOnClickListener(view -> {
            Common.daftarLokasiActivity = DaftarLokasiActivity.this;
            startActivity(new Intent(DaftarLokasiActivity.this, LocationPickerActivity.class));
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Common.daftarLokasiSelected = daftarLokasiModelArrayList.get(i);
            UpdateForm();
        });

        ImageView bHome = findViewById(R.id.btn_home);
        ImageView bLayananInformasi = findViewById(R.id.btn_layanan_informasi);
        ImageView bDaftarLokasi = findViewById(R.id.btn_daftar_lokasi);
        ImageView bEmergencyCall = findViewById(R.id.btn_emergency);
        ImageView bLogout = findViewById(R.id.btn_logout);

        bHome.setOnClickListener(view -> finish());

        bLayananInformasi.setOnClickListener(view -> {
            Intent intent = new Intent(DaftarLokasiActivity.this, LayananInformasi.class);
            startActivity(intent);
            finish();
        });

        bEmergencyCall.setOnClickListener(view -> {
            Intent intent = new Intent(DaftarLokasiActivity.this, MapEmergencyCall.class);
            startActivity(intent);
        });

        bLogout.setOnClickListener(view -> DialogLogout());
    }


    private void signOut() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username","");
        editor.putString("password","");
        editor.apply();
        Intent intent = new Intent(DaftarLokasiActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbLokasi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                daftarLokasiModelArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    DaftarLokasiModel daftarLokasiModel = dataSnapshot1.getValue(DaftarLokasiModel.class);
                    daftarLokasiModelArrayList.add(daftarLokasiModel);
                }

                adapter = new MyDaftarLokasiAdapter(DaftarLokasiActivity.this);
                adapter.setDaftarLokasiList(daftarLokasiModelArrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DaftarLokasiActivity.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DialogForm() {
        dialog = new AlertDialog.Builder(DaftarLokasiActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_daftar_lokasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Form Tambah Lokasi");

        txt_nama    = (EditText) dialogView.findViewById(R.id.txt_nama_lokasi);
        txt_alamat    = (EditText) dialogView.findViewById(R.id.txt_alamat_lokasi);
        txt_lat  = (EditText) dialogView.findViewById(R.id.txt_lat_lokasi);
        txt_long = (EditText) dialogView.findViewById(R.id.txt_long_lokasi);

        txt_lat.setText(Common.selectedLatitude.toString());
        txt_long.setText(Common.selectedLongitude.toString());
        txt_alamat.setText(Common.selectedAlamat.toString());

        dialog.setPositiveButton("SUBMIT", (dialog, which) -> {
            addDataFirebase();
            dialog.dismiss();
        });

        dialog.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        dialog.show();
    }

    private void UpdateForm() {
        dialog = new AlertDialog.Builder(DaftarLokasiActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_daftar_lokasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Form Ubah Lokasi");

        txt_nama    = (EditText) dialogView.findViewById(R.id.txt_nama_lokasi);
        txt_alamat    = (EditText) dialogView.findViewById(R.id.txt_alamat_lokasi);
        txt_lat  = (EditText) dialogView.findViewById(R.id.txt_lat_lokasi);
        txt_long = (EditText) dialogView.findViewById(R.id.txt_long_lokasi);

        txt_nama.setText(Common.daftarLokasiSelected.getNama());
        txt_alamat.setText(Common.daftarLokasiSelected.getAlamat());
        txt_lat.setText(Common.daftarLokasiSelected.getLatitude().toString());
        txt_long.setText(Common.daftarLokasiSelected.getLongitude().toString());

        dialog.setPositiveButton("Ubah", (dialog, which) -> {
            updateDataFirebase();
            dialog.dismiss();
        });

        dialog.setNegativeButton("Hapus", (dialog, which) -> {
            deleteDataFirebase();
            dialog.dismiss();
        });

        dialog.setNeutralButton("Batal", (dialog, which) -> dialog.dismiss());

        dialog.show();
    }

    private void updateDataFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        if(isEmpty(txt_nama.getText()) && isEmpty(txt_alamat.getText()) && isEmpty(txt_lat.getText()) && isEmpty(txt_long.getText())){
            Toast.makeText(DaftarLokasiActivity.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
        }else {
            getReference.child("DaftarLokasi").child(Common.daftarLokasiSelected.getKey())
                    .setValue(new DaftarLokasiModel(txt_nama.getText().toString(), txt_alamat.getText().toString(), Double.parseDouble(txt_lat.getText().toString()) , Double.parseDouble(txt_long.getText().toString()) ,Common.daftarLokasiSelected.getKey()))
                    .addOnSuccessListener(this, (OnSuccessListener) o -> {
                        txt_nama.setText("");
                        txt_alamat.setText("");
                        txt_lat.setText("");
                        txt_long.setText("");
                        Toast.makeText(DaftarLokasiActivity.this, "Data Berhasil Diubah", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteDataFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        getReference.child("DaftarLokasi").child(Common.daftarLokasiSelected.getKey())
                .removeValue();

        Toast.makeText(DaftarLokasiActivity.this, "Data telah dihapus...",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                txt_alamat.setText(place.getAddress());
                txt_lat.setText(String.valueOf(place.getLatLng().latitude));
                txt_long.setText(String.valueOf(place.getLatLng().longitude));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addDataFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        if(isEmpty(txt_nama.getText()) && isEmpty(txt_alamat.getText()) && isEmpty(txt_lat.getText()) && isEmpty(txt_long.getText())){
            Toast.makeText(DaftarLokasiActivity.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
        }else {

            String key = getReference.child("DaftarLokasi").push().getKey();
            getReference.child("DaftarLokasi").child(key)
                    .setValue(new DaftarLokasiModel(txt_nama.getText().toString(), txt_alamat.getText().toString(), Double.parseDouble(txt_lat.getText().toString()), Double.parseDouble(txt_long.getText().toString()) ,key))
                    .addOnSuccessListener(this, (OnSuccessListener) o -> {
                        txt_nama.setText("");
                        txt_alamat.setText("");
                        txt_lat.setText("");
                        txt_long.setText("");
                        Toast.makeText(DaftarLokasiActivity.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void DialogLogout() {
        AlertDialog.Builder dialog;
        LayoutInflater inflater;
        View dialogView;
        dialog = new AlertDialog.Builder(DaftarLokasiActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_logout, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        final AlertDialog  alertDialog=dialog.create();
        Button bYa = dialogView.findViewById(R.id.btn_logout_ya);
        Button bTidak = dialogView.findViewById(R.id.btn_logout_tidak);

        bTidak.setOnClickListener(v -> alertDialog.dismiss());

        bYa.setOnClickListener(v -> signOut());

        alertDialog.show();
    }
}