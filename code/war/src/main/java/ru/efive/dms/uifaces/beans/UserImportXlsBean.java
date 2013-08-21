package ru.efive.dms.uifaces.beans;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.context.annotation.DependsOn;

import ru.efive.dms.util.ApplicationContextHelper;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAO;
import ru.efive.sql.entity.user.User;

@Singleton
@Startup
@Named("userImport")
@DependsOn("indexManagement")
@ApplicationScoped
public class UserImportXlsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * @Inject
     * @Named("sessionManagement") private transient SessionManagementBean sessionManagement;
     */

    private UserDAO userDao;

    private static final String RESOURCE = "/import/Users.xls";

    @PostConstruct
    public void init() throws IOException {
        InputStream fis = null;
        try {
            fis = new FileInputStream(getClass().getResource(RESOURCE).getFile());
            HSSFWorkbook book = new HSSFWorkbook(fis);
            Sheet sheet = book.getSheetAt(0);
            if (!isImported(sheet)) {
                doImport(sheet);
            }
            setImported(sheet);
            OutputStream fos = new FileOutputStream(getClass().getResource(RESOURCE).getFile());
            book.write(fos);
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }

    public void doImport(Sheet sheet) throws IOException {
        userDao = ApplicationContextHelper.getBean(ApplicationHelper.USER_DAO);
        System.out.println("Import users from xls file");
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        iterator.next();
        while (iterator.hasNext()) {
            importUser(iterator.next());
        }
    }

    private void importUser(Row row) {
        User user = new User();
        String email = row.getCell(3).getStringCellValue();
        user.setEmail(email);
        user.setLogin(getWordAt(email, "@", 0));

        User dbUser = userDao.getByLogin(user.getLogin());
        if (dbUser == null || Boolean.TRUE.equals(dbUser.getFired())) {

            if (dbUser != null) {
                user = dbUser;
            } else {
                user.setPassword("123");
            }

            String fullName = row.getCell(0).getStringCellValue();
            user.setLastName(getWordAt(fullName, 0));
            user.setFirstName(getWordAt(fullName, 1));
            user.setMiddleName(getWordAt(fullName, 2));

            user.setJobPosition(row.getCell(1).getStringCellValue());
            user.setJobDepartment(row.getCell(2).getStringCellValue());

            if (row.getCell(4).getCellType() == 1) {
                String phone = row.getCell(4).getStringCellValue();
                user.setPhone(phone);
            }
            if (row.getCell(4).getCellType() == 0) {
                double phone = row.getCell(4).getNumericCellValue();
                user.setPhone(String.valueOf(phone));
            }

            user.setFired(false);
            System.out.println("Savin user: " + user.getLogin());
            userDao.save(user);
        }
    }

    private String getWordAt(String str, int pos) {
        String[] words = str.split(" ");
        return words.length <= pos ? "":  words[pos];
    }

    private String getWordAt(String str, String splitter, int pos) {
        String[] words = str.split(splitter);
        return words.length <= pos ? "":  words[pos];
    }

    private boolean isImported(Sheet sheet) {
        return sheet.getRow(0).getCell(0).getNumericCellValue() == 1;
    }

    private void setImported(Sheet sheet) {
        sheet.getRow(0).getCell(0).setCellValue(1);
    }

}
