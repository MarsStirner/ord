package ru.efive.dao;

public interface DAO<T extends Data> {

    boolean connect();

    boolean disconnect();

    java.util.List<T> getDataList();

    T getDataById(String id);

    boolean createData(T data);

    boolean updateData(T data);

    boolean deleteData(T data);

}