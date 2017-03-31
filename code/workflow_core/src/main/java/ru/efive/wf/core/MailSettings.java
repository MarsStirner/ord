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

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smptUser) {
        this.smtpUser = smptUser;
    }

    public String getSmptPassword() {
        return smptPassword;
    }

    public void setSmptPassword(String smptPassword) {
        this.smptPassword = smptPassword;
    }

    public boolean isSmtpFlag() {
        return smtpFlag;
    }

    public void setSmtpFlag(boolean smtpFlag) {
        this.smtpFlag = smtpFlag;
    }

    public boolean isTestServer() {
        return testServer;
    }

    public void setTestServer(boolean testServer) {
        this.testServer = testServer;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    public void setSendFrom(String sendFrom) {
        this.sendFrom = sendFrom;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }
}