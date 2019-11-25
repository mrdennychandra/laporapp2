package id.ac.unpad.laporapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

//DDL tabel lapor
@Entity
public class Lapor implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public long id;
    public String path;
    public String keterangan;
    public Date waktu;
    public String lokasi;
    public String pil;
    public int sent;//flag 1 = terkirim,0=belum
}
