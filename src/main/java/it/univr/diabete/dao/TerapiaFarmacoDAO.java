package it.univr.diabete.dao;

import it.univr.diabete.model.TerapiaFarmaco;

import java.util.List;

public interface TerapiaFarmacoDAO {

    /**
     * Restituisce tutti i farmaci (TerapiaFarmaco) associati a una terapia.
     */
    List<TerapiaFarmaco> findByTerapiaId(int terapiaId) throws Exception;

    void insert(TerapiaFarmaco tf) throws Exception;

    void update(TerapiaFarmaco tf) throws Exception;

    void delete(int id) throws Exception;
}