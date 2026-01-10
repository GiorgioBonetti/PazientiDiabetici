package it.univr.diabete.dao;

import it.univr.diabete.model.Patologia;
import java.util.List;

public interface PatologiaDAO {
    List<Patologia> findByPaziente(String fkPaziente) throws Exception;

    void insert(Patologia patologia) throws Exception;

    void delete(int id) throws Exception;

    boolean existsByPazienteAndNome(String fkPaziente, String nome) throws Exception;
}
