package it.univr.diabete.dao;

import it.univr.diabete.model.AssunzioneTerapia;
import java.util.List;

public interface AssunzioneTerapiaDAO {

    /**
     * Restituisce tutte le assunzioni di uno specifico farmaco
     * all'interno di una terapia, per un certo paziente.
     *
     * @param pazienteId        id del paziente
     * @param terapiaFarmacoId  id della riga TerapiaFarmaco
     */
    List<AssunzioneTerapia> findByPazienteAndTerapiaFarmaco(String pazienteId,
                                                            int terapiaFarmacoId) throws Exception;

    /**
     * Inserisce una nuova riga di assunzione.
     */
    void insert(AssunzioneTerapia a) throws Exception;

    /**
     * Aggiorna una assunzione esistente (popup di modifica).
     */
    void update(AssunzioneTerapia a) throws Exception;

    /**
     * Elimina una assunzione.
     */
    void delete(int id) throws Exception;
}