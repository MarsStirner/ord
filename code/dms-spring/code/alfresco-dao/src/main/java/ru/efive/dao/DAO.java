package ru.efive.dao;

public interface DAO<T extends Data> {
	
	public boolean connect();
	public boolean disconnect();
	
	public java.util.List<T> getDataList();
	public T getDataById(String id);
	public boolean createData(T data);
	public boolean updateData(T data);
	public boolean deleteData(T data);
	
}