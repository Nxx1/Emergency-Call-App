package com.potensiutama.emergencycallclient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.potensiutama.emergencycallclient.Model.DaftarLokasiModel;
import com.potensiutama.emergencycallclient.R;
import java.util.ArrayList;

public class MyDaftarLokasiAdapter extends BaseAdapter {
    Context context;
    private ArrayList<DaftarLokasiModel> daftarLokasiModelArrayList = new ArrayList<>();

    public void setDaftarLokasiList(ArrayList<DaftarLokasiModel> daftarLokasiModelArrayList) {
        this.daftarLokasiModelArrayList = daftarLokasiModelArrayList;
    }

    public MyDaftarLokasiAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return daftarLokasiModelArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return daftarLokasiModelArrayList.get(i);
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
                    .inflate(R.layout.carditem_daftarlokasi, viewGroup, false);
        }
        ViewHolder viewHolder = new ViewHolder(itemView);
        DaftarLokasiModel daftarLokasiModel = (DaftarLokasiModel) getItem(i);
        viewHolder.bind(daftarLokasiModel);
        return itemView;
    }

    private class ViewHolder {
        private TextView txtNama, txtAlamat,txtLatLong;

        ViewHolder(View view) {
            txtNama = view.findViewById(R.id.txt_cv_namaspbu);
            txtAlamat = view.findViewById(R.id.txt_cv_alamatspbu);
            txtLatLong = view.findViewById(R.id.txt_cv_latlongspbu);
        }

        void bind(DaftarLokasiModel daftarLokasiModel) {
            txtNama.setText(daftarLokasiModel.getNama());
            txtAlamat.setText(daftarLokasiModel.getAlamat());
            txtLatLong.setText(daftarLokasiModel.getLatitude().toString() + " | " + daftarLokasiModel.getLongitude().toString() );

        }
    }
}
