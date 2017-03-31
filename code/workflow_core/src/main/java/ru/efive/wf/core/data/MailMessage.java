package ru.efive.wf.core.data;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MailMessage {

    private List<String> sendTo;
    private List<String> copyTo;
    private List<String> blindCopyTo;
    private String subject;
    private String body;
    private String contentType;

    public MailMessage(List<String> sendTo, List<String> copyTo, String subject, String body) {
        if (sendTo != null) {
            this.sendTo = new ArrayList<>(sendTo.size());
            for (String s : sendTo) {
                if (StringUtils.isNotEmpty(s)) {
                    this.sendTo.add(s);
                }
            }
        }
        if (copyTo != null) {
            this.copyTo = new ArrayList<>(copyTo.size());
            for (String s : copyTo) {
                if (StringUtils.isNotEmpty(s)) {
                    this.copyTo.add(s);
                }
            }
        }
        this.subject = subject;
        this.body = body;
    }

    public List<String> getSendTo() {
        return sendTo == null ? new ArrayList<String>() : sendTo;
    }

    public void setSendTo(List<String> sendTo) {
        if (sendTo != null) {
            this.sendTo = new ArrayList<>(sendTo.size());
            for (String s : sendTo) {
                if (StringUtils.isNotEmpty(s)) {
                    this.sendTo.add(s);
                }
            }
        }
    }

    public List<String> getCopyTo() {
        return copyTo == null ? new ArrayList<String>() : copyTo;
    }

    public void setCopyTo(List<String> copyTo) {
        if (copyTo != null) {
            this.copyTo = new ArrayList<>(copyTo.size());
            for (String s : copyTo) {
                if (StringUtils.isNotEmpty(s)) {
                    this.copyTo.add(s);
                }
            }
        }
    }

    public List<String> getBlindCopyTo() {
        return blindCopyTo == null ? new ArrayList<String>() : blindCopyTo;
    }

    public void setBlindCopyTo(List<String> blindCopyTo) {
        this.blindCopyTo = blindCopyTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}