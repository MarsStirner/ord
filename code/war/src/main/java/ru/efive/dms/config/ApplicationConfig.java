package ru.efive.dms.config;

import com.typesafe.config.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.efive.dao.InitializationException;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.util.ldap.LDAPImportService;
import ru.efive.wf.core.MailSettings;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 16:33 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Configuration
@ComponentScan({"ru.efive.dms", "ru.hitsl.sql.dao"})
@EnableTransactionManagement
public class ApplicationConfig {
    public static final String SESSION_FACTORY = "ordSessionFactory";
    private static final Logger log = LoggerFactory.getLogger("CONFIG");
    private static ApplicationContext context;

    @Bean("config")
    public Config config() {
        final Config result = ConfigFactory.parseResources(
                "application.conf",
                ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)
        );
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
        final Config subCfg = config.getConfig("alfresco");
        final AlfrescoDAO<Attachment> result = new AlfrescoDAO<>();
        result.initClass(Attachment.class);
        log.info("Initialized {}", result);
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


    @Bean(value = "ordDataSource", destroyMethod = "")
    public DataSource lookupOrdDatasource(final Config cfg) {
        final Config dsCfg = cfg.getConfig("datasource");
        if (dsCfg.hasPath("jndiName")) {
            final String jndiName = dsCfg.getString("jndiName");
            log.info("DATASOURCE: Try initialize by jndiName[{}]", jndiName);
            try {
                final DataSource result = new JndiTemplate().lookup(jndiName, DataSource.class);
                log.info("DATASOURCE: Initialized JNDI[{}] [@{}]", jndiName, Integer.toHexString(result.hashCode()));
                return result;
            } catch (NamingException e) {
                throw new IllegalStateException("No Datasource with JNDI " + jndiName, e);
            }
        } else {
            final StringBuilder jdbcUrlBuilder = new StringBuilder("jdbc:");
            jdbcUrlBuilder.append(dsCfg.getString("rdbms"));
            jdbcUrlBuilder.append("://").append(dsCfg.getString("host")).append(':').append(dsCfg.getInt("port"));
            jdbcUrlBuilder.append('/').append(dsCfg.getString("schema"));
            log.info("DATASOURCE: JDBC URL = '{}'", jdbcUrlBuilder);
            final HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrlBuilder.toString());
            config.setUsername(dsCfg.getString("username"));
            config.setPassword(dsCfg.getString("password"));
            if (dsCfg.hasPath("connectionProperties")) {
                for (Map.Entry<String, ConfigValue> entry : dsCfg.getConfig("connectionProperties").entrySet()) {
                    config.addDataSourceProperty(entry.getKey(), entry.getValue().unwrapped());
                }
            }
            return new HikariDataSource(config);
        }
    }

    @Bean("ordHibernateProperties")
    public Properties ordHibernateProperties() {
        final Properties result = new Properties();
        result.put("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
        result.put("hibernate.show_sql", "true");
        result.put("hibernate.format_sql", "true");
        result.put("hibernate.max_fetch_depth", "3");
        result.put("hibernate.hbm2ddl.auto", ""); // means "NONE"
        return result;
    }

    @Bean(SESSION_FACTORY)
    public LocalSessionFactoryBean ordSessionFactory(
            @Qualifier("ordDataSource") final DataSource dataSource,
            @Qualifier("ordHibernateProperties") final Properties props
    ) {
        final LocalSessionFactoryBean result = new LocalSessionFactoryBean();
        result.setDataSource(dataSource);
        result.setPackagesToScan("ru.entity.model");
        result.setHibernateProperties(props);
        log.info("Initialized 'hospitalSessionFactory'[@{}]", Integer.toHexString(result.hashCode()));
        return result;
    }

    @Bean("ordTransactionManager")
    public HibernateTransactionManager ordTransactionManager(
            @Qualifier(SESSION_FACTORY) final SessionFactory sessionFactory
    ) {
        final HibernateTransactionManager result = new HibernateTransactionManager(sessionFactory);
        log.info("Initialized 'ordTransactionManager'[@{}]", Integer.toHexString(result.hashCode()));
        return result;
    }

}
