package ru.efive.dms.config;

import com.typesafe.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.util.ldap.LDAPImportService;
import ru.efive.wf.core.MailSettings;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 16:33 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Configuration
@ComponentScan({"ru.efive.dms", "ru.hitsl.sql.dao"})
@Import({PersistenceConfig.class})
public class ApplicationConfig {

    private static final Logger log = LoggerFactory.getLogger("CONFIG");

    @Bean("config")
    public Config config() {
        final Config result = ConfigFactory.parseResources("application.conf",ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)).resolve();
        log.info("Configuration parsed:\n{}", result.root().render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false)));
        return result;
    }


    @Bean("ldapImportService")
    public LDAPImportService ldapImportService(Config config) {
        final Config subCfg = config.getConfig("ldap");
        final LDAPImportService result = new LDAPImportService();
        result.setLdapAddressValue(subCfg.getString("host"));
        result.setLoginValue(subCfg.getString("login"));
        result.setPasswordValue(subCfg.getString("password"));
        result.setFilterValue(subCfg.getString("filter"));
        result.setBaseValue(subCfg.getString("path.base"));
        result.setFiredBaseValue(subCfg.getString("path.fired"));
        log.info("Initialized {}", result);
        return result;
    }

    @Bean("alfrescoDao")
    public AlfrescoDAO<Attachment> alfrescoDAO(Config config) throws InitializationException {
        final Logger log = LoggerFactory.getLogger("ALFRESCO");
        final Config subCfg = config.getConfig("alfresco");
        log.info("Configuration parsed:\n{}", subCfg.root().render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false)));

        final AlfrescoDAO<Attachment> result = new AlfrescoDAO<>();
        result.setLogin(subCfg.getString("login"));
        result.setPassword(subCfg.getString("password"));
        result.setPath(subCfg.getString("path"));
        result.setServerUrl(subCfg.getString("url"));
        result.initClass(Attachment.class);
        result.connect();
        log.info("Initialized ALFRESCO. Connect status={}", result);
        return result;
    }

    @Bean("mailSettings")
    public MailSettings mailSettings(Config config) {
        final Config subCfg = config.getConfig("mail");
        final MailSettings result = new MailSettings();
        if (subCfg.hasPath("jndiName")) {
            result.setJndi(subCfg.getString("jndiName"));
        }
        log.info("Initialized {}", result);
        return result;
    }

}
