package org.constellation.database.configuration;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class DatabaseConfigurationTest {

    @Test
    public void testDatabaseURLParser() {
        String postgresURL = "postgres://login:passwd@localhost:5432/database";
        final String postgresJDBC = DatabaseConfigurationUtils.extractJDBCUrl(postgresURL);
        final Map.Entry<String, String> userInfo = DatabaseConfigurationUtils.extractUserPassword(postgresURL);
        Assert.assertEquals("jdbc:postgresql://localhost:5432/database", postgresJDBC);
        Assert.assertEquals("login", userInfo.getKey());
        Assert.assertEquals("passwd", userInfo.getValue());

        String derbyMemoryURL = "derby:derby:memory:db";
        final String derbyMemJDBC = DatabaseConfigurationUtils.extractJDBCUrl(derbyMemoryURL);
        Assert.assertEquals("jdbc:derby:derby:memory:db;create=true", derbyMemJDBC);

        String derbyFSURL = "derby:/folder/derby/database";
        final String derbyFSJDBC = DatabaseConfigurationUtils.extractJDBCUrl(derbyFSURL);
        Assert.assertEquals("jdbc:derby:/folder/derby/database;create=true", derbyFSJDBC);

    }

}
