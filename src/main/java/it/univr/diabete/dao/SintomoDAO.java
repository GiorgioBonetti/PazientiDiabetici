package it.univr.diabete.dao;

import it.univr.diabete.model.Sintomo;
import java.time.LocalDate;
import java.util.List;

public interface SintomoDAO {
    List<Sintomo> findByPazienteAndDate(String fkPaziente, LocalDate date) throws Exception;

    Sintomo findLatestByPaziente(String fkPaziente) throws Exception;

    void insert(Sintomo sintomo) throws Exception;
}
