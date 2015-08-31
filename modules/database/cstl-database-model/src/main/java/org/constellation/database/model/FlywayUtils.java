package org.constellation.database.model;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class FlywayUtils {

    public static Flyway createFlywayConfig(DataSource dataSource) throws SQLException {

        final String locationString = "org/constellation/database/model/migration";

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(locationString);

        final HashMap<String, String> placeholders = new HashMap<>();
        final PlaceholderReplacer defaultReplacer = new PlaceholderReplacer(placeholders, "${", "}");
        final String encoding = "UTF-8";
        final String prefix = "T";
        final String separator = "__";
        final String suffix = ".sql";

        try (final Connection connection = dataSource.getConnection()) {
            DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, false);
            final MigrationResolver sqlResolver = new SqlMigrationResolver(
                    dbSupport,
                    flyway.getClassLoader(),
                    new Location(locationString),
                    defaultReplacer,
                    encoding,
                    prefix,
                    separator,
                    suffix);
            flyway.setResolvers(sqlResolver);
        }

        return flyway;
    }
}
