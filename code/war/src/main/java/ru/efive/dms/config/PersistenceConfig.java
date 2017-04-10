package ru.efive.dms.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import static ru.util.ApplicationHelper.ORD_PERSISTENCE_UNIT_NAME;

/**
 * Author: Upatov Egor <br>
 * Date: 31.03.2017, 18:37 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfig {
    public static final String SESSION_FACTORY = "ordSessionFactory";
    private static final Logger log = LoggerFactory.getLogger("CONFIG");


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
        result.put("hibernate.show_sql", "false");
        result.put("hibernate.format_sql", "true");
        result.put("hibernate.max_fetch_depth", "3");
        result.put("hibernate.hbm2ddl.auto", ""); // means "NONE"
        result.put("hibernate.generate_statistics", "false");
        return result;
    }


    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            @Qualifier("ordDataSource") final DataSource dataSource,
            @Qualifier("ordHibernateProperties") final Properties props
    ) {
        final LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setDataSource(dataSource);
        result.setPackagesToScan("ru.entity.model");
        result.setJpaProperties(props);
        result.setPersistenceUnitName(ORD_PERSISTENCE_UNIT_NAME);
        result.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        log.info("Initialized 'EntityManagerFactory'[@{}]: with DataSource[@{}]",
                Integer.toHexString(result.hashCode()),
                Integer.toHexString(dataSource.hashCode())
        );
        return result;
    }

    @Bean(name = "ordTransactionManager")
    public JpaTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") final EntityManagerFactory emf
    ) {
        final JpaTransactionManager result = new JpaTransactionManager(emf);
        result.setDefaultTimeout(36000);
        log.info("Initialized 'transactionManager'[@{}]: with EntityManagerFactory[@{}]",
                Integer.toHexString(result.hashCode()),
                Integer.toHexString(emf.hashCode())
        );
        return result;
    }




}
