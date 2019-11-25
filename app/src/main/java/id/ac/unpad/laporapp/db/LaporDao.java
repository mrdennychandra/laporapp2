package id.ac.unpad.laporapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.ac.unpad.laporapp.model.Lapor;

//Operasi CRUD
@Dao
public interface LaporDao {

    @Query("SELECT * FROM lapor")
    List<Lapor> getAll();

    @Query("SELECT * FROM lapor WHERE id=:id")
    Lapor getById(Long id);

    @Insert
    void insert(Lapor lapor);

    @Update
    void update(Lapor lapor);

    @Delete
    void delete(Lapor lapor);

    @Query("SELECT COUNT(*) from lapor")
    Integer count();

}
