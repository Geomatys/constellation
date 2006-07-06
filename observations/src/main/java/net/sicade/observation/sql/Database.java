/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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

// Bases de donn�es
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

// Entr�s/sorties (incluant RMI)
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
 * Connexion vers une base de donn�es d'observations via JDBC (<cite>Java Database Connectivity</cite>).
 * Les requ�tes SQL sont sauvegard�es dans un fichier de configuration {@code "DatabaseQueries.xml"}
 * plac� dans le r�pertoire {@code "Application Data\Sicade"} (sous Windows) ou {@code ".Sicade"} (sous Unix)
 * de l'utilisateur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Database {
    /**
     * Param�tres de connexions � une {@linkplain Database base de donn�es d'observations}. En plus
     * des informations de connexion, cette interface peut fournir un URL optionel vers un fichier
     * de configuration qui contiendra les requ�tes URL � utiliser pour interroger la base de donn�es.
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
     * Le sous-r�pertoire pour enregistrer la configuration.
     */
    private static final String CONFIG_DIRECTORY = "Sicade";

    /**
     * Le nom du fichier de configuration.
     */
    private static final String CONFIG_FILENAME = "DatabaseQueries.xml";

    /**
     * Le pilote de la base de donn�es. Utilis� seulement si aucun {@link DataSource} n'a �t�
     * sp�cifi� au constructeur. Exemple: {@code org.postgresql.Driver}.
     */
    public static final ConfigurationKey DRIVER = new ConfigurationKey("Driver", null);

    /**
     * L'URL vers la base de donn�es. Utilis� seulement si aucun {@link DataSource} n'a �t�
     * sp�cifi� au constructeur. Exemple: {@code jdbc:postgresql://monserveur.com/mabase}.
     */
    public static final ConfigurationKey DATABASE = new ConfigurationKey("Database", null);

    /**
     * L'utilisateur se connectant � la {@linkplain #DATABASE base de donn�es}.
     * Utilis� seulement si aucun {@link DataSource} n'a �t� sp�cifi� au constructeur.
     */
    public static final ConfigurationKey USER = new ConfigurationKey("User", null);

    /**
     * Le mot de passe de l'{@linkplain #USER utilisateur}. Utilis� seulement si aucun
     * {@link DataSource} n'a �t� sp�cifi� au constructeur.
     */
    public static final ConfigurationKey PASSWORD = new ConfigurationKey("Password", null);

    /**
     * Cl� d�signant le serveur RMI distant (<cite>Remote Method Invocation</cite>). La valeur par
     * d�faut est {@code null}, ce qui signifie que les images devront �tre rapatri�es par FTP et
     * trait�es localement plut�t que de laisser un serveur distant n'envoyer que les r�sultats.
     */
    public static final ConfigurationKey REMOTE_SERVER = new ConfigurationKey("RemoteServer", null);

    /**
     * Cl� d�signant le fuseau horaire de dates comprises dans la base de donn�es.
     * Ce fuseau horaire peut �tre configur�e par l'utilisateur. Si aucun fuseau
     * n'est sp�cifi�, alors le fuseau horaire local est utilis�.
     *
     * @see #getProperty
     */
    public static final ConfigurationKey TIMEZONE = new ConfigurationKey("TimeZone", "UTC");

    /**
     * {@code false} si les RMI devraient �tre d�sactiv�s. Ce drapeau est d�finie � partir de
     * la ligne de commande avec l'option {@code -Drmi.enabled=false}. Il est utile de d�sactiver
     * les RMI pendant les d�boguages si l'on souhaite faire du pas-�-pas dans le code qui aurait
     * normalement �t� ex�cut� sur le serveur.
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
            Entry.LOGGER.config("RMI d�sactiv�s.");
        }
    }

    /**
     * Source de donn�es.
     */
    private final DataSource source;

    /**
     * Connexion vers la base de donn�es.
     */
    private Connection connection;

    /**
     * Ensemble des tables qui ont �t� cr��es.
     */
    private final Map<Class<? extends Table>, Table> tables = new HashMap<Class<? extends Table>, Table>();

    /**
     * Ensemble des connections RMI d�j� obtenues avec {@link #getRemote}.
     */
    private final Map<String, Remote> remotes = new HashMap<String, Remote>();

    /**
     * Fuseau horaire � utiliser pour lire ou �crire des dates dans la base de donn�es.
     * Ce fuseau horaire peut �tre sp�cifi� par la propri�t� {@link #TIMEZONE}, et sera
     * utilis� pour construire le calendrier retourn� par {@link Table#getCalendar}.
     */
    final TimeZone timezone;

    /**
     * Propri�t�s � utiliser pour extraire les valeurs du fichier de configuration.
     */
    private final Properties properties;

    /**
     * Indique si les propri�t�s ont �t� modifi�es.
     */
    private boolean modified;

    /**
     * Appell� automatiquement lors de l'arr�t de la machine virtuelle. Ferme les
     * connections � la base de donn�es si l'utilisateur ne l'avait pas fait lui-m�me.
     */
    private Thread finalizer;

    /**
     * Pr�pare une connection vers une base de donn�es en n'utilisant que les informations trouv�es
     * dans le fichier de configuration.
     *
     * @throws IOException si le fichier de configuration existe mais n'a pas pu �tre ouvert.
     */
    public Database() throws IOException {
        this(null);
    }

    /**
     * Pr�pare une connection vers une base de donn�es en utilisant la source sp�cifi�e.
     *
     * @param  source Source de donn�es.
     * @throws IOException si le fichier de configuration existe mais n'a pas pu �tre ouvert.
     */
    public Database(final DataSource source) throws IOException {
        /*
         * Proc�de d'abord � la lecture du fichier de configuration,  afin de permettre
         * � la m�thode 'getProperty' de fonctionner. Cette derni�re sera utilis�e dans
         * les lignes suivantes, et risque aussi d'�tre surcharg�e.
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
         * Ecrase la configuration r�cup�r�e sur le r�seau par la configuration sp�cifi�e
         * explicitement par l'utilisateur, s'il l'a d�finie.
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
         * Pr�pare un processus qui fermera automatiquement les connections lors de l'arr�t
         * de la machine virtuelle si l'utilisateur n'appelle par close() lui-m�me.
         */
        finalizer = new Finalizer();
        Runtime.getRuntime().addShutdownHook(finalizer);
    }

    /**
     * Retourne le fichier de configuration de l'utilisateur, ou {@code null} s'il n'a pas
     * �t� trouv�. Le r�pertoire {@value #CONFIG_DIRECTORY} ne sera cr�� que si l'argument
     * {@code create} est {@code true}.
     */
    private static File getConfigurationFile(final boolean create) {
        /*
         * Donne priorit� au fichier de configuration dans le r�pertoire courant, s'il existe.
         */
        File path = new File(CONFIG_FILENAME);
        if (path.isFile()) {
            return path;
        }
        /*
         * Recherche dans le r�pertoire de configuration de l'utilisateur.
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
     * Retourne la connexion � la base de donn�es. Si une {@linkplain DataSource source de donn�es}
     * a �t� sp�cifi� au constructeur, elle sera utilis�e mais en �crasant �ventuellement le nom de
     * l'utilisateur et le mot de passe par ceux qui ont �t� sp�cifi�s dans le fichier de configuration.
     * Si aucune source de donn�e n'a �t� sp�cifi�, alors on utilisera les propri�t�s d�finies pas
     * {@link #DATABASE} et {@link #DRIVER}.
     *
     * @return La connexion � la base de donn�es.
     * @throws SQLException si la connexion n'a pas pu �tre �tablie.
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
                    Element.LOGGER.info("Connect� � la base de donn�es "+connection.getMetaData().getURL());
                }
            }
        }
        return connection;
    }

    /**
     * Retourne le fuseau horaire des dates exprim�es dans cette base de donn�es. Cette
     * information peut �tre sp�cifi�e par la propri�t�e {@link #TIMEZONE} et est utilis�e
     * pour convertir des dates du fuseau horaire de la base de donn�es vers le fuseau UTC.
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
     * Retourne une des propri�t�e de la base de donn�es. La cl� {@code name}
     * est habituellement une constante comme {@link #TIMEZONE}. Cette m�thode
     * retourne {@code null} si la propri�t� demand�e n'est pas d�finie.
     */
    public String getProperty(final ConfigurationKey key) {
        // Pas besoin de synchronizer; 'Properties' l'est d�j�.
        String value = properties.getProperty(key.getName(), key.getDefaultValue());
        if (value == null) {
            if (key.equals(TIMEZONE)) {
                return timezone.getID();
            }
        }
        return value;
    }

    /**
     * Retourne la valeur de la propri�t� sp�cifi�e, ou de {@code fallback} si aucune propri�t�
     * n'est d�finie pour {@code key}.
     */
    final String getProperty(final ConfigurationKey key, final ConfigurationKey fallback) {
        final String candidate = getProperty(key);
        return (candidate != null) ? candidate : getProperty(fallback);
    }

    /**
     * Affecte une nouvelle valeur sous la cl� sp�cifi�e. Cette valeur sera sauvegard�e dans
     * le fichier de configuration qui se trouve dans le r�pertoire locale de l'utilisateur.
     *
     * @param key   La cl�.
     * @param value Nouvelle valeur, ou {@code null} pour r�tablir la propri�t� � sa valeur par d�faut.
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
     * Retourne une table du type sp�cifi�. Cette m�thode peut retourner une instance d'une table
     * d�j� existante si elle r�pond aux conditions suivantes:
     * <p>
     * <ul>
     *   <li>Une instance de la table demand�e avait d�j� �t� cr��e pr�c�dement.</li>
     *   <li>La table impl�mente l'interface {@link Shareable}.</li>
     *   <li>La table n'a pas �t� ferm�e.</li>
     * </ul>
     * <p>
     * Si les conditions ci-dessous ne sont pas remplies, alors cette m�thode cr�era une nouvelle
     * instance en appellant la m�thode <code>{@linkplain #createTable createTable}(type)</code>,
     * et le r�sultat sera �ventuellement conserv�e dans une cache interne pour les appels ult�rieurs.
     *
     * @param  type Le type de la table (par exemple <code>{@linkplain StationTable}.class</code>).
     * @return Une instance d'une table du type sp�cifi�.
     * @throws NoSuchElementException si le type sp�cifi� n'est pas connu.
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
     * Retourne une nouvelle instance du type de table sp�cifi�. Cette m�thode est appel�e
     * automatiquement par <code>{@linkplain #getTable getTable}(type)</code> lorsque cette
     * derni�re a d�termin�e que la cr�ation d'une nouvelle instance de la table est n�cessaire.
     * L'impl�mentation par d�faut tente d'appeller un constructeur qui attend pour seul argument
     * un objet {@code Database}. Les classes d�riv�es devraient red�finir cette m�thode si elles
     * veulent prendre en compte un plus grand nombre de tables.
     *
     * @param  type Le type de la table (par exemple <code>{@linkplain StationTable}.class</code>).
     * @return Une nouvelle instance d'une table du type sp�cifi�.
     * @throws NoSuchElementException si le type sp�cifi� n'est pas connu.
     */
    public <T extends Table> T createTable(final Class<T> type) throws NoSuchElementException {
        try {
            final Constructor<T> c = type.getConstructor(Database.class);
            return c.newInstance(this);
        } catch (Exception exception) {
            /*
             * Attraper toutes les exceptions n'est pas recommand�,
             * mais il y en a un bon paquet dans le code ci-dessus.
             */
            final NoSuchElementException e = new NoSuchElementException(Utilities.getShortName(type));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Obtient un objet distant du nom sp�cifi�. Si l'objet {@code name} avait d�j� �t� demand�e
     * lors d'un pr�c�dent appel, le m�me objet sera retourn�. Sinon, un nouvel objet sera obtenu
     * et retourn�.
     * <p>
     * Si l'objet distant du nom sp�cifi� n'a pas pu �tre obtenu par ce qu'aucun objet de ce nom
     * n'est d�fini ou parce que le serveur a refus� la connexion, alors cette methode retourne
     * {@code null}. Si l'objet n'a pas pu �tre obtenu pour une autre raison, alors une exception
     * est lanc�e.
     *
     * @param  name Le nom de l'objet d�sir� (sans le nom du serveur). Typiquement une constante
     *         telle que {@link net.sicade.observation.coverage.rmi.DataConnectionFactory#REGISTRY_NAME
     *         REGISTRY_NAME}.
     * @return Une instance partag�e (entre les diff�rents appels de cette m�thode) de l'objet distant,
     *         ou {@code null} si aucun objet du nom de sp�cifi� n'est disponible.
     * @throws RemoteException si la connexion au serveur a �chou�e.
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
                         * Ne devrait pas se produire, puisque l'on a construit notre URL nous-m�me
                         * et que l'argument 'name' est habituellement une constante d'une des
                         * interfaces du paquet net.sicade.observation.coverage.rmi.
                         */
                        throw new IllegalArgumentException(name, exception);
                    } catch (ConnectException exception) {
                        /*
                         * La connexion au serveur a �t� refus�e; peut-�tre le serveur n'est pas
                         * en service. Retourne null, ce qui signifie que l'appellant devra se
                         * passer de RMI (par exemple en t�l�chargeant les images par FTP).
                         */
                        unexpectedException("getRemote", exception);
                    } catch (NotBoundException exception) {
                        /*
                         * L'objet demand� n'a pas �t� mis en service. Retourne null, ce qui
                         * signifie que l'appellant devra se passer de RMI (par exemple en
                         * t�l�chargeant les images par FTP).
                         */
                        unexpectedException("getRemote", exception);
                    }
                }
                /*
                 * Sauvegarde la r�f�rence m�me si elle est rest�e nulle afin d'�viter
                 * de recommencer les tentatives de connections � chaque appel.
                 */
                remotes.put(name, candidate);
            }
            return candidate;
        }
    }

    /**
     * Enregistre un �v�nement dans le journal signalant qu'une erreur est survenue dans la
     * m�thode sp�cifi�e, mais que cette erreur n'emp�che pas un fonctionnement � peu pr�s
     * normal.
     */
    private void unexpectedException(final String method, final Exception exception) {
        Utilities.unexpectedException(Element.LOGGER.getName(), Utilities.getShortClassName(this),
                                      method, exception);
    }

    /**
     * Ferme la connexion avec la base de donn�es.
     *
     * @throws SQLException si un probl�me est survenu lors de la fermeture de la connexion.
     * @throws IOException si la configuration de l'utilisateur n'a pas pu �tre sauvegard�e.
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
         * Enregistre les propri�t�s.
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
                    throw new FileNotFoundException("Aucun fichier o� enregistrer la configuration.");
                }
            }
        }
    }

    /**
     * Lib�re les ressources utilis�es par cette base de donn�es si ce n'�tait pas d�j� fait.
     * Cette m�thode est appell�e automatiquement par le ramasse-miettes lorsqu'il a d�tect�
     * que cette base de donn�es n'est plus utilis�e. L'impl�mentation par d�faut ne fait
     * qu'appeller {@link #close}.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Appell� automatiquement lors de l'arr�t de la machine virtuelle. Cette classe ferme
     * les connections � la base de donn�es si l'utilisateur ne l'a pas fait lui-m�me.
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
                     * de s'arr�ter. Les journaux (java.util.logging) ne sont d�j� plus disponibles.
                     */
                }
            }
        }
    }
}
