package ru.efive.uifaces.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "testTimeBean")
@SessionScoped
public class InputTimeBean {

    private Date time1;
    private Date time2;
    private Date time3;
    private Date time4;
    private Date time5;

    public Date getTime1() {
        return time1;
    }

    public void setTime1(Date time1) {
        this.time1 = time1;
    }

    public Date getTime2() {
        return time2;
    }

    public void setTime2(Date time2) {
        this.time2 = time2;
    }

    public Date getTime3() {
        return time3;
    }

    public void setTime3(Date time3) {
        this.time3 = time3;
    }

    public Date getTime4() {
        return time4;
    }

    public void setTime4(Date time4) {
        this.time4 = time4;
    }

    public Date getTime5() {
        return time5;
    }

    public void setTime5(Date time5) {
        this.time5 = time5;
    }

    public String getTime1String() {
        return time1 != null ? new SimpleDateFormat("HH:mm:ss").format(time1) : "";
    }

    public String getTime2String() {
        return time2 != null ? new SimpleDateFormat("HH:mm:ss").format(time2) : "";
    }

    public String getTime3String() {
        return time3 != null ? new SimpleDateFormat("HH:mm:ss").format(time3) : "";
    }

    public String getTime4String() {
        return time4 != null ? new SimpleDateFormat("HH:mm:ss").format(time4) : "";
    }

    public String getTime5String() {
        return time5 != null ? new SimpleDateFormat("HH:mm:ss").format(time5) : "";
    }

    private ModalWindowHolderBean window = new ModalWindowHolderBean();

    public ModalWindowHolderBean getWindow() {
        return window;
    }
}
