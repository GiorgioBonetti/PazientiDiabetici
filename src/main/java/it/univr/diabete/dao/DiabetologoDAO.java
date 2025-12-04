package it.univr.diabete.dao;

import it.univr.diabete.model.Diabetologo;

public interface DiabetologoDAO {
    Diabetologo findByEmailAndPassword(String email, String password) throws Exception;
}
