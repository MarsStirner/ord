package ru.efive.wf.core;

public class MailSettings {
    private String jndi;
    private String smtpHost;
    private String smtpUser;
    private String smptPassword;
    private boolean smtpFlag;
    private boolean testServer;
    private String sendTo;
    private String sendFrom;
    private String smtpPort;

    public String getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public String getSmptPassword() {
        return smptPassword;
    }

    public boolean isSmtpFlag() {
        return smtpFlag;
    }

    public boolean isTestServer() {
        return testServer;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public void setSmtpUser(String smptUser) {
        this.smtpUser = smptUser;
    }

    public void setSmptPassword(String smptPassword) {
        this.smptPassword = smptPassword;
    }

    public void setSmtpFlag(boolean smtpFlag) {
        this.smtpFlag = smtpFlag;
    }

    public void setTestServer(boolean testServer) {
        this.testServer = testServer;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setSendFrom(String sendFrom) {
        this.sendFrom = sendFrom;
    }
}