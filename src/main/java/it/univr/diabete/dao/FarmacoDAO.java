package it.univr.diabete.dao;

import it.univr.diabete.model.Farmaco;

import java.util.List;

public interface FarmacoDAO {

    List<Farmaco> findAll() throws Exception;

    Farmaco findById(int id) throws Exception;

    void insert(Farmaco farmaco) throws Exception;

    void update(Farmaco farmaco) throws Exception;

    void delete(int id) throws Exception;
}