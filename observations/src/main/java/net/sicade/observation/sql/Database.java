/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

// Bases de données
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

// Entrés/sorties (incluant RMI)
import java.net.URL;
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

// Utilitaires
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Properties;
import java.util.NoSuchElementException;
import java.lang.reflect.Constructor;

// Geotools
import org.geotools.resources.JDBC;
import org.geotools.resources.Utilities;

// Sicade
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;


/**
 * Connexion vers une base de données d'observations via JDBC (<cite>Java Database Connectivity</cite>).
 * Les requêtes SQL sont sauvegardées dans un fichier de configuration {@code "DatabaseQueries.xml"}
 * placé dans le répertoire {@code "Application Data\Sicade"} (sous Windows) ou {@code ".Sicade"} (sous Unix)
 * de l'utilisateur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Database {
    /**
     * Paramètres de connexions à une {@linkplain Database base de données d'observations}. En plus
     * des informations de connexion, cette interface peut fournir un URL optionel vers un fichier
     * de configuration qui contiendra les requêtes URL à utiliser pour interroger la base de données.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public interface Source extends DataSource {
        /**
         * Retourne l'URL vers le fichier de configuration, or {@code null} s'il n'y en a pas.
         */
        URL getConfiguration();
    }

    /**
     * Le sous-répertoire pour enregistrer la configuration.
     */
    private static final String CONFIG_DIRECTORY = "Sicade";

    /**
     * Le nom du fichier de configuration.
     */
    private static final String CONFIG_FILENAME = "DatabaseQueries.xml";

    /**
     * Le pilote de la base de données. Utilisé seulement si aucun {@link DataSource} n'a été
     * spécifié au constructeur. Exemple: {@code org.postgresql.Driver}.
     */
    public static final ConfigurationKey DRIVER = new ConfigurationKey("Driver", null);

    /**
     * L'URL vers la base de données. Utilisé seulement si aucun {@link DataSource} n'a été
     * spécifié au constructeur. Exemple: {@code jdbc:postgresql://monserveur.com/mabase}.
     */
    public static final ConfigurationKey DATABASE = new ConfigurationKey("Database", null);

    /**
     * L'utilisateur se connectant à la {@linkplain #DATABASE base de données}.
     * Utilisé seulement si aucun {@link DataSource} n'a été spécifié au constructeur.
     */
    public static final ConfigurationKey USER = new ConfigurationKey("User", null);

    /**
     * Le mot de passe de l'{@linkplain #USER utilisateur}. Utilisé seulement si aucun
     * {@link DataSource} n'a été spécifié au constructeur.
     */
    public static final ConfigurationKey PASSWORD = new ConfigurationKey("Password", null);

    /**
     * Clé désignant le serveur RMI distant (<cite>Remote Method Invocation</cite>). La valeur par
     * défaut est {@code null}, ce qui signifie que les images devront être rapatriées par FTP et
     * traitées localement plutôt que de laisser un serveur distant n'envoyer que les résultats.
     */
    public static final ConfigurationKey REMOTE_SERVER = new ConfigurationKey("RemoteServer", null);

    /**
     * Clé désignant le fuseau horaire de dates comprises dans la base de données.
     * Ce fuseau horaire peut être configurée par l'utilisateur. Si aucun fuseau
     * n'est spécifié, alors le fuseau horaire local est utilisé.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey TIMEZONE = new ConfigurationKey("TimeZone", "UTC");

    /**
     * {@code false} si les RMI devraient être désactivés. Ce drapeau est définie à partir de
     * la ligne de commande avec l'option {@code -Drmi.enabled=false}. Il est utile de désactiver
     * les RMI pendant les déboguages si l'on souhaite faire du pas-à-pas dans le code qui aurait
     * normalement été exécuté sur le serveur.
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
     * Source de données.
     */
    private final DataSource source;

    /**
     * Connexion vers la base de données.
     */
    private Connection connection;

    /**
     * Ensemble des tables qui ont été créées.
     */
    private final Map<Class<? extends Table>, Table> tables = new HashMap<Class<? extends Table>, Table>();

    /**
     * Ensemble des connections RMI déjà obtenues avec {@link #getRemote}.
     */
    private final Map<String, Remote> remotes = new HashMap<String, Remote>();

    /**
     * Fuseau horaire à utiliser pour lire ou écrire des dates dans la base de données.
     * Ce fuseau horaire peut être spécifié par la propriété {@link #TIMEZONE}, et sera
     * utilisé pour construire le calendrier retourné par {@link Table#getCalendar}.
     */
    final TimeZone timezone;

    /**
     * Propriétés à utiliser pour extraire les valeurs du fichier de configuration.
     */
    private final Properties properties;

    /**
     * Indique si les propriétés ont été modifiées.
     */
    private boolean modified;

    /**
     * Appellé automatiquement lors de l'arrêt de la machine virtuelle. Ferme les
     * connections à la base de données si l'utilisateur ne l'avait pas fait lui-même.
     */
    private Thread finalizer;

    /**
     * Prépare une connection vers une base de données en n'utilisant que les informations trouvées
     * dans le fichier de configuration.
     *
     * @throws IOException si le fichier de configuration existe mais n'a pas pu être ouvert.
     */
    public Database() throws IOException {
        this(null);
    }

    /**
     * Prépare une connection vers une base de données en utilisant la source spécifiée.
     *
     * @param  source Source de données.
     * @throws IOException si le fichier de configuration existe mais n'a pas pu être ouvert.
     */
    public Database(final DataSource source) throws IOException {
        /*
         * Procède d'abord à la lecture du fichier de configuration,  afin de permettre
         * à la méthode 'getProperty' de fonctionner. Cette dernière sera utilisée dans
         * les lignes suivantes, et risque aussi d'être surchargée.
         */
        properties = new Properties();
        this.source = source;
        if (source instanceof Source) {
            final URL url = ((Source) source).getConfiguration();
            if (url != null) {
                final InputStream in = new BufferedInputStream(url.openStream());
                properties.loadFromXML(in);
                in.close();
            }
        }
        /*
         * Ecrase la configuration récupérée sur le réseau par la configuration spécifiée
         * explicitement par l'utilisateur, s'il l'a définie.
         */
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
     * Retourne la connexion à la base de données. Si une {@linkplain DataSource source de données}
     * a été spécifié au constructeur, elle sera utilisée mais en écrasant éventuellement le nom de
     * l'utilisateur et le mot de passe par ceux qui ont été spécifiés dans le fichier de configuration.
     * Si aucune source de donnée n'a été spécifié, alors on utilisera les propriétés définies pas
     * {@link #DATABASE} et {@link #DRIVER}.
     *
     * @return La connexion à la base de données.
     * @throws SQLException si la connexion n'a pas pu être établie.
     */
    protected synchronized Connection getConnection() throws SQLException {
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
                if (connection != null) {
                    Element.LOGGER.info("Connecté à la base de données "+connection.getMetaData().getURL());
                }
            }
        }
        return connection;
    }

    /**
     * Retourne le fuseau horaire des dates exprimées dans cette base de données. Cette
     * information peut être spécifiée par la propriétée {@link #TIMEZONE} et est utilisée
     * pour convertir des dates du fuseau horaire de la base de données vers le fuseau UTC.
     *
     * @see Table#getCalendar
     */
    public final TimeZone getTimeZone() {
        return (TimeZone) timezone.clone();
    }

    /**
     * Retourne la langue dans laquelle formatter les messages.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Retourne une des propriétée de la base de données. La clé {@code name}
     * est habituellement une constante comme {@link #TIMEZONE}. Cette méthode
     * retourne {@code null} si la propriété demandée n'est pas définie.
     */
    public String getProperty(final ConfigurationKey key) {
        // Pas besoin de synchronizer; 'Properties' l'est déjà.
        String value = properties.getProperty(key.getName(), key.getDefaultValue());
        if (value == null) {
            if (key.equals(TIMEZONE)) {
                return timezone.getID();
            }
        }
        return value;
    }

    /**
     * Retourne la valeur de la propriété spécifiée, ou de {@code fallback} si aucune propriété
     * n'est définie pour {@code key}.
     */
    final String getProperty(final ConfigurationKey key, final ConfigurationKey fallback) {
        final String candidate = getProperty(key);
        return (candidate != null) ? candidate : getProperty(fallback);
    }

    /**
     * Affecte une nouvelle valeur sous la clé spécifiée. Cette valeur sera sauvegardée dans
     * le fichier de configuration qui se trouve dans le répertoire locale de l'utilisateur.
     *
     * @param key   La clé.
     * @param value Nouvelle valeur, ou {@code null} pour rétablir la propriété à sa valeur par défaut.
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
     * Retourne une table du type spécifié. Cette méthode peut retourner une instance d'une table
     * déjà existante si elle répond aux conditions suivantes:
     * <p>
     * <ul>
     *   <li>Une instance de la table demandée avait déjà été créée précédement.</li>
     *   <li>La table implémente l'interface {@link Shareable}.</li>
     *   <li>La table n'a pas été fermée.</li>
     * </ul>
     * <p>
     * Si les conditions ci-dessous ne sont pas remplies, alors cette méthode créera une nouvelle
     * instance en appellant la méthode <code>{@linkplain #createTable createTable}(type)</code>,
     * et le résultat sera éventuellement conservée dans une cache interne pour les appels ultérieurs.
     *
     * @param  type Le type de la table (par exemple <code>{@linkplain StationTable}.class</code>).
     * @return Une instance d'une table du type spécifié.
     * @throws NoSuchElementException si le type spécifié n'est pas connu.
     */
    public final <T extends Table> T getTable(final Class<T> type) throws NoSuchElementException {
        synchronized (tables) {
            T table = type.cast(tables.get(type));
            if (table == null) {
                table = createTable(type);
                if (table instanceof Shareable) {
                    tables.put(type, table);
                }
            } else {
                assert table instanceof Shareable : table;
            }
            return table;
        }
    }

    /**
     * Retourne une nouvelle instance du type de table spécifié. Cette méthode est appelée
     * automatiquement par <code>{@linkplain #getTable getTable}(type)</code> lorsque cette
     * dernière a déterminée que la création d'une nouvelle instance de la table est nécessaire.
     * L'implémentation par défaut tente d'appeller un constructeur qui attend pour seul argument
     * un objet {@code Database}. Les classes dérivées devraient redéfinir cette méthode si elles
     * veulent prendre en compte un plus grand nombre de tables.
     *
     * @param  type Le type de la table (par exemple <code>{@linkplain StationTable}.class</code>).
     * @return Une nouvelle instance d'une table du type spécifié.
     * @throws NoSuchElementException si le type spécifié n'est pas connu.
     */
    public <T extends Table> T createTable(final Class<T> type) throws NoSuchElementException {
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
     * Enregistre un événement dans le journal signalant qu'une erreur est survenue dans la
     * méthode spécifiée, mais que cette erreur n'empêche pas un fonctionnement à peu près
     * normal.
     */
    private void unexpectedException(final String method, final Exception exception) {
        Utilities.unexpectedException(Element.LOGGER.getName(), Utilities.getShortClassName(this),
                                      method, exception);
    }

    /**
     * Ferme la connexion avec la base de données.
     *
     * @throws SQLException si un problème est survenu lors de la fermeture de la connexion.
     * @throws IOException si la configuration de l'utilisateur n'a pas pu être sauvegardée.
     */
    public void close() throws SQLException, IOException {
        /*
         * Ferme les connections.
         */
        synchronized (tables) {
            if (finalizer != null) {
                Runtime.getRuntime().removeShutdownHook(finalizer);
                finalizer = null;
            }
            for (final Iterator<Table> it=tables.values().iterator(); it.hasNext();) {
                it.next().finalize();
                it.remove();
            }
            tables.clear(); // Paranoiac safety.
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
        /*
         * Enregistre les propriétés.
         */
        synchronized (properties) {
            if (modified) {
                modified = false;
                final File file = getConfigurationFile(true);
                if (file != null) {
                    final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    properties.storeToXML(out, "SQL queries", "ISO-8859-1");
                    out.close();
                } else {
                    // TODO: provide a localized message.
                    throw new FileNotFoundException("Aucun fichier où enregistrer la configuration.");
                }
            }
        }
    }

    /**
     * Libère les ressources utilisées par cette base de données si ce n'était pas déjà fait.
     * Cette méthode est appellée automatiquement par le ramasse-miettes lorsqu'il a détecté
     * que cette base de données n'est plus utilisée. L'implémentation par défaut ne fait
     * qu'appeller {@link #close}.
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
