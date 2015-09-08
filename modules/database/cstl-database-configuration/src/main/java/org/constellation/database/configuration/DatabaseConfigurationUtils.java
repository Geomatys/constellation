package org.constellation.database.configuration;

import com.zaxxer.hikari.HikariConfig;
import org.constellation.configuration.ConfigurationRuntimeException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class DatabaseConfigurationUtils {

    private DatabaseConfigurationUtils() {}

    /**
     * Parse and convert database URL in Hiroku form into JDBC compatible URL.
     * Only support postgres and derby JDBC url.
     *
     * @param databaseURL
     * @return JDBC url String
     * @throws ConfigurationRuntimeException
     */
    public static String extractJDBCUrl(String databaseURL) throws ConfigurationRuntimeException {
        URI dbUri;
        try {
            dbUri = new URI(databaseURL);
        } catch (URISyntaxException e) {
            throw new ConfigurationRuntimeException("", e);
        }

        String scheme = dbUri.getScheme();
        if (scheme.equals("derby")) {
            if (!databaseURL.contains("create=true")) {
                databaseURL = databaseURL.concat(";create=true");
            }
            return "jdbc:"+databaseURL;
        }

        if (scheme.equals("postgres")) {
            scheme = "postgresql";
        }

        return  "jdbc:"+ scheme +"://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
    }

    /**
     * Parse and extract from database URL in Hiroku user infos (login, password).
     *
     * @param databaseURL
     * @return
     * @throws ConfigurationRuntimeException
     */
    public static Map.Entry<String, String> extractUserPassword(String databaseURL) throws ConfigurationRuntimeException {
        URI dbUri;
        try {
            dbUri = new URI(databaseURL);
        } catch (URISyntaxException e) {
            throw new ConfigurationRuntimeException("", e);
        }
        if (dbUri.getUserInfo() != null) {
            final String username = dbUri.getUserInfo().split(":")[0];
            final String password = dbUri.getUserInfo().split(":")[1];
            return new AbstractMap.SimpleImmutableEntry<>(username, password);
        }
        return null;
    }

    /**
     *
     * @param databaseURL postgres database URL in Hiroku like format
     * @param poolName pool name optional
     * @param maxPoolSize maximum pool size. If null use Hikari default value
     * @return
     */
    public static HikariConfig createHikariConfig(String databaseURL, String poolName, Integer maxPoolSize) {

        final String dbUrl = extractJDBCUrl(databaseURL);
        final Map.Entry<String, String> userInfo = extractUserPassword(databaseURL);

        String user = null;
        String password = null;

        if (userInfo != null) {
            user = userInfo.getKey();
            password = userInfo.getValue();
        }

        return createHikariConfig(poolName, maxPoolSize, dbUrl, user, password);
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
