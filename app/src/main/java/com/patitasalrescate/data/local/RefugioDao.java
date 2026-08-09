package com.patitasalrescate.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RefugioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RefugioEntity> refugios);

    @Query("SELECT * FROM refugios")
    List<RefugioEntity> getAll();

    @Query("SELECT * FROM refugios WHERE idRefugio = :id")
    RefugioEntity getById(String id);

    @Update
    void update(RefugioEntity refugio);
}