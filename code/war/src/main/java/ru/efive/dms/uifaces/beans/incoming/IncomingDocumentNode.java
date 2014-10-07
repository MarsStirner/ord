package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.data.IncomingDocument;

/**
 * Author: Upatov Egor <br>
 * Date: 06.10.2014, 20:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class IncomingDocumentNode {
    private final IncomingDocument indoc;

    public IncomingDocumentNode(){
           indoc = null;
    }

    public IncomingDocumentNode(IncomingDocument indoc){
        this.indoc = indoc;
    }

    public IncomingDocument getDoc() {
        return indoc;
    }
}
