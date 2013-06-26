package ru.efive.uifaces.component.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;
import ru.efive.uifaces.filter.util.DataInputURL;
import ru.efive.uifaces.filter.util.org.apache.tools.ant.util.ReaderInputStream;

/**
 *
 * @author Pavel Porubov
 */
public class Include extends TagHandler {

    public enum Attributes {

        src, url, data
    }

    public static class VariableMapperWrapper extends VariableMapper {

        private final VariableMapper target;
        private Map vars;

        public VariableMapperWrapper(VariableMapper orig) {
            super();
            this.target = orig;
        }

        public ValueExpression resolveVariable(String variable) {
            ValueExpression ve = null;
            try {
                if (this.vars != null) {
                    ve = (ValueExpression) this.vars.get(variable);
                }
                if (ve == null) {
                    return this.target.resolveVariable(variable);
                }
                return ve;
            }
            catch (StackOverflowError e) {
                throw new ELException("Could not Resolve Variable [Overflow]: "
                        + variable, e);
            }
        }

        public ValueExpression setVariable(String variable,
                ValueExpression expression) {
            if (this.vars == null) {
                this.vars = new HashMap();
            }
            return (ValueExpression) this.vars.put(variable, expression);
        }
    }

    public Include(TagConfig config) {
        super(config);
    }

    private String data;
    private URL dataUrl;

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        Attributes a = null;
        TagAttribute ta = null;
        Object av = null;
        for (Attributes aa : Attributes.values()) {
            ta = this.getAttribute(aa.name());
            if (ta != null) {
                av = ta.getObject(ctx);
                if (av != null) {
                    a = aa;
                    break;
                }
            }
        }
        if (a == null) {
            return;
        }

        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try {
            this.nextHandler.apply(ctx, null);
            switch (a) {
                case src:
                    ctx.includeFacelet(parent, (String) av);
                    break;
                case url:
                    ctx.includeFacelet(parent, (URL) av);
                    break;
                case data:
                    String pgd = null;
                    if (av instanceof String) {
                        pgd = (String) av;
                    } else if (av instanceof InputStream || av instanceof Reader) {
                        StringBuilder pgdb = new StringBuilder();
                        Reader pgdr;
                        if (av instanceof InputStream) {
                            pgdr = new InputStreamReader((InputStream) av);
                        } else {
                            pgdr = (Reader) av;
                        }
                        char[] buf = new char[1024];
                        for (;;) {
                            int rsz = pgdr.read(buf);
                            if (rsz < 0) {
                                break;
                            }
                            pgdb.append(buf, 0, rsz);
                        }
                        pgd = pgdb.toString();
                    }

                    if (pgd != null && !pgd.equals(data)) {
                        data = pgd;
                        dataUrl = DataInputURL.make(data);
                    }
                    ctx.includeFacelet(parent, dataUrl);
                    break;
            }
        } catch (IOException e) {
            throw new TagAttributeException(this.tag, ta);
        } finally {
            ctx.setVariableMapper(orig);
        }
    }
}
