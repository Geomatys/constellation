/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.DataRecord;
import org.constellation.configuration.DataRecord.DataType;
import org.constellation.configuration.ProviderRecord;
import org.constellation.configuration.ProviderRecord.ProviderType;
import org.constellation.configuration.StyleRecord;
import org.constellation.configuration.TaskRecord;
import org.constellation.configuration.TaskRecord.TaskState;
import org.constellation.configuration.UserRecord;
import org.geotoolkit.util.StringUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Session for administration database operations
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class AdminSession {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(AdminSession.class);

    /**
     * SQL query templates.
     */
    private static final Properties QUERIES = new Properties();
    static {
        final ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        final InputStream prop = loader.getResourceAsStream("org/constellation/sql/v1/queries.properties");
        try {
            QUERIES.load(prop);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "An error occurred while loading SQL queries properties.", ex);
        }
    }
    private static final String READ_PROVIDER                = "provider.read";
    private static final String READ_PROVIDER_LIST           = "provider.read.list";
    private static final String WRITE_PROVIDER               = "provider.write";
    private static final String DELETE_PROVIDER              = "provider.delete";
    private static final String READ_USER                    = "user.read";
    private static final String READ_USER_LIST               = "user.read.list";
    private static final String WRITE_USER                   = "user.write";
    private static final String UPDATE_USER                  = "user.update";
    private static final String DELETE_USER                  = "user.delete";
    private static final String READ_DATA                    = "data.read";
    private static final String READ_DATA_LIST               = "data.read.list";
    private static final String READ_DATA_LIST_FOR_STYLE     = "data.read.list.for.style";
    private static final String READ_DATA_LIST_FOR_PROVIDER  = "data.read.list.for.provider";
    private static final String WRITE_DATA                   = "data.write";
    private static final String DELETE_DATA                  = "data.delete";
    private static final String READ_STYLE                   = "style.read";
    private static final String READ_STYLE_LIST              = "style.read.list";
    private static final String READ_STYLE_LIST_FOR_DATA     = "style.read.list.for.data";
    private static final String READ_STYLE_LIST_FOR_PROVIDER = "style.read.list.for.provider";
    private static final String WRITE_STYLE                  = "style.write";
    private static final String DELETE_STYLE                 = "style.delete";
    private static final String WRITE_STYLED_DATA            = "styled_data.write";
    private static final String DELETE_STYLED_DATA           = "styled_data.delete";
    private static final String READ_TASK                    = "task.read";
    private static final String READ_TASK_LIST               = "task.read.list";
    private static final String READ_TASK_LIST_FROM_STATE    = "task.read.list.from.state";
    private static final String WRITE_TASK                   = "task.write";
    private static final String UPDATE_TASK                  = "task.update";
    private static final String DELETE_TASK                  = "task.delete";


    /**
     * Database {@link Connection} instance.
     */
    private final Connection connect;

    /**
     * Users cache for improved authentication performance.
     */
    private final Map<String, UserRecord> userCache;

    /**
     * Create a new {@link AdminSession} instance.
     *
     * @param connect the {@link Connection} instance
     * @param userCache a cache for queried users
     */
    AdminSession(final Connection connect, final Map<String, UserRecord> userCache) {
        this.connect   = connect;
        this.userCache = userCache;
    }

    /**
     * Returns the wrapper database active {@link Connection} instance.
     *
     * @return the {@link Connection} instance
     */
    public Connection getConnection() {
        return connect;
    }

    /**
     * Close the session. {@link AdminSession} instance should not be used after this.
     */
    public void close() {
        try {
            connect.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "An error occurred while closing database connection.");
        }
    }


    public UserRecord readUser(final String login) throws SQLException {
        UserRecord record = userCache.get(login);
        if (record == null) {
            record = selectOne(READ_USER, new Object[]{login}, UserRecord.class);
            if (record != null) {
                userCache.put(login, record);
            }
        }
        return record;
    }

    public List<UserRecord> readUsers() throws SQLException {
        return selectMany(READ_USER_LIST, UserRecord.class);
    }

    public void writeUser(final String login, final String password, final String name, final List<String> roles) throws SQLException {
        update(WRITE_USER, new Object[]{login, StringUtilities.MD5encode(password), name, StringUtils.join(roles,',')});
    }

    public void updateUser(final String login, final String newLogin, final String newPwd, final String newName, final List<String> newRoles) throws SQLException {
        update(UPDATE_USER, new Object[]{newLogin, StringUtilities.MD5encode(newPwd), newName, StringUtils.join(newRoles, ','), login});
    }

    public void deleteUser(final String login) throws SQLException {
        update(DELETE_USER, new Object[]{login});
        userCache.remove(login);
    }


    public ProviderRecord readProvider(final String identifier) throws SQLException {
        return selectOne(READ_PROVIDER, new Object[]{identifier}, ProviderRecord.class);
    }

    public List<ProviderRecord> readProvider() throws SQLException {
        return selectMany(READ_PROVIDER_LIST, ProviderRecord.class);
    }

    public void writeProvider(final String identifier, final ProviderType type, final String impl, final String owner) throws SQLException {
        update(WRITE_PROVIDER, new Object[]{identifier, type.name(), impl, owner});
    }

    public void deleteProvider(final String identifier) throws SQLException {
        update(DELETE_PROVIDER, new Object[]{identifier});
    }


    public StyleRecord readStyle(final String styleName, final String providerId) throws SQLException {
        return selectOne(READ_STYLE, new Object[]{styleName, providerId}, StyleRecord.class);
    }

    public List<StyleRecord> readStyles() throws SQLException {
        return selectMany(READ_STYLE_LIST, StyleRecord.class);
    }

    public List<StyleRecord> readStyles(final DataRecord data) throws SQLException {
        return selectMany(READ_STYLE_LIST_FOR_DATA, new Object[]{data.getId()}, StyleRecord.class);
    }

    public List<StyleRecord> readStyles(final String providerId) throws SQLException {
        return selectMany(READ_STYLE_LIST_FOR_PROVIDER, new Object[]{providerId}, StyleRecord.class);
    }

    public void writeStyle(final String providerId, final String name, final String owner, final long date) throws SQLException {
        update(WRITE_STYLE, new Object[]{name, providerId, owner, date});
    }

    public void deleteStyle(final String styleName, final String providerId) throws SQLException {
        update(DELETE_STYLE, new Object[]{styleName, providerId});
    }


    public DataRecord readData(final String dataName, final String providerId) throws SQLException {
        return selectOne(READ_DATA, new Object[]{dataName, providerId}, DataRecord.class);
    }

    public List<DataRecord> readData() throws SQLException {
        return selectMany(READ_DATA_LIST, DataRecord.class);
    }

    public List<DataRecord> readData(final StyleRecord style) throws SQLException {
        return selectMany(READ_DATA_LIST_FOR_STYLE, new Object[]{style.getId()}, DataRecord.class);
    }

    public List<DataRecord> readData(final String providerId) throws SQLException {
        return selectMany(READ_DATA_LIST_FOR_PROVIDER, new Object[]{providerId}, DataRecord.class);
    }

    public void writeData(final String providerId, final String name, final DataType type, final String owner, final long date) throws SQLException {
        update(WRITE_DATA, new Object[]{name, providerId, type.name(), owner, date});
    }

    public void deleteData(final String dataName, final String providerId) throws SQLException {
        update(DELETE_DATA, new Object[]{dataName, providerId});
    }


    public void writeStyledData(final StyleRecord style, final DataRecord data) throws SQLException {
        update(WRITE_STYLED_DATA, new Object[]{style.getId(), data.getId()});
    }

    public void deleteStyledData(final StyleRecord style, final DataRecord data) throws SQLException {
        update(DELETE_STYLED_DATA, new Object[]{style.getId(), data.getId()});
    }


    public TaskRecord readTask(final String identifier) throws SQLException {
        return selectOne(READ_TASK, new Object[]{identifier}, TaskRecord.class);
    }

    public List<TaskRecord> readTasks() throws SQLException {
        return selectMany(READ_TASK_LIST, TaskRecord.class);
    }

    public TaskRecord readTasks(final String state) throws SQLException {
        return selectOne(READ_TASK_LIST_FROM_STATE, new Object[]{state}, TaskRecord.class);
    }

    public void writeTask(final String identifier, final String type, final String description, final String owner) throws SQLException {
        update(WRITE_TASK, new Object[]{identifier, TaskRecord.TaskState.PENDING, type, description, new java.util.Date().getTime(), owner});
    }

    public void updateTask(final String identifier, final TaskState state) throws SQLException {
        update(UPDATE_TASK, new Object[]{state.name(),new java.util.Date().getTime(),identifier});
    }

    public void deleteTask(final String identifier) throws SQLException {
        update(DELETE_TASK, new Object[]{identifier});
    }

    public void update(final String query, final Object[] args) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connect.prepareStatement(QUERIES.getProperty(query));
            prepareStatement(stmt, args);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T selectOne(final String query, final Object[] args, final Class<T> _class) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connect.prepareStatement(QUERIES.getProperty(query));
            prepareStatement(stmt, args);
            rs = stmt.executeQuery();
            if (rs.next()) {
                try {
                    return _class.getConstructor(ResultSet.class).newInstance(rs);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "An error occurred while creating a " + _class.getCanonicalName() + " instance from ResultSet.");
                }
            }
        } finally {
            if (rs != null)   rs.close();
            if (stmt != null) stmt.close();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> selectMany(final String query, final Class<T> _class) throws SQLException {
        final List<T> list = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connect.createStatement();
            rs = stmt.executeQuery(QUERIES.getProperty(query));
            while (rs.next()) {
                try {
                    list.add(_class.getConstructor(ResultSet.class).newInstance(rs));
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "An error occurred while creating a " + _class.getCanonicalName() + " instance from ResultSet.");
                }
            }
        } finally {
            if (rs != null)   rs.close();
            if (stmt != null) stmt.close();
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> selectMany(final String query, final Object[] args, final Class<T> _class) throws SQLException {
        final List<T> list = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connect.prepareStatement(QUERIES.getProperty(query));
            prepareStatement(stmt, args);
            rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    list.add(_class.getConstructor(ResultSet.class).newInstance(rs));
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "An error occurred while creating a " + _class.getCanonicalName() + " instance from ResultSet.");
                }
            }
        } finally {
            if (rs != null)   rs.close();
            if (stmt != null) stmt.close();
        }
        return list;
    }

    private void prepareStatement(final PreparedStatement stmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                stmt.setString(i + 1, (String) args[i]);
            } else if (args[i] instanceof Integer) {
                stmt.setInt(i + 1, (Integer) args[i]);
            } else if (args[i] instanceof Array) {
                stmt.setArray(i + 1, (Array) args[i]);
            } else if (args[i] instanceof Double) {
                stmt.setDouble(i + 1, (Double) args[i]);
            } else if (args[i] instanceof Float) {
                stmt.setFloat(i + 1, (Float) args[i]);
            } else if (args[i] instanceof Long) {
                stmt.setLong(i + 1, (Long) args[i]);
            } else if (args[i] instanceof Date) {
                stmt.setDate(i + 1, (Date) args[i]);
            } else {
                stmt.setObject(i + 1, args[i]);
            }
        }
    }
}
