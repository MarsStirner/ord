package ru.efive.uifaces.beans;

import java.io.IOException;
import java.io.InputStream;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import ru.efive.uifaces.filter.UploadHandler;
import ru.efive.uifaces.filter.UploadInfo;

import static java.lang.String.format;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "fileUploadData")
@SessionScoped
public class FileUploadTestBean implements UploadHandler {

    private StringBuilder filesInfo = new StringBuilder();
    private String filesInfoText;

    public UploadHandler getUploadHandler() {
        return this;
    }

    @Override
    public void handleUpload(UploadInfo uploadInfo) {
        if (uploadInfo.getFileName() == null || uploadInfo.getFileName().isEmpty()) {
            return;
        }
        try {
            InputStream data = uploadInfo.getData();
            byte[] buf = new byte[4096];
            int rsz = 0, dsz = 0;
            while (rsz >= 0) {
                dsz += rsz;
                rsz = data.read(buf);
            }
            data.close();
            filesInfo.append(format("name: \"%s\", size: %d; ", uploadInfo.getFileName(), dsz));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getFilesInfo() {
        return filesInfoText;
    }

    public String action() throws InterruptedException {
        filesInfoText = filesInfo.toString();
        filesInfo.setLength(0);
        Thread.sleep(3000);
        return null;
    }
}
