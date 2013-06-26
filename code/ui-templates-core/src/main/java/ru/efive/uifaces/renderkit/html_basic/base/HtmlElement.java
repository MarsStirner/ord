package ru.efive.uifaces.renderkit.html_basic.base;

/**
 * 
 * @author Denis Kotegov
 */
public final class HtmlElement {
    
    public static final HtmlElement A = new HtmlElement("a");
    
    public static final HtmlElement CAPTION = new HtmlElement("caption");
    
    public static final HtmlElement DIV = new HtmlElement("div");
    
    public static final HtmlElement INPUT = new HtmlElement("input");
    
    public static final HtmlElement TABLE = new HtmlElement("table");
    
    public static final HtmlElement TBODY = new HtmlElement("tbody");

    public static final HtmlElement TD = new HtmlElement("td");

    public static final HtmlElement TF = new HtmlElement("tf");

    public static final HtmlElement TFOOT = new HtmlElement("tfoot");

    public static final HtmlElement TH = new HtmlElement("th");

    public static final HtmlElement THEAD = new HtmlElement("thead");
    
    public static final HtmlElement TR = new HtmlElement("tr");
    
    public static final HtmlElement SPAN = new HtmlElement("span");
    
    public static final HtmlElement SCRIPT = new HtmlElement("script");

    public static final HtmlElement SELECT = new HtmlElement("select");

    public static final HtmlElement OPTION = new HtmlElement("option");

    // ----------------------------------------------------------------------------------------------------------------
    
    private final String name;
    
    public HtmlElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
