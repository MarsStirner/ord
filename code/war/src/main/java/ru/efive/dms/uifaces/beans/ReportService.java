package ru.efive.dms.uifaces.beans;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.report.Report;

import javax.faces.context.FacesContext;

import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.standard.PrinterName;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("reportService")
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @PersistenceContext(unitName = ApplicationHelper.ORD_PERSISTENCE_UNIT_NAME)
    private EntityManager em;


    public void sqlPrintReportByRequestParams(final Report report, final Map<String, Object> reportParameters) throws JRException, IOException {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport("templates/" + report.getTemplateName());
        } catch (JRException e) {
            log.error("Report[{}] '{}' error: Cannot compile template [{}]", report.getId(), report.getDisplayName(), report.getTemplateName(), e);
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_REPORT_CREATION_CAUSE_TEMPLATE, report.getDisplayName(), report.getTemplateName(), e.getMessage());
        }
        report.getTemplateName();

        Map<String, String> requestProperties = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String in_reportName = requestProperties.get("reportName");
        String in_printerName = requestProperties.get("printerName");

        //Properties printerProperties=new Properties();
        PrintService psZebra = null;
        String sPrinterName = null;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            PrintServiceAttribute attr = service.getAttribute(PrinterName.class);
            sPrinterName = ((PrinterName) attr).getValue();
            if (sPrinterName.equals(in_printerName)) {
                psZebra = service;
                break;
            }
        }
        if (psZebra == null) {
            System.out.println(in_printerName + " is not found.");
        } else {
            System.out.println("Founded printer is >> " + psZebra.getName());
        }

        /* Get Data source */
        JasperPrint print = null;

//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
//        report = JasperCompileManager.compileReport(inputStream);
//        Map<String, Object> in_map = new HashMap<>();
//        for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
//            System.out.println("_" + entry.getKey());
//            if (StringUtils.contains(entry.getKey(), "Date")) {
//                try {
//                    Date date = new SimpleDateFormat("dd.MM.yyyy").parse(entry.getValue());
//                    in_map.put("_" + entry.getKey(), date);
//                    in_map.put(entry.getKey(), date);
//                } catch (ParseException e) {
//                    System.out.println("Wrong date parameter");
//                    in_map.put("_" + entry.getKey(), entry.getValue());
//                    in_map.put(entry.getKey(), entry.getValue());
//                }
//            } else {
//                in_map.put("_" + entry.getKey(), entry.getValue());
//                in_map.put(entry.getKey(), entry.getValue());
//            }
//        }
//        in_map.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION,  em.unwrap(Session.class));
//        print = JasperFillManager.fillReport(report, in_map);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        //Set reponse content type
        response.setContentType("application/pdf");
        //Export PDF file to browser window
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
        exporter.exportReport();


    }

    private Map<String, Object> convertToReportObjectTypes(Map<String, Object> reportParameters) {
        final Map<String, Object> result = new HashMap<>(reportParameters.size());
        reportParameters.forEach((key, value) -> {
            if (value instanceof User) {
                result.put(key, ((User) value).getId());
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    public void sqlPrintReportByRequestParams(Report reportTemplate) throws IOException, ClassNotFoundException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JRException {


        //TODO broken reportTemplate.getProperties()
        Map<String, Object> requestProperties = new HashMap<>();
        String in_reportName = requestProperties.get("reportName").toString();

        /* Get Data source */
        JasperReport report = null;
        JasperPrint print = null;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/" + in_reportName);
        report = JasperCompileManager.compileReport(inputStream);
        Map<String, Object> in_map = new HashMap<>();
        for (Map.Entry<String, Object> entry : requestProperties.entrySet()) {
            System.out.println("_" + entry.getKey());
            in_map.put("_" + entry.getKey(), entry.getValue());
            in_map.put(entry.getKey(), entry.getValue());
        }
        in_map.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION,  em.unwrap(Session.class));
        print = JasperFillManager.fillReport(report, in_map);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        //Set reponse content type
        response.setContentType("application/pdf");
        //Export PDF file to browser window
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
        exporter.exportReport();

    }
}