/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.catalog;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import javax.sql.DataSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Properties;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Constructor;

import org.geotoolkit.io.TableWriter;
import org.geotoolkit.internal.jdbc.JDBC;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Connection to an observation database through JDBC (<cite>Java Database Connectivity</cite>).
 * Connection parameters are stored in a {@code "config.xml"} configuration file stored in the
 * {@code "Application Data\Sicade"} user directory on Windows, or {@code ".sicade"} user
 * directory on Unix.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Database {
    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";

    /**
     * The file where to store configuration parameters.
     */
    private static final String CONFIG_FILENAME = "config.xml";

    /**
     * {@code false} if RMI should be disabled. This flag is set on the command line using the
     * {@code -Drmi.enabled=false} option. We recommand disabling RMI while debugging in order
     * to make easier to step into code that would be normaly be executed on a distant server.
     */
    private static final boolean RMI_ENABLED;
    static {
        boolean enabled;
        try {
            enabled = Boolean.parseBoolean(System.getProperty("rmi.enabled", "true"));
        } catch (SecurityException e) {
            enabled = true;
        }
        RMI_ENABLED = enabled;
        if (!RMI_ENABLED) {
            Entry.LOGGER.config("RMI désactivés.");
        }
    }

    /**
     * The data source, or {@code null} if none.
     */
    private final DataSource source;

    /**
     * The catalog, or {@code null} if none.
     */
    public final String catalog;

    /**
     * The schema, or {@code null} if none.
     */
    public final String schema;

    /**
     * Connection to the database. Will be etablished only when first needed.
     */
    private Connection connection;

    /**
     * {@code true} if the {@code toString()} method on {@link PreparedStatement} instances
     * returns the SQL query with parameters filled in. This is the case with the PostgreSQL
     * JDBC driver.
     */
    private final boolean isStatementFormatted;

    /**
     * If non-null, SQL {@code INSERT}, {@code UPDATE} or {@code DELETE} statements will not be
     * executed but will rather be printed to this stream. This is used for testing and debugging
     * purpose only.
     */
    private PrintWriter updateSimulator;

    /**
     * Lock for transactions performing write operations. The {@link Connection#commit} or
     * {@link Connection#rollback} method will be invoked when the lock count reach zero.
     *
     * @see #transactionBegin
     * @see #transactionEnd
     *
     * @todo This lock alone is not suffisient. We also need to use a {@link Connection}
     *       for write operation which is different than the connection for read operations,
     *       in order to avoid the read operation to see ungoing changes before they are
     *       commited. In this process, we should probably consider replacing the permanent
     *       {@link #connection} reference by a connection pool.
     */
    private final ReentrantLock transactionLock = new ReentrantLock(true);

    /**
     * The tables created up to date.
     */
    private final Map<Class<? extends Table>, Table> tables = new HashMap<Class<? extends Table>, Table>();

    /**
     * The RMI connections created up to date with the {@link #getRemote} method.
     */
    private final Map<String, Remote> remotes = new HashMap<String, Remote>();

    /**
     * The timezone to use for reading and writing dates in the database. This timezone
     * can be set by the {@link #TIMEZONE} property and will be used for creating the
     * calendar returned by {@link Table#getCalendar}.
     */
    private final TimeZone timezone;

    /**
     * The type of CRS. Hard-coded for now but may change in a future version.
     */
    final CRS crsType = CRS.XYT;

    /**
     * The properties read from the {@link #configFilename} file.
     */
    private final Properties properties;

    /**
     * The configuration filename. Should be {@value #CONFIG_FILENAME} in most cases.
     */
    private final String configFilename;

    /**
     * Set to {@code true} if the {@linkplain #properties} have been modified.
     * In such case, they will be saved when the database will be {@linkplain #close closed}.
     */
    private boolean modified;

    /**
     * Invoked automatically when the virtual machine is about to shutdown.
     * The finalizer invokes {@link #close}.
     */
    private Thread finalizer;

    /**
     * Opens a new connection using only the information provided in the configuration file.
     *
     * @throws IOException if an error occured while reading the configuration file.
     */
    public Database() throws IOException {
        this((DataSource) null);
    }

    /**
     * Opens a new connection using the provided data source.
     *
     * @param  source The data source, or {@code null} if none.
     * @throws IOException if an error occured while reading the configuration file.
     */
    public Database(final DataSource source) throws IOException {
        this(source, CONFIG_FILENAME);
    }

    /**
     * Opens a new connection using the provided configuration file.
     *
     * @param  configFile The configuration file or directory.
     * @throws IOException if an error occured while reading the configuration file.
     */
    public Database(final File configFile) throws IOException {
        this(null, configFile.getPath());
    }

    /**
     * Opens a new connection using the provided data source and configuration file.
     *
     * @param  source The data source, or {@code null} if none.
     * @param  configFilename The configuration filename.
     * @throws IOException if an error occured while reading the configuration file.
     */
    public Database(final DataSource source, final String configFilename) throws IOException {
        this(source, null, configFilename);
    }

    /**
     * Uses an existing connection and configuration properties. The properties are the same
     * ones than the ones usually read from a configuration file, with the same {@linkplain
     * ConfigurationKey configuration keys}. The configuration file will not be read at all.
     * <p>
     * This method is used when the connection and the configuration are already defined in
     * another way, for example in the <cite>Talend</cite> framework.
     *
     * @param  connection The connection to the database.
     * @param  properties The configuration properties, like the driver used or the root
     *         directory for images.
     * @throws IOException if an error occured while reading additional configuration data.
     * @throws SQLException if an error occured while configuring the connection.
     */
    public Database(final Connection connection,  final Properties properties)
            throws IOException, SQLException
    {
        this(null, properties, null);
        this.connection = connection;
        setupConnection();
    }

    /**
     * Opens a new connection ou uses an existing ones.
     *
     * @param  source The data source, or {@code null} if none.
     * @param  connection The connection to the database,
     *         or {@code null} to fetch it from the data source.
     * @param  properties The configuration properties, or {@code null} if none.
     * @param  configFilename The configuration filename. Ignored if {@code properties} is non-null.
     * @throws IOException if an error occured while reading the configuration file.
     */
    private Database(final DataSource source, final Properties props, final String configFilename)
            throws IOException
    {
        this.source = source;
        this.configFilename = configFilename;
        /*
         * Procède d'abord à la lecture du fichier de configuration,  afin de permettre
         * à la méthode 'getProperty' de fonctionner. Cette dernière sera utilisée dans
         * les lignes suivantes, et risque aussi d'être surchargée.
         */
        properties = new Properties();
        if (props != null) {
            properties.putAll(props);
        } else {
            final File file = getConfigurationFile(false);
            if (file!=null && file.exists()) {
                final InputStream in = new FileInputStream(file);
                properties.loadFromXML(in);
                in.close();
                Element.LOGGER.config("PostGrid configuration file is " + file);
            } else {
                Element.LOGGER.warning("No " + CONFIG_FILENAME + " file found. Fallback on default.");
            }
        }
        final String id = getProperty(ConfigurationKey.TIMEZONE);
        timezone = (id!=null && !id.equalsIgnoreCase("local")) ? TimeZone.getTimeZone(id) : TimeZone.getDefault();
        catalog  = getProperty(ConfigurationKey.CATALOG);
        schema   = getProperty(ConfigurationKey.SCHEMA);
        /*
         * Checks if the database is spatial-enabled.
         * TODO: Following code is PostgreSQL specific. We need to make it more generic.
         */
        final String driver = (source!=null) ? source.getClass().getName() : getProperty(ConfigurationKey.DRIVER);
        if (driver.startsWith("org.postgresql")) {
            isStatementFormatted = true;
        } else {
            isStatementFormatted = false;
        }
        /*
         * Prépare un processus qui fermera automatiquement les connections lors de l'arrêt
         * de la machine virtuelle si l'utilisateur n'appelle par close() lui-même.
         */
        finalizer = new Finalizer();
        Runtime.getRuntime().addShutdownHook(finalizer);
    }

    /**
     * Returns the file where to read or write user configuration. If no such file is found,
     * then this method returns {@code null}. This method is allowed to create the destination
     * directory if and only if {@code create} is {@code true}.
     * <p>
     * Subclasses may override this method in order to search for an other file than the default one.
     *
     * @param  create {@code true} if this method is allowed to create the destination directory.
     * @return The configuration file, or {@code null} if none.
     */
    File getConfigurationFile(final boolean create) {
        if (configFilename == null) {
            return null;
        }
        /*
         * Searchs in current directory first.
         */
        File path = new File(configFilename);
        if (path.isFile()) {
            return path;
        }
        if (path.isDirectory()) {
            path = new File(path, CONFIG_FILENAME);
            if (path.isFile()) {
                return path;
            }
        }
        /*
         * If the file is relative and do not exists, replaces what we have computed
         * so far by a new values inferred from the user home directory.
         */
        if (!path.isAbsolute()) {
            final String home = System.getProperty("user.home");
            if (System.getProperty("os.name", "").startsWith("Windows")) {
                path = new File(home, WINDOWS_DIRECTORY);
            } else {
                path = new File(home, UNIX_DIRECTORY);
            }
        }
        if (!path.exists()) {
            if (!create || !path.mkdir()) {
                return null;
            }
        }
        return new File(path, configFilename);
    }

    /**
     * Returns the connection to the database. If a {@linkplain DataSource data source} has been
     * specified at construction time. If {@linkplain ConfigurationKey#USER user} and {@linkplain
     * ConfigurationKey#PASSWORD password} properties are defined, they will have precedence over
     * the value specified in the {@linkplain DataSource data source}. If no data source was specified
     * at construction time, then the {@linkplain ConfigurationKey#DATABASE database} and
     * {@linkplain ConfigurationKey#DRIVER driver} properties will be queried.
     *
     * @return The connection to the database.
     * @throws SQLException if the connection can not be created.
     */
    public synchronized Connection getConnection() throws SQLException {
        // The following assertions are used in order to reduce the risk
        // of deadlock in the 'close()' method.
        assert !Thread.holdsLock(properties);
        assert !Thread.holdsLock(tables);
        assert !Thread.holdsLock(remotes);
        if (connection == null) {
            final String user     = getProperty(ConfigurationKey.USER);
            final String password = getProperty(ConfigurationKey.PASSWORD);
            if (source != null) {
                if (user!=null && password!=null) {
                    connection = source.getConnection(user, password);
                } else {
                    connection = source.getConnection();
                }
            } else {
                final String database = getProperty(ConfigurationKey.DATABASE);
                if (database != null) {
                    JDBC.loadDriver(getProperty(ConfigurationKey.DRIVER));
                    connection = DriverManager.getConnection(database, user, password);
                } else {
                    connection = null;
                }
            }
            setupConnection();
        }
        return connection;
    }

    /**
     * Setup the connection. This method is invoked when a new connection is etablished
     * or supplied by user.
     *
     * @throws SQLException if an error occured while setting-up the connection.
     *
     * @todo Localize
     */
    private void setupConnection() throws SQLException {
        if (connection != null) {
            connection.setReadOnly(Boolean.valueOf(getProperty(ConfigurationKey.READONLY)));
            final DatabaseMetaData metadata = connection.getMetaData();
            Element.LOGGER.info("Connected to \"" + metadata.getURL() +
                                "\" database as \"" + metadata.getUserName() + "\" user.");
        }
    }

    /**
     * Returns the locale for international message formatting.
     *
     * @return The locale for message formatting.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns the timezone in which the dates in the database are expressed. This information
     * can be specified through the {@link ConfigurationKey#TIMEZONE} property. It is used in
     * order to convert the dates from the database timezone to UTC.
     *
     * @return The time zone for dates to appear in database records.
     *
     * @see Table#getCalendar
     */
    public final TimeZone getTimeZone() {
        return (TimeZone) timezone.clone();
    }

    /**
     * Creates and returns a new calendar using the database timezone.
     * <p>
     * <strong>Note à propos de l'implémentation:</strong> Les conventions locales utilisées
     * seront celles du {@linkplain Locale#CANADA Canada anglais}, car elles sont proches de
     * celles des États-Unis (utilisées sur la plupart des logiciels comme PostgreSQL) tout en
     * étant un peu plus pratique (dates dans l'ordre "yyyy/mm/dd"). Notez que nous n'utilisons
     * pas {@link Calendar#getInstance()}, car ce dernier peut retourner un calendrier très
     * différent de celui utilisé par la plupart des logiciels de bases de données existants
     * (par exemple un calendrier japonais).
     *
     * @param database The database, which may be {@code null}.
     */
    static Calendar getCalendar(final Database database) {
        if (database != null) {
            return new GregorianCalendar(database.timezone, Locale.CANADA);
        } else {
            return new GregorianCalendar(Locale.CANADA);
        }
    }

    /**
     * Returns the coordinate reference system to be used for spatial queries in the database.
     * This is usually the CRS matching the one used in an indexed geometry column.
     *
     * @return The coordinate reference system for spatial queries.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crsType.getCoordinateReferenceSystem();
    }

    /**
     * Returns a property. The key is usually a constant like {@link ConfigurationKey#TIMEZONE}.
     *
     * @param key The key for the property to fetch.
     * @return The property value, or {@code null} if none.
     */
    public String getProperty(final ConfigurationKey key) {
        // No need to synchronize since 'Properties' is already synchronized.
        final String value = properties.getProperty(key.getKey(), key.getDefaultValue());
        if (value == null) {
            if (key.equals(ConfigurationKey.TIMEZONE)) {
                return timezone.getID();
            }
        }
        return value;
    }

    /**
     * Sets a new property value for the given key. This new value will be saved in the
     * configuration file at some later time. The value may not be taken in account before
     * the database is {@linkplain #close closed} and restarted.
     *
     * @param key   The property key.
     * @param value The new value, or {@code null} for restoring the default value.
     */
    public void setProperty(final ConfigurationKey key, final String value) {
        synchronized (properties) {
            if (value != null) {
                if (!value.equals(properties.setProperty(key.getKey(), value))) {
                    modified = true;
                }
            } else {
                if (properties.remove(key.getKey()) != null) {
                    modified = true;
                }
            }
        }
    }

    /**
     * Returns a table of the specified type.
     *
     * @param  <T> The table class.
     * @param  type The table class.
     * @return An instance of a table of the specified type.
     * @throws NoSuchTableException if the specified type is unknown to this database.
     */
    public final <T extends Table> T getTable(final Class<T> type) throws NoSuchTableException {
        synchronized (tables) {
            T table = type.cast(tables.get(type));
            if (table == null) {
                table = createTable(type);
                table.freeze();
                tables.put(type, table);
            }
            assert !table.isModifiable() : table;
            return table;
        }
    }

    /**
     * Retourne une nouvelle instance du type de table spécifié. Cette méthode est appelée
     * automatiquement par <code>{@linkplain #getTable getTable}(type)</code> lorsque cette
     * dernière a déterminée que la création d'une nouvelle instance de la table est nécessaire.
     * L'implémentation par défaut tente d'appeller un constructeur qui attend pour seul argument
     * un objet {@code Database}.
     *
     * @param  type Le type de la table (par exemple <code>{@linkplain StationTable}.class</code>).
     * @return Une nouvelle instance d'une table du type spécifié.
     * @throws NoSuchTableException si le type spécifié n'est pas connu.
     */
    private <T extends Table> T createTable(final Class<T> type) throws NoSuchTableException {
        try {
            final Constructor<T> c = type.getConstructor(Database.class);
            c.setAccessible(true);
            return c.newInstance(this);
        } catch (Exception exception) {
            /*
             * Attraper toutes les exceptions n'est pas recommandé,
             * mais il y en a un bon paquet dans le code ci-dessus.
             */
            throw new NoSuchTableException(Classes.getShortName(type), exception);
        }
    }

    /**
     * Returns an remote object for the specified name. The name should be registered on the remote server.
     * A typical value is {@value org.constellation.coverage.catalog.rmi.DataConnectionFactory#REGISTRY_NAME}.
     * <p>
     * If this method has already been invoked previously with the specified name, the same object
     * is returned. Otherwise, a new connection is etablished and the object returned. If the object
     * can't be obtained because no object is defined for the specified name or because the server
     * refused the connection (for example because the server is down), then this method returns
     * {@code null}. If the object can't be obtained for an other reason, then an exception is
     * thrown.
     *
     * @param  name The name of the remote object to fetch, or {@code null} if none.
     * @return A shared instance of the specified object, or {@code null} if no object is available
     *         on the server for that name.
     * @throws RemoteException if the operation failed for an other reason than non-existant
     *         object or server not responding.
     */
    public Remote getRemote(final String name) throws RemoteException {
        if (!RMI_ENABLED) {
            return null;
        }
        synchronized (remotes) {
            Remote candidate = remotes.get(name);
            if (candidate==null && !remotes.containsKey(name)) {
                final String server = getProperty(ConfigurationKey.REMOTE_SERVER);
                if (server != null) {
                    final StringBuilder url = new StringBuilder();
                    if (!server.startsWith("/")) {
                        url.append("//");
                    }
                    url.append(server);
                    if (!server.endsWith("/")) {
                        url.append('/');
                    }
                    url.append(name);
                    try {
                        candidate = Naming.lookup(url.toString());
                    } catch (MalformedURLException exception) {
                        /*
                         * Ne devrait pas se produire, puisque l'on a construit notre URL nous-même
                         * et que l'argument 'name' est habituellement une constante d'une des
                         * interfaces du paquet org.constellation.observation.coverage.rmi.
                         */
                        throw new IllegalArgumentException(name, exception);
                    } catch (ConnectException exception) {
                        /*
                         * La connexion au serveur a été refusée; peut-être le serveur n'est pas
                         * en service. Retourne null, ce qui signifie que l'appellant devra se
                         * passer de RMI (par exemple en téléchargeant les images par FTP).
                         */
                        unexpectedException("getRemote", exception);
                    } catch (NotBoundException exception) {
                        /*
                         * L'objet demandé n'a pas été mis en service. Retourne null, ce qui
                         * signifie que l'appellant devra se passer de RMI (par exemple en
                         * téléchargeant les images par FTP).
                         */
                        unexpectedException("getRemote", exception);
                    }
                }
                /*
                 * Sauvegarde la référence même si elle est restée nulle afin d'éviter
                 * de recommencer les tentatives de connections à chaque appel.
                 */
                remotes.put(name, candidate);
            }
            return candidate;
        }
    }

    /**
     * Sets the default behaviour of writing access for the connection. If {@true}, the
     * connection only allows to read data. Otherwise writing permission is granted.
     *
     * @param readOnly {@code true} if the database connection should be read-only.
     * @throws SQLException if a database access error occurs.
     */
    public void setReadOnly(final boolean readOnly) throws SQLException {
        properties.setProperty(ConfigurationKey.READONLY.getKey(), String.valueOf(readOnly));
        if (connection != null) {
            connection.setReadOnly(readOnly);
        }
    }

    /**
     * If non-null, SQL {@code INSERT}, {@code UPDATE} or {@code DELETE} statements will not be
     * executed but will rather be printed to this stream. This is used for testing and debugging
     * purpose only.
     *
     * @param out Where to print SQL statements that perform changes in database content.
     */
    public void setUpdateSimulator(final PrintWriter out) {
        updateSimulator = out;
    }

    /**
     * Returns the value set by the last call to {@link #setUpdateSimulator},
     * or {@code null} if none.
     */
    final PrintWriter getUpdateSimulator() {
        return updateSimulator;
    }

    /**
     * Returns a string representation of the specified statement. This method tries
     * to replace the {@code '?'} parameters by the actual parameter values.
     *
     * @param statement The SQL statement to format.
     * @param query     The SQL query used for preparing the statement.
     * @return A string representation of the given statement.
     */
    public String format(final Statement statement, final String query) {
        if (isStatementFormatted) {
            return statement.toString();
        } else {
            return query; // TODO: do something better.
        }
    }

    /**
     * Enregistre un événement dans le journal signalant qu'une erreur est survenue dans la
     * méthode spécifiée, mais que cette erreur n'empêche pas un fonctionnement à peu près
     * normal.
     */
    private void unexpectedException(final String method, final Exception exception) {
        Logging.unexpectedException(Element.LOGGER, Database.class, method, exception);
    }

    /**
     * Invoked before an arbitrary amount of {@code INSERT}, {@code UPDATE} or {@code DELETE}
     * SQL statement. This method <strong>must</strong> be invoked in a {@code try} ... {@code
     * finally} block as below:
     *
     * <blockquote><pre>
     * boolean success = false;
     * transactionBegin();
     * try {
     *     // Do some operation here...
     *     success = true;
     * } finally {
     *     transactionEnd(success);
     * }
     * </pre></blockquote>
     *
     * @throws SQLException If the operation failed.
     */
    final void transactionBegin() throws SQLException {
        transactionLock.lock();
        try {
            if (transactionLock.getHoldCount() == 1) {
                synchronized (this) {
                    if (connection != null) {
                        connection.setAutoCommit(false);
                    }
                }
            }
        } catch (SQLException exception) {
            transactionLock.unlock();
            throw exception;
        } catch (RuntimeException exception) {
            transactionLock.unlock();
            throw exception;
        } catch (Error exception) {
            transactionLock.unlock();
            throw exception;
        } catch (Throwable exception) {
            transactionLock.unlock();
            throw new UndeclaredThrowableException(exception);
        }
    }

    /**
     * Invoked after the {@code INSERT}, {@code UPDATE} or {@code DELETE}
     * SQL statement finished.
     *
     * @param  success {@code true} if the operation succeed and should be commited,
     *         or {@code false} if we should rollback.
     * @throws SQLException If the commit or the rollback failed.
     */
    final void transactionEnd(final boolean success) throws SQLException {
        ensureOngoingTransaction();
        try {
            if (transactionLock.getHoldCount() == 1) {
                synchronized (this) {
                    if (connection != null) {
                        if (success) {
                            connection.commit();
                        } else {
                            connection.rollback();
                        }
                        connection.setAutoCommit(true);
                    }
                }
            }
        } finally {
            transactionLock.unlock();
        }
    }

    /**
     * Ensures that the current thread is allowed to performs a transaction.
     *
     * @todo Use {@link java.sql.SQLNonTransientException} when we will be allowed to target Java 6.
     */
    final void ensureOngoingTransaction() throws SQLException {
        if (!transactionLock.isHeldByCurrentThread()) {
            throw new SQLException("La transaction n'a pas commencé dans ce thread."); // TODO: localize
        }
    }

    /**
     * Clears the cache. This method should be invoked when the database content changed.
     *
     * @throws CatalogException if a logical error occured.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized void flush() throws CatalogException, SQLException {
        for (final Table table : tables.values()) {
            synchronized (table) {
                if (table.getStatement((QueryType) null) != null) {
                    throw new AssertionError(table); // Should never occurs
                }
                table.flush();
            }
        }
    }

    /**
     * Closes the connection to the database. If the connection was already closed, then this
     * method does nothing.
     *
     * @throws SQLException if an error occured while closing the connection.
     */
    public synchronized void close() throws SQLException {
        // We should not have a deadlock here since 'getRemote' and 'getProperty' do not
        // synchronize on 'this'. We use Thread.holdLocks(...) assertions for checking that.
        synchronized (remotes) {
            remotes.clear();
        }
        /*
         * Closes connections. There is no Table.close() method because the user never know
         * if a table is shared by an other user.  However Table.getStatement(null) has the
         * side effect of closing the statement and canceling the timer.
         */
        SQLException exception = null;
        synchronized (tables) {
            if (finalizer != null) {
                Runtime.getRuntime().removeShutdownHook(finalizer);
                finalizer = null;
            }
            for (final Iterator<Table> it=tables.values().iterator(); it.hasNext();) {
                final Table table = it.next();
                synchronized (table) {
                    try {
                        if (table.getStatement((QueryType) null) != null) {
                            throw new AssertionError(table); // Should never occurs
                        }
                    } catch (SQLException e) {
                        if (exception == null) {
                            exception = e;
                        } else {
                            exception.setNextException(e);
                        }
                    } catch (CatalogException e) {
                        final SQLException warning = new SQLException(e.getLocalizedMessage());
                        warning.initCause(e); // TODO: put in constructor when we will be allowed to compile for Java 6.
                        if (exception == null) {
                            exception = warning;
                        } else {
                            exception.setNextException(warning);
                        }
                    }
                    table.flush();
                }
                it.remove();
            }
            tables.clear(); // Paranoiac safety.
        }
        /*
         * Shutdown the Derby database engine.
         */
        if (connection != null) {
            connection.close();
            connection = null;
            Element.LOGGER.info("Database connection closed.");
        }
        String database = getProperty(ConfigurationKey.DATABASE);
        if (database != null && database.startsWith("jdbc:derby:")) {
            final int param = database.indexOf(';');
            if (param >= 0) {
                database = database.substring(0, param);
            }
            database += ";shutdown=true";
            try {
                DriverManager.getConnection(database);
            } catch (SQLException e) {
                // This is the expected exception according Derby documentation.
            }
        }
        /*
         * Saves properties if they were modified.
         */
        synchronized (properties) {
            if (modified) {
                modified = false;
                final File file = getConfigurationFile(true);
                if (file != null) try {
                    final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    properties.storeToXML(out, "PostGrid configuration", "UTF-8");
                    out.close();
                } catch (IOException e) {
                    final SQLWarning warning = new SQLWarning(e.getLocalizedMessage());
                    warning.initCause(e); // TODO: put in constructor when we will be allowed to compile for Java 6.
                    if (exception == null) {
                        exception = warning;
                    } else {
                        exception.setNextException(warning);
                    }
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Invoked when this object is no longer referenced in the Java Virtual Machine.
     * The default implementation invokes {@link #close}.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Appellé automatiquement lors de l'arrêt de la machine virtuelle. Cette classe ferme
     * les connections à la base de données si l'utilisateur ne l'a pas fait lui-même.
     */
    private final class Finalizer extends Thread {
        public Finalizer() {
            super("Database.Finalizer");
        }

        @Override
        public void run() {
            synchronized (tables) {
                finalizer = null;
                try {
                    close();
                } catch (Exception ignore) {
                    /*
                     * Rien que nous ne puissions faire, puisque la machine virtuelle est en train
                     * de s'arrêter. Les journaux (java.util.logging) ne sont déjà plus disponibles.
                     */
                }
            }
        }
    }

    /**
     * Tries the connection to the database.
     *
     * @param args Command-line arguments.
     * @throws SQLException If an error occured while connecting to the database.
     * @throws IOException If an error occured while reading the configuration file.
     */
    public static void main(String[] args) throws SQLException, IOException {
        final Database database = new Database();
        final DatabaseMetaData metadata = database.getConnection().getMetaData();
        final OutputStreamWriter writer = new OutputStreamWriter(System.out);
        final TableWriter table = new TableWriter(writer, 1);
        table.write("Database:\t");
        table.write(metadata.getDatabaseProductName());
        table.write(' ');
        table.write(metadata.getDatabaseProductVersion());
        table.nextLine();
        table.write("JDBC:\t");
        table.write(metadata.getDriverVersion());
        table.nextLine();
        table.write("JAI:\t");
        String version;
        try {
            version = String.valueOf(Class.forName("javax.media.jai.JAI")
                    .getMethod("getBuildVersion", (Class[]) null).invoke(null, (Object[]) null));
        } catch (Exception e) {
            version = e.toString();
        }
        table.write(version);
        table.flush();
        writer.close();
        database.close();
    }
}
