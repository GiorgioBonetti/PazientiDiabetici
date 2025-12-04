package it.univr.diabete.dao;

import it.univr.diabete.model.Paziente;

import java.util.List;

public interface PazienteDAO {

    Paziente findByEmailAndPassword(String email, String password) throws Exception;

    List<Paziente> findAll() throws Exception;

    Paziente findById(int id) throws Exception;

    void insert(Paziente paziente) throws Exception;

    void update(Paziente paziente) throws Exception;
}
