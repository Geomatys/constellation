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
package net.sicade.observation.sql;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.geotools.resources.Utilities;
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;


/**
 * Base class for connections to a database. A {@linkplain PreparedStatement prepared statement}
 * can be obtained using the {@link #getStatement(QueryType)} method in a synchronized block.
 * Note that the statement may be altered or disposed at any time after the synchronized lock
 * has been released.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Table {
    /**
     * The logger for table-related events.
     */
    protected static final Logger LOGGER = Element.LOGGER;

    /**
     * A timer for closing {@link #statement} after some delay. Some subclasses cache their
     * values, so the statement may not be needed anymore even if the table still in use.
     */
    private static final Timer TIMER = new Timer(true);

    /**
     * The minimal delay (in millisecondes) to wait before to close {@link #statement}.
     */
    private static final long DELAY = 3 * (60*1000L);

    /**
     * The query to execute.
     *
     * @see #getStatement(QueryType)
     */
    protected final Query query;

    /**
     * The query type for current {@linkplain #statement}, or {@code null} if none.
     */
    private QueryType queryType;

    /**
     * The last used query in SQL language. This is the query used for the {@link #statement}
     * creation. May be {@code null}.
     *
     * @see #getStatement(String)
     */
    private String querySQL;

    /**
     * The prepared statement for fetching data. May be {@code null} if not yet created
     * or if it has been closed.
     *
     * @see #getStatement(QueryType)
     * @see #getStatement(String)
     */
    private PreparedStatement statement;

    /**
     * {@code true} if {@link #statement} need to be {@linkplain #configure configured}.
     * This is the case when a new statement has just been created.
     */
    private boolean changed;

    /**
     * {@code true} if this table is unmodifiable (i.e. no {@code set} method are allowed).
     * A table is always modifiable upon construction, but may become unmodifiable at some
     * later stage (when {@link #freeze} is invoked).
     */
    private boolean unmodifiable;

    /**
     * Last time that {@link #statement} has been used. This is used by the {@linkplain #disposer}
     * in order to determine if the statement should be closed.
     */
    private long lastAccess;

    /**
     * The timer in charge of closing the {@linkplain #statement} after some idle time.
     */
    private TimerTask disposer;

    /**
     * The timer in charge of closing the {@linkplain #statement} after some idle time.
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
                    querySQL  = null;
                    cancel();
                }
            }
        }
    }

    /**
     * Calendrier pouvant être utilisé pour lire ou écrire des dates dans la base de données. Ce
     * calendrier utilisera le fuseau horaire spécifié par la propriété {@link Database#TIMEZONE},
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
     * Creates a new table using the specified query. The query given in argument should be some
     * subclass with {@link Query#addColumn addColumn} or {@link Query#addParameter addParameter}
     * methods invoked in its constructor.
     */
    protected Table(final Query query) {
        this.query = query;
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Creates a new table connected to the same {@linkplain #getDatabase database} and using
     * the same {@linkplain #query} than the specified table. Subclass constructors should
     * not modify the query, since it is shared.
     */
    protected Table(final Table table) {
        query      = table.query;
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Returns the database that contains this table. This is the database
     * specified at construction time.
     */
    public final Database getDatabase() {
        return query.database;
    }

    /**
     * Returns a property for the given key. This method tries to {@linkplain Database#getProperty
     * get the property} from the {@linkplain #getDatabase database} if available, or return the
     * {@linkplain ConfigurationKey#getDefaultValue default value} otherwise.
     *
     * @param  key The property key, usually one of {@link Database} constants.
     * @return The property value, or {@code null} if none.
     */
    protected final String getProperty(final ConfigurationKey key) {
        if (key == null) {
            return null;
        }
        final Database database = getDatabase();
        if (database != null) {
            final String value = database.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        return key.getDefaultValue();
    }

    /**
     * Returns a prepared statement for the given SQL query. If the specified {@code query} is the
     * same one that last time this method has been invoked, then this method returns the same
     * {@link PreparedStatement} instance (if it still available). Otherwise this method closes
     * the previous statement and creates a new one.
     * <p>
     * If a new statement is created, or if the table has {@linkplain #fireStateChanged
     * changed its state} since the last call, then this method invokes {@link #configure}.
     * <p>
     * The caller <strong>must</strong> holds the lock on {@code this} table before to invoke
     * this method.
     *
     * @param  query The SQL query to prepare, {@code null} if none.
     * @return The prepared statement, or {@code null} if {@code query} was null.
     * @throws SQLException if the statement can not be created.
     */
    protected final PreparedStatement getStatement(final String query) throws SQLException {
        assert Thread.holdsLock(this);
        if (!Utilities.equals(querySQL, query)) {
            if (statement != null) {
                try {
                    statement.close();
                } finally {
                    statement = null;
                    querySQL  = null;
                }
            }
            if (query == null) {
                if (disposer != null) {
                    disposer.cancel();
                    disposer = null;
                }
            } else {
                statement  = getDatabase().getConnection().prepareStatement(query);
                changed    = true;
                if (disposer == null) {
                    disposer = new Disposer();
                    TIMER.schedule(disposer, DELAY, DELAY);
                }
            }
            querySQL = query;
        }
        if (statement != null) {
            if (changed) {
                configure(queryType, statement);
                final Level level = queryType!=null ? queryType.level : Level.FINE;
                if (LOGGER.isLoggable(level)) {
                    final LogRecord record = new LogRecord(level,
                            getDatabase().isStatementFormatted() ? statement.toString() : query);
                    record.setSourceClassName(getClass().getName());
                    record.setSourceMethodName(getCallerMethodName(queryType));
                    LOGGER.log(record);
                }
            }
            lastAccess = System.currentTimeMillis();
        }
        return statement;
    }

    /**
     * Returns a prepared statement for the given query type.
     *
     * @param  type The query type, or {@code null}.
     * @return The prepared statement, or {@code null} if none.
     * @throws SQLException if the statement can not be created.
     */
    protected final PreparedStatement getStatement(final QueryType type) throws SQLException {
        final String sql = (query != null) ? query.select(type) : null;
        queryType = type;
        return getStatement(sql);
    }

    /**
     * Invoked automatically by {@link #getStatement(String)} for a newly created statement, or
     * for an existing statement when this table {@linkplain #fireStateChanged changed its state}.
     * Subclasses should override this method if they need to set SQL parameters according the
     * table state. Overriding methods should invoke {@code super.configure(type, statement)}
     * first.
     *
     * @param  type The query type.
     * @param  statement The statement to configure (never {@code null}).
     * @throws SQLException If the statement can not be configured.
     */
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        assert Thread.holdsLock(this);
        changed = false;
    }

    /**
     * Delegates to <code>element.{@linkplain IndexedSqlElement#indexOf indexOf}(type)</code>,
     * except that an exception is thrown if the specified element is not applicable to the
     * current query type. The {@code type} value is the argument given to the last call to
     * {@link #getStatement(QueryType)}.
     *
     * @param  role The element.
     * @return The element index (starting with 1).
     * @throws SQLException if the specified element is not applicable.
     */
    protected final int indexOf(final IndexedSqlElement element) throws SQLException {
        final int index = element.indexOf(queryType);
        if (index > 0) {
            return index;
        } else {
            throw new SQLException("L'élément " + element + " ne s'applique pas au type " + queryType);
        }
    }

    /**
     * Returns a calendar using the {@linkplain Database#getTimeZone database time zone}.
     * This calendar should be used for fetching dates from the database as in the example
     * below:
     *
     * <blockquote><pre>
     * Calendar   calendar = getCalendar();
     * Timestamp startTime = resultSet.getTimestamp(1, calendar);
     * Timestamp   endTime = resultSet.getTimestamp(2, calendar); 
     * </pre></blockquote>
     *
     * This calendar should be used for storing dates as well. The caller must holds the lock
     * on {@code this} table.
     */
    protected final Calendar getCalendar() {
        assert Thread.holdsLock(this);
        if (calendar == null) {
            calendar = Database.getCalendar(getDatabase());
        }
        assert query.database==null || calendar.getTimeZone().equals(query.database.getTimeZone());
        return calendar;
    }

    /**
     * Notifies that this table state changed. Subclasses should invoke this method each time
     * some {@code setXXX(...)} has been invoked on this {@code Table} object. If a subclass
     * override this method, then it must invoke {@code super.fireStateChanged(property)} first.
     *
     * @param property The name of the property that changed.
     *
     * @todo Log the changes.
     */
    protected void fireStateChanged(final String property) {
        assert Thread.holdsLock(this);
        changed = true;
        if (unmodifiable) {
            throw new IllegalStateException("Violation du contrat des tables partagées.");
        }
    }

    /**
     * Returns {@code true} if this table is modifiable.
     */
    protected final boolean isModifiable() {
        return !unmodifiable;
    }

    /**
     * Marks this table as unmodifiable.
     */
    final void freeze() {
        unmodifiable = true;
    }

    /**
     * Notifies that a recoverable error occured.
     *
     * @param method The method name in which the error occured.
     * @param exception The error.
     */
    protected final void logWarning(final String method, final Throwable exception) {
        final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName(getClass().getName());
        record.setSourceMethodName(method);
        Element.LOGGER.log(record);
    }

    /**
     * Returns the presumed name of the method that called {@link #getStatement},
     * for logging purpose.
     *
     * @todo See if we can do something more reliable.
     */
    private String getCallerMethodName(final QueryType type) {
        switch (type) {
            case SELECT:               return "getEntry(String)";
            case SELECT_BY_IDENTIFIER: return "getEntry(int)";
            case LIST:                 return "getEntries()";
            default:                   return "getStatement";
        }
    }

    /**
     * Clears the cache, if any. This method is overriden and given {@code protected}
     * access by {@link SingletonTable}. An empty method with package-private access
     * is defined here as a hook invoked by {@link Database#close}.
     */
    void clearCache() {
        assert Thread.holdsLock(this);
    }

    /**
     * Returns a string representation of this table.
     * This is used mostly for debugging purpose.
     */
    @Override
    public synchronized String toString() {
        final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
        buffer.append('[');
        final long delay = System.currentTimeMillis() - lastAccess;
        if (delay > 0) {
            buffer.append("Dernière utilisation il y a ").append(delay / (60*1000)).append(" minutes.");
        }
        buffer.append(']');
        if (querySQL != null) {
            buffer.append(':').append(System.getProperty("line.separator", "\n")).append(querySQL);
        }
        return buffer.toString();
    }
}
