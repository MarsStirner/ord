package ru.efive.uifaces.beans;

import java.util.Date;
import java.util.Random;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "tabPanelData")
@SessionScoped
public class TabPanelTestBean {

    private Random rand = new Random(System.currentTimeMillis());

    public String getText1() {
        return new Date().toString();
    }

    public int getRand1() {
        return rand.nextInt();
    }

    private String lastAction;

    public String getLastAction() {
        return lastAction;
    }

    public String page1Action() {
        lastAction = "page1Action";
        return null;
    }

    public String page2Action() {
        lastAction = "page2Action";
        return null;
    }

    public String page3Action() {
        lastAction = "page3Action";
        return null;
    }

    public String page4Action() {
        lastAction = "page4Action";
        return null;
    }

    private boolean page1Selected = false;
    private boolean page2Selected = true;
    private boolean page3Selected = false;
    private boolean page4Selected = false;

    public boolean isPage1Selected() {
        return page1Selected;
    }

    public void setPage1Selected(boolean page1Selected) {
        this.page1Selected = page1Selected;
    }

    public boolean isPage2Selected() {
        return page2Selected;
    }

    public void setPage2Selected(boolean page2Selected) {
        this.page2Selected = page2Selected;
    }

    public boolean isPage3Selected() {
        return page3Selected;
    }

    public void setPage3Selected(boolean page3Selected) {
        this.page3Selected = page3Selected;
    }

    public boolean isPage4Selected() {
        return page4Selected;
    }

    public void setPage4Selected(boolean page4Selected) {
        this.page4Selected = page4Selected;
    }
}
