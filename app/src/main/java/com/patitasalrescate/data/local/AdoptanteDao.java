package com.patitasalrescate.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdoptanteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdoptanteEntity> adoptantes);

    @Query("SELECT * FROM adoptantes")
    List<AdoptanteEntity> getAll();

    @Query("SELECT * FROM adoptantes WHERE idAdoptante = :id")
    AdoptanteEntity getById(String id);

    @Update
    void update(AdoptanteEntity adoptante);
}