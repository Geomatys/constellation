/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package org.constellation.generic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;

import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.jdbc.WrappedDataSource;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.util.logging.Logging;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "BDD")
public class BDD {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.generic.database");

    public static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

    public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

    private static final Map<BDD, Connection> CONNECTION_MAP = new HashMap<BDD, Connection>();
    
    /**
     * The className of the driver
     */
    private String className;
    
    /**
     * The url to connect the database
     */
    private String connectURL;
    
    /**
     * The username connecting the database
     */
    private String user;
    
    /**
     * The password of the user
     */
    private String password;

    /**
     * Constructor used by JAXB
     */
    public BDD() {

    }

    /**
     * Build a new DataSource informations.
     *
     * @param className the type of the driver (such as "org.postgresql.Driver").
     * @param connectURL the url of the database.
     * @param user The user name.
     * @param password The password.
     */
    public BDD(String className, String connectURL, String user, String password) {
        this.className  = className;
        this.connectURL = connectURL;
        this.password   = password;
        this.user       = user;
    }

    /**
     * Return the type of the driver (such as "org.postgresql.Driver").
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     * Return the url of the database
     * @return
     */
    public String getConnectURL() {
        return connectURL;
    }

    /**
     * Return  The user name.
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * return the passsword of the user.
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * extract the host name from the database url.
     * @return
     */
    public String getHostName() {
        if (connectURL != null && connectURL.indexOf("://") != -1) {
            String hostName = connectURL.substring(connectURL.indexOf("://") + 3);
            if (hostName.indexOf(':') != -1) {
                hostName        = hostName.substring(0, hostName.indexOf(':'));
                return hostName;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * extract the database name from the database url.
     * @return
     */
    public String getDatabaseName() {
        if (connectURL != null && connectURL.lastIndexOf('/') != -1) {
            return connectURL.substring(connectURL.lastIndexOf('/') + 1);
        }
        return null;
    }

    /**
     * extract the port number from the database url or 5432 if its not present.
     * @return
     */
    public int getPortNumber() {
        if (connectURL != null && connectURL.lastIndexOf(':') != -1) {
            String portName = connectURL.substring(connectURL.lastIndexOf(':') + 1);
            if (portName.indexOf('/') != -1) {
                portName        = portName.substring(0, portName.indexOf('/'));
                try {
                    return Integer.parseInt(portName);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "unable to parse the port number: {0} using default", portName);
                    return 5432;
                }
            } else {
                return 5432;
            }
        }
        return 5432;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @param connectURL the connectURL to set
     */
    public void setConnectURL(final String connectURL) {
        this.connectURL = connectURL;
    }

    /**
     * @param user the user to set
     */
    public void setUser(final String user) {
        this.user = user;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }
    
    /**
     * Return a new connection to the database.
     * 
     * @return
     * @throws java.sql.SQLException
     *
     * @todo The call to Class.forName(...) is not needed anymore since Java 6 and should be removed.
     */
    public Connection getConnection() throws SQLException {
        Connection conec = CONNECTION_MAP.get(this);
        if (conec == null || conec.isClosed()) {
            // by Default  we use the postgres driver.
            if (className == null) {
                className = POSTGRES_DRIVER_CLASS;
            }
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                // Non-fatal exception, ignore. If there is really a problem, the
                // following line is expected to throw the appropriate SQLException.
            }
            conec = DriverManager.getConnection(connectURL, user, password);
            CONNECTION_MAP.put(this, conec);
        }
        return conec;
    }

    /**
     * Return a new connection to the database.
     *
     * @return
     * @throws java.sql.SQLException
     *
     * @todo The call to Class.forName(...) is not needed anymore since Java 6 and should be removed.
     */
    public DataSource getDataSource() throws SQLException {
        final DataSource source;
        // by Default  we use the postgres driver.
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        if (className.equals(POSTGRES_DRIVER_CLASS)) {
            if (connectURL != null && connectURL.startsWith("jdbc:postgresql://")) {
                final PGSimpleDataSource pgSource = new PGSimpleDataSource();
                fillSourceFromURL(pgSource);
                source = pgSource;
            } else {
                return null;
            }
        } else if (className.equals(ORACLE_DRIVER_CLASS)) {
            final OracleDataSource oraSource = new OracleDataSource();
            oraSource.setURL(connectURL);
            oraSource.setUser(user);
            oraSource.setPassword(password);
            source = oraSource;
        } else {
            source = new DefaultDataSource(connectURL);
        }
        return source;
    }

    /**
     * Return a new connection to the database.
     *
     * @return
     * @throws java.sql.SQLException
     *
     * @todo The call to Class.forName(...) is not needed anymore since Java 6 and should be removed.
     */
    public DataSource getPooledDataSource() throws SQLException {
        final DataSource source;
        // by Default  we use the postgres driver.
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        if (className.equals(POSTGRES_DRIVER_CLASS)) {
            if (connectURL != null && connectURL.startsWith("jdbc:postgresql://")) {
                final PGConnectionPoolDataSource pgSource = new PGConnectionPoolDataSource();
                fillSourceFromURL(pgSource);
                source = new WrappedDataSource(pgSource);
            } else {
                return null;
            }
        } else if (className.equals(ORACLE_DRIVER_CLASS)) {
            final OracleConnectionPoolDataSource oraSource = new OracleConnectionPoolDataSource();
            oraSource.setURL(connectURL);
            oraSource.setUser(user);
            oraSource.setPassword(password);
            source = new WrappedDataSource(oraSource);
        } else {
            source = new DefaultDataSource(connectURL);
        }
        return source;
    }

    /**
     * Fill the dataSource supplied with the informations extracted from the database URL.
     * @param pgSource
     */
    private void fillSourceFromURL(final BaseDataSource pgSource) {
         // exemple : jdbc:postgresql://localhost:5432/mdweb-SML
         String url = connectURL.substring(18);
        final String host = url.substring(0, url.indexOf(':'));
        url = url.substring(url.indexOf(':') + 1);
        final String sPort = url.substring(0, url.indexOf('/'));
        final int port = Integer.parseInt(sPort);
        final String dbName = url.substring(url.indexOf('/') + 1);

        pgSource.setServerName(host);
        pgSource.setPortNumber(port);
        pgSource.setDatabaseName(dbName);
        pgSource.setUser(user);
        pgSource.setPassword(password);
    }

    /**
     * Return a new connection to the database.
     *
     * @return
     * @throws java.sql.SQLException
     *
     * @todo The call to Class.forName(...) is not needed anymore since Java 6 and should be removed.
     */
    public Connection getFreshConnection() throws SQLException {

        // by Default  we use the postgres driver.
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Non-fatal exception, ignore. If there is really a problem, the
            // following line is expected to throw the appropriate SQLException.
        }
        return DriverManager.getConnection(connectURL, user, password);
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[BDD]");
        s.append("className: ").append(className).append('\n');
        s.append("connectURL: ").append(connectURL).append('\n');
        s.append("user: ").append(user).append('\n');
        s.append("password: ").append(password).append('\n');
        return s.toString();
    }
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BDD) {
            final BDD that = (BDD) object;

            return Utilities.equals(this.className,  that.className)  &&
                   Utilities.equals(this.connectURL, that.connectURL) &&
                   Utilities.equals(this.user  ,     that.user)       &&
                   Utilities.equals(this.password,   that.password);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 59 * hash + (this.connectURL != null ? this.connectURL.hashCode() : 0);
        hash = 59 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 59 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }
}