package it.univr.diabete.dao;

import it.univr.diabete.model.Paziente;

import java.util.List;

public interface PazienteDAO {

    Paziente findByEmailAndPassword(String email, String password) throws Exception;

    List<Paziente> findAll() throws Exception;

    List<Paziente> findByDiabetologo(String diabetologoId) throws Exception;

    Paziente findById(String CodiceFiscale) throws Exception;

    void insert(Paziente paziente) throws Exception;

    void update(Paziente paziente) throws Exception;

    void deleteById(String codiceFiscale) throws Exception;
}
