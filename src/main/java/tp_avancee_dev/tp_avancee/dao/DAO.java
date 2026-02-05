package tp_avancee_dev.tp_avancee.dao;

import java.util.List;

public abstract class DAO<T> {
    public abstract T find(long id) throws Exception;
    public abstract List<T> findAll() throws Exception;
    public abstract boolean create(T obj) throws Exception;
    public abstract boolean update(T obj) throws Exception;
    public abstract boolean delete(long id) throws Exception;
}
