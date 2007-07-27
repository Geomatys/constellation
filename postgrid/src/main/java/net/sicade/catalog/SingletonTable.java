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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import org.geotools.util.WeakValueHashMap;

import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.NoSuchRecordException;
import net.sicade.coverage.catalog.IllegalRecordException;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Base class for tables with a {@code getEntry(...)} method returning at most one element. The
 * elements are uniquely identified by a name and (optionally) a numeric ID.
 * <p>
 * {@code SingletonTable} defines the {@link #getEntries()}, {@link #getEntry(String)} and
 * {@link #getEntry(int)} methods. Subclasses must provides implementation for the following
 * methods:
 * <p>
 * <ul>
 *   <li>{@link #createEntry}<br>
 *       Creates an element for the current row.</li>
 *   <li>{@link #postCreateEntry} (facultatif)<br>
 *       Performs additional element construction after the {@linkplain ResultSet result set} has
 *       been closed. This is useful if the construction may implies new queries involving the same
 *       table or statement.</li>
 * </ul>
 * <p>
 * The elements created by this class are cached for faster access the next time a
 * {@code getEntry(...)} method is invoked again.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class SingletonTable<E extends Element> extends Table {
    /**
     * The query to use for selecting a record by its name.
     */
    private static final QueryType SELECT_BY_NAME = QueryType.SELECT;

    /**
     * The query to use for selecting a record by its number.
     */
    private static final QueryType SELECT_BY_NUMBER = QueryType.SELECT_BY_IDENTIFIER;

    /**
     * The parameter to use for looking an element by name, or {@code 0} if unset.
     * This is usually the parameter for the value to search in the primary key column.
     *
     * @see #setIdentifierParameters
     */
    private int indexByName;

    /**
     * The parameter to use for looking an element by its numeric identifier,
     * or {@code 0} if unset.
     *
     * @see #setIdentifierParameters
     */
    private int indexByNumber;

    /**
     * The elements created up to date. The key should be {@link Integer} or {@link String}
     * instances only.
     * <p>
     * Note: L'utilisation d'une cache n'est pas forcément souhaitable. Si la base de données a été
     *       mise à jour après qu'une entrée est été mise dans la cache, la mise-à-jour ne sera pas
     *       visible. Une solution possible serait de prévoir un Listener à appeller lorsque la base
     *       de données a été mise à jour.
     */
    private final Map<Object,E> pool = new WeakValueHashMap();

    /**
     * Creates a new table using the specified query. The query given in argument should be some
     * subclass with {@link Query#addColumn addColumn} and {@link Query#addParameter addParameter}
     * methods invoked in its constructor.
     */
    protected SingletonTable(final Query query) {
        super(query);
    }

    /**
     * Creates a new table connected to the same {@linkplain #getDatabase database} and using
     * the same {@linkplain #query query} than the specified table. Subclass constructors should
     * not modify the query, since it is shared.
     */
    protected SingletonTable(final SingletonTable<E> table) {
        super(table);
    }

    /**
     * Sets the parameter to use for looking an element by identifier. This is usually the
     * parameter for the value to search in the primary key column. This information is needed
     * for {@link #getEntry(String)} execution and is usually specified at construction time.
     *
     * @param  byName   The parameter for looking an element by name, or {@code null} if none.
     * @param  byNumber The parameter for looking an element by its numeric identifier, or
     *                  {@code null} if none. Most table do not provide a numeric identifer.
     * @throws IllegalArgumentException if the specified parameters are not one of those
     *         declared for {@link QueryType#SELECT} or {@link QueryType#SELECT_BY_IDENTIFIER}.
     */
    protected synchronized void setIdentifierParameters(final Parameter byName, final Parameter byNumber)
            throws IllegalArgumentException
    {
        int   newByName = 0;
        int newByNumber = 0;
        boolean success = true;
        String    name = "byName";
        Parameter param = byName;
        if (byName == null) {
            if (byNumber != null) {
                newByName = byNumber.indexOf(SELECT_BY_NAME);
                // Optional, so don't test for success.
            }
        } else {
            if (success = query.getParameters(SELECT_BY_NAME).contains(byName)) {
                newByName = byName.indexOf(SELECT_BY_NAME);
                success = (newByName != 0);
            }
        }
        if (success) {
            name = "byNumber";
            param = byNumber;
            if (byNumber == null) {
                if (byName != null) {
                    newByNumber = byName.indexOf(SELECT_BY_NUMBER);
                    // Optional, so don't test for success.
                }
            } else {
                if (success = query.getParameters(SELECT_BY_NUMBER).contains(byNumber)) {
                    newByNumber = byNumber.indexOf(SELECT_BY_NUMBER);
                    success = (newByNumber != 0);
                }
            }
        }
        if (success) {
            if ((newByName != indexByName) || (newByNumber != indexByNumber)) {
                indexByName   = newByName;
                indexByNumber = newByNumber;
                clearCache();
                fireStateChanged("identifierParameters");
            }
            return;
        }
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_ARGUMENT_$2, name, param));
    }

    /**
     * Creates an {@link Element} object for the current {@linkplain ResultSet result set} row.
     * This method is invoked automatically by {@link #getEntry(String)} and {@link #getEntries()}.
     *
     * @param  results  The result set to use for fetching data. Only the current row should be
     *                  used, i.e. {@link ResultSet#next} should <strong>not</strong> be invoked.
     * @return The element for the current row in the specified {@code results}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected abstract E createEntry(final ResultSet results) throws CatalogException, SQLException;

    /**
     * Completes the creation of the specified element. This method is invoked after the
     * {@link ResultSet} has been closed, which allow recursive call to {@code getEntry(...)}.
     * The default implementation do nothing.
     *
     * @param  entry The element to complete.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected void postCreateEntry(final E entry) throws CatalogException, SQLException {
    }

    /**
     * Returns {@code true} if {@link #getEntries} should accept the given element.
     * The default implementation always returns {@code true}.
     *
     * @param  entry En element created by {@link #getEntries}.
     * @return {@code true} if the element should be added to the set returned by {@link #getEntries}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected boolean accept(final E entry) throws CatalogException, SQLException {
        return true;
    }

    /**
     * Adds the specified entry in the map, while making sure that it wasn't already there.
     */
    private void cache(final Object key, final E entry) {
        assert (key instanceof String) || (key instanceof Integer) : key;
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
                table = getClass().getSimpleName();
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
            key = name; // Use the same instance (slight memory saver).
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
            roolback(name, key);
            throw exception;
        } catch (SQLException exception) {
            roolback(name, key);
            throw exception;
        } catch (RuntimeException exception) {
            roolback(name, key);
            throw exception;
        }
        return entry;
    }

    /**
     * Returns an element for the specified ID number. This method is often similar to
     * <code>{@link #getEntry(String) getEntry}(String.valueOf(identifier))</code>,
     * but some subclasses (typically the ones that implement the {@link NumericAccess}
     * interface) may be more efficient.
     *
     * @param  identifier The numeric identifier of the element to fetch.
     * @return The element for the given numeric identifier.
     * @throws CatalogException if no element has been found for the specified identifier,
     *         or if an element contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized E getEntry(final int identifier) throws CatalogException, SQLException {
        final Integer key = identifier; // Autoboxing
        E entry = pool.get(key);
        if (entry != null) {
            return entry;
        }
        if (indexByNumber == 0) {
            throw new IllegalStateException();
        }
        final PreparedStatement statement = getStatement(SELECT_BY_NUMBER);
        statement.setInt(indexByNumber, identifier);
        return executeQuery(statement, key);
    }

    /**
     * Returns an element for the given name.
     *
     * @param  name The name of the element to fetch.
     * @return The element for the given name, or {@code null} if {@code name} was null.
     * @throws CatalogException if no element has been found for the specified name,
     *         or if an element contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
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
        if (indexByName == 0) {
            throw new IllegalStateException();
        }
        final PreparedStatement statement = getStatement(SELECT_BY_NAME);
        statement.setString(indexByName, name);
        return executeQuery(statement, name);
    }

    /**
     * Returns all entries available in the database. Modification in the returned
     * set will not alter this table.
     *
     * @return The set of entries. May be empty, but neven {@code null}.
     * @throws CatalogException if an element contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized Set<E> getEntries() throws CatalogException, SQLException {
        return getEntries(QueryType.LIST);
    }

    /**
     * Returns all entries available in the database using the specified query type.
     *
     * @param  The query type, usually {@link QueryType#LIST}.
     * @return The set of entries. May be empty, but neven {@code null}.
     * @throws CatalogException if an element contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected Set<E> getEntries(final QueryType type) throws CatalogException, SQLException {
        assert Thread.holdsLock(this);
        final Map<E,Boolean> set = new LinkedHashMap<E,Boolean>();
        final PreparedStatement statement = getStatement(type);
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
     * Removes the given entries from the cache. This is invoked when an exception
     * occured inside the {@link #executeQuery} method.
     */
    private void roolback(final String name, final Object key) {
        pool.remove(name);
        pool.remove(key);
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
     * Clears this table cache. Subclasses should invoke this method when the table
     * {@linkplain #fireStateChanged state changed} in some way that affect the
     * {@linkplain #createEntry entries to be created}, not just the set of entries
     * to be returned.
     */
    @Override
    protected void clearCache() {
        super.clearCache();
        pool.clear();
    }
}
