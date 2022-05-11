package com.potensiutama.emergencycalladmin.Model;

public class EmergencyCallModel {
    private String key;
    private String nama;
    private String alamat;
    private Double latitude, longitude;
    private boolean finished;
    private Double haversine;

    public EmergencyCallModel() {
    }

    public EmergencyCallModel(String key, String nama, String alamat, Double latitude, Double longitude, boolean finished, Double haversine) {
        this.key = key;
        this.nama = nama;
        this.alamat = alamat;
        this.latitude = latitude;
        this.longitude = longitude;
        this.finished = finished;
        this.haversine = haversine;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Double getHaversine() {
        return haversine;
    }

    public void setHaversine(Double haversine) {
        this.haversine = haversine;
    }
}
