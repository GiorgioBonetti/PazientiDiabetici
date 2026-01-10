package it.univr.diabete.dao;

import it.univr.diabete.model.Diabetologo;
import java.util.List;

public interface DiabetologoDAO {
    Diabetologo findByEmailAndPassword(String email, String password) throws Exception;

    List<Diabetologo> findAll() throws Exception;

    void insert(Diabetologo d) throws Exception;

    void update(Diabetologo d) throws Exception;

    void deleteByEmail(String email) throws Exception;
}
