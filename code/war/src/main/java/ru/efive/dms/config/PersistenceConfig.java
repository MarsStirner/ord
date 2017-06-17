package ru.efive.dms.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.entity.model.mapped.MappedEnum;
import ru.entity.model.mapped.ReferenceBookEntity;

import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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


    @Bean(name = "ordDataSource", destroyMethod = "")
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


    @Bean("mappedEnumLoader")
    public Object loadMappedEnums(
            @Qualifier("ordTransactionManager") final JpaTransactionManager transactionManager
    ) {
        final Instant startTime = Instant.now();
        log.info("Start loading mapped enums");
        final Reflections reflections = new Reflections("ru.entity.model", new FieldAnnotationsScanner());
        final Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(MappedEnum.class);
        if (!annotatedFields.isEmpty()) {
            log.info("There are {} annotated fields", annotatedFields.size());
            boolean errors = false;
            final EntityManagerFactory entityManagerFactory = transactionManager.getEntityManagerFactory();
            final CriteriaBuilder criteriaBuilder = entityManagerFactory.getCriteriaBuilder();
            final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
            final EntityManager em = entityManagerFactory.createEntityManager();
            try {
                for (Field field : annotatedFields) {
                    final Class<?> entityTypeClass = field.getType();
                    final MappedEnum mappedEnum = field.getAnnotation(MappedEnum.class);
                    if (!entityTypeClass.isAnnotationPresent(Entity.class)) {
                        errors = true;
                        log.error("MappedEnum field [{}] class[{}] is not annotated with @{}!", field, entityTypeClass.getName(), Entity.class.getName());
                        continue;
                    }
                    if (!ReferenceBookEntity.class.isAssignableFrom(entityTypeClass)) {
                        log.warn("MappedEnum field [{}] class[{}] not extends @Entity Class[{}]!", field, entityTypeClass, ReferenceBookEntity.class.getName());
                    }
                    final CriteriaQuery<?> query = criteriaBuilder.createQuery(entityTypeClass);
                    final Root root = query.from(entityTypeClass);
                    final ParameterExpression<String> parameter = criteriaBuilder.parameter(String.class);
                    query.select(root).where(criteriaBuilder.equal(root.get(mappedEnum.uniqueField()), parameter));
                    Object result = em.createQuery(query).setParameter(parameter, mappedEnum.value()).getSingleResult();
                    log.info("MappedEnum field [{}] is {}", field, result);
                    field.setAccessible(true);
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    field.set(null, result);
                    modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
                }
            } catch (final IllegalAccessException | NoSuchFieldException e) {
                log.error("Cannot set field value", e);
            } finally {
                transactionManager.commit(transaction);
            }
        } else {
            log.warn("No fields with Annotation[{}] found", MappedEnum.class);
        }
        log.info("End loading mapped enums [{} ms] ", Duration.between(startTime, Instant.now()).toMillis());
        return null;
    }


}
