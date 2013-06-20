package ru.efive.dms.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dms.data.Attachment;

public class ContentVersioningTest {

    public ContentVersioningTest() {

    }

    @Test
    public void testMethod() throws Exception {
        /*AlfrescoDAO<Attachment> dao = new AlfrescoDAO<Attachment>(Attachment.class);
          dao.setLogin("admin");
          dao.setPassword("admin");
          if (!dao.connect()) throw new InitializationException();

          InputStream f = new FileInputStream("C:\\Work\\tmp\\empty_attachments_id.txt");
          int ch;
          StringBuffer inpBuf = new StringBuffer();
          while ((ch = f.read()) != -1) {
              inpBuf.append((char)ch);
          }

          String [] displayNames = StringUtils.split(inpBuf.toString());

          OutputStream f0 = new FileOutputStream("C:\\Work\\tmp\\empty_attachments.txt");

          for (String name: displayNames) {
              System.out.println(StringUtils.trim(name));

              List<Attachment> attachments = new ArrayList<Attachment>();
              Query query = new Query(Constants.QUERY_LANG_LUCENE, "+TYPE:\"" + new Attachment().getNamedNodeType() + "\"" +
                      " +@cm\\:name:\"" + StringUtils.trim(name) + "\"");
              System.out.println("Prepare query: " + query.getStatement());
              QueryResult queryResult = dao.getRepositoryService().query(new Store(Constants.WORKSPACE_STORE, "SpacesStore"), query, false);
              ResultSet resultSet = queryResult.getResultSet();
              ResultSetRow[] rows = resultSet.getRows();
              if (rows != null) {
                  System.out.println("Result set is not null, length - " + rows.length);
                  for (ResultSetRow row : rows) {
                      Attachment data = new Attachment();
                      data.setId(row.getNode().getId());
                      data.setNodeProperties(row.getColumns());
                      attachments.add(data);
                  }
              }

              if (attachments != null && attachments.size() > 0) {
                  Attachment attachment = attachments.get(0);

                  System.out.println("Attachment: " + attachment.getFileName());
                  long size = dao.getContentSize(attachment);
                  if (size == 0) {
                      String parentDocType = "";
                      String pid = attachment.getParentId();
                      if (pid.contains("incoming_")) {
                          parentDocType = "Входящий документ";
                      }
                      else if (pid.contains("outgoing_")) {
                          parentDocType = "Исходящий документ";
                      }
                      else if (pid.contains("internal_")) {
                          parentDocType = "Внутренний документ";
                      }
                      else if (pid.contains("request_")) {
                          parentDocType = "Обращение";
                      }
                      else if (pid.contains("task_")) {
                          parentDocType = "Поручение";
                      }
                      String id = StringUtils.right(pid, pid.length() - StringUtils.lastIndexOf(pid, "_") - 1);
                      StringBuffer buf = new StringBuffer(parentDocType).append("\t").append(id).append("\t").append(attachment.getFileName()).append("\t").
                              append(new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(attachment.getCreated())).append("\t").
                              append(attachment.getAuthorId()).append("\n");
                      f0.write(buf.toString().getBytes());
                  }
              }

          }

          f0.close();
          dao.disconnect();*/
    }

}