package it.univr.diabete.dao;

import it.univr.diabete.model.Assunzione;
import java.util.List;

public interface AssunzioneDAO {

    /**
     * Restituisce tutte le assunzioni di uno specifico farmaco
     * all'interno di una terapia, per un certo paziente.
     *
     * @param pazienteId        id del paziente
     * @param fkFarmaco  id della riga farmaco
     * @param fkTerapia  id della riga terapia
     */
    List<Assunzione> findByPazienteAndTerapiaAndFarmaco(String pazienteId,
                                                        int fkFarmaco, int fkTerapia) throws Exception;

    /**
     * Inserisce una nuova riga di assunzione.
     */
    void insert(Assunzione a) throws Exception;

    /**
     * Aggiorna una assunzione esistente (popup di modifica).
     */
    void update(Assunzione a) throws Exception;

    /**
     * Elimina una assunzione.
     */
    void delete(int id) throws Exception;

    /**
     * Restituisce tutte le assunzioni di un paziente in un intervallo di date (inclusivo).
     */
    List<Assunzione> findByPazienteAndDateRange(String pazienteId, java.time.LocalDate start, java.time.LocalDate end) throws Exception;
}
