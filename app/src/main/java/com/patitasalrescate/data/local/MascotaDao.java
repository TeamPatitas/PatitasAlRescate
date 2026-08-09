package com.patitasalrescate.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MascotaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MascotaEntity> mascotas);

    @Query("SELECT * FROM mascotas")
    List<MascotaEntity> getAll();

    @Query("SELECT * FROM mascotas WHERE idMascota = :id")
    MascotaEntity getById(String id);

    @Query("SELECT * FROM mascotas WHERE idRefugio = :idRefugio")
    List<MascotaEntity> getByRefugio(String idRefugio);

    @Update
    void update(MascotaEntity mascota);
}