package org.constellation.admin.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.apache.commons.io.FileUtils;

public class LiquidDatabase {

    public static void main(String[] args) throws IOException {

        String path = args[0];

        if (!path.startsWith("/tmp/") && !path.contains("target/"))
            throw new IOException("tmp files must be located in /tmp or target folder: " + path);

        File dbDir = new File(path);
        if (dbDir.exists()) {
            FileUtils.deleteDirectory(dbDir);
        }

        Liquibase liquibase = null;
        try (Connection con = DriverManager.getConnection("jdbc:derby:" + path + " ;create=true")) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(con));
            liquibase = new Liquibase("cstl/db/db-changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");

            database.close();
        } catch (LiquibaseException | SQLException e) {
            throw new IOException(e.getMessage(), e);
        }

    }

}
