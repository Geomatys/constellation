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

// Base de données
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Utilitaires
import java.util.Timer;
import java.util.TimerTask;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import net.sicade.observation.SpatialConfigurationKey;

// Geotools
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;

// Sicade
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;

/**
 * Classe de base des connections vers une table de la base de données. Chaque instance de
 * {@code Table} contient une et une seule instance de {@link PreparedStatement}, qui peut
 * changer (c'est-à-dire être fermée et une nouvelle instance recréée) plusieurs fois.
 * <p>
 * Les classes dérivées devraient construire leur requête SQL seulement la première fois où elle sera
 * nécessaire, typiquement à l'intérieur d'une méthode {@code getEntry(...)} public et synchronisée.
 * La requête est construite en appelant une des méthodes {@link #getStatement(String) getStatement(...)}.
 * Notez que la requête ainsi construite peut être détruite à tout moment une fois sortit du bloc
 * synchronisé, de sorte que les classes dérivées ne devraient jamais compter sur sa pérénité.
 * L'exemple suivant donne une implémentation typique:
 *
 * <blockquote><pre>
 * public <b>synchronized</b> {@linkplain Element} getEntry(String name) throws SQLException {
 *     PreparedStatement statement = {@linkplain #getStatement(ConfigurationKey) getStatement}(SELECT);
 *     statement.{@linkplain PreparedStatement#setString setString}(1, name);
 *     ResultSet r = statement.{@linkplain PreparedStatement#executeQuery() executeQuery}();
 *     // <i>Traiter ici les informations de la base de données...</i>
 *     r.{@linkplain java.sql.ResultSet#close close}();
 *     // <i>Ne <b>pas</b> fermer </i>statement<i>, car il sera réutilisé autant que possible.</i>
 * }
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Table {
    /**
     * Utilisé pour fermer la requête {@link #statement} après un certain délai. On supposera
     * qu'après quelques minutes d'inutilisation, les données désirées se trouvent dans une
     * quelconque cache gérée par les sous-classes.
     */
    private static final Timer TIMER = new Timer(true);

    /**
     * Le délai d'inactivité (en millisecondes) après lequel la requête {@link #statement}
     * sera fermée. La valeur actuelle est de 15 minutes.
     */
    private static final long DELAY = 15 * (60*1000L);

    /**
     * Ensemble d'indices à donner à Geotools concernant les fabriques à utiliser.
     * Ces indices permettent d'ajuster certains détails de l'implémentation des
     * fabriques.
     */
    protected static final Hints FACTORY_HINTS = null;

    /**
     * La base de données d'où provient cette table.
     */
    protected final Database database;

    /**
     * Calendrier pouvant être utilisé pour lire ou écrire des dates dans la base de données.
     * Ce calendrier utilisera le fuseau horaire spécifié par la propriété {@link #TIMEZONE},
     * qui devrait désigner le fuseau horaire des dates dans la base de données.
     * <p>
     * Nous construisons une instance de {@link GregorianCalendar} pour chaque table (plutôt
     * qu'une instance partagée par tous) afin d'éviter des problèmes en cas d'utilisation des
     * tables dans un environnement multi-thread.
     *
     * @see #getCalendar
     */
    private transient Calendar calendar;

    /**
     * Requête SQL à utiliser pour obtenir les données. Peut être {@code null} si la requête
     * a été fermée.
     */
    private PreparedStatement statement;

    /**
     * La requête sous forme textuelle qui a servit à construire {@link #statement}, ou {@code null}.
     */
    private String query;

    /**
     * La dernière fois où {@link #statement} a été utilisée.
     */
    private long lastAccess;

    /**
     * Tâche ayant la charge de fermer la requête {@link #statement} après une certaine
     * période d'inactivité.
     */
    private TimerTask disposer;

    /**
     * {@code true} si {@link #statement} a besoin d'être {@linkplain #configure configuré}.
     */
    private boolean changed;

    /**
     * Tâche ayant la charge de fermer la requête {@link #statement} après une certaine
     * période d'inactivité.
     */
    private final class Disposer extends TimerTask {
        public void run() {
            synchronized (Table.this) {
                if (System.currentTimeMillis() - lastAccess >= DELAY) try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (Exception exception) {
                    logWarning("close", exception);
                } finally {
                    statement = null;
                    disposer  = null;
                    query     = null;
                    cancel();
                }
            }
        }
    }

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database Connexion vers la base de données d'observations.
     */
    protected Table(final Database database) {
        this.database = database;
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Retourne la base de données d'où provient cette table.
     */
    public final Database getDatabase() {
        return database;
    }

    /**
     * Retourne une propriété pour la clé spécifiée. Cette méthode extrait la propriété de la
     * {@linkplain #database base de données} si elle est non-nulle, ou retourne une propriété
     * par défaut sinon.
     */
    protected final String getProperty(final ConfigurationKey key) {
        if (key == null) {
            return null;
        }
        if (database != null) {
            final String value = database.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        if (key instanceof SpatialConfigurationKey) {
            return ((SpatialConfigurationKey) key).getSpatialValue();
        }
        return key.getDefaultValue();
    }

    /**
     * Retourne une version pré-compilée de la requête désignée par la clé spécifiée. Cette méthode
     * est un racourci pour <code>{@linkplain #getStatement(String) getStatement}({@linkplain
     * #getProperty getProperty}(key))</code>.
     *
     * @param  query Clé désignant la requête SQL à soumettre à la base de données, ou
     *         {@code null} pour fermer la requête courante sans en construire de nouvelle.
     * @return La requête pré-compilée, ou {@code null} si {@code query} était nul.
     * @throws SQLException si la requête SQL n'a pas pu être construite.
     */
    protected final PreparedStatement getStatement(final ConfigurationKey query) throws SQLException {
        return getStatement(getProperty(query));
    }

    /**
     * Retourne une version pré-compilée de la requête spécifiée. Si le texte {@code query} décrit la
     * même requête que celle qui a été construite la dernière fois que cette méthode a été appelée,
     * et si cette requête pré-compilée n'a pas encore été fermée, alors cette méthode effectue les
     * opérations suivantes:
     * <p>
     * <ul>
     *   <li>Si cette table a {@linkplain #fireStateChanged changé d'état} depuis le dernier appel
     *       de cette méthode, alors {@linkplain #configure configure} la requête.</li>
     * </ul>
     * <p>
     * Dans tous les autres cas (la requête spécifiée n'est pas la même que la dernière fois, où la
     * précédente requête pré-compilée a été fermée), alors cette méthode effectue les opérations
     * suivantes:
     * <p>
     * <ul>
     *   <li>La requête courante (s'il y en a une) sera fermée.</li>
     *   <li>Une nouvelle requête est pré-compilée et {@linkplain #configure configurée}.</li>
     * </ul>
     *
     * @param  query La requête SQL à soumettre à la base de données, ou {@code null}
     *         pour fermer la requête courante sans en construire de nouvelle.
     * @return La requête pré-compilée, ou {@code null} si {@code query} était nul.
     * @throws SQLException si la requête SQL n'a pas pu être construite.
     */
    protected final PreparedStatement getStatement(final String query) throws SQLException {
        assert Thread.holdsLock(this);
        if (!Utilities.equals(this.query, query)) {
            if (statement != null) {
                try {
                    statement.close();
                } finally {
                    statement  = null;
                    this.query = null;
                }
            }
            if (query == null) {
                if (disposer != null) {
                    disposer.cancel();
                    disposer = null;
                }
            } else {
                statement  = database.getConnection().prepareStatement(query);
                changed    = true;
                if (disposer == null) {
                    disposer = new Disposer();
                    TIMER.schedule(disposer, DELAY, DELAY);
                }
            }
            this.query = query;
        }
        if (statement != null) {
            if (changed) {
                configure(getQueryType(), statement);
            }
            lastAccess = System.currentTimeMillis();
        }
        return statement;
    }

    /**
     * Retourne le type de requête actuellement en usage, ou {@code null} si aucun. Cette
     * information peut être utilisée par les méthodes {@link #configure configure} (lorsque
     * redéfinie dans des classes dérivées) afin d'adapter la configuration en fonction du
     * type de requête à exécuter.
     */
    QueryType getQueryType() {
        return null;
    }

    /**
     * Appelée automatiquement lorsqu'une méthode {@link #getStatement(String) getStatement(...)}
     * a déterminé que c'était nécessaire. Cette appel se produit lorsqu'une nouvelle requête vient
     * d'être pré-compilée, ou que cette table a {@linkplain #fireStateChanged changé d'état}. Les
     * classes dérivées devraient redéfinir cette méthode si des paramètres de la requête ont besoin
     * d'être définie en fonction de l'état de la table. Les implémentations des classes dérivées
     * devrait commencer par appeler {@code super.configure(statement)}.
     *
     * @param  type Type de requête à configurer.
     * @param  statement La requête à configurer (jamais {@code null}).
     * @throws SQLException si la configuration de la requête a échoué.
     */
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        assert Thread.holdsLock(this);
        changed = false;
    }

    /**
     * Retourne la chaîne spécifiée après avoir préfixé chaque caractères génériques par le
     * caractère d'échappement. Cette méthode peut être utilisée pour effectuer une recherche
     * exacte dans une instruction {@code LIKE}. Dans le cas particulier ou l'argument {@code text}
     * est {@code null}, alors cette méthode retourne le caractère d'échappement {@code "%"}, qui
     * accepte toutes les chaînes de caractères.
     *
     * @param  text Le texte qui peut contenir les caractères génériques {@code '%'} et {@code '_'}.
     * @return Le texte avec un caractère d'échappement devant chaque caractères génériques.
     * @throws SQLException si une erreur est survenue lors de l'accès à la base de données.
     */
    protected final String escapeSearch(final String text) throws SQLException {
        if (text == null) {
            return "%";
        }
        StringBuilder buffer = null;
        String escape = "\\";
        int lower = 0;
        final int length = text.length();
        for (int i=0; i<length; i++) {
            final char c = text.charAt(i);
            if (c=='_' || c=='%') {
                if (buffer == null) {
                    buffer = new StringBuilder(length + 5);
                    if (database != null) {
                        escape = database.getConnection().getMetaData().getSearchStringEscape();
                    }
                }
                buffer.append(text.substring(lower, i));
                buffer.append(escape);
                lower = i;
            }
        }
        if (buffer == null) {
            return text;
        }
        buffer.append(text.substring(lower));
        return buffer.toString();
    }

    /**
     * Extrait la partie {@code "SELECT ... FROM ..."} de la requête spécifiée. Cette méthode
     * retourne la chaîne {@code query} à partir du début jusqu'au dernier caractère précédant
     * la première clause {@code "WHERE"}. La clause {@code "WHERE"} et tout ce qui suit jusqu'à
     * la clause {@code "GROUP"} ou {@code "ORDER"} ne sera pas inclue.
     */
    static String selectWithoutWhere(String query) {
        final int lower = indexOfWord(query, "WHERE", 0);
        if (lower >= 0) {
            final String old = query;
            query = query.substring(0, lower);
            int upper  = indexOfWord(old, "GROUP", lower);
            int upper2 = indexOfWord(old, "ORDER", lower);
            if (upper<0 || (upper2>=0 && upper2<upper)) {
                upper = upper2;
            }
            if (upper >= 0) {
                query += old.substring(upper);
            }
        }
        return query;
    }

    /**
     * Recherche une sous-chaîne dans une chaîne en ignorant les différences entre majuscules et
     * minuscules. Les racourcis du genre <code>text.toUpperCase().indexOf("SEARCH FOR")</code>
     * ne fonctionnent pas car {@code toUpperCase()} et {@code toLowerCase()} peuvent changer le
     * nombre de caractères de la chaîne. De plus, cette méthode vérifie que le mot est délimité
     * par des espaces ou de la ponctuation.
     */
    private static int indexOfWord(final String text, final String searchFor, final int startAt) {
        final int searchLength = searchFor.length();
        final int length = text.length();
        for (int i=startAt; i<length; i++) {
            if (text.regionMatches(true, i, searchFor, 0, searchLength)) {
                if (i!=0 && Character.isUnicodeIdentifierPart(text.charAt(i-1))) {
                    continue;
                }
                final int upper = i+length;
                if (upper<length && Character.isUnicodeIdentifierPart(text.charAt(upper))) {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Retourne un calendrier utilisant le {@linkplain Database#getTimeZone fuseau horaire de la
     * base de données}. Ce calendrier doit être utilisé pour lire les dates de la base de données,
     * comme dans l'exemple ci-dessous:
     *
     * <blockquote><pre>
     * Calendar   calendar = getCalendar();
     * Timestamp startTime = resultSet.getTimestamp(1, calendar);
     * Timestamp   endTime = resultSet.getTimestamp(2, calendar); 
     * </pre></blockquote>
     *
     * Ce calendrier doit aussi être utilisé pour écrire des valeurs ou spécifier des paramètres.
     * <p>
     * <strong>Note à propos de l'implémentation:</strong> Les conventions locales utilisées seront
     * celles du {@linkplain Locale#CANADA Canada anglais}, car elles sont proches de celles des États-Unis
     * (utilisés sur la plupart des logiciels comme PostgreSQL) tout en étant un peu plus pratique
     * (dates dans l'ordre <var>année</var>/<var>mois</var>/<var>jour</var>). Notez que nous
     * n'utilisons pas {@link Calendar#getInstance()}, car ce dernier peut retourner un calendrier
     * bien plus élaboré que celui utilisé par la plupart des logiciels de bases de données existants,
     * (par exemple un calendrier japonais), et nous voulons coller à ces derniers.
     */
    protected final Calendar getCalendar() {
        assert Thread.holdsLock(this);
        if (calendar == null) {
            if (database != null) {
                calendar = new GregorianCalendar(database.timezone, Locale.CANADA);
            } else {
                calendar = new GregorianCalendar(Locale.CANADA);
            }
        }
        return calendar;
    }

    /**
     * Prévient que l'état de cette table a changé. Les classes dérivées devraient appeller cette
     * méthode chaque fois qu'une méthode {@code setXXX(...)} a été appelée. L'implémentation par
     * défaut lève un drapeau de façon à {@linkplain #configure configurer} la requête la prochaine
     * fois qu'une méthode {@linkplain #getStatement(String) getStatement(...)} sera appelée.
     *
     * @param property Nom de la propriété qui a changée.
     *
     * @todo Enregistrer les changements dans un journal.
     */
    protected void fireStateChanged(final String property) {
        assert Thread.holdsLock(this);
        if (this instanceof Shareable) {
            throw new IllegalStateException("Violation du contrat des tables partageables.");
        }
        changed = true;
    }

    /**
     * Indique qu'une erreur inatendue est survenue, mais que le programme peut quand-même
     * continuer à fonctionner.
     *
     * @param method Le nom de la méthode dans laquelle est survenue l'erreur.
     * @param exception L'erreur survenue.
     */
    protected final void logWarning(final String method, final Throwable exception) {
        final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName(Utilities.getShortClassName(this));
        record.setSourceMethodName(method);
        Element.LOGGER.log(record);
    }

    /**
     * Retourne une représentation de cette table sous forme de chaîne de caractères.
     * Cette méthode est principalement utilisée à des fins de déboguages.
     */
    @Override
    public synchronized String toString() {
        final StringBuilder buffer = new StringBuilder(Utilities.getShortClassName(this));
        buffer.append('[');
        final long delay = System.currentTimeMillis() - lastAccess;
        if (delay > 0) {
            buffer.append("Dernière utilisation il y a ");
            buffer.append(delay / (60*1000));
            buffer.append(" minutes.");
        }
        buffer.append(']');
        if (query != null) {
            buffer.append(':');
            buffer.append(System.getProperty("line.separator", "\n"));
            buffer.append(query);
        }
        return buffer.toString();
    }

    /**
     * Libère les ressources utilisées par cette table si ce n'était pas déjà fait.
     * Cette méthode est appellée automatiquement par le ramasse-miettes lorsqu'il
     * a détecté que cette table n'est plus utilisée. L'implémentation par défaut
     * ferme la requête pré-compilée courante, s'il y en a une.
     * <p>
     * Notez qu'il n'y a pas de méthode {@code close()}, car une table peut être
     * {@linkplain Shareable partagée} par d'autres utilisateurs qui ont encore
     * besoin de ses services. On se fiera plutôt au ramasse-miette pour fermer
     * les connections lorsque cette table n'est plus du tout référencée.
     *
     * @throws SQLException si un problème est survenu lors de la disposition des ressources.
     */
    @Override
    protected synchronized void finalize() throws SQLException {
        getStatement((String) null);
    }
}
