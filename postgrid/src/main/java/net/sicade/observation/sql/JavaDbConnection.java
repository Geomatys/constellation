/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.observation.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Manages connection to Java Database. 
 *
 * @version $Id$
 * @author Cédric Briançon
 *
 * @deprecated Contient des valeurs codées en dur pour l'instant.
 */
public class JavaDbConnection {
    /**
     * The name of the database.
     */
    private static String DBNAME = "postgrid";

    /**
     * The address of the database.
     */
    private static String ADDRESS = "localhost";

    /**
     * The port to listen.
     */
    private static String PORT = "1527";

    /**
     * The user who wants to connect to the database.
     */
    private static String USER = "postgrid";

    /**
     * The password to connect to the database.
     */
    private static String PASS = "postgrid";

    /**
     * Try to connect to a Java DataBase.
     *
     * @throws SQLException
     */
    public JavaDbConnection() {
        Connection connec = null;
        try {
            connec = getConnection();
        } catch (SQLException ex) {
            System.out.println("Unable to get a connection to the JavaDB "+ DBNAME +".");
        }
        try {
            PreparedStatement stat = connec.prepareStatement("select * from postgrid.\"GridCoverages\"");
            System.out.println(stat);
            ResultSet res = stat.executeQuery();
            if (res == null) {
                System.out.println("manqué !");
            }
            res.close();
            stat.close();
        }
        catch (SQLException sql) {
            System.out.println("Unable to perform the request...");
            sql.printStackTrace();
        }
        try {
            connec.close();
        } catch (SQLException ex) {
            System.out.println("Unable to close the connection.");
        }
    }

    /**
     * Return a connection to the Java Database, if it has succeeded.
     * Otherwise it returns {@code null}.
     *
     * @return The connection or {@code null} if it fails.
     * @throws SQLException
     */
    private synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        final String url = "jdbc:derby:"+DBNAME+";user="+ USER +";password="+ PASS +";restoreFrom=C:/Program Files/Java/jdk1.6.0_01/db/frameworks/embedded/bin/postgrid";
        System.out.println(url);
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Unable to find the JDBC driver specified.");
            System.exit(-1);
        }
        connection = DriverManager.getConnection(url);
        return connection;
    }

    /**
     * Test a connection to a Java DataBase.
     */
    public static void main(String[] args) {
        new JavaDbConnection();
    }
}
