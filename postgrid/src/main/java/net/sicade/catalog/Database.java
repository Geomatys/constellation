/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.catalog;

// Database
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgis.PGbox3d;
import org.postgis.PGgeometry;
import org.postgresql.PGConnection;

// Input / Output
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;

// Miscenaleous
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Properties;
import java.util.NoSuchElementException;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.GregorianCalendar;

// Geotools
import org.geotools.util.Logging;
import org.geotools.resources.JDBC;
import org.geotools.resources.Utilities;

// Sicade
import net.sicade.coverage.catalog.Element;


/**
 * Connection to an observation database through JDBC (<cite>Java Database Connectivity</cite>).
 * Connection parameters are stored in a {@code "DatabaseQueries.xml"} configuration file stored
 * in the {@code "Application Data\Sicade"} user directory on Windows, or {@code ".Sicade"} user
 * directory on Unix.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Database {
    /**
     * The user directory where to store the configuration file.
     */
    private static final String CONFIG_DIRECTORY = "Sicade";

    /**
     * The file where to store configuration parameters.
     */
    private static final String CONFIG_FILENAME = "DatabaseQueries.xml";

    /**
     * Property key for database driver. Example: {@code org.postgresql.Driver}.
     * Used only if no {@linkplain DataSource data source} has been given to the constructor.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey DRIVER = new ConfigurationKey("Driver", null);

    /**
     * Property key for URL to the database. Example: {@code jdbc:postgresql://myserver.com/mydatabase}.
     * Used only if no {@linkplain DataSource data source} has been given to the constructor.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey DATABASE = new ConfigurationKey("Database", null);

    /**
     * Property key for user name connecting to the {@linkplain #DATABASE database}.
     * Used only if no {@linkplain DataSource data source} has been given to the constructor.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey USER = new ConfigurationKey("User", null);

    /**
     * Property key for {@linkplain #USER user} password.
     * Used only if no {@linkplain DataSource data source} has been given to the constructor.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey PASSWORD = new ConfigurationKey("Password", null);

    /**
     * Property key for the RMI server (<cite>Remote Method Invocation</cite>). The default value
     * is {@code null}, which means that images will be downloaded by FTP processed locally instead
     * of delagating the work to some distant server.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey REMOTE_SERVER = new ConfigurationKey("RemoteServer", null);

    /**
     * Property key for the timezone. This apply to the date that appears in the database.
     * If no timezone is given, then the local timezone is used.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey TIMEZONE = new ConfigurationKey("TimeZone", null);

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
     * Connection to the database. Will be etablished only when first needed.
     */
    private Connection connection;

    /**
     * {@code true} if the database supports spatial extension, like the {code BBOX3D} type.
     */
    private boolean isSpatialEnabled;

    /**
     * {@code true} if the {@code toString()} method on {@link PreparedStatement} instances
     * returns the SQL query with parameters filled in. This is the case with the PostgreSQL
     * JDBC driver.
     */
    private boolean isStatementFormatted;

    /**
     * The tables created up to date.
     */
    private final Map<Class<? extends Table>, Table> tables = new HashMap<Class<? extends Table>, Table>();

    /**
     * The RMI connections created up to date with the {@link #getRemote} method.
     */
    private final Map<String, Remote> remotes = new HashMap<String, Remote>();

    /**
     * The timezone to use for reading and writing date in the database. This timezone
     * can be set by the {@link #TIMEZONE} property and will be used for creating the
     * calendar returned by {@link Table#getCalendar}.
     */
    private final TimeZone timezone;

    /**
     * The properties read from the {@value #CONFIG_FILENAME} file.
     */
    private final Properties properties;

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
        this(null);
    }

    /**
     * Opens a new connection using the provided data source.
     *
     * @param  source The data source.
     * @throws IOException if an error occured while reading the configuration file.
     */
    public Database(final DataSource source) throws IOException {
        this.source = source;
        /*
         * Procède d'abord à la lecture du fichier de configuration,  afin de permettre
         * à la méthode 'getProperty' de fonctionner. Cette dernière sera utilisée dans
         * les lignes suivantes, et risque aussi d'être surchargée.
         */
        properties = new Properties();
        final File file = getConfigurationFile(false);
        if (file!=null && file.exists()) {
            final InputStream in = new BufferedInputStream(new FileInputStream(file));
            properties.loadFromXML(in);
            in.close();
        }
        final String ID = getProperty(TIMEZONE);
        timezone = (ID!=null) ? TimeZone.getTimeZone(ID) : TimeZone.getDefault();
        /*
         * Prépare un processus qui fermera automatiquement les connections lors de l'arrêt
         * de la machine virtuelle si l'utilisateur n'appelle par close() lui-même.
         */
        finalizer = new Finalizer();
        Runtime.getRuntime().addShutdownHook(finalizer);
    }

    /**
     * Retourne le fichier de configuration de l'utilisateur, ou {@code null} s'il n'a pas
     * été trouvé. Le répertoire {@value #CONFIG_DIRECTORY} ne sera créé que si l'argument
     * {@code create} est {@code true}.
     */
    private static File getConfigurationFile(final boolean create) {
        /*
         * Donne priorité au fichier de configuration dans le répertoire courant, s'il existe.
         */
        File path = new File(CONFIG_FILENAME);
        if (path.isFile()) {
            return path;
        }
        /*
         * Recherche dans le répertoire de configuration de l'utilisateur.
         */
        final String home = System.getProperty("user.home");
        path = new File(home, "Application Data");
        if (path.isDirectory()) {
            path = new File(path, CONFIG_DIRECTORY);
        } else {
            path = new File(home, '.'+CONFIG_DIRECTORY);
        }
        if (!path.exists()) {
            if (!create || !path.mkdir()) {
                return null;
            }
        }
        return new File(path, CONFIG_FILENAME);
    }

    /**
     * Returns the connection to the database. If a {@linkplain DataSource data source} has
     * been specified at construction time, it will be used but the configuration file will
     * have precedence for the {@linkplain #USER user} and {@linkplain #PASSWORD password}
     * properties. If no data source was specified at construction time, then the
     * {@link #DATABASE} et {@link #DRIVER} properties will be queried.
     *
     * @return The connection to the database.
     * @throws SQLException if the connection can not be created.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null) {
            final String user     = getProperty(USER);
            final String password = getProperty(PASSWORD);
            if (source != null) {
                if (user!=null && password!=null) {
                    connection = source.getConnection(user, password);
                } else {
                    connection = source.getConnection();
                }
            } else {
                final String database = getProperty(DATABASE);
                if (database != null) {
                    JDBC.loadDriver(getProperty(DRIVER));
                    connection = DriverManager.getConnection(database, user, password);
                } else {
                    connection = null;
                }
            }
            if (connection != null) {
                Element.LOGGER.info("Connecté à la base de données " + connection.getMetaData().getURL());
            }
            /*
             * Dans le cas d'une connection sur une base de type PostgreSQL, le type de données 
             * "box3d" doit être spécifié, afin de permettre son utilisation dans les tables.
             *
             * TODO: Following code is PostgreSQL specific. Need to make it more generic,
             *       or at the very least optional.
             */
            isSpatialEnabled = false;
            isStatementFormatted = false;
            if (connection instanceof PGConnection) {
                final PGConnection pgc = (PGConnection) connection;
                pgc.addDataType("box3d",    PGbox3d.class);
                pgc.addDataType("geometry", PGgeometry.class);
                isSpatialEnabled = true;
                isStatementFormatted = true;
            }
        }
        return connection;
    }

    /**
     * Returns the locale for international message formatting.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns the timezone in which the dates in the database are expressed. This information
     * can be specified through the {@link #TIMEZONE} property. It is used in order to convert
     * the dates from the database timezone to UTC.
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
     * Returns a property. The key is usually a constant like {@link #TIMEZONE}.
     *
     * @param key The key for the property to fetch.
     * @return The property value, or {@code null} if none.
     */
    public String getProperty(final ConfigurationKey key) {
        // No need to synchronize since 'Properties' is already synchronized.
        String value = properties.getProperty(key.getName(), key.getDefaultValue());
        if (value == null) {
            if (key.equals(TIMEZONE)) {
                return ((timezone != null) ? timezone : TimeZone.getDefault()).getID();
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
                if (!value.equals(properties.setProperty(key.getName(), value))) {
                    modified = true;
                }
            } else {
                if (properties.remove(key.getName()) != null) {
                    modified = true;
                }
            }
        }
    }

    /**
     * Returns a table of the specified type.
     *
     * @param  type The table class (e.g. <code>{@linkplain StationTable}.class</code>).
     * @return An instance of a table of the specified type.
     * @throws NoSuchElementException if the specified type is unknown to this database.
     */
    public final <T extends Table> T getTable(final Class<T> type) throws NoSuchElementException {
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
     * @throws NoSuchElementException si le type spécifié n'est pas connu.
     */
    private <T extends Table> T createTable(final Class<T> type) throws NoSuchElementException {
        try {
            final Constructor<T> c = type.getConstructor(Database.class);
            return c.newInstance(this);
        } catch (Exception exception) {
            /*
             * Attraper toutes les exceptions n'est pas recommandé,
             * mais il y en a un bon paquet dans le code ci-dessus.
             */
            final NoSuchElementException e = new NoSuchElementException(Utilities.getShortName(type));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Obtient un objet distant du nom spécifié. Si l'objet {@code name} avait déjà été demandée
     * lors d'un précédent appel, le même objet sera retourné. Sinon, un nouvel objet sera obtenu
     * et retourné.
     * <p>
     * Si l'objet distant du nom spécifié n'a pas pu être obtenu par ce qu'aucun objet de ce nom
     * n'est défini ou parce que le serveur a refusé la connexion, alors cette methode retourne
     * {@code null}. Si l'objet n'a pas pu être obtenu pour une autre raison, alors une exception
     * est lancée.
     *
     * @param  name Le nom de l'objet désiré (sans le nom du serveur). Typiquement une constante
     *         telle que {@link net.sicade.observation.coverage.rmi.DataConnectionFactory#REGISTRY_NAME
     *         REGISTRY_NAME}.
     * @return Une instance partagée (entre les différents appels de cette méthode) de l'objet distant,
     *         ou {@code null} si aucun objet du nom de spécifié n'est disponible.
     * @throws RemoteException si la connexion au serveur a échouée.
     */
    public Remote getRemote(final String name) throws RemoteException {
        if (!RMI_ENABLED) {
            return null;
        }
        synchronized (remotes) {
            Remote candidate = remotes.get(name);
            if (candidate==null && !remotes.containsKey(name)) {
                final String server = getProperty(REMOTE_SERVER);
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
                         * interfaces du paquet net.sicade.observation.coverage.rmi.
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
     * Returns {@code true} if the database support spatial extensions. For example
     * PostGIS is an optional spatial extension to PostgreSQL. Those extensions define
     * new types like {@code BOX3D}.
     */
    public boolean isSpatialEnabled() throws SQLException {
        if (connection == null) {
            // Force the computation of 'isSpatialEnabled' flag.
            getConnection();
        }
        return isSpatialEnabled;
    }

    /**
     * Returns {@code true} if the {@code toString()} method on {@link PreparedStatement}
     * instances returns the SQL query with parameters filled in. This is the case with the
     * PostgreSQL JDBC driver.
     */
    final boolean isStatementFormatted() {
        if (connection == null) {
            throw new IllegalStateException();
        }
        return isStatementFormatted;
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
     * Closes the connection to the database. If the connection was already closed, then this
     * method do nothing.
     *
     * @throws SQLException if an error occured while closing the connection.
     * @throws IOException if some {@linkplain #setProperty properties changed} but can't be saved.
     */
    public void close() throws SQLException, IOException {
        /*
         * Closes connections. There is no Table.close() method because the user never know
         * if a table is shared by an other user.  However Table.getStatement(null) has the
         * side effect of closing the statement and canceling the timer.
         */
        synchronized (tables) {
            if (finalizer != null) {
                Runtime.getRuntime().removeShutdownHook(finalizer);
                finalizer = null;
            }
            for (final Iterator<Table> it=tables.values().iterator(); it.hasNext();) {
                final Table table = it.next();
                synchronized (table) {
                    if (table.getStatement((String) null) != null) {
                        throw new AssertionError(table); // Should never occurs
                    }
                    table.clearCache();
                }
                it.remove();
            }
            tables.clear(); // Paranoiac safety.
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
        /*
         * Saves properties if they were modified.
         */
        synchronized (properties) {
            if (modified) {
                modified = false;
                final File file = getConfigurationFile(true);
                if (file != null) {
                    final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    properties.storeToXML(out, "PostGrid configuration", "UTF-8");
                    out.close();
                } else {
                    // TODO: provide a localized message.
                    throw new FileNotFoundException("Aucun fichier où enregistrer la configuration.");
                }
            }
        }
    }

    /**
     * Invoked when this object is no longer referenced in the Java Virtual Machine.
     * The default implementation just invokes {@link #close}.
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
}
