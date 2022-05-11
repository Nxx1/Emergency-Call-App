package com.potensiutama.emergencycallclient;

import static android.text.TextUtils.isEmpty;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.potensiutama.emergencycallclient.Common.Common;
import com.potensiutama.emergencycallclient.Model.DaftarLokasiModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SOSActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {

    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private ImageView hoveringMarker;
    private Layer droppedMarkerLayer;

    Double koordinatLongitude;
    Double koordinatLatitude;


    TextView tKoordinatLokasi,tNamaLokasi,tFixKoordinat,tFixNamaLokasi;
    EditText edtKeadaanDarurat;

    LinearLayout lnCreateSOS,lnGetKoordinat;

    Button bGantiLokasi,bCreateSOS,selectLocationButton;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_location_picker);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        tKoordinatLokasi = findViewById(R.id.txt_koordinat_lokasi);
        tNamaLokasi = findViewById(R.id.txt_nama_lokasi);
        tFixKoordinat = findViewById(R.id.txt_fix_koordinat_lokasi);
        tFixNamaLokasi = findViewById(R.id.txt_fix_nama_lokasi);

        edtKeadaanDarurat = findViewById(R.id.edt_nama_keadaan_darurat);

        lnCreateSOS = findViewById(R.id.ln_create_sos);
        lnGetKoordinat = findViewById(R.id.ln_get_koordinat);

        selectLocationButton = findViewById(R.id.select_location_button);
        bCreateSOS = findViewById(R.id.btn_create_sos);
        bGantiLokasi = findViewById(R.id.btn_ganti_lokasi);
    }

    @Override
    public void onBackPressed() {
        Common.selectedLongitude = null;
        Common.selectedLatitude =null;
        super.onBackPressed();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        SOSActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationPlugin(style);

            Toast.makeText(
                    SOSActivity.this,
                    "Drag peta untuk menentukan titik koordinat,\nSelanjutnya Klik tombol pilih lokasi", Toast.LENGTH_SHORT).show();

            hoveringMarker = new ImageView(SOSActivity.this);
            hoveringMarker.setImageResource(R.drawable.sosmapicon);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            hoveringMarker.setLayoutParams(params);
            mapView.addView(hoveringMarker);

            initDroppedMarker(style);

            LatLng mapLatLng = mapboxMap.getCameraPosition().target;
            Double tmpLng = mapLatLng.getLongitude();
            Double tmpLat = mapLatLng.getLatitude();

            tKoordinatLokasi.setText(tmpLat.toString()+ " , "+ tmpLng.toString());


            reverseGeocode(tmpLat,tmpLng);

            selectLocationButton.setOnClickListener(view -> {
                final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;

                hoveringMarker.setVisibility(View.INVISIBLE);

                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                    GeoJsonSource source = style.getSourceAs("dropped-marker-source-id");
                    if (source != null) {
                        source.setGeoJson(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));
                    }
                    droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                    if (droppedMarkerLayer != null) {
                        droppedMarkerLayer.setProperties(visibility(VISIBLE));
                    }
                }

                koordinatLongitude = mapTargetLatLng.getLongitude();
                koordinatLatitude = mapTargetLatLng.getLatitude();

                Common.selectedLatitude = koordinatLatitude;
                Common.selectedLongitude = koordinatLongitude;

                lnCreateSOS.setVisibility(View.VISIBLE);
                lnGetKoordinat.setVisibility(View.GONE);
                tFixKoordinat.setText(tmpLat.toString()+ " , "+ tmpLng.toString());
            });

            bGantiLokasi.setOnClickListener(v -> {
                lnGetKoordinat.setVisibility(View.VISIBLE);
                lnCreateSOS.setVisibility(View.GONE);

                hoveringMarker.setVisibility(View.VISIBLE);

                droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                if (droppedMarkerLayer != null) {
                    droppedMarkerLayer.setProperties(visibility(NONE));
                }

                Common.selectedLatitude = null;
                Common.selectedLongitude = null;
            });

            bCreateSOS.setOnClickListener(view -> {
                final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;

                koordinatLongitude = mapTargetLatLng.getLongitude();
                koordinatLatitude = mapTargetLatLng.getLatitude();

                Common.selectedLatitude = koordinatLatitude;
                Common.selectedLongitude = koordinatLongitude;

                addFirebaseSOS();
            });
        });

        mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector detector) {
            }

            @Override
            public void onMove(@NonNull MoveGestureDetector detector) {
            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector detector) {
                LatLng mapLatLng = mapboxMap.getCameraPosition().target;
                Double tmpLng = mapLatLng.getLongitude();
                Double tmpLat = mapLatLng.getLatitude();
                tKoordinatLokasi.setText(tmpLat.toString()+ " , "+ tmpLng.toString());
                reverseGeocode(tmpLat,tmpLng);
            }
        });
    }



    private void initDroppedMarker(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("dropped-icon-image", BitmapFactory.decodeResource(
                getResources(), R.drawable.sosmapicon));
        loadedMapStyle.addSource(new GeoJsonSource("dropped-marker-source-id"));
        loadedMapStyle.addLayer(new SymbolLayer(DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id").withProperties(
                iconImage("dropped-icon-image"),
                visibility(NONE),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        ));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted && mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                enableLocationPlugin(style);
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void reverseGeocode(Double kLat, Double kLng) {
        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .query(Point.fromLngLat(kLng, kLat))
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    Log.d("GEOCODE",response.toString());
                    if (response.body() != null) {
                        tNamaLokasi.setText(response.body().features().get(0).placeName());
                        tFixNamaLokasi.setText(response.body().features().get(0).placeName());
                    }else{
                        tNamaLokasi.setText("");
                        tFixNamaLokasi.setText("");
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: %s", throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
                    this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void addFirebaseSOS(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        if(isEmpty(edtKeadaanDarurat.getText()) && isEmpty(tFixKoordinat.getText()) && isEmpty(tFixNamaLokasi.getText())){
            Toast.makeText(SOSActivity.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
        }else {

            String key = getReference.child("Pasien").push().getKey();

            getReference.child("Pasien").child(key)
                    .setValue(new DaftarLokasiModel(edtKeadaanDarurat.getText().toString(), tFixNamaLokasi.getText().toString(), Common.selectedLatitude, Common.selectedLongitude ,key))
                    .addOnSuccessListener(SOSActivity.this, (OnSuccessListener) o -> {
                        Toast.makeText(SOSActivity.this, "Sukses", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

}