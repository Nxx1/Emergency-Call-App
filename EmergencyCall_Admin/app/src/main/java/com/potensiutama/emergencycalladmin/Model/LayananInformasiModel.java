package com.potensiutama.emergencycalladmin.Model;

import java.sql.Timestamp;

public class LayananInformasiModel {
    private String key;
    private String nama,penjelasan,image;
    private Timestamp timestamp;

    public LayananInformasiModel() {
    }

    public LayananInformasiModel(String key, String nama, String penjelasan, String image, Timestamp timestamp) {
        this.key = key;
        this.nama = nama;
        this.penjelasan = penjelasan;
        this.image = image;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPenjelasan() {
        return penjelasan;
    }

    public void setPenjelasan(String penjelasan) {
        this.penjelasan = penjelasan;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
