package ru.efive.dao;

public class InitializationException extends Exception {
	
	public InitializationException() {
		super();
	}
	
	@Override
	public String getMessage() {
		return "Failed to initialize DAO session";
	}
	
	private static final long serialVersionUID = -3001352053090875884L;
}