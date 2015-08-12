package ru.efive.dms.uifaces.beans;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import ru.entity.model.document.ReportTemplate;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named("reports")
@RequestScoped
public class ReportsManagmentBean {

    public void previewSqlReportByRequestParams() throws IOException, ClassNotFoundException, SQLException {
        Map<String, String> requestProperties = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String in_reportName = requestProperties.get("reportName");
        ApplicationContext context = indexManagement.getContext();
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        Connection conn = dataSource.getConnection();

        /* Get Data source */
        JasperReport report = null;
        JasperPrint print = null;
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
            report = JasperCompileManager.compileReport(inputStream);
            Map<String, Object> in_map = new HashMap<String, Object>();
            for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                System.out.println("_" + entry.getKey() + " - " + entry.getValue());
                if (StringUtils.contains(entry.getKey(), "Date")) {
                    try {
                        Date date = new SimpleDateFormat("dd.MM.yyyy").parse(entry.getValue());
                        in_map.put("_" + entry.getKey(), date);
                        in_map.put(entry.getKey(), date);
                    } catch (ParseException e) {
                        System.out.println("Wrong date parameter");
                        in_map.put("_" + entry.getKey(), entry.getValue());
                        in_map.put(entry.getKey(), entry.getValue());
                    }
                } else {
                    in_map.put("_" + entry.getKey(), entry.getValue());
                    in_map.put(entry.getKey(), entry.getValue());
                }
            }
            print = JasperFillManager.fillReport(report, in_map, conn);

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            //Set reponse content type
            response.setContentType("application/pdf");
            //Export PDF file to browser window
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();

        } catch (JRException e) {
            e.printStackTrace();
        } finally{
            conn.close();
        }
    }

    public void getHttpReportByXML() {
        System.out.println("Starting");
        Connection conn = null;
        try {
            ApplicationContext context = indexManagement.getContext();
            DataSource dataSource = (DataSource) context.getBean("dataSource");
            System.out.println("Data source is " + dataSource);
            conn = dataSource.getConnection();
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/reports/customers.xml"));

            // Передаем resultSet в отчет
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), conn);

            JasperExportManager.exportReportToPdfFile(jasperPrint, "c:\\customers_report.pdf");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JRException e) {
            e.printStackTrace();
        } finally {
            // Корректно закрываем соединение с базой
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done.");

    }

    public void hibernatePrintReportByRequestParams() throws IOException, ClassNotFoundException, SQLException {

        Map<String, String> requestProperties = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String in_reportName = requestProperties.get("reportName");
        String in_printerName = requestProperties.get("printerName");

        //Properties printerProperties=new Properties();
        PrintService psZebra = null;
        String sPrinterName = null;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (int i = 0; i < services.length; i++) {
            PrintServiceAttribute attr = services[i].getAttribute(PrinterName.class);
            sPrinterName = ((PrinterName) attr).getValue();
            if (sPrinterName.equals(in_printerName)) {
                psZebra = services[i];
                break;
            }
        }
        if (psZebra == null) {
            System.out.println(in_printerName + " is not found.");

        } else {
            System.out.println("Founded printer is >> " + psZebra.getName());
        }
        /* Get Data source */
        JasperReport report = null;
        JasperPrint print = null;
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
            report = JasperCompileManager.compileReport(inputStream);
            Map<String, Object> in_map = new HashMap<String, Object>();
            for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                System.out.println("_" + entry.getKey());
                in_map.put("_" + entry.getKey(), entry.getValue());
                in_map.put(entry.getKey(), entry.getValue());
            }
            //Session session = HibernateUtil.getSessionFactory().openSession();
            Session session = ((SessionFactory) indexManagement.getContext().getBean("sessionFactory")).openSession();
            in_map.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
            print = JasperFillManager.fillReport(report, in_map);

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            //Set reponse content type
            response.setContentType("application/pdf");
            //Export PDF file to browser window
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();

            //JasperExportManager.exportReportToPdfFile(print,"c:/reports/db_big_report.pdf");
            //ResponseStream response=FacesContext.getCurrentInstance().getExternalContext();//getResponseStream();

            //
            session.close();

        } catch (JRException e) {
            e.printStackTrace();
        } finally {

        }
        if (true) {
            return;
        }
        ;
        //Configure page printing on found print
        DocPrintJob job = psZebra.createPrintJob();
        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
        MediaSizeName mediaSizeName = null;
        //if(in_printerName==null){
        mediaSizeName = MediaSizeName.ISO_A4;
        //}else{
        //mediaSizeName=MediaSize.findMedia(
        //Float.parseFloat(printerProperties.getProperty(in_printerPath+".x")),
        //Float.parseFloat(printerProperties.getProperty(in_printerPath+".y")),
        //MediaPrintableArea.MM
        //);
        //}
        System.out.println(mediaSizeName);

        printRequestAttributeSet.add(mediaSizeName);
        printRequestAttributeSet.add(new Copies(1));
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        /* We set the selected service and pass it as a paramenter */
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, psZebra);
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, psZebra.getAttributes());
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);

        try {
            exporter.exportReport();
            System.out.println("Printed success");
        } catch (JRException e) {
            e.printStackTrace();
            System.out.println("not printed");
        }
    }

    public void sqlPrintReportByRequestParams() throws IOException, ClassNotFoundException, SQLException {

        Map<String, String> requestProperties = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String in_reportName = requestProperties.get("reportName");
        String in_printerName = requestProperties.get("printerName");
        ApplicationContext context = indexManagement.getContext(); //new ClassPathXmlApplicationContext("applicationContext.xml");
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        Connection conn = dataSource.getConnection();

        //Properties printerProperties=new Properties();
        PrintService psZebra = null;
        String sPrinterName = null;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (int i = 0; i < services.length; i++) {
            PrintServiceAttribute attr = services[i].getAttribute(PrinterName.class);
            sPrinterName = ((PrinterName) attr).getValue();
            if (sPrinterName.equals(in_printerName)) {
                psZebra = services[i];
                break;
            }
        }
        if (psZebra == null) {
            System.out.println(in_printerName + " is not found.");
        } else {
            System.out.println("Founded printer is >> " + psZebra.getName());
        }

        /* Get Data source */
        JasperReport report = null;
        JasperPrint print = null;
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
            report = JasperCompileManager.compileReport(inputStream);
            Map<String, Object> in_map = new HashMap<String, Object>();
            for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                System.out.println("_" + entry.getKey());
                if (StringUtils.contains(entry.getKey(), "Date")) {
                    try {
                        Date date = new SimpleDateFormat("dd.MM.yyyy").parse(entry.getValue());
                        in_map.put("_" + entry.getKey(), date);
                        in_map.put(entry.getKey(), date);
                    } catch (ParseException e) {
                        System.out.println("Wrong date parameter");
                        in_map.put("_" + entry.getKey(), entry.getValue());
                        in_map.put(entry.getKey(), entry.getValue());
                    }
                } else {
                    in_map.put("_" + entry.getKey(), entry.getValue());
                    in_map.put(entry.getKey(), entry.getValue());
                }
            }
            print = JasperFillManager.fillReport(report, in_map, conn);

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            //Set reponse content type
            response.setContentType("application/pdf");
            //Export PDF file to browser window
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();

            //JasperExportManager.exportReportToPdfFile(print,"c:/reports/db_big_report.pdf");
        } catch (JRException e) {
            e.printStackTrace();
        } finally{
            conn.close();
        }
        if (true) {
            return;
        }
        ;
        //Configure page printing on found print
        DocPrintJob job = psZebra.createPrintJob();
        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
        MediaSizeName mediaSizeName = null;
        //if(in_printerName==null){
        mediaSizeName = MediaSizeName.ISO_A4;
        //}else{
        //mediaSizeName=MediaSize.findMedia(
        //Float.parseFloat(printerProperties.getProperty(in_printerPath+".x")),
        //Float.parseFloat(printerProperties.getProperty(in_printerPath+".y")),
        //MediaPrintableArea.MM
        //);
        //}
        System.out.println(mediaSizeName);

        printRequestAttributeSet.add(mediaSizeName);
        printRequestAttributeSet.add(new Copies(1));
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        /* We set the selected service and pass it as a paramenter */
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, psZebra);
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, psZebra.getAttributes());
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);

        try {
            exporter.exportReport();
            System.out.println("Printed success");
        } catch (JRException e) {
            e.printStackTrace();
            System.out.println("not printed");
        }
    }

    public void sqlPrintReportByRequestParams(ReportTemplate reportTemplate) throws IOException, ClassNotFoundException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ApplicationContext context = indexManagement.getContext();
        Map<String, Object> requestProperties = reportTemplate.getProperties();
        String in_reportName = requestProperties.get("reportName").toString();
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        Connection conn = dataSource.getConnection();

        /* Get Data source */
        JasperReport report = null;
        JasperPrint print = null;
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
            report = JasperCompileManager.compileReport(inputStream);
            Map<String, Object> in_map = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : requestProperties.entrySet()) {
                System.out.println("_" + entry.getKey());
                in_map.put("_" + entry.getKey(), entry.getValue());
                in_map.put(entry.getKey(), entry.getValue());
            }
            print = JasperFillManager.fillReport(report, in_map, conn);

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            //Set reponse content type
            response.setContentType("application/pdf");
            //Export PDF file to browser window
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();

        } catch (JRException e) {
            throw new SQLException(e);
        } finally{
            conn.close();
        }
    }


    @Inject
    @Named("indexManagement")
    private transient IndexManagementBean indexManagement;
}