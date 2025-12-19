package it.univr.diabete.dao;

import it.univr.diabete.model.FarmacoTerapia;

import java.util.List;

public interface FarmacoTerapiaDAO {

    /**
     * Restituisce tutti i farmaci (TerapiaFarmaco) associati a una terapia.
     */
    List<FarmacoTerapia> findByTerapiaId(int terapiaId) throws Exception;

    void insert(FarmacoTerapia tf) throws Exception;

    void update(FarmacoTerapia tf) throws Exception;

    void delete(int fkFarmaco, int fkTerapia, int fkVersioneTerapia) throws Exception;
}