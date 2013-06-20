package ru.efive.dms.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;


public class DocumentConverter {
    public void run() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline=filename=file.pdf");

            try {
                //response.getOutputStream().write(yourdata[]);
                OutputStream httpOS = response.getOutputStream();
                excutePost("http://localhost:8080/converter/service", httpOS);
                httpOS.flush();
                httpOS.close();
                context.responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String arg[]) throws Exception {
        //excutePost("http://localhost:8080/converter/service", "Hello World!");
        /*
          File inputFile = new File("C:/Temp/Опыт Элемент 5.docx");
          File outputFile = new File("C:/Temp/document.pdf");

          // connect to an OpenOffice.org instance running on port 8100
          OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
          connection.connect();

          // convert

          StreamOpenOfficeDocumentConverter strmOO;
          strmOO.convert(inputStream, inputFormat, outputStream, outputFormat)
          OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter(connection);
          converter.convert(inputFile, outputFile);

          // close the connection
          connection.disconnect();
          //To convert from/to other formats, simply change the file names and the formats will be determined based on file extensions; e.g. to convert an Excel file to OpenDocument Spreadsheet:
          //File inputFile = new File("spreadsheet.xls");
          //File outputFile = new File("spreadsheet.pdf");
          //converter.convert(inputFile, outputFile);
           */
        System.out.println("end");
    }


    public static void excutePost(String targetURL, OutputStream out) {
        targetURL = "http://10.0.17.247:8080/converter/service";

        String inputFileName = "file:///C:/Temp/Руководство пользователя ТРФУ.docx";
        String outputFileName = "C:/Temp/document.pdf";

        HttpURLConnection connection = null;


        //Create connection
        URL url;
        try {
            url = new URL(targetURL);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            //connection.setRequestProperty("Content-Type", "application/vnd.oasis.opendocument.text");			//
            //connection.setRequestProperty("Content-Disposition","form-data; name='inputDocument'; filename='doc.odt'");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Charset", "windows-1251,utf-8;q=0.7,*;q=0.3");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            connection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");

            connection.setRequestProperty("Content-Type", "application/vnd.oasis.opendocument.text");

            connection.setRequestProperty("Connection", "keep-alive");

            connection.setRequestProperty("Accept", "application/pdf");
            connection.setRequestProperty("Cache-Control", "max-age=0");
            //connection.setRequestProperty("Content-Length", "" + data.length);

            connection.setUseCaches(true);
            connection.setDoInput(true);
            connection.setDoOutput(true);


            //Send request
            //FileInputStream fis = new FileInputStream(inputFileName);

            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //int next = fis.read();
            //while (next > -1) {
            //bos.write(next);
            //next = fis.read();
            //}
            //bos.flush();
            //byte[] data1 = bos.toByteArray();

            /*byte[] inputData = new byte[fis.available()];
               /fis.read(inputData);
               fis.close();
               connection.setRequestProperty("Content-Length", "" + inputData.length);
               DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
               wr.write(inputData);
               wr.flush();
               wr.close();
                */

            url = new URL(inputFileName);
            InputStream is1 = url.openStream();
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            byte[] b = new byte[2048];
            int length;
            while ((length = is1.read(b)) != -1) {
                wr.write(b, 0, length);
            }
            wr.flush();
            wr.close();
            is1.close();
            //connection.connect();

            //Get Response
            InputStream is = connection.getInputStream();

            // write the inputStream to a FileOutputStream
            //OutputStream out=new FileOutputStream(new File(outputFileName));
            byte[] buffer = new byte[is.available()];

            int read = 0;
            while ((read = is.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();
            out.close();

            return;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
