package it.univr.diabete.dao;

import it.univr.diabete.model.Glicemia;
import java.util.List;

public interface GlicemiaDAO {

    List<Glicemia> findByPazienteId(int idPaziente) throws Exception;

    // ⭐ AGGIUNGERE QUESTO:
    List<Glicemia> findAll() throws Exception;

    void insert(Glicemia g) throws Exception;

    void update(Glicemia g) throws Exception;

    void delete(int id) throws Exception;
}