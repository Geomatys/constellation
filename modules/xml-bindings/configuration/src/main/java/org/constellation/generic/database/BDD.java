/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.constellation.generic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.UndeclaredThrowableException;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.dbcp.BasicDataSource;

import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.jdbc.DBCPDataSource;
import org.geotoolkit.jdbc.WrappedDataSource;
import org.apache.sis.util.logging.Logging;

import javax.sql.ConnectionPoolDataSource;
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
     * The database schema.
     */
    private String schema;

    private boolean sharedConnection = false;

    /**
     * Constructor used by JAXB
     */
    public BDD() {

    }

    public BDD(final BDD that) {
        this.className        = that.className;
        this.connectURL       = that.connectURL;
        this.password         = that.password;
        this.schema           = that.schema;
        this.sharedConnection = that.sharedConnection;
        this.user             = that.user;
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
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return the sharedConnection
     */
    public boolean isSharedConnection() {
        return sharedConnection;
    }

    /**
     * @param sharedConnection the sharedConnection to set
     */
    public void setSharedConnection(boolean sharedConnection) {
        this.sharedConnection = sharedConnection;
    }

    /**
     * Creates a data source for the given classname using the reflection API.
     * This avoid direct dependency to a driver that we can not redistribute.
     */
    private Object createDataSourceByReflection(final String classname) throws SQLException {
        try {
            final Class<?> c = Class.forName(classname);
            final Object source = c.newInstance();
            c.getMethod("setURL",      String.class).invoke(source, connectURL);
            c.getMethod("setUser",     String.class).invoke(source, user);
            c.getMethod("setPassword", String.class).invoke(source, password);
            return source;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Return a new connection to the database.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public DataSource getDataSource() throws SQLException {
        final DataSource source;
        // by Default  we use the postgres driver.
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        switch (className) {
            case POSTGRES_DRIVER_CLASS:
                if (connectURL != null && connectURL.startsWith("jdbc:postgresql://")) {
                    final PGSimpleDataSource pgSource = new PGSimpleDataSource();
                    fillSourceFromURL(pgSource);
                    source = pgSource;
                } else {
                    return null;
                }
                break;
            case ORACLE_DRIVER_CLASS:
                source = (DataSource) createDataSourceByReflection("oracle.jdbc.pool.OracleDataSource");
                break;
            default:
                source = new DefaultDataSource(connectURL);
                break;
        }
        return source;
    }

    /**
     * Return a new connection to the database.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public DataSource getPooledDataSource() {
        final DataSource source;
        // by Default  we use the postgres driver.
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        switch (className) {
            case POSTGRES_DRIVER_CLASS:
                if (connectURL != null && connectURL.startsWith("jdbc:postgresql://")) {
                    //final PGConnectionPoolDataSource pgSource = new PGConnectionPoolDataSource();
                    final BasicDataSource dataSource = new BasicDataSource();

                    // some default data source behaviour
                    dataSource.setPoolPreparedStatements(true);

                    // driver
                    dataSource.setDriverClassName(POSTGRES_DRIVER_CLASS);

                    // url
                    dataSource.setUrl(connectURL);

                    // username
                    dataSource.setUsername(user);

                    // password
                    if (password != null) {
                        dataSource.setPassword(password);
                    }

                    /* max wait
                    final Integer maxWait = (Integer) params.parameter(MAXWAIT.getName().toString()).getValue();
                    if (maxWait != null && maxWait != -1) {
                        dataSource.setMaxWait(maxWait * 1000);
                    }

                    // connection pooling options
                    final Integer minConn = (Integer) params.parameter(MINCONN.getName().toString()).getValue();
                    if ( minConn != null ) {
                        dataSource.setMinIdle(minConn);
                    }

                    final Integer maxConn = (Integer) params.parameter(MAXCONN.getName().toString()).getValue();
                    if ( maxConn != null ) {
                        dataSource.setMaxActive(maxConn);
                    }

                    final Boolean validate = (Boolean) params.parameter(VALIDATECONN.getName().toString()).getValue();
                    if(validate != null && validate && getValidationQuery() != null) {
                        dataSource.setTestOnBorrow(true);
                        dataSource.setValidationQuery(getValidationQuery());
                    }*/

                    // some datastores might need this
                    dataSource.setAccessToUnderlyingConnectionAllowed(true);

                    return new DBCPDataSource(dataSource);
                } else {
                    return null;
                }
            case ORACLE_DRIVER_CLASS:
                try {
                    source = new WrappedDataSource((ConnectionPoolDataSource)
                            createDataSourceByReflection("oracle.jdbc.pool.OracleConnectionPoolDataSource"));
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "SQLException while creating oracle datasource", ex);
                    return null;
                }
                break;
            default:
                source = new DefaultDataSource(connectURL);
                break;
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
        final String host;
        final int port;
        if (url.indexOf(':') != -1) {
            host = url.substring(0, url.indexOf(':'));
            url = url.substring(url.indexOf(':') + 1);
            final String sPort = url.substring(0, url.indexOf('/'));
            port = Integer.parseInt(sPort);
        } else {
            host = url.substring(0, url.indexOf('/'));
            port = 5432;
            LOGGER.finer("Using default postgres post 5432");
        }
            
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

    public boolean isPostgres() {
        if (className == null) {
            className = POSTGRES_DRIVER_CLASS;
        }
        return className.equals(POSTGRES_DRIVER_CLASS);
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

            return Objects.equals(this.className,  that.className)  &&
                   Objects.equals(this.connectURL, that.connectURL) &&
                   Objects.equals(this.user  ,     that.user)       &&
                   Objects.equals(this.schema  ,   that.schema)     &&
                   Objects.equals(this.password,   that.password);
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
        hash = 59 * hash + (this.schema != null ? this.schema.hashCode() : 0);
        return hash;
    }
}