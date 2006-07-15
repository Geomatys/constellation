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

// Base de donn�es
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

// Geotools
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;

// Sicade
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;

/**
 * Classe de base des connections vers une table de la base de donn�es. Chaque instance de
 * {@code Table} contient une et une seule instance de {@link PreparedStatement}, qui peut
 * changer (c'est-�-dire �tre ferm�e et une nouvelle instance recr��e) plusieurs fois.
 * <p>
 * Les classes d�riv�es devraient construire leur requ�te SQL seulement la premi�re fois o� elle sera
 * n�cessaire, typiquement � l'int�rieur d'une m�thode {@code getEntry(...)} public et synchronis�e.
 * La requ�te est construite en appelant une des m�thodes {@link #getStatement(String) getStatement(...)}.
 * Notez que la requ�te ainsi construite peut �tre d�truite � tout moment une fois sortit du bloc
 * synchronis�, de sorte que les classes d�riv�es ne devraient jamais compter sur sa p�r�nit�.
 * L'exemple suivant donne une impl�mentation typique:
 *
 * <blockquote><pre>
 * public <b>synchronized</b> {@linkplain Element} getEntry(String name) throws SQLException {
 *     PreparedStatement statement = {@linkplain #getStatement(ConfigurationKey) getStatement}(SELECT);
 *     statement.{@linkplain PreparedStatement#setString setString}(1, name);
 *     ResultSet r = statement.{@linkplain PreparedStatement#executeQuery() executeQuery}();
 *     // <i>Traiter ici les informations de la base de donn�es...</i>
 *     r.{@linkplain java.sql.ResultSet#close close}();
 *     // <i>Ne <b>pas</b> fermer </i>statement<i>, car il sera r�utilis� autant que possible.</i>
 * }
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Table {
    /**
     * Utilis� pour fermer la requ�te {@link #statement} apr�s un certain d�lai. On supposera
     * qu'apr�s quelques minutes d'inutilisation, les donn�es d�sir�es se trouvent dans une
     * quelconque cache g�r�e par les sous-classes.
     */
    private static final Timer TIMER = new Timer(true);

    /**
     * Le d�lai d'inactivit� (en millisecondes) apr�s lequel la requ�te {@link #statement}
     * sera ferm�e. La valeur actuelle est de 15 minutes.
     */
    private static final long DELAY = 15 * (60*1000L);

    /**
     * Ensemble d'indices � donner � Geotools concernant les fabriques � utiliser.
     * Ces indices permettent d'ajuster certains d�tails de l'impl�mentation des
     * fabriques.
     */
    protected static final Hints FACTORY_HINTS = null;

    /**
     * La base de donn�es d'o� provient cette table.
     */
    protected final Database database;

    /**
     * Calendrier pouvant �tre utilis� pour lire ou �crire des dates dans la base de donn�es.
     * Ce calendrier utilisera le fuseau horaire sp�cifi� par la propri�t� {@link #TIMEZONE},
     * qui devrait d�signer le fuseau horaire des dates dans la base de donn�es.
     * <p>
     * Nous construisons une instance de {@link GregorianCalendar} pour chaque table (plut�t
     * qu'une instance partag�e par tous) afin d'�viter des probl�mes en cas d'utilisation des
     * tables dans un environnement multi-thread.
     *
     * @see #getCalendar
     */
    private transient Calendar calendar;

    /**
     * Requ�te SQL � utiliser pour obtenir les donn�es. Peut �tre {@code null} si la requ�te
     * a �t� ferm�e.
     */
    private PreparedStatement statement;

    /**
     * La requ�te sous forme textuelle qui a servit � construire {@link #statement}, ou {@code null}.
     */
    private String query;

    /**
     * La derni�re fois o� {@link #statement} a �t� utilis�e.
     */
    private long lastAccess;

    /**
     * T�che ayant la charge de fermer la requ�te {@link #statement} apr�s une certaine
     * p�riode d'inactivit�.
     */
    private TimerTask disposer;

    /**
     * {@code true} si {@link #statement} a besoin d'�tre {@linkplain #configure configur�}.
     */
    private boolean changed;

    /**
     * T�che ayant la charge de fermer la requ�te {@link #statement} apr�s une certaine
     * p�riode d'inactivit�.
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
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database Connexion vers la base de donn�es d'observations.
     */
    protected Table(final Database database) {
        this.database = database;
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Retourne la base de donn�es d'o� provient cette table.
     */
    public final Database getDatabase() {
        return database;
    }

    /**
     * Retourne une propri�t� pour la cl� sp�cifi�e. Cette m�thode extrait la propri�t� de la
     * {@linkplain #database base de donn�es} si elle est non-nulle, ou retourne une propri�t�
     * par d�faut sinon.
     */
    protected final String getProperty(final ConfigurationKey key) {
        return (key==null) ? null : (database!=null) ? database.getProperty(key) : key.getDefaultValue();
    }

    /**
     * Retourne une version pr�-compil�e de la requ�te d�sign�e par la cl� sp�cifi�e. Cette m�thode
     * est un racourci pour <code>{@linkplain #getStatement(String) getStatement}({@linkplain
     * #getProperty getProperty}(key))</code>.
     *
     * @param  query Cl� d�signant la requ�te SQL � soumettre � la base de donn�es, ou
     *         {@code null} pour fermer la requ�te courante sans en construire de nouvelle.
     * @return La requ�te pr�-compil�e, ou {@code null} si {@code query} �tait nul.
     * @throws SQLException si la requ�te SQL n'a pas pu �tre construite.
     */
    protected final PreparedStatement getStatement(final ConfigurationKey query) throws SQLException {
        return getStatement(getProperty(query));
    }

    /**
     * Retourne une version pr�-compil�e de la requ�te sp�cifi�e. Si le texte {@code query} d�crit la
     * m�me requ�te que celle qui a �t� construite la derni�re fois que cette m�thode a �t� appel�e,
     * et si cette requ�te pr�-compil�e n'a pas encore �t� ferm�e, alors cette m�thode effectue les
     * op�rations suivantes:
     * <p>
     * <ul>
     *   <li>Si cette table a {@linkplain #fireStateChanged chang� d'�tat} depuis le dernier appel
     *       de cette m�thode, alors {@linkplain #configure configure} la requ�te.</li>
     * </ul>
     * <p>
     * Dans tous les autres cas (la requ�te sp�cifi�e n'est pas la m�me que la derni�re fois, o� la
     * pr�c�dente requ�te pr�-compil�e a �t� ferm�e), alors cette m�thode effectue les op�rations
     * suivantes:
     * <p>
     * <ul>
     *   <li>La requ�te courante (s'il y en a une) sera ferm�e.</li>
     *   <li>Une nouvelle requ�te est pr�-compil�e et {@linkplain #configure configur�e}.</li>
     * </ul>
     *
     * @param  query La requ�te SQL � soumettre � la base de donn�es, ou {@code null}
     *         pour fermer la requ�te courante sans en construire de nouvelle.
     * @return La requ�te pr�-compil�e, ou {@code null} si {@code query} �tait nul.
     * @throws SQLException si la requ�te SQL n'a pas pu �tre construite.
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
     * Retourne le type de requ�te actuellement en usage, ou {@code null} si aucun. Cette
     * information peut �tre utilis�e par les m�thodes {@link #configure configure} (lorsque
     * red�finie dans des classes d�riv�es) afin d'adapter la configuration en fonction du
     * type de requ�te � ex�cuter.
     */
    QueryType getQueryType() {
        return null;
    }

    /**
     * Appel�e automatiquement lorsqu'une m�thode {@link #getStatement(String) getStatement(...)}
     * a d�termin� que c'�tait n�cessaire. Cette appel se produit lorsqu'une nouvelle requ�te vient
     * d'�tre pr�-compil�e, ou que cette table a {@linkplain #fireStateChanged chang� d'�tat}. Les
     * classes d�riv�es devraient red�finir cette m�thode si des param�tres de la requ�te ont besoin
     * d'�tre d�finie en fonction de l'�tat de la table. Les impl�mentations des classes d�riv�es
     * devrait commencer par appeler {@code super.configure(statement)}.
     *
     * @param  type Type de requ�te � configurer.
     * @param  statement La requ�te � configurer (jamais {@code null}).
     * @throws SQLException si la configuration de la requ�te a �chou�.
     */
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        assert Thread.holdsLock(this);
        changed = false;
    }

    /**
     * Retourne la cha�ne sp�cifi�e apr�s avoir pr�fix� chaque caract�res g�n�riques par le
     * caract�re d'�chappement. Cette m�thode peut �tre utilis�e pour effectuer une recherche
     * exacte dans une instruction {@code LIKE}. Dans le cas particulier ou l'argument {@code text}
     * est {@code null}, alors cette m�thode retourne le caract�re d'�chappement {@code "%"}, qui
     * accepte toutes les cha�nes de caract�res.
     *
     * @param  text Le texte qui peut contenir les caract�res g�n�riques {@code '%'} et {@code '_'}.
     * @return Le texte avec un caract�re d'�chappement devant chaque caract�res g�n�riques.
     * @throws SQLException si une erreur est survenue lors de l'acc�s � la base de donn�es.
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
     * Extrait la partie {@code "SELECT ... FROM ..."} de la requ�te sp�cifi�e. Cette m�thode
     * retourne la cha�ne {@code query} � partir du d�but jusqu'au dernier caract�re pr�c�dant
     * la premi�re clause {@code "WHERE"}. La clause {@code "WHERE"} et tout ce qui suit jusqu'�
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
     * Recherche une sous-cha�ne dans une cha�ne en ignorant les diff�rences entre majuscules et
     * minuscules. Les racourcis du genre <code>text.toUpperCase().indexOf("SEARCH FOR")</code>
     * ne fonctionnent pas car {@code toUpperCase()} et {@code toLowerCase()} peuvent changer le
     * nombre de caract�res de la cha�ne. De plus, cette m�thode v�rifie que le mot est d�limit�
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
     * base de donn�es}. Ce calendrier doit �tre utilis� pour lire les dates de la base de donn�es,
     * comme dans l'exemple ci-dessous:
     *
     * <blockquote><pre>
     * Calendar   calendar = getCalendar();
     * Timestamp startTime = resultSet.getTimestamp(1, calendar);
     * Timestamp   endTime = resultSet.getTimestamp(2, calendar); 
     * </pre></blockquote>
     *
     * Ce calendrier doit aussi �tre utilis� pour �crire des valeurs ou sp�cifier des param�tres.
     * <p>
     * <strong>Note � propos de l'impl�mentation:</strong> Les conventions locales utilis�es seront
     * celles du {@linkplain Locale#CANADA Canada anglais}, car elles sont proches de celles des �tats-Unis
     * (utilis�s sur la plupart des logiciels comme PostgreSQL) tout en �tant un peu plus pratique
     * (dates dans l'ordre <var>ann�e</var>/<var>mois</var>/<var>jour</var>). Notez que nous
     * n'utilisons pas {@link Calendar#getInstance()}, car ce dernier peut retourner un calendrier
     * bien plus �labor� que celui utilis� par la plupart des logiciels de bases de donn�es existants,
     * (par exemple un calendrier japonais), et nous voulons coller � ces derniers.
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
     * Pr�vient que l'�tat de cette table a chang�. Les classes d�riv�es devraient appeller cette
     * m�thode chaque fois qu'une m�thode {@code setXXX(...)} a �t� appel�e. L'impl�mentation par
     * d�faut l�ve un drapeau de fa�on � {@linkplain #configure configurer} la requ�te la prochaine
     * fois qu'une m�thode {@linkplain #getStatement(String) getStatement(...)} sera appel�e.
     *
     * @param property Nom de la propri�t� qui a chang�e.
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
     * Indique qu'une erreur inatendue est survenue, mais que le programme peut quand-m�me
     * continuer � fonctionner.
     *
     * @param method Le nom de la m�thode dans laquelle est survenue l'erreur.
     * @param exception L'erreur survenue.
     */
    protected final void logWarning(final String method, final Throwable exception) {
        final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName(Utilities.getShortClassName(this));
        record.setSourceMethodName(method);
        Element.LOGGER.log(record);
    }

    /**
     * Retourne une repr�sentation de cette table sous forme de cha�ne de caract�res.
     * Cette m�thode est principalement utilis�e � des fins de d�boguages.
     */
    @Override
    public synchronized String toString() {
        final StringBuilder buffer = new StringBuilder(Utilities.getShortClassName(this));
        buffer.append('[');
        final long delay = System.currentTimeMillis() - lastAccess;
        if (delay > 0) {
            buffer.append("Derni�re utilisation il y a ");
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
     * Lib�re les ressources utilis�es par cette table si ce n'�tait pas d�j� fait.
     * Cette m�thode est appell�e automatiquement par le ramasse-miettes lorsqu'il
     * a d�tect� que cette table n'est plus utilis�e. L'impl�mentation par d�faut
     * ferme la requ�te pr�-compil�e courante, s'il y en a une.
     * <p>
     * Notez qu'il n'y a pas de m�thode {@code close()}, car une table peut �tre
     * {@linkplain Shareable partag�e} par d'autres utilisateurs qui ont encore
     * besoin de ses services. On se fiera plut�t au ramasse-miette pour fermer
     * les connections lorsque cette table n'est plus du tout r�f�renc�e.
     *
     * @throws SQLException si un probl�me est survenu lors de la disposition des ressources.
     */
    @Override
    protected synchronized void finalize() throws SQLException {
        getStatement((String) null);
    }
}
