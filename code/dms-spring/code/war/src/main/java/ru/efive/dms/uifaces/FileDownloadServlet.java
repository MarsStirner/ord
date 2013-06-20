package ru.efive.dms.uifaces;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.StringUtils;

import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;

public class FileDownloadServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String version = request.getParameter("revision");
		Attachment attachment = null;
		String downloadFileName = "";
		byte[] buffer = {};
		if (StringUtils.isEmpty(id)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		else {
			if (StringUtils.isEmpty(version)) {
				attachment = fileManagement.getFileById(id);
				downloadFileName = attachment.getCurrentRevision().getFileName();
				buffer = fileManagement.downloadFile(id);
			}
			else {
				attachment = fileManagement.getFileById(id);
				if (attachment.isVersionable()) {
					for (Revision revision: attachment.getRevisions()) {
						if (revision.getVersion().equals(version)) {
							downloadFileName = revision.getFileName();
						}
					}
				}
				if (StringUtils.isEmpty(downloadFileName)) {
					downloadFileName = attachment.getFileName();
				}
				buffer = fileManagement.downloadFile(id, version);
			}
		}
		
		BufferedOutputStream output = null;
		try {
			System.out.println("Uploading file - get bytes, length - " + buffer.length);
			response.reset();
			response.setHeader("Cache-Control", "public");
			response.setHeader("Pragma", "public");					
			response.setHeader("Content-Type", "application/octet-stream;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(downloadFileName,"UTF-8").replace("+", "%20") + "\"");
			response.setHeader("Content-Length", String.valueOf(buffer.length));
			output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			output.write(buffer, 0, buffer.length);
			output.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			close(output);
		}
	}

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final int DEFAULT_BUFFER_SIZE = 10240;
	
	@Inject @Named("fileManagement")
	private transient FileManagementBean fileManagement;
	
	
	private static final long serialVersionUID = -5496962539479604473L;
}