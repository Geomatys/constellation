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

// SQL
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.rmi.RemoteException;

// Collections
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import org.geotools.util.WeakValueHashMap;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

// Sicade
import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;
import net.sicade.observation.NoSuchRecordException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Classe de base des tables dont les m�thodes {@code getEntry(...)} retourneront un et un seul
 * enregistrement. Les enregistrements de ces tables sont identifi�s de fa�on unique par un nom
 * et (optionnellement) un num�ro ID. Le nom ou num�ro ID fait souvent partie d'une cl� primaire
 * d'une autre table.
 * <p>
 * La classe {@code SingletonTable} d�finit des m�thodes {@link #getEntries()},
 * {@link #getEntry(String)} et {@link #getEntry(int)}. En contrepartie, les
 * classes d�riv�es doivent impl�menter les m�thodes suivantes:
 * <p>
 * <ul>
 *   <li>{@link #getQuery}<br>
 *       pour retourner l'instruction SQL � utiliser pour obtenir les donn�es � partir de son nom
 *       ou num�ro ID.</li>
 *   <li>{@link #createEntry}<br>
 *       pour construire une entr�e � partir de la ligne courante.</li>
 *   <li>{@link #postCreateEntry} (facultatif)<br>
 *       pour achever la construction d'une entr�e apr�s que le r�sultat de la requ�te SQL ait �t�
 *       ferm�. Particuli�rement utile si la phase finale peut impliquer de nouvelles requ�tes sur
 *       le m�me objet {@link java.sql.Statement}.</li>
 * </ul>
 * <p>
 * Les entr�s obtenues lors des appels pr�c�dents peuvent �tre cach�es pour un acc�s plus rapide
 * la prochaine fois qu'une m�thode {@code getEntry(...)} est appel�e avec la m�me cl�.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class SingletonTable<E extends Element> extends Table {
    /**
     * Le type de la requ�te courante, ou {@code null} si aucune.
     */
    private QueryType type;

    /**
     * La requ�te SQL correspondant au type {@link #type}, ou {@code null}.
     */
    private String query;

    /**
     * Ensemble des entr�s d�j� obtenues pour chaque nom ou ID. Les cl�s doivent �tre soit des
     * objets {@link Integer}, ou soit des objets {@link String}. Aucune autre classe ne devrait
     * �tre utilis�e.
     * <p>
     * Note: L'utilisation d'une cache n'est pas forc�ment souhaitable. Si la base de donn�es a �t�
     *       mise � jour apr�s qu'une entr�e est �t� mise dans la cache, la mise-�-jour ne sera pas
     *       visible. Une solution possible serait de pr�voir un Listener � appeller lorsque la base
     *       de donn�es a �t� mise � jour.
     */
    @SuppressWarnings("unchecked")
    private final Map<Object,E> pool = new WeakValueHashMap();

    /**
     * Construit une table pour la connexion sp�cifi�e.
     *
     * @param  database Connexion vers la base de donn�es d'observations.
     */
    protected SingletonTable(final Database database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final QueryType getQueryType() {
        return type;
    }

    /**
     * Retourne la requ�te pr�-compil�e � utiliser pour le type sp�cifi�. Cette m�thode pr�parera
     * une nouvelle requ�te si la requ�te courante n'est pas d�j� du type sp�cifi�. Cette m�thode
     * est appel�e automatiquement par {@link #getEntries()}, {@link #getEntry(int)} et
     * {@link #getEntry(String)}.
     *
     * @param  type Le type de la requ�te.
     * @return La requ�te pr�-compil�e � utiliser.
     * @throws SQLException si la requ�te SQL n'a pas pu �tre construite.
     *
     * @see #getStatement(String)
     */
    protected final PreparedStatement getStatement(final QueryType type) throws SQLException {
        assert Thread.holdsLock(this);
        if (type != this.type) {
            query = getQuery(type);
        }
        this.type = type; // Doit �tre avant 'getStatement'.
        return getStatement(query);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les donn�es dans le contexte sp�cifi�. Cette
     * m�thode peut �tre appel�e avant l'ex�cution de {@link #getEntries()}, {@link #getEntry(String)}
     * ou {@link #getEntry(int)}. La valeur de l'argument {@code type} d�pend de laquelle des m�thodes
     * cit�es sera ex�cut�e.
     * <p>
     * L'impl�mentation par d�faut appelle <code>{@link #getQuery getQuery}({@linkplain
     * QueryType#SELECT SELECT})</code>, qui doit �tre sp�cifi� en red�finissant cette
     * m�thode comme ci-dessous:
     *
     * <blockquote><pre>
     * {@code @Override}
     * protected String getQuery({@linkplain QueryType} type) {
     *     switch (type) {
     *         case SELECT: return {@link #getProperty getProperty}(<var>SELECT_KEY</var>);
     *         default: return super.getQuery(type);
     *     }
     * }
     * </pre><blockquote>
     * 
     * o� <var>SELECT_KEY</var> d�pend de la classe d�riv�e. D'autres instructions {@code case}
     * peuvent �tre ajout�es si des requ�tes pr�d�finies existes pour d'autres cas. Pour les cas
     * restants, l'impl�mentation par d�faut retourne une requ�te d�riv�e du cas {@code SELECT}
     * en supposant que cette derni�re r�pond aux conditions suivantes:
     * <p>
     * <ul>
     *   <li>La premi�re colonne apr�s la clause {@code SELECT} doit �tre l'identifiant des
     *       enregistrements (habituellement la cl� primaire de la table), g�n�ralement sous
     *       forme d'une cha�ne de caract�res.</li>
     *
     *   <li>Dans le cas particulier o� cette table impl�mente {@link NumericAccess}, alors la r�gle
     *       pr�c�dente est �tendue en stipulant que les deux premi�res colonnes apr�s la clause
     *       {@code SELECT} doivent �tre dans l'ordre le nom de l'enregistrement (l'identifiant
     *       textuel) suivit de l'identifiant num�rique.</li>
     *
     *   <li>L'instruction SQL doit contenir une clause du genre {@code WHERE identifier=?}, ou
     *       {@code identifier} est le nom de la premi�re colonne dans la clause {@code SELECT}
     *       (voir le premier point). L'utilisateur est libre d'utiliser le nom de colonne de son
     *       choix; {@code "identifier"} n'est pas un nom obligatoire.</li>
     *
     *   <li>Le premier argument (le premier point d'interrogation dans la clause {@code WHERE})
     *       doit �tre le nom ou l'identifiant de l'enregistrement recherch�.</li>
     *
     *   <li>Si d'autres arguments sont utilis�s, il est de la responsabilit� 
     *       des classes d�riv�es de leur affecter une valeur.</li>
     * </ul>
     *
     * @param  type Le type de la requ�te.
     * @return La requ�te � utiliser pour la construction d'un objet {@link java.sql.PreparedStatement}.
     * @throws SQLException si la requ�te n'a pas pu �tre construite.
     */
    protected String getQuery(final QueryType type) throws SQLException {
        String query;
        switch (type) {
            case LIST: {
                query = getQuery(QueryType.SELECT);
                query = selectWithoutWhere(query);
                break;
            }
            case SELECT_BY_IDENTIFIER: {
                query = getQuery(QueryType.SELECT);
                if (this instanceof NumericAccess) {
                    query = changeArgumentTarget(query, 2);
                }
                break;
            }
            case SELECT: {
                // TODO: localize
                throw new IllegalStateException("getQuery(SELECT) n'a pas �t� d�finie.");
            }
            default: {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                   "type", type.toString()));
            }
        }
        return query.trim();
    }

    /**
     * Retourne l'index de l'argument pour le r�le sp�cifi�. L'impl�mentation par d�faut retourne
     * 1 pour {@link Role#IDENTIFIER IDENTIFIER}. Les classes d�riv�es peuvent red�finir cette
     * m�thode pour modifier les index des arguments.
     */
    protected int getArgumentIndex(final Role role) {
        switch (role) {
            case IDENTIFIER: return 1;
            default: throw new IllegalArgumentException(String.valueOf(role));
        }
    }

    /**
     * Change la colonne sur laquelle s'applique un argument apr�s la clause {@code WHERE}.
     * Par exemple cette m�thode peut remplacer:
     *
     * <blockquote><pre>
     * SELECT identifier, name FROM stations WHERE <em>identifier</em>=?
     * </pre></blockquote>
     *
     * par
     *
     * <blockquote><pre>
     * SELECT identifier, name FROM stations WHERE <em>name</em>=?
     * </pre></blockquote>
     *
     * Celle m�thode proc�de comme suit:
     *
     * <ol>
     *   <li>Recherche dans la requ�te la premi�re colonne apr�s la clause {@code SELECT}.</li>
     *   <li>Recherche le colonne <var>target</var> apr�s la clause {@code SELECT}, o� <var>target</var>
     *       est un num�ro de colonne compt� � partir de 1.</li>
     *   <li>Recherche le premier nom dans la clause {@code WHERE}, et le remplace par le deuxi�me nom.</li>
     * </ol>
     *
     * @param  query  La requ�te SQL dont on veut changer la cible d'un argument.
     * @param  target Le num�ro de colonne dont le nom doit remplacer celui de la premi�re colonne
     *         dans les arguments.
     * @return La requ�te SQL avec l'argument modifi�.
     * @throws SQLException si cette m�thode n'a pas pu comprendre la requ�te SQL sp�cifi�e.
     */
    @SuppressWarnings("fallthrough")
    static String changeArgumentTarget(final String query, int target) throws SQLException {
        String  oldColumn = null;
        String  newColumn = null;
        int     step      = 0;
        int     lower     = 0;
        boolean scanword  = false;
        final int length  = query.length();
        for (int index=0; index<length; index++) {
            /*
             * Recherche les mots d�limit�s par des espaces, virgules ou symbole '='.
             * Plusieurs de ces symboles peuvent �tre cons�cutifs; ils seront ignor�s.
             */
            final char c = query.charAt(index);
            if ((c!=',' && c!='=' && !Character.isSpaceChar(c)) == scanword) {
                continue;
            }
            scanword = !scanword;
            if (scanword) {
                lower = index;
                continue;
            }
            /*
             * Un mot a �t� trouv�. L'action entreprise d�pend de l'�tape o� l'on se trouve
             * dans notre processus d'analyse. Tous les cas ci-dessous seront ex�cut�s l'un
             * apr�s l'autre, dans l'ordre.
             */
            final String word = query.substring(lower, index);
            switch (step) {
                // Ignore l'instruction "SELECT". Tous ce qui peut se trouver avant le premier
                // "SELECT" sera ignor� (mais dans une requ�te SQL normale, il n'y aura rien).
                case 0: {
                    if (!word.equalsIgnoreCase("SELECT")) {
                        continue;
                    }
                    break;
                }
                // Le premier mot apr�s "SELECT" sera le nom de colonne que l'on cherchera
                // � remplacer (dans une �tape ult�rieure) apr�s l'instruction "WHERE".
                case 1: {
                    oldColumn = word;
                    break;
                }
                // Si le nom de colonne de l'�tape pr�c�dente est imm�diatement suivit de
                // l'instruction AS, revient � l'�tape pr�c�dente (c'est-�-dire que c'est
                // le mot suivant qu'il faut utiliser comme nom de colonne).
                case 2: {
                    if (word.equalsIgnoreCase("AS")) {
                        step = 1;
                        continue;
                    }
                    step++;
                    // fall through
                }
                // Recherche la colonne sp�cifi� par l'argument 'target'. Note: pour cette
                // partie, nous ne prennons pas encore en compte d'�ventuels alias ("AS").
                case 3: {
                    if (word.equalsIgnoreCase("FROM")) {
                        throw new SQLException("Num�ro de colonne introuvable.");
                    }
                    if (--target != 1) {
                        continue;
                    }
                    newColumn = word;
                    break;
                }
                // Ignore tout ce qui suit dans la requ�te SQL jusqu'� la premi�re instruction WHERE.
                case 4: {
                    if (!word.equalsIgnoreCase("WHERE")) {
                        continue;
                    }
                    break;
                }
                // A la premi�re occurence de la premi�re colonne apr�s WHERE, remplace l'ancien
                // nom de colonne par le nouveau nom. Tout le reste est laiss� inchang�.
                case 5: {
                    if (!word.equalsIgnoreCase(oldColumn)) {
                        continue;
                    }
                    final StringBuilder buffer = new StringBuilder(query.substring(0, lower));
                    buffer.append(newColumn);
                    buffer.append(query.substring(index));
                    return buffer.toString();
                }
                // On ne devrait jamais atteindre ce point.
                default: {
                    throw new AssertionError(step);
                }
            }
            step++;
        }
        throw new SQLException("La premi�re colonne apr�s SELECT devrait appara�tre dans la clause WHERE.");
    }

    /**
     * Retourne un objet {@link Element} correspondant � la ligne courante du {@link ResultSet} sp�cifi�.
     * Cette m�thode est appel�e automatiquement par {@link #getEntry(String)} et {@link #getEntries()}.
     *
     * @param  results  Le r�sultat de la requ�te. Seul l'enregistrement courant doit �tre pris en compte.
     * @return L'entr� pour l'enregistrement courant de {@code results}.
     * @throws CatalogException si une erreur logique a �t� d�cel�e dans le contenu de la base de donn�es.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    protected abstract E createEntry(final ResultSet results) throws CatalogException, SQLException;

    /**
     * Compl�te la construction de l'entr�e sp�cifi�e. Cette m�thode est appel�e automatiquement
     * apr�s que toutes les requ�tes SQL aient �t� compl�t�es. On �vite ainsi des appels recursifs
     * qui pourraient entra�ner la cr�ation de plusieurs {@link ResultSet}s pour le m�me
     * {@link java.sql.Statement}, ce que ne supportent pas tous les pilotes JDBC.
     * L'impl�mentation par d�faut ne fait rien.
     *
     * @param  entry L'entr� dont on veut compl�ter la construction.
     * @throws CatalogException si une erreur logique a �t� d�cel�e dans le contenu de la base de donn�es.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    protected void postCreateEntry(final E entry) throws CatalogException, SQLException {
    }

    /**
     * Indique si la m�thode {@link #getEntries} devrait accepter l'entr�e sp�cifi�e.
     * L'impl�mentation par d�faut retourne toujours {@code true}.
     *
     * @param  entry Une entr� trouv�e par {@link #getEntries}.
     * @return {@code true} si l'entr� sp�cifi� doit �tre ajout� � l'ensemble retourn� par {@link #getEntries}.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou�.
     */
    protected boolean accept(final E entry) throws SQLException {
        return true;
    }

    /**
     * Ajoute l'entr�e sp�cifi�e dans la cache, en v�rifiant qu'elle n'existe pas d�j�.
     */
    private void cache(final Object key, final E entry) {
        if (pool.put(key, entry) != null) {
            throw new AssertionError(key);
        }
    }

    /**
     * Retourne une seule entr�e pour l'objet {@link #statement} courant. Tous les arguments de
     * {@link #statement} doivent avoir �t� d�finis avent d'appeler cette m�thode. Cette m�thode
     * suppose que l'appellant a d�j� v�rifi� qu'aucune entr�e n'existait pr�alablement dans la
     * cache pour la cl� sp�cifi�e. La requ�te sera ex�cut�e et {@link #createEntry} appel�e.
     * Le r�sultat sera alors plac� dans la cache, et {@link #postCreateEntry} appel�e.
     *
     * @param  statement Requ�te SQL � ex�cuter.
     * @param  key Cl� identifiant l'entr�.
     * @return L'entr� pour la cl� sp�cifi�e et l'�tat courant de {@link #statement}.
     * @throws CatalogException si aucun enregistrement ne correspond � l'identifiant demand�,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    private E executeQuery(final PreparedStatement statement, Object key)
            throws CatalogException, SQLException
    {
        assert Thread.holdsLock(this);
        assert !pool.containsKey(key) : key;
        E entry = null;
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final E candidate = createEntry(results);
            if (entry == null) {
                entry = candidate;
            } else if (!entry.equals(candidate)) {
                final String table = results.getMetaData().getTableName(1);
                results.close();
                throw new IllegalRecordException(table, Resources.format(
                          ResourceKeys.ERROR_DUPLICATED_RECORD_$1, key));
            }
        }
        if (entry == null) {
            String table = results.getMetaData().getTableName(1);
            results.close();
            if (table==null || (table=table.trim()).length()==0) {
                table = Utilities.getShortClassName(this);
                final String suffix = "Table";
                if (table.endsWith(suffix)) {
                    table = table.substring(0, table.length()-suffix.length());
                }
            }
            throw new NoSuchRecordException(Resources.format(
                      ResourceKeys.ERROR_KEY_NOT_FOUND_$2, table, key), table);
        }
        results.close();
        /*
         * Element creation is now finished. Stores under the specified key, which may be the
         * name (as a String) or the identifier (as an Integer). If the key is the entry's
         * name, uses the entry's String instance in order to reduce the number of objects
         * to be managed by the garbage collector.
         */
        final String name = entry.getName();
        if (key.equals(name)) {
            key = name;
        } else {
            // Maybe the key is an Integer and an object already exists in the pool using the
            // key as a String. Checks using the String key, and add the Integer key as an alias.
            final E candidate = pool.get(name);
            if (candidate != null) {
                cache(key, candidate);
                return candidate;
            } else {
                cache(name, entry);
            }
        }
        cache(key, entry);
        /*
         * Termine la construction en appellant postCreateEntry(...), mais seulement apr�s avoir
         * plac� le r�sultat dans la cache car postCreateEntry(...) peut appeller getEntry(...) de
         * mani�re r�cursive pour la m�me cl�. En cas d'�chec, on retirera l'entr�e de la cache.
         */
        try {
            postCreateEntry(entry);
        } catch (CatalogException exception) {
            pool.remove(name);
            pool.remove(key);
            throw exception;
        } catch (SQLException exception) {
            pool.remove(name);
            pool.remove(key);
            throw exception;
        } catch (RuntimeException exception) {
            pool.remove(name);
            pool.remove(key);
            throw exception;
        }
        return entry;
    }

    /**
     * Retourne une entr� pour le num�ro ID sp�cifi�. Cette m�thode est � peu pr�s synonyme
     * de <code>{@link #getEntry(String) getEntry}(String.valueOf(identifier))</code>, sauf
     * si cette table impl�mente {@link NumericAccess}.
     *
     * @param  identifier Le num�ro de l'entr�e d�sir�e.
     * @return L'entr�e demand�e.
     * @throws CatalogException si aucun enregistrement ne correspond � l'identifiant demand�,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized E getEntry(final int identifier) throws CatalogException, SQLException {
        final Integer key = new Integer(identifier);
        E entry = pool.get(key);
        if (entry != null) {
            return entry;
        }
        final PreparedStatement statement = getStatement(QueryType.SELECT_BY_IDENTIFIER);
        statement.setInt(getArgumentIndex(Role.IDENTIFIER), identifier);
        return executeQuery(statement, key);
    }

    /**
     * Retourne une entr� pour le nom sp�cifi�.
     *
     * @param  name Le nom de l'entr�e d�sir�e.
     * @return L'entr�e demand�e, ou {@code null} si {@code name} �tait nul.
     * @throws CatalogException si aucun enregistrement ne correspond au nom demand�,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized E getEntry(String name) throws CatalogException, SQLException {
        if (name == null) {
            return null;
        }
        name = name.trim();
        E entry = pool.get(name);
        if (entry != null) {
            return entry;
        }
        final PreparedStatement statement = getStatement(QueryType.SELECT);
        statement.setString(getArgumentIndex(Role.IDENTIFIER), name);
        return executeQuery(statement, name);
    }

    /**
     * Retourne toutes les entr�es disponibles dans la base de donn�es.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized Set<E> getEntries() throws CatalogException, SQLException {
        final Map<E,Boolean> set = new LinkedHashMap<E,Boolean>();
        final PreparedStatement statement = getStatement(QueryType.LIST);
        final ResultSet results = statement.executeQuery();
        try {
            while (results.next()) {
                E entry = createEntry(results);
                if (accept(entry)) {
                    final String name = entry.getName();
                    /*
                     * Si une entr�e existait d�j� dans la cache, r�utilise cette entr�e en se
                     * souvenant que postCreateEntry(...) n'a pas besoin d'�tre appel�e pour elle.
                     */
                    Boolean initialized = Boolean.FALSE;
                    final E previous = pool.get(name);
                    if (previous != null) {
                        entry       = previous;
                        initialized = Boolean.TRUE;
                    } else {
                        cache(name, entry);
                    }
                    if (set.put(entry, initialized) != null) {
                        throw new IllegalRecordException(results.getMetaData().getTableName(1),
                                Resources.format(ResourceKeys.ERROR_DUPLICATED_RECORD_$1, name));
                    }
                }
            }
            results.close();
            for (final Map.Entry<E,Boolean> entry : set.entrySet()) {
                if (!entry.getValue().booleanValue()) {
                    postCreateEntry(entry.getKey());
                    entry.setValue(Boolean.TRUE); // Mark as initialized.
                }
            }
        } catch (CatalogException exception) {
            roolback(set);
            throw exception;
        } catch (SQLException exception) {
            roolback(set);
            throw exception;
        } catch (RuntimeException exception) {
            roolback(set);
            throw exception;
        }
        /*
         * Recopie tous dans un nouvel ensemble, car set.keySet() n'est pas serializable.
         */
        return new LinkedHashSet<E>(set.keySet());
    }

    /**
     * Retire de la cache toutes les entr�es qui n'avaient pas encore �t� initialis�es.
     * Cette m�thode est appel�e en cas d'erreur � l'int�rieur de {@link #getEntries}
     * afin de conserver {@link SingletonTable} dans un �tat r�cup�rable.
     */
    private void roolback(final Map<E,Boolean> set) {
        for (final Map.Entry<E,Boolean> entry : set.entrySet()) {
            if (!entry.getValue().booleanValue()) {
                pool.remove(entry.getKey().getName());
            }
        }
    }

    /**
     * Vide la cache de toutes les r�f�rences vers les entr�es pr�c�demment cr��es. L'impl�mentation
     * par d�faut de {@code SingletonTable} n'appelle jamais cette m�thode. Toutefois, les classes
     * d�riv�es devraient l'appeller si {@linkplain #fireStateChanged un aspect de la table a chang�},
     * et que ce changement affecte l'�tat des prochaines {@linkplain #createEntry entr�es qui seront
     * cr��es} (au lieu de n'affecter que l'ensemble des entr�es qui seront trouv�es).
     */
    protected void clearCache() {
        assert Thread.holdsLock(this);
        pool.clear();
    }
}
