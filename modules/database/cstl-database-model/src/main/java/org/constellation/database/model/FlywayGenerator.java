package org.constellation.database.model;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Main used to init database using flyway during Jooq POJO/DAO generation.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class FlywayGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayGenerator.class);

        public static void main(String[] args) throws Exception {

            String databaseURL = args[0];
            String user = args[1];
            String password = args[2];
            String update = args[3];

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                LOGGER.warn(ex.getMessage(), ex);
            }

            final HikariConfig config = createHikariConfig("constellation-generator", null, databaseURL, user, password);
            final HikariDataSource dataSource = new HikariDataSource(config);

            //Clean database before apply liquidbase scripts
            final String schemaExist = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'admin';";
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(schemaExist)) {
                if (rs.next()) {
                    LOGGER.info("Admin schema already exist, drop it.");
                    stmt.execute("DROP SCHEMA admin CASCADE;");
                    stmt.execute("DROP TABLE IF EXISTS public.databasechangelog CASCADE;");
                    stmt.execute("DROP TABLE IF EXISTS public.databasechangeloglock CASCADE;");
                }
            }

            final Flyway flyway = FlywayUtils.createFlywayConfig(dataSource);
            flyway.clean();
            flyway.migrate();

        }

        public static HikariConfig createHikariConfig(String poolName, Integer maxPoolSize, String dbUrl, String userName, String password) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);

            config.setUsername(userName);
            config.setPassword(password);

            if (poolName != null) {
                config.setPoolName(poolName);
            }

            if (maxPoolSize != null) {
                config.setMaximumPoolSize(maxPoolSize);
            }
            return config;
        }
}
