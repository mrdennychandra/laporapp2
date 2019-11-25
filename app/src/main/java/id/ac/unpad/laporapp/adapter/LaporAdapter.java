package id.ac.unpad.laporapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;

import id.ac.unpad.laporapp.R;
import id.ac.unpad.laporapp.model.Lapor;

//controller antara data dan row_lapor
//1. Mengetahui row_lapor (layoutnya yg mana)
//2. Mengetahui id setiap komponen dan mengisinya
//3. Mengetahui jumlah data
public class LaporAdapter extends
        RecyclerView.Adapter<LaporAdapter.ViewHolder>{

    //digunakan di activity yang mana
    Context context;
    //array dari lapor
    List<Lapor> lapors;
    //mengisikan data
    public LaporAdapter(Context context,List<Lapor> lapors){
        this.context = context;
        this.lapors = lapors;
    }

    //1.mengetahui layoutnya
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_lapor, parent, false);
        return new ViewHolder(view);
    }

    //2. mengisi id nya
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lapor lapor = lapors.get(position);
        holder.txtPil.setText(lapor.pil);
        holder.txtKeterangan.setText(lapor.keterangan);
        holder.txtLokasi.setText(lapor.lokasi);
        if(lapor.waktu!= null) {
            holder.txtWaktu.setText(new
                    SimpleDateFormat("dd-MM-yyyy").format(lapor.waktu));
        }
        if(lapor.path != null){
            Glide.with(context).load(lapor.path)
                    .into(holder.imgLapor);
        }
        if(lapor.sent == 1){
            //
        }
    }

    //mengetahui jumlah datanya
    @Override
    public int getItemCount() {
        return lapors != null ? lapors.size() : 0;
    }

    //mengambil setiap id dr layout
    class ViewHolder extends RecyclerView.ViewHolder{
        //deklarasi variabel
        TextView txtPil,txtKeterangan,txtWaktu,txtLokasi;
        ImageView imgLapor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPil = itemView.findViewById(R.id.txt_pil);
            txtKeterangan = itemView.findViewById(R.id.txt_keterangan);
            txtWaktu = itemView.findViewById(R.id.txt_waktu);
            txtLokasi = itemView.findViewById(R.id.txt_lokasi);
            imgLapor = itemView.findViewById(R.id.img_lapor);
        }
    }
}
