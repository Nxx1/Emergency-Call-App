package com.potensiutama.emergencycalladmin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.potensiutama.emergencycalladmin.Model.LayananInformasiModel;
import com.potensiutama.emergencycalladmin.R;

import java.util.ArrayList;

public class MyLayananInformasiAdapter extends BaseAdapter {
    Context context;
    private ArrayList<LayananInformasiModel> layananInformasiModelArrayList = new ArrayList<>();

    public void setLayananInformasiModelArrayList(ArrayList<LayananInformasiModel> layananInformasiModelArrayList) {
        this.layananInformasiModelArrayList = layananInformasiModelArrayList;
    }

    public MyLayananInformasiAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return layananInformasiModelArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return layananInformasiModelArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;

        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.carditem_layanan_informasi, viewGroup, false);
        }

        ViewHolder viewHolder = new ViewHolder(itemView);

        LayananInformasiModel layananInformasiModel = (LayananInformasiModel) getItem(i);
        viewHolder.bind(layananInformasiModel);
        return itemView;
    }

    private class ViewHolder {
        private TextView txtNama, txtPenjelasan;
        private ImageView imgInformasi;

        ViewHolder(View view) {
            txtNama = view.findViewById(R.id.informasi_card_nama);
            txtPenjelasan = view.findViewById(R.id.informasi_card_penjelasan);
            imgInformasi = view.findViewById(R.id.informasi_card_img);
        }

        void bind(LayananInformasiModel layananInformasiModel) {
            txtNama.setText(layananInformasiModel.getNama());
            txtPenjelasan.setText(layananInformasiModel.getPenjelasan());
            Glide.with(context).load(layananInformasiModel.getImage()).into(imgInformasi);
        }
    }
}