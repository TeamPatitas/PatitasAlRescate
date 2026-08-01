package com.patitasalrescate.data.repository;

import android.content.Context;
import android.util.Log;

import com.patitasalrescate.data_access.ApiRefugiosSimulada; // o el nombre que uses para llamar a la nueva API
import com.patitasalrescate.data.local.AppDatabase;
import com.patitasalrescate.data.local.RefugioDao;
import com.patitasalrescate.data.local.RefugioEntity;
import com.patitasalrescate.model.Refugio;

import java.util.ArrayList;
import java.util.List;

public class RefugioRepository {

    private final RefugioDao localDao;
    private final ApiRefugiosSimulada api;   // Cambia si tu clase se llama diferente

    public RefugioRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        localDao = db.refugioDao();
        api = new ApiRefugiosSimulada();   // Ajusta según el nombre real de tu clase
    }

    // Carga rápida desde caché local (offline)
    public List<Refugio> getAllCached() {
        List<RefugioEntity> entities = localDao.getAll();
        List<Refugio> result = new ArrayList<>();
        for (RefugioEntity e : entities) {
            result.add(convertToModel(e));
        }
        return result;
    }

    // Sincronización con la nueva API
    public void syncAll() {
        new Thread(() -> {
            try {
                Log.d("RefugioRepository", "Sincronizando con API: " + api.getClass().getSimpleName());

                List<Refugio> fromApi = api.getRefugios();   // Llama a tu nueva API

                if (fromApi != null && !fromApi.isEmpty()) {
                    List<RefugioEntity> entities = new ArrayList<>();
                    for (Refugio r : fromApi) {
                        entities.add(new RefugioEntity(r));
                    }
                    localDao.insertAll(entities);
                    Log.d("RefugioRepository", "Sincronizados " + fromApi.size() + " refugios");
                }
            } catch (Exception e) {
                Log.e("RefugioRepository", "Error al sincronizar: " + e.getMessage());
            }
        }).start();
    }

    private Refugio convertToModel(RefugioEntity entity) {
        Refugio r = new Refugio();
        r.setIdRefugio(entity.getIdRefugio());
        r.setNombre(entity.getNombre());
        r.setDireccion(entity.getDireccion());
        r.setLatitud(entity.getLatitud());
        r.setLongitud(entity.getLongitud());
        r.setCorreo(entity.getCorreo());
        r.setNumCelular(entity.getNumCelular());
        r.setFotoUrl(entity.getFotoUrl());
        r.setLastSync(entity.getLastSync());
        return r;
    }
}