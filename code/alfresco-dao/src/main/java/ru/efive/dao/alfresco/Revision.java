package ru.efive.dao.alfresco;

public class Revision {

    public Revision() {

    }

    public Revision(String version, int authorId, String fileName) {
        this.version = version;
        this.authorId = authorId;
        this.fileName = fileName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    private String version;
    private int authorId;
    private String fileName;
}