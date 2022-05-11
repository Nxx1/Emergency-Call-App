package com.potensiutama.emergencycallclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.potensiutama.emergencycallclient.Auth.LoginActivity;
import com.potensiutama.emergencycallclient.Common.Common;
import com.potensiutama.emergencycallclient.Model.LayananInformasiModel;

import java.util.ArrayList;

public class MenuAwalActivity extends AppCompatActivity {
    DatabaseReference dbInformasi;

    FirebaseStorage storage;
    StorageReference storageReference;

    ImageButton btnSOS,bLayananInformasi,bDaftarLokasi,bLogout;

    CardView sosAmbulans,sosPolisi,sosPemadam,sosSAR;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_awal);

        bLayananInformasi = findViewById(R.id.home_btn_informasi);
        bDaftarLokasi = findViewById(R.id.home_btn_maps);
        bLogout = findViewById(R.id.home_btn_logout);

        bLayananInformasi.setOnClickListener(view -> {
            Intent intent = new Intent(MenuAwalActivity.this, LayananInformasi.class);
            startActivity(intent);
        });

        bDaftarLokasi.setOnClickListener(view -> {
            Intent intent = new Intent(MenuAwalActivity.this, DaftarLokasiActivity.class);
            startActivity(intent);
        });

        btnSOS = findViewById(R.id.home_btn_sos);

        btnSOS.setOnClickListener(v -> {
            Common.menuAwalActivity = MenuAwalActivity.this;
            startActivity(new Intent(MenuAwalActivity.this, SOSActivity.class));
        });

        bLogout.setOnClickListener(v -> {

            DialogLogout();

        });

        LoadLastInformasi();

        sosAmbulans = findViewById(R.id.sos_ambulans);
        sosPolisi = findViewById(R.id.sos_polisi);
        sosPemadam = findViewById(R.id.sos_pemadam);
        sosSAR = findViewById(R.id.sos_sar);

        sosAmbulans.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+118));
            startActivity(callIntent);
        });

        sosPolisi.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+110));
            startActivity(callIntent);
        });

        sosPemadam.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+113));
            startActivity(callIntent);
        });

        sosSAR.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+115));
            startActivity(callIntent);
        });


    }

    private void LoadLastInformasi(){

        ArrayList<LayananInformasiModel> layananInformasiModelArrayList = new ArrayList<>();

        dbInformasi = FirebaseDatabase.getInstance().getReference("Informasi");

        CardView cvInformasi1 = findViewById(R.id.cv_informasi1);
        CardView cvInformasi2 = findViewById(R.id.cv_informasi2);



        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbInformasi.limitToLast(2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                layananInformasiModelArrayList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    LayananInformasiModel layananInformasiModel = dataSnapshot1.getValue(LayananInformasiModel.class);
                    layananInformasiModelArrayList.add(layananInformasiModel);
                }



                ImageView img1 = findViewById(R.id.informasi_card_img1);
                TextView tPenjelasan1 = findViewById(R.id.txt_penjelasan_informasi1);
                ImageView img2 = findViewById(R.id.informasi_card_img2);
                TextView tPenjelasan2 = findViewById(R.id.txt_penjelasan_informasi2);

                tPenjelasan1.setText(layananInformasiModelArrayList.get(0).getNama().replace(" ","\n"));
                Glide.with(MenuAwalActivity.this).load(layananInformasiModelArrayList.get(0).getImage()).into(img1);
                tPenjelasan2.setText(layananInformasiModelArrayList.get(1).getNama().replace(" ","\n"));
                Glide.with(MenuAwalActivity.this).load(layananInformasiModelArrayList.get(1).getImage()).into(img2);

                cvInformasi1.setOnClickListener(v -> DialogInformasi(layananInformasiModelArrayList.get(0)));
                cvInformasi2.setOnClickListener(v -> DialogInformasi(layananInformasiModelArrayList.get(1)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MenuAwalActivity.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DialogInformasi(LayananInformasiModel layananInformasiModel) {
        AlertDialog.Builder dialog;
        LayoutInflater inflater;
        View dialogView;
        dialog = new AlertDialog.Builder(MenuAwalActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_penjelasan_informasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);

        TextView tJudul = dialogView.findViewById(R.id.txt_title_informasi);
        TextView tPenjelasan = dialogView.findViewById(R.id.txt_deskripsi_informasi);
        ImageView imgInformasi = dialogView.findViewById(R.id.img_informasi);

        tJudul.setText(layananInformasiModel.getNama());
        tPenjelasan.setText(layananInformasiModel.getPenjelasan());
        Glide.with(MenuAwalActivity.this).load(layananInformasiModel.getImage()).into(imgInformasi);


        dialog.setNeutralButton("Tutup", (dialog1, which) -> dialog1.dismiss());


        dialog.show();
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

        bTidak.setOnClickListener(v -> alertDialog.dismiss());

        bYa.setOnClickListener(v -> {
            mAuth = FirebaseAuth.getInstance();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                mAuth.signOut();
                finish();
                startActivity(new Intent(MenuAwalActivity.this,LoginActivity.class));
            }else{
                Toast.makeText(MenuAwalActivity.this, "Logout gagal!", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

}