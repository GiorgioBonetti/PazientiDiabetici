package it.univr.diabete.dao;

import it.univr.diabete.model.AssunzioneTerapia;

import java.util.List;

public interface AssunzioneTerapiaDAO {

    /**
     * Restituisce tutte le assunzioni di una certa terapia per un paziente.
     */
    List<AssunzioneTerapia> findByPazienteAndTerapia(int pazienteId, int terapiaId) throws Exception;

    /**
     * Inserisce una nuova riga di assunzione.
     */
    void insert(AssunzioneTerapia a) throws Exception;

    /**
     * Aggiorna una assunzione esistente (usata dal popup di modifica).
     */
    void update(AssunzioneTerapia a) throws Exception;

    /**
     * Elimina una assunzione (se in futuro vuoi il bottone elimina).
     */
    void delete(int id) throws Exception;
}