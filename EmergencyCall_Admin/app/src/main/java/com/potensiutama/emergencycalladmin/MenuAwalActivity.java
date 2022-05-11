package com.potensiutama.emergencycalladmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.potensiutama.emergencycalladmin.Common.Common;
import com.potensiutama.emergencycalladmin.Menu.DaftarLokasiActivity;
import com.potensiutama.emergencycalladmin.Menu.LayananInformasi;

public class MenuAwalActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_awal);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
        } else {
            requestPermission();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        ImageView bHome = findViewById(R.id.btn_home);
        ImageView bLayananInformasi = findViewById(R.id.btn_layanan_informasi);
        ImageView bDaftarLokasi = findViewById(R.id.btn_daftar_lokasi);
        ImageView bEmergencyCall = findViewById(R.id.btn_emergency);
        ImageView bLogout = findViewById(R.id.btn_logout);

        bLayananInformasi.setOnClickListener(view -> {
            Intent intent = new Intent(MenuAwalActivity.this, LayananInformasi.class);
            startActivity(intent);
        });

        bDaftarLokasi.setOnClickListener(view -> {
            Intent intent = new Intent(MenuAwalActivity.this, DaftarLokasiActivity.class);
            startActivity(intent);
        });

        bEmergencyCall.setOnClickListener(view -> {
            Intent intent = new Intent(MenuAwalActivity.this, MapEmergencyCall.class);
            startActivity(intent);
        });

        bLogout.setOnClickListener(view -> DialogLogout());

        TextView tEmergency,tLokasi,tInformasi;

        tEmergency = findViewById(R.id.txt_home_emergency);
        tLokasi = findViewById(R.id.txt_home_daftar_lokasi);
        tInformasi = findViewById(R.id.txt_home_layanan_informasi);

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("DaftarLokasi");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count= dataSnapshot.getChildrenCount();
                tLokasi.setText(String.valueOf(count)+ " Data\nDaftar Lokasi");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference mDatabaseRef2 = FirebaseDatabase.getInstance().getReference("Pasien");

        mDatabaseRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot3) {
                long count3= dataSnapshot3.getChildrenCount();
                tEmergency.setText(String.valueOf(count3)+ " Data\nEmergency Call");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference mDatabaseRef3 = FirebaseDatabase.getInstance().getReference("Informasi");

        mDatabaseRef3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                long count2= dataSnapshot2.getChildrenCount();
                tInformasi.setText(String.valueOf(count2)+ " Data\nLayanan Informasi");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signOut() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username","");
        editor.putString("password","");
        editor.apply();
        Intent intent = new Intent(MenuAwalActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onLocationChanged(Location location) {
        Common.currentLat = location.getLatitude();
        Common.currentLong = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    (dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermission();
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MenuAwalActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void DialogLogout() {
        AlertDialog.Builder dialog;
        LayoutInflater inflater;
        View dialogView;
        dialog = new AlertDialog.Builder(MenuAwalActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_logout, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        final AlertDialog  alertDialog=dialog.create();
        Button bYa = dialogView.findViewById(R.id.btn_logout_ya);
        Button bTidak = dialogView.findViewById(R.id.btn_logout_tidak);

        bTidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        bYa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        alertDialog.show();
    }

}