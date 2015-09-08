package org.constellation.database.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.constellation.configuration.ConfigurationRuntimeException;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class TestingDBConfiguration implements IDatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingDBConfiguration.class);
    private static final String DEFAULT_TEST_DATABASE_URL = "postgres://cstl:admin@localhost:5432/cstl-test";

    private DataSource testDatasource;
    private Settings testSettings;
    private SQLDialect testDialect;

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }

        String testDBURL = Application.getProperty(AppProperty.TEST_DATABASE_URL);
        if (testDBURL == null) {
            testDBURL = DEFAULT_TEST_DATABASE_URL;
        }

        HikariConfig config = DatabaseConfigurationUtils.createHikariConfig(testDBURL, "testing", 5);
        try {
            testDatasource = new HikariDataSource(config);
            testSettings = new Settings().withRenderNameStyle(RenderNameStyle.AS_IS);
            testDialect = SQLDialect.POSTGRES;
        } catch (Exception e) {
            throw new ConfigurationRuntimeException("No testing database found with matching URL : "+testDBURL, e);
        }

    }

    @Override
    public Settings getJOOQSettings() {
        return testSettings;
    }

    @Override
    public SQLDialect getDialect() {
        return testDialect;
    }

    @Override
    public DataSource createCstlDatasource() {
        return testDatasource;
    }

    @Override
    public DataSource createEPSGDatasource() {
        return testDatasource;
    }
}
