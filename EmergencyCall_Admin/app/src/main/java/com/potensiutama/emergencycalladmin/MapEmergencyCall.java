package com.potensiutama.emergencycalladmin;

import static android.text.TextUtils.isEmpty;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.potensiutama.emergencycalladmin.Adapter.MyDaftarLokasiAdapter;
import com.potensiutama.emergencycalladmin.Common.Common;
import com.potensiutama.emergencycalladmin.Menu.DaftarLokasiActivity;
import com.potensiutama.emergencycalladmin.Model.DaftarLokasiModel;
import com.potensiutama.emergencycalladmin.Model.EmergencyCallModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapEmergencyCall extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;

    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;

    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    private ArrayList<EmergencyCallModel> daftarLokasiModelArrayList;
    DatabaseReference dbSOS;

    TextView txtNamaSOS, txtAlamatSOS;
    LinearLayout lnLokasiSOS;

    ImageButton bClose;

    Button bNavigasi,bSelesaiEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbSOS = FirebaseDatabase.getInstance().getReference("Pasien");

        daftarLokasiModelArrayList = new ArrayList<>();

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map_emergency_call);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        bSelesaiEmergency = findViewById(R.id.btn_emergency_selesai);
        bNavigasi = findViewById(R.id.startButton);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.TRAFFIC_DAY, style -> {
            enableLocationComponent(style);

            addDestinationIconSymbolLayer(style);

            mapboxMap.addOnMapClickListener(MapEmergencyCall.this);

            txtNamaSOS = findViewById(R.id.txt_nama_lokasi);
            txtAlamatSOS = findViewById(R.id.txt_alamat_lokasi);
            lnLokasiSOS = findViewById(R.id.linearLayout3);
            lnLokasiSOS.setVisibility(View.GONE);

            bClose = findViewById(R.id.close_button);

            bNavigasi.setOnClickListener(v -> {
                bSelesaiEmergency.setVisibility(View.VISIBLE);
                bNavigasi.setVisibility(View.GONE);

                boolean simulateRoute = true;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();
                NavigationLauncher.startNavigation(MapEmergencyCall.this, options);
            });

            bSelesaiEmergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDataFirebase();
                }
            });

            bClose.setOnClickListener(v -> {
                navigationMapRoute.removeRoute();
                lnLokasiSOS.setVisibility(View.GONE);
                bSelesaiEmergency.setVisibility(View.GONE);
                bNavigasi.setVisibility(View.VISIBLE);
            });

            mapboxMap.setOnMarkerClickListener(marker -> {

                for(int i =0;i<daftarLokasiModelArrayList.size();i++){
                    if(daftarLokasiModelArrayList.get(i).getLatitude() == marker.getPosition().getLatitude() && daftarLokasiModelArrayList.get(i).getLongitude() == marker.getPosition().getLongitude() ){
                        Common.emergencyCallSelected = daftarLokasiModelArrayList.get(i);
                    }
                }

                Point destinationPoint = Point.fromLngLat(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());
                Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                        locationComponent.getLastKnownLocation().getLatitude());

                GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                if (source != null) {
                    source.setGeoJson(Feature.fromGeometry(destinationPoint));
                    lnLokasiSOS.setVisibility(View.VISIBLE);
                }

                txtNamaSOS.setText(marker.getTitle());
                txtAlamatSOS.setText(marker.getSnippet());

                getRoute(originPoint, destinationPoint);
                bSelesaiEmergency.setVisibility(View.GONE);
                bNavigasi.setVisibility(View.VISIBLE);
                return false;
            });

            dbSOS.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    daftarLokasiModelArrayList.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        EmergencyCallModel emergencyCallModel = dataSnapshot1.getValue(EmergencyCallModel.class);
                        if(!emergencyCallModel.isFinished()){
                            daftarLokasiModelArrayList.add(emergencyCallModel);
                            createMarkerMap(emergencyCallModel.getLatitude(), emergencyCallModel.getLongitude(), emergencyCallModel.getNama(), emergencyCallModel.getAlamat());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MapEmergencyCall.this, "Terjadi kesalahan.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return true;
    }


    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
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
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
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


    protected void createMarkerMap(double latitude, double longitude, String title, String snippet) {
        int height = 64;
        int width = 64;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.sosmapicon);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        IconFactory mIconFactory = IconFactory.getInstance(this);
        Icon icon = mIconFactory.fromBitmap(smallMarker);

        MarkerOptions options = new MarkerOptions();
        options.title(title);
        options.setSnippet(snippet);
        options.position(new LatLng(latitude, longitude));
        options.icon(icon);

        mapboxMap.addMarker(options);
    }

    private void updateDataFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getReference;

        getReference = database.getReference();

        EmergencyCallModel emergencyCallModel = Common.emergencyCallSelected;
        emergencyCallModel.setFinished(true);

        getReference.child("Pasien").child(Common.emergencyCallSelected.getKey())
                .setValue(emergencyCallModel)
                .addOnSuccessListener(this, (OnSuccessListener) o -> {
                    Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show();
                    finish();
                    });
    }


}