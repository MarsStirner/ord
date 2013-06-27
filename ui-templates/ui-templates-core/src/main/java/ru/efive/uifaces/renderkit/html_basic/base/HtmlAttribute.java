package ru.efive.uifaces.renderkit.html_basic.base;

/**
 *
 * @author Denis Kotegov
 */
public final class HtmlAttribute {

    public static final HtmlAttribute BORDER = new HtmlAttribute("border");
    
    public static final HtmlAttribute CELLPADDING = new HtmlAttribute("cellpadding");

    public static final HtmlAttribute CELLSPACING = new HtmlAttribute("cellspacing");

    public static final HtmlAttribute CLASS = new HtmlAttribute("class");

    public static final HtmlAttribute COLSPAN = new HtmlAttribute("colspan");

    public static final HtmlAttribute HREF = new HtmlAttribute("href");
    
    public static final HtmlAttribute ID = new HtmlAttribute("id");

    public static final HtmlAttribute STYLE = new HtmlAttribute("style");
    
    public static final HtmlAttribute TABINDEX = new HtmlAttribute("tabindex");
    
    public static final HtmlAttribute TARGET = new HtmlAttribute("target");

    public static final HtmlAttribute TYPE = new HtmlAttribute("type");
    
    public static final HtmlAttribute VALUE = new HtmlAttribute("value");
    
    public static final HtmlAttribute WIDTH = new HtmlAttribute("width");

    public static final HtmlAttribute NAME = new HtmlAttribute("name");

    public static final HtmlAttribute SELECTED = new HtmlAttribute("selected");

    // ----------------------------------------------------------------------------------------------------------------

    public static final HtmlAttribute ONCLICK = new HtmlAttribute("onclick");
    
    public static final HtmlAttribute ONDBLCLICK = new HtmlAttribute("ondblclick");

    public static final HtmlAttribute ONKEYDOWN = new HtmlAttribute("onkeydown");

    public static final HtmlAttribute ONKEYPRESS = new HtmlAttribute("onkeypress");

    public static final HtmlAttribute ONKEYUP = new HtmlAttribute("onkeyup");

    public static final HtmlAttribute ONMOUSEDOWN = new HtmlAttribute("onmousedown");

    public static final HtmlAttribute ONMOUSEMOVE = new HtmlAttribute("onmousemove");

    public static final HtmlAttribute ONMOUSEOUT = new HtmlAttribute("onmouseout");

    public static final HtmlAttribute ONMOUSEOVER = new HtmlAttribute("onmouseover");

    public static final HtmlAttribute ONMOUSEUP = new HtmlAttribute("onmouseup");

    public static final HtmlAttribute ONCHANGE = new HtmlAttribute("onchange");

    // ----------------------------------------------------------------------------------------------------------------
    
    public static final HtmlAttribute E5UI_CURRENT = new HtmlAttribute("e5ui-current");

    public static final HtmlAttribute E5UI_DOCTABLE = new HtmlAttribute("e5ui-doctable");
    
    public static final HtmlAttribute E5UI_LEVEL = new HtmlAttribute("e5ui-level");

    public static final HtmlAttribute E5UI_SELECTED = new HtmlAttribute("e5ui-selected");
    
    // ----------------------------------------------------------------------------------------------------------------
    
    private final String name; 
    
    public HtmlAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
