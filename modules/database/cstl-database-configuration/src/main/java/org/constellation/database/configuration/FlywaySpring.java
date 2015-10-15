package org.constellation.database.configuration;

import org.constellation.configuration.ConfigurationRuntimeException;
import org.constellation.database.model.FlywayUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;

/**
 * Bean used to initialize/migrate database in Spring context.
 *
 * @author Quentin Boileau (Geomatys)
 */
@Configuration
public class FlywaySpring {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywaySpring.class);

    @Autowired
    @Qualifier(value = "dataSource")
    private DataSource dataSource;


    @PostConstruct
    public void migrate() throws ConfigurationRuntimeException {

        LOGGER.info("Start database migration check");
        boolean liquibaseInstalled = false;
        boolean liquibaseUpToDate = false;

        //search for previous installation using liquibase
        try (Connection conn = dataSource.getConnection()) {
            final DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet liquibaseTable = metaData.getTables(null, "public", "databasechangelog", null)) {
                if (liquibaseTable.next()) {
                    liquibaseInstalled = true;

                    final String lastLBVersion = "SELECT 1 FROM public.databasechangelog WHERE id='version_1.45';";
                    try (Statement stmt = conn.createStatement();
                         ResultSet upToDate = stmt.executeQuery(lastLBVersion)) {

                        if (upToDate.next()) {
                            liquibaseUpToDate = true;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new ConfigurationRuntimeException("An error occurs during database analysis searching for " +
                    "previous installations.", ex);
        }

        try {
            final Flyway flyway = FlywayUtils.createFlywayConfig(dataSource);

            //create schema_version table if not exist, even if database is not empty
            flyway.setBaselineOnMigrate(true);

            //previous liquibase installation found but not up to date
            if (liquibaseInstalled) {
                if (liquibaseUpToDate) {
                    //start after 1.1.0.0
                    flyway.setBaselineVersion(MigrationVersion.fromVersion("1.1.0.0"));
                    LOGGER.info("Previous installation with Liquibase detected and up to date, start migration from 1.1.0.0 patch");
                } else {
                    throw new ConfigurationRuntimeException("Previous database installation found but not up to date, " +
                            "please update to 1.0.13 before applying this update.");
                }
            }
            flyway.migrate();
        } catch (SQLException ex) {
            throw new ConfigurationRuntimeException(ex.getMessage(), ex);
        }

        //clean old liquibase changelogs
        if (liquibaseInstalled) {
            LOGGER.info("Drop old liquibase changelogs tables");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS public.databasechangelog CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS public.databasechangeloglock CASCADE;");
            } catch (SQLException ex) {
                throw new ConfigurationRuntimeException("Unable to delete old liquibase changelog tables", ex);
            }
        }
    }
}
