package it.univr.diabete.dao;

import it.univr.diabete.model.Terapia;
import java.util.List;

public interface TerapiaDAO {

    /**
     * Restituisce tutte le terapie assegnate a un paziente.
     */
    List<Terapia> findByPazienteId(int idPaziente) throws Exception;
}