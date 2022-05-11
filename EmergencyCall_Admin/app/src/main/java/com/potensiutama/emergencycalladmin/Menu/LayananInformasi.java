package com.potensiutama.emergencycalladmin.Menu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.potensiutama.emergencycalladmin.Adapter.MyLayananInformasiAdapter;
import com.potensiutama.emergencycalladmin.Common.Common;
import com.potensiutama.emergencycalladmin.LoginActivity;
import com.potensiutama.emergencycalladmin.MapEmergencyCall;
import com.potensiutama.emergencycalladmin.Model.LayananInformasiModel;
import com.potensiutama.emergencycalladmin.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;

public class LayananInformasi extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 1234;

    FloatingActionButton fab;
    AlertDialog.Builder dialog;
    LayoutInflater inflater;
    View dialogView;
    EditText txt_nama, txt_penjelasan;
    ImageView imgFormInformasi;

    private ListView listView;

    private Uri imageUri = null;

    private MyLayananInformasiAdapter adapter;
    private ArrayList<LayananInformasiModel> layananInformasiModelArrayList;
    DatabaseReference dbInformasi;

    private ProgressDialog progressDialog;

    FirebaseStorage storage;
    StorageReference storageReference;

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

        fab = findViewById(R.id.fab_tambah_layanan_informasi);
        fab.setOnClickListener(view -> DialogForm());

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Common.layananInformasiSelected = layananInformasiModelArrayList.get(i);
            UpdateForm();
        });

        ImageView bHome = findViewById(R.id.btn_home);
        ImageView bDaftarLokasi = findViewById(R.id.btn_daftar_lokasi);
        ImageView bEmergencyCall = findViewById(R.id.btn_emergency);
        ImageView bLogout = findViewById(R.id.btn_logout);

        bHome.setOnClickListener(view -> finish());

        bDaftarLokasi.setOnClickListener(view -> {
            Intent intent = new Intent(LayananInformasi.this, DaftarLokasiActivity.class);
            startActivity(intent);
            finish();
        });

        bEmergencyCall.setOnClickListener(view -> {
            Intent intent = new Intent(LayananInformasi.this, MapEmergencyCall.class);
            startActivity(intent);
            finish();
        });

        bLogout.setOnClickListener(view -> DialogLogout());

    }


    private void signOut() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username","");
        editor.putString("password","");
        editor.apply();
        Intent intent = new Intent(LayananInformasi.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void DialogForm() {
        dialog = new AlertDialog.Builder(LayananInformasi.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_daftar_informasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Tambah Layanan Informasi");

        txt_nama = (EditText) dialogView.findViewById(R.id.txt_nama_informasi);
        txt_penjelasan = (EditText) dialogView.findViewById(R.id.txt_penjelasan_informasi);
        imgFormInformasi = dialogView.findViewById(R.id.informasi_img);


        imgFormInformasi.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);

        });

        dialog.setPositiveButton("SUBMIT", (dialog, which) -> {
            progressDialog.show();

            LayananInformasiModel layananInformasiModel = new LayananInformasiModel();
            layananInformasiModel.setNama(txt_nama.getText().toString());
            layananInformasiModel.setPenjelasan(txt_penjelasan.getText().toString());
            if (imageUri != null) {
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        layananInformasiModel.setImage(uri.toString());
                        addDataFirebase(layananInformasiModel);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }

            dialog.dismiss();
        });

        dialog.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        dialog.show();
    }

    private void UpdateForm() {
        dialog = new AlertDialog.Builder(LayananInformasi.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_daftar_informasi, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Form Ubah Informasi");

        txt_nama = dialogView.findViewById(R.id.txt_nama_informasi);
        txt_penjelasan = dialogView.findViewById(R.id.txt_penjelasan_informasi);
        imgFormInformasi = dialogView.findViewById(R.id.informasi_img);

        txt_nama.setText(Common.layananInformasiSelected.getNama());
        txt_penjelasan.setText(Common.layananInformasiSelected.getPenjelasan());
        Glide.with(getApplicationContext()).load(Common.layananInformasiSelected.getImage()).into(imgFormInformasi);

        dialog.setPositiveButton("Ubah", (dialog, which) -> {

            progressDialog.show();

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("nama",txt_nama.getText().toString());
            updateData.put("penjelasan",txt_penjelasan.getText().toString());
            updateData.put("key",Common.layananInformasiSelected.getKey());

            if (imageUri != null) {
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateData.put("image",uri.toString());
                        updateDataFirebase(updateData);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            } else {
                updateData.put("image",Common.layananInformasiSelected.getImage());
                updateDataFirebase(updateData);
            }
            dialog.dismiss();
        });

        dialog.setNegativeButton("Hapus", (dialog, which) -> {
            deleteDataFirebase();
            dialog.dismiss();
        });

        dialog.setNeutralButton("Batal", (dialog, which) -> dialog.dismiss());

        dialog.show();
    }

    private void updateDataFirebase(Map<String, Object> updateData) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        if (isEmpty(txt_nama.getText()) && isEmpty(txt_penjelasan.getText())) {
            Toast.makeText(LayananInformasi.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
        } else {
            getReference.child("Informasi").child(Common.layananInformasiSelected.getKey())
                    .setValue(updateData)
                    .addOnSuccessListener(this, (OnSuccessListener) o -> {
                        txt_nama.setText("");
                        txt_penjelasan.setText("");
                        imgFormInformasi.setImageURI(null);
                        Toast.makeText(LayananInformasi.this, "Data Berhasil Diubah", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(this, e -> Toast.makeText(LayananInformasi.this, "Update gagal!", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteDataFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        getReference.child("Informasi").child(Common.layananInformasiSelected.getKey())
                .removeValue();

        Toast.makeText(LayananInformasi.this, "Data telah dihapus...",
                Toast.LENGTH_SHORT).show();

        imageUri = null;
    }

    private void addDataFirebase(LayananInformasiModel layananInformasiModel) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        if (isEmpty(txt_nama.getText()) && isEmpty(txt_penjelasan.getText())) {
            Toast.makeText(LayananInformasi.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
        } else {

            String key = getReference.child("Informasi").push().getKey();

            layananInformasiModel.setKey(key);

            getReference.child("Informasi").child(key)
                    .setValue(layananInformasiModel)
                    .addOnSuccessListener(this, (OnSuccessListener) o -> {
                        txt_nama.setText("");
                        txt_penjelasan.setText("");
                        imgFormInformasi.setImageURI(null);
                        Toast.makeText(LayananInformasi.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LayananInformasi.this, "Proses gagal!", Toast.LENGTH_SHORT).show();
                }
            });

            progressDialog.dismiss();
        }

        imageUri = null;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imgFormInformasi.setImageURI(imageUri);
            }
        }
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

        bYa.setOnClickListener(v -> signOut());

        alertDialog.show();
    }
}