package com.kbhkn.restexceptionhandler.repository;

import com.kbhkn.restexceptionhandler.configurations.ExceptionHandlerPropertiesReader;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * It is a service, of which can search for API definitions.
 * It can use any datasource created as a bean and only needs the bean name for usage.
 * It can create a datasource without any conflict with the whose created in the Spring Context.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiErrorFinderRepositoryService implements ApplicationContextAware {
    private final ExceptionHandlerPropertiesReader propertiesReader;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ApplicationContext applicationContext;

    /**
     * Finds error desc for the exceptions.
     *
     * @param application which application?
     * @param module      which module of application? For the monolithic app, use self, and for microservices, use the name of microservice.
     * @param code        search key, could be error code, message, i18n apps' keys.
     * @param locale      which lang? TR, ENG, IT etc.
     * @return localizations' messages, if it exists, otherwise return empty.
     */
    public Optional<String> findErrorDescription(String application, String module, String code, String locale) {
        String sql = prepareSqlQuery();

        Map<String, String> queryParameters = getQueryParameters(application, module, code, locale);

        List<String> values = namedParameterJdbcTemplate.queryForList(sql, queryParameters, String.class);

        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    private String prepareSqlQuery() {
        String schema = propertiesReader.getDatasource().getSchemaName();

        return "select value from " + schema.toLowerCase() + ".api_translations"
            + " where application = :application and module = :module and code = :code and locale = :locale"
            + " and type = 'EX' and rownum < 2";
    }

    private Map<String, String> getQueryParameters(String application, String module, String code, String locale) {
        // Used Java11 Map feature, the first value is a key, and the second value is the value of the key.
        return Map.of(
            "code", code,
            "module", module,
            "application", application,
            "locale", locale.toUpperCase()
        );
    }


    /**
     * Creates this.context.
     *
     * @param applicationContext context.
     * @throws BeansException exception.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates namedParameterJdbcTemplate from inputted datasource name with lazy.
     */
    @PostConstruct
    public void createNamedParamJdbcTemp() {
        HikariDataSource dataSource = createOrGetDatasource();

        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        log.info("--- Api Translations Service's created. Schema: {}, Table: api_translations", propertiesReader.getDatasource().getSchemaName());
    }

    private HikariDataSource createOrGetDatasource() {
        String datasourceUrl = propertiesReader.getDatasource().getUrl();

        if (Objects.isNull(datasourceUrl) || datasourceUrl.trim().isEmpty()) {
            return getDatasourceFromApplicationContext();
        } else {
            return createHikariDatasourceWith();
        }
    }

    private HikariDataSource createHikariDatasourceWith() {
        ExceptionHandlerPropertiesReader.Datasource datasourceProperties = propertiesReader.getDatasource();

        HikariDataSource hikariDataSource = DataSourceBuilder.create()
            .url(datasourceProperties.getUrl())
            .username(datasourceProperties.getUsername())
            .password(datasourceProperties.getPassword())
            .driverClassName(datasourceProperties.getDriver())
            .type(HikariDataSource.class)
            .build();


        hikariDataSource.setPoolName("GEH_" + datasourceProperties.getName());

        log.info("--- Api Translations Service's Datasource ops. Created Api Error Datasource name: {}", hikariDataSource.getPoolName());

        return hikariDataSource;
    }

    private HikariDataSource getDatasourceFromApplicationContext() {
        String dsName = propertiesReader.getDatasource().getName();

        if (applicationContext.containsBeanDefinition(dsName)) {
            log.info("--- Api Translations Service's Datasource ops. Using already defined datasource: {}", dsName);

            return applicationContext.getBean(dsName, HikariDataSource.class);
        } else {
            log.error("There is no bean definition with: {}", dsName);

            // Can't start the application. Show error message and shutdown the application context.
            ((ConfigurableApplicationContext) applicationContext).close();

            return null;
        }
    }
}
