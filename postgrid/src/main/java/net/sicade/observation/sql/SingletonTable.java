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
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Classe de base des tables dont les méthodes {@code getEntry(...)} retourneront un et un seul
 * enregistrement. Les enregistrements de ces tables sont identifiés de façon unique par un nom
 * et (optionnellement) un numéro ID. Le nom ou numéro ID fait souvent partie d'une clé primaire
 * d'une autre table.
 * <p>
 * La classe {@code SingletonTable} définit des méthodes {@link #getEntries()},
 * {@link #getEntry(String)} et {@link #getEntry(int)}. En contrepartie, les
 * classes dérivées doivent implémenter les méthodes suivantes:
 * <p>
 * <ul>
 *   <li>{@link #getQuery}<br>
 *       pour retourner l'instruction SQL à utiliser pour obtenir les données à partir de son nom
 *       ou numéro ID.</li>
 *   <li>{@link #createEntry}<br>
 *       pour construire une entrée à partir de la ligne courante.</li>
 *   <li>{@link #postCreateEntry} (facultatif)<br>
 *       pour achever la construction d'une entrée après que le résultat de la requête SQL ait été
 *       fermé. Particulièrement utile si la phase finale peut impliquer de nouvelles requêtes sur
 *       le même objet {@link java.sql.Statement}.</li>
 * </ul>
 * <p>
 * Les entrés obtenues lors des appels précédents peuvent être cachées pour un accès plus rapide
 * la prochaine fois qu'une méthode {@code getEntry(...)} est appelée avec la même clé.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class SingletonTable<E extends Element> extends Table {
    /**
     * Le type de la requête courante, ou {@code null} si aucune.
     */
    private QueryType type;

    /**
     * La requête SQL correspondant au type {@link #type}, ou {@code null}.
     */
    private String query;

    /**
     * Ensemble des entrés déjà obtenues pour chaque nom ou ID. Les clés doivent être soit des
     * objets {@link Integer}, ou soit des objets {@link String}. Aucune autre classe ne devrait
     * être utilisée.
     * <p>
     * Note: L'utilisation d'une cache n'est pas forcément souhaitable. Si la base de données a été
     *       mise à jour après qu'une entrée est été mise dans la cache, la mise-à-jour ne sera pas
     *       visible. Une solution possible serait de prévoir un Listener à appeller lorsque la base
     *       de données a été mise à jour.
     */
    @SuppressWarnings("unchecked")
    private final Map<Object,E> pool = new WeakValueHashMap();

    /**
     * Construit une table pour la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
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
     * Retourne la requête pré-compilée à utiliser pour le type spécifié. Cette méthode préparera
     * une nouvelle requête si la requête courante n'est pas déjà du type spécifié. Cette méthode
     * est appelée automatiquement par {@link #getEntries()}, {@link #getEntry(int)} et
     * {@link #getEntry(String)}.
     *
     * @param  type Le type de la requête.
     * @return La requête pré-compilée à utiliser.
     * @throws SQLException si la requête SQL n'a pas pu être construite.
     *
     * @see #getStatement(String)
     */
    protected final PreparedStatement getStatement(final QueryType type) throws SQLException {
        assert Thread.holdsLock(this);
        if (type != this.type) {
            query = getQuery(type);
        }
        this.type = type; // Doit être avant 'getStatement'.
        return getStatement(query);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les données dans le contexte spécifié. Cette
     * méthode peut être appelée avant l'exécution de {@link #getEntries()}, {@link #getEntry(String)}
     * ou {@link #getEntry(int)}. La valeur de l'argument {@code type} dépend de laquelle des méthodes
     * citées sera exécutée.
     * <p>
     * L'implémentation par défaut appelle <code>{@link #getQuery getQuery}({@linkplain
     * QueryType#SELECT SELECT})</code>, qui doit être spécifié en redéfinissant cette
     * méthode comme ci-dessous:
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
     * où <var>SELECT_KEY</var> dépend de la classe dérivée. D'autres instructions {@code case}
     * peuvent être ajoutées si des requêtes prédéfinies existes pour d'autres cas. Pour les cas
     * restants, l'implémentation par défaut retourne une requête dérivée du cas {@code SELECT}
     * en supposant que cette dernière répond aux conditions suivantes:
     * <p>
     * <ul>
     *   <li>La première colonne après la clause {@code SELECT} doit être l'identifiant des
     *       enregistrements (habituellement la clé primaire de la table), généralement sous
     *       forme d'une chaîne de caractères.</li>
     *
     *   <li>Dans le cas particulier où cette table implémente {@link NumericAccess}, alors la rêgle
     *       précédente est étendue en stipulant que les deux premières colonnes après la clause
     *       {@code SELECT} doivent être dans l'ordre le nom de l'enregistrement (l'identifiant
     *       textuel) suivit de l'identifiant numérique.</li>
     *
     *   <li>L'instruction SQL doit contenir une clause du genre {@code WHERE identifier=?}, ou
     *       {@code identifier} est le nom de la première colonne dans la clause {@code SELECT}
     *       (voir le premier point). L'utilisateur est libre d'utiliser le nom de colonne de son
     *       choix; {@code "identifier"} n'est pas un nom obligatoire.</li>
     *
     *   <li>Le premier argument (le premier point d'interrogation dans la clause {@code WHERE})
     *       doit être le nom ou l'identifiant de l'enregistrement recherché.</li>
     *
     *   <li>Si d'autres arguments sont utilisés, il est de la responsabilité 
     *       des classes dérivées de leur affecter une valeur.</li>
     * </ul>
     *
     * @param  type Le type de la requête.
     * @return La requête à utiliser pour la construction d'un objet {@link java.sql.PreparedStatement}.
     * @throws SQLException si la requête n'a pas pu être construite.
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
                throw new IllegalStateException("getQuery(SELECT) n'a pas été définie.");
            }
            default: {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                   "type", type.toString()));
            }
        }
        return query.trim();
    }

    /**
     * Retourne l'index de l'argument pour le rôle spécifié. L'implémentation par défaut retourne
     * 1 pour {@link Role#IDENTIFIER IDENTIFIER}. Les classes dérivées peuvent redéfinir cette
     * méthode pour modifier les index des arguments.
     */
    protected int getArgumentIndex(final Role role) {
        switch (role) {
            case IDENTIFIER: return 1;
            default: throw new IllegalArgumentException(String.valueOf(role));
        }
    }

    /**
     * Change la colonne sur laquelle s'applique un argument après la clause {@code WHERE}.
     * Par exemple cette méthode peut remplacer:
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
     * Celle méthode procède comme suit:
     *
     * <ol>
     *   <li>Recherche dans la requête la première colonne après la clause {@code SELECT}.</li>
     *   <li>Recherche le colonne <var>target</var> après la clause {@code SELECT}, où <var>target</var>
     *       est un numéro de colonne compté à partir de 1.</li>
     *   <li>Recherche le premier nom dans la clause {@code WHERE}, et le remplace par le deuxième nom.</li>
     * </ol>
     *
     * @param  query  La requête SQL dont on veut changer la cible d'un argument.
     * @param  target Le numéro de colonne dont le nom doit remplacer celui de la première colonne
     *         dans les arguments.
     * @return La requête SQL avec l'argument modifié.
     * @throws SQLException si cette méthode n'a pas pu comprendre la requête SQL spécifiée.
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
             * Recherche les mots délimités par des espaces, virgules ou symbole '='.
             * Plusieurs de ces symboles peuvent être consécutifs; ils seront ignorés.
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
             * Un mot a été trouvé. L'action entreprise dépend de l'étape où l'on se trouve
             * dans notre processus d'analyse. Tous les cas ci-dessous seront exécutés l'un
             * après l'autre, dans l'ordre.
             */
            final String word = query.substring(lower, index);
            switch (step) {
                // Ignore l'instruction "SELECT". Tous ce qui peut se trouver avant le premier
                // "SELECT" sera ignoré (mais dans une requête SQL normale, il n'y aura rien).
                case 0: {
                    if (!word.equalsIgnoreCase("SELECT")) {
                        continue;
                    }
                    break;
                }
                // Le premier mot après "SELECT" sera le nom de colonne que l'on cherchera
                // à remplacer (dans une étape ultérieure) après l'instruction "WHERE".
                case 1: {
                    oldColumn = word;
                    break;
                }
                // Si le nom de colonne de l'étape précédente est immédiatement suivit de
                // l'instruction AS, revient à l'étape précédente (c'est-à-dire que c'est
                // le mot suivant qu'il faut utiliser comme nom de colonne).
                case 2: {
                    if (word.equalsIgnoreCase("AS")) {
                        step = 1;
                        continue;
                    }
                    step++;
                    // fall through
                }
                // Recherche la colonne spécifié par l'argument 'target'. Note: pour cette
                // partie, nous ne prennons pas encore en compte d'éventuels alias ("AS").
                case 3: {
                    if (word.equalsIgnoreCase("FROM")) {
                        throw new SQLException("Numéro de colonne introuvable.");
                    }
                    if (--target != 1) {
                        continue;
                    }
                    newColumn = word;
                    break;
                }
                // Ignore tout ce qui suit dans la requête SQL jusqu'à la première instruction WHERE.
                case 4: {
                    if (!word.equalsIgnoreCase("WHERE")) {
                        continue;
                    }
                    break;
                }
                // A la première occurence de la première colonne après WHERE, remplace l'ancien
                // nom de colonne par le nouveau nom. Tout le reste est laissé inchangé.
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
        throw new SQLException("La première colonne après SELECT devrait apparaître dans la clause WHERE.");
    }

    /**
     * Retourne un objet {@link Element} correspondant à la ligne courante du {@link ResultSet} spécifié.
     * Cette méthode est appelée automatiquement par {@link #getEntry(String)} et {@link #getEntries()}.
     *
     * @param  results  Le résultat de la requête. Seul l'enregistrement courant doit être pris en compte.
     * @return L'entré pour l'enregistrement courant de {@code results}.
     * @throws CatalogException si une erreur logique a été décelée dans le contenu de la base de données.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     */
    protected abstract E createEntry(final ResultSet results) throws CatalogException, SQLException;

    /**
     * Complète la construction de l'entrée spécifiée. Cette méthode est appelée automatiquement
     * après que toutes les requêtes SQL aient été complétées. On évite ainsi des appels recursifs
     * qui pourraient entraîner la création de plusieurs {@link ResultSet}s pour le même
     * {@link java.sql.Statement}, ce que ne supportent pas tous les pilotes JDBC.
     * L'implémentation par défaut ne fait rien.
     *
     * @param  entry L'entré dont on veut compléter la construction.
     * @throws CatalogException si une erreur logique a été décelée dans le contenu de la base de données.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     */
    protected void postCreateEntry(final E entry) throws CatalogException, SQLException {
    }

    /**
     * Indique si la méthode {@link #getEntries} devrait accepter l'entrée spécifiée.
     * L'implémentation par défaut retourne toujours {@code true}.
     *
     * @param  entry Une entré trouvée par {@link #getEntries}.
     * @return {@code true} si l'entré spécifié doit être ajouté à l'ensemble retourné par {@link #getEntries}.
     * @throws SQLException si l'interrogation de la base de données a échoué.
     */
    protected boolean accept(final E entry) throws SQLException {
        return true;
    }

    /**
     * Ajoute l'entrée spécifiée dans la cache, en vérifiant qu'elle n'existe pas déjà.
     */
    private void cache(final Object key, final E entry) {
        if (pool.put(key, entry) != null) {
            throw new AssertionError(key);
        }
    }

    /**
     * Retourne une seule entrée pour l'objet {@link #statement} courant. Tous les arguments de
     * {@link #statement} doivent avoir été définis avent d'appeler cette méthode. Cette méthode
     * suppose que l'appellant a déjà vérifié qu'aucune entrée n'existait préalablement dans la
     * cache pour la clé spécifiée. La requête sera exécutée et {@link #createEntry} appelée.
     * Le résultat sera alors placé dans la cache, et {@link #postCreateEntry} appelée.
     *
     * @param  statement Requête SQL à exécuter.
     * @param  key Clé identifiant l'entré.
     * @return L'entré pour la clé spécifiée et l'état courant de {@link #statement}.
     * @throws CatalogException si aucun enregistrement ne correspond à l'identifiant demandé,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
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
         * Termine la construction en appellant postCreateEntry(...), mais seulement après avoir
         * placé le résultat dans la cache car postCreateEntry(...) peut appeller getEntry(...) de
         * manière récursive pour la même clé. En cas d'échec, on retirera l'entrée de la cache.
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
     * Retourne une entré pour le numéro ID spécifié. Cette méthode est à peu près synonyme
     * de <code>{@link #getEntry(String) getEntry}(String.valueOf(identifier))</code>, sauf
     * si cette table implémente {@link NumericAccess}.
     *
     * @param  identifier Le numéro de l'entrée désirée.
     * @return L'entrée demandée.
     * @throws CatalogException si aucun enregistrement ne correspond à l'identifiant demandé,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
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
     * Retourne une entré pour le nom spécifié.
     *
     * @param  name Le nom de l'entrée désirée.
     * @return L'entrée demandée, ou {@code null} si {@code name} était nul.
     * @throws CatalogException si aucun enregistrement ne correspond au nom demandé,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
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
     * Retourne toutes les entrées disponibles dans la base de données.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
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
                     * Si une entrée existait déjà dans la cache, réutilise cette entrée en se
                     * souvenant que postCreateEntry(...) n'a pas besoin d'être appelée pour elle.
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
     * Retire de la cache toutes les entrées qui n'avaient pas encore été initialisées.
     * Cette méthode est appelée en cas d'erreur à l'intérieur de {@link #getEntries}
     * afin de conserver {@link SingletonTable} dans un état récupérable.
     */
    private void roolback(final Map<E,Boolean> set) {
        for (final Map.Entry<E,Boolean> entry : set.entrySet()) {
            if (!entry.getValue().booleanValue()) {
                pool.remove(entry.getKey().getName());
            }
        }
    }

    /**
     * Vide la cache de toutes les références vers les entrées précédemment créées. L'implémentation
     * par défaut de {@code SingletonTable} n'appelle jamais cette méthode. Toutefois, les classes
     * dérivées devraient l'appeller si {@linkplain #fireStateChanged un aspect de la table a changé},
     * et que ce changement affecte l'état des prochaines {@linkplain #createEntry entrées qui seront
     * créées} (au lieu de n'affecter que l'ensemble des entrées qui seront trouvées).
     */
    protected void clearCache() {
        assert Thread.holdsLock(this);
        pool.clear();
    }
}
