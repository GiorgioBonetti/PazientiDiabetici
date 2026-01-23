package it.univr.diabete.dao;

import it.univr.diabete.model.Glicemia;
import java.util.List;

public interface GlicemiaDAO {

    List<Glicemia> findByPazienteId(String codiceFiscale) throws Exception;

    // ⭐ AGGIUNGERE QUESTO:
    List<Glicemia> findAll() throws Exception;

    List<Glicemia> findByPazienteIdAndDate(String codiceFiscale, java.time.LocalDate day) throws Exception;

    List<Glicemia> findByPazienteIdAndDateRange(String codiceFiscale, java.time.LocalDate start, java.time.LocalDate end) throws Exception;

    void insert(Glicemia g) throws Exception;

    void update(Glicemia g) throws Exception;

    void delete(int id) throws Exception;
}
