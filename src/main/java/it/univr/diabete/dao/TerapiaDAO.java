package it.univr.diabete.dao;

import it.univr.diabete.model.Terapia;
import java.util.List;

public interface TerapiaDAO {

    /**
     * Restituisce tutte le terapie assegnate a un paziente.
     * (Ogni Terapia conterrà anche la lista dei TerapiaFarmaco associati)
     */
    List<Terapia> findByPazienteId(int idPaziente) throws Exception;

    void insert(Terapia terapia) throws Exception;

    void update(Terapia terapia) throws Exception;
}