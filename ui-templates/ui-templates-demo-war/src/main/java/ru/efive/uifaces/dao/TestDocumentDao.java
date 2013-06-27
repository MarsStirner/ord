package ru.efive.uifaces.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.efive.uifaces.data.TestDocument;

/**
 * Provides access to test documents.
 * 
 * @author Ramil_Habirov
 */
public class TestDocumentDao {

	/**
	 * Next available id of document.
	 */
	private static int nextId = 0;

	/**
	 * Map of all stored documents.
	 */
	private static Map<Integer, TestDocument> testDocumentMap = new HashMap<Integer, TestDocument>();

	/**
	 * Returns next available id of document.
	 * 
	 * @return next available id of document.
	 */
	public static synchronized Integer getNextId() {
		return nextId++;
	}

	/**
	 * Returns all documents.
	 * 
	 * @return all documents.
	 * @throws Exception
	 *             When can't return all documents.
	 */
	public static synchronized List<TestDocument> getAll() {
//		System.out.println("Executing getAll()...");
		outTestDocumentMap();
		return new ArrayList<TestDocument>(testDocumentMap.values());
	}

	/**
	 * Returns a document by it's id.
	 * 
	 * @param id
	 *            id of document.
	 * @return document.
	 * @throws Exception
	 *             When can't return document by it's id.
	 */
	public static synchronized TestDocument get(final Integer id)
			throws Exception {
//		System.out.println("Executing get(" + id + ")...");
		outTestDocumentMap();
		TestDocument testDocument = null;
		if (testDocumentMap.containsKey(id)) {
			testDocument = testDocumentMap.get(id);
		} else {
			throw new Exception("Document with id " + id
					+ " does not exists.");
		}
//		System.out.println("Returning " + testDocument + "...");
		return testDocument;
	}

	/**
	 * Saves document.
	 * 
	 * @param testDocument
	 *            document.
	 * @throws Exception
	 *             When can't save document.
	 */
	public static synchronized void save(final TestDocument testDocument)
			throws Exception {
//		System.out.println("Executing save(" + testDocument + ")...");
		if (testDocument.getId() != null) {
			throw new Exception("id of document must be null.");
		} else {
			testDocument.setId(getNextId());
			testDocumentMap.put(testDocument.getId(), testDocument);
		}
		outTestDocumentMap();
	}

	/**
	 * Updates document.
	 * 
	 * @param testDocument
	 *            document.
	 * @throws Exception
	 *             When can't update document.
	 */
	public static synchronized void update(final TestDocument testDocument)
			throws Exception {
//		System.out.println("Executing update(" + testDocument + ")...");
		Integer testDocumentId = testDocument.getId();
		if (testDocumentMap.containsKey(testDocumentId)) {
			testDocumentMap.put(testDocumentId, testDocument);
		} else {
			throw new Exception("Document with id " + testDocumentId
					+ " does not exists.");
		}
		outTestDocumentMap();
	}

	/**
	 * Deletes document.
	 * 
	 * @param testDocument
	 *            document.
	 * @throws Exception
	 *             When can't delete document.
	 */
	public static synchronized void delete(final TestDocument testDocument)
			throws Exception {
//		System.out.println("Executing delete(" + testDocument + ")...");
		testDocumentMap.remove(testDocument.getId());
		outTestDocumentMap();
	}

	/**
	 * Prints all stored documents to System.out.
	 */
	private static void outTestDocumentMap() {
//		System.out.println("Stored documents:");
		for (Integer key : testDocumentMap.keySet()) {
			TestDocument testDocument = testDocumentMap.get(key);
			System.out.println("{" + key + ", " + testDocument + "}");
		}
	}
}
