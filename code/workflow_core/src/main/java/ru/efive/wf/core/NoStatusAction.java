package ru.efive.wf.core;

import java.io.Serializable;

public class NoStatusAction extends UserAction implements Serializable {

    private static final long serialVersionUID = 6635875631266091978L;

    public NoStatusAction(Process process) {
        super(process);
    }


}