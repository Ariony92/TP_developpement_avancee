package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.dao;

import java.util.List;

public abstract class dao<T> {
    public abstract T find(int id) throws Exception;
    public abstract List<T> findAll() throws Exception;
    public abstract boolean create(T obj) throws Exception;
    public abstract boolean update(T obj) throws Exception;
    public abstract boolean delete(int id) throws Exception;
}
