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

package org.constellation.admin.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.constellation.admin.dao.StyleRecord.StyleType;
import org.constellation.admin.dao.TaskRecord.TaskState;
import org.constellation.admin.util.IOUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Session for administration database operations
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Session {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(Session.class);

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
            LOGGER.log(Level.SEVERE, "An error occurred while loading SQL queries property file.", ex);
        }
    }

    private static final String READ_NEXT_I18N_ID         = "i18n_id.read.next";

    private static final String READ_I18N                 = "i18n.read";
    private static final String WRITE_I18N                = "i18n.write";
    private static final String UPDATE_I18N               = "i18n.update";
    private static final String DELETE_I18N               = "i18n.delete";

    private static final String READ_USER                 = "user.read";
    private static final String LIST_USERS                = "user.list";
    private static final String WRITE_USER                = "user.write";
    private static final String UPDATE_USER               = "user.update";

    private static final String READ_PROVIDER             = "provider.read";
    private static final String READ_PROVIDER_FROM_ID     = "provider.read.from.id";
    private static final String READ_PROVIDER_CONFIG      = "provider.read.config";
    private static final String LIST_PROVIDERS            = "provider.list";
    private static final String LIST_PROVIDERS_FROM_TYPE  = "provider.list.from.type";
    private static final String WRITE_PROVIDER            = "provider.write";
    private static final String UPDATE_PROVIDER           = "provider.update";
    private static final String UPDATE_PROVIDER_CONFIG    = "provider.update.config";
    private static final String DELETE_PROVIDER           = "provider.delete";

    private static final String READ_STYLE                = "style.read";
    private static final String READ_STYLE_FROM_ID        = "style.read";
    private static final String READ_STYLE_BODY           = "style.read.body";
    private static final String LIST_STYLES               = "style.list";
    private static final String LIST_STYLES_FROM_DATA     = "style.list.from.data";
    private static final String LIST_STYLES_FROM_PROVIDER = "style.list.from.provider";
    private static final String WRITE_STYLE               = "style.write";
    private static final String UPDATE_STYLE              = "style.update";
    private static final String UPDATE_STYLE_BODY         = "style.update.body";
    private static final String DELETE_STYLE              = "style.delete";

    private static final String READ_DATA                 = "data.read";
    private static final String READ_DATA_FROM_ID         = "data.read.from.id";
    private static final String LIST_DATA                 = "data.list";
    private static final String LIST_DATA_FROM_STYLE      = "data.list.from.style";
    private static final String LIST_DATA_FROM_PROVIDER   = "data.list.from.provider";
    private static final String WRITE_DATA                = "data.write";
    private static final String UPDATE_DATA               = "data.update";
    private static final String DELETE_DATA               = "data.delete";

    private static final String WRITE_STYLED_DATA         = "styled_data.write";
    private static final String DELETE_STYLED_DATA        = "styled_data.delete";

    private static final String READ_SERVICE              = "service.read";
    private static final String READ_SERVICE_FROM_ID      = "service.read.from.id";
    private static final String READ_SERVICES_CONFIG      = "service.read.config";
    private static final String LIST_SERVICES             = "service.list";
    private static final String LIST_SERVICES_FROM_TYPE   = "service.list.from.type";
    private static final String WRITE_SERVICE             = "service.write";
    private static final String UPDATE_SERVICE            = "service.update";
    private static final String UPDATE_SERVICE_CONFIG     = "service.update.config";
    private static final String DELETE_SERVICE            = "service.delete";

    private static final String READ_LAYER                = "layer.read";
    private static final String READ_LAYER_FROM_ID        = "layer.read.from.id";
    private static final String READ_LAYER_CONFIG         = "layer.read.config";
    private static final String LIST_LAYERS               = "layer.list";
    private static final String LIST_LAYERS_FROM_SERVICE  = "layer.list.from.service";
    private static final String WRITE_LAYER               = "layer.write";
    private static final String UPDATE_LAYER              = "layer.update";
    private static final String UPDATE_LAYER_CONFIG       = "layer.update.config";
    private static final String DELETE_LAYER              = "layer.delete";

    private static final String READ_TASK                 = "task.read";
    private static final String LIST_TASKS                = "task.list";
    private static final String LIST_TASKS_FROM_STATE     = "task.list.from.state";
    private static final String WRITE_TASK                = "task.write";
    private static final String UPDATE_TASK               = "task.update";
    private static final String DELETE_TASK               = "task.delete";


    /**
     * Wrapper database {@link Connection} instance.
     */
    private final Connection connect;

    /**
     * Users cache for improved authentication performance.
     */
    private final Map<String, UserRecord> userCache;

    /**
     * Create a new {@link Session} instance.
     *
     * @param connect   the {@link Connection} instance
     * @param userCache a cache for queried users
     */
    public Session(final Connection connect, final Map<String, UserRecord> userCache) {
        this.connect   = connect;
        this.userCache = userCache;
    }

    /**
     * Close the session. {@link Session} instance should not be used after this.
     */
    public void close() {
        try {
            connect.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while closing database connection.", ex);
        }
    }


    /**************************************************************************
     *                              schema queries                            *
     **************************************************************************/

    /**
     * Checks if a schema exists on a database.
     *
     * @param schemaName the schema name to find
     * @return {@code true} if the schema exists, otherwise {@code false}
     * @throws SQLException if an error occurred while executing a SQL statement
     */
    public boolean schemaExists(final String schemaName) throws SQLException {
        ensureNonNull("schemaName", schemaName);
        final ResultSet schemas = connect.getMetaData().getSchemas();
        while (schemas.next()) {
            if (schemaName.equals(schemas.getString(1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs an {@code .sql} script file.
     *
     * @param stream the sql stream
     * @throws IOException if an error occurred while reading the input
     * @throws SQLException if an error occurred while executing a SQL statement
     */
    public void runSql(final InputStream stream) throws IOException, SQLException {
        ensureNonNull("stream", stream);
        new DerbySqlScriptRunner(connect).run(stream);
    }


    /**************************************************************************
     *                         i18n-id sequence query                         *
     **************************************************************************/

    /**
     * Queries the next value for the {@code "admin"."i18n-id"} sequence.
     *
     * @return the generated id
     * @throws SQLException if a database access error occurs
     */
    private Integer nextIdForI18n() throws SQLException {
        return new Query(READ_NEXT_I18N_ID).select().getFirstAt(1, Integer.class);
    }


    /**************************************************************************
     *                        i18n-value table queries                        *
     **************************************************************************/

    /**
     * Queries an internationalized {@link String} value for the specified {@code id}
     * and {@link Locale}.
     *
     * @param id     the i18n id
     * @param locale the locale to query
     * @return the {@link String} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    /* internal */ String readI18n(final int id, final Locale locale) throws SQLException {
        ensureNonNull("locale", locale);
        try {
            final InputStream stream = new Query(READ_I18N).with(id, locale.getLanguage()).select().getClob();
            if (stream != null) {
                return IOUtilities.readString(stream);
            }
        } catch (IOException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected IO error occurred when reading 'java.sql.Clob' instance value.", unexpected);
        }
        return null;
    }

    /**
     * Inserts an internationalized {@link String} value with the specified {@code id}
     * and {@link Locale}.
     *
     * @param id     the i18n id
     * @param locale the locale to apply
     * @param value  the value
     * @throws SQLException if a database access error occurs
     */
    /* internal */ void writeI18n(final int id, final Locale locale, final String value) throws SQLException {
        ensureNonNull("locale", locale);
        new Query(WRITE_I18N).with(id, locale.getLanguage(), new StringReader(value)).update();
    }

    /**
     * Modifies an internationalized {@link String} value for the specified {@code id}
     * and {@link Locale}.
     *
     * @param id       the i18n id
     * @param locale   the locale to apply
     * @param newValue the new value
     * @throws SQLException if a database access error occurs
     */
    /* internal */ void updateI18n(final int id, final Locale locale, final String newValue) throws SQLException {
        ensureNonNull("locale", locale);
        new Query(UPDATE_I18N).with(new StringReader(newValue), id, locale.getLanguage()).update();
    }

    /**
     * Deletes all internationalized {@link String} values for the specified {@code id}.
     *
     * @param id the i18n id
     * @throws SQLException if a database access error occurs
     */
    /* internal */ void deleteI18n(final int id) throws SQLException {
        new Query(DELETE_I18N).with(id).update();
    }


    /**************************************************************************
     *                           user table queries                           *
     **************************************************************************/

    /**
     * Queries a user for the specified {@code login}.
     *
     * @param login the user login
     * @return a {@link UserRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public UserRecord readUser(final String login) throws SQLException {
        ensureNonNull("login", login);
        UserRecord user = null;
        if (userCache != null) {
            user = userCache.get(login);
        }
        if (user == null) {
            user = new Query(READ_USER).with(login).select().getFirst(UserRecord.class);
            if (user != null && userCache != null) {
                userCache.put(login, user);
            }
        }
        return user;
    }

    /**
     * Queries the complete list of registered users.
     *
     * @return a {@link List} of {@link UserRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<UserRecord> readUsers() throws SQLException {
        return new Query(LIST_USERS).select().getAll(UserRecord.class);
    }

    /**
     * Inserts a new user.
     *
     * @param login    the user login
     * @param password the user password
     * @param name     the user name
     * @param roles    the user roles
     * @return the inserted {@link UserRecord} instance
     * @throws SQLException if a database access error occurs
     */
    public UserRecord writeUser(final String login, String password, final String name, final List<String> roles) throws SQLException {
        ensureNonNull("login",    login);
        ensureNonNull("password", password);
        ensureNonNull("name",     name);

        // Prepare insertion.
        password = StringUtilities.MD5encode(password);

        // Proceed to insertion.
        new Query(WRITE_USER).with(login, password, name, StringUtils.join(roles,',')).update();

        // Return inserted line.
        return new UserRecord(this, login, password, name, roles);
    }

    /**
     * Updates the user with specified {@code login}.
     *
     * @param login    the user login
     * @param newPwd   the new user password (already encoded)
     * @param newName  the new user name
     * @param newRoles the new user roles
     * @throws SQLException if a database access error occurs
     */
    /* internal */ void updateUser(final String login, final String newPwd, final String newName, final List<String> newRoles) throws SQLException {
        new Query(UPDATE_USER).with(newPwd, newName, newRoles, login).update();
    }


    /**************************************************************************
     *                        provider table queries                          *
     **************************************************************************/

    /**
     * Queries a provider for the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @return the {@link ProviderRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    /* internal */ ProviderRecord readProvider(final int generatedId) throws SQLException {
        return new Query(READ_PROVIDER_FROM_ID).with(generatedId).select().getFirst(ProviderRecord.class);
    }

    /**
     * Queries a provider for the specified {@code identifier}.
     *
     * @param identifier the provider identifier
     * @return the {@link ProviderRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public ProviderRecord readProvider(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        return new Query(READ_PROVIDER).with(identifier).select().getFirst(ProviderRecord.class);
    }

    /**
     * Queries the configuration of the provider with the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @param descriptor  the descriptor for provider configuration
     * @return the {@link ParameterValueGroup} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ GeneralParameterValue readProviderConfig(final int generatedId, final ParameterDescriptorGroup descriptor) throws SQLException, IOException {
        final InputStream stream = new Query(READ_PROVIDER_CONFIG).with(generatedId).select().getClob();
        return IOUtilities.readParameter(stream, descriptor);
    }

    /**
     * Queries the complete list of registered providers.
     *
     * @return a {@link List} of {@link ProviderRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<ProviderRecord> readProviders() throws SQLException {
        return new Query(LIST_PROVIDERS).select().getAll(ProviderRecord.class);
    }

    /**
     * Queries the list of registered providers for the specified {@link ProviderType}.
     *
     * @param type the provider type to query
     * @return a {@link List} of {@link ProviderRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<ProviderRecord> readProviders(final ProviderType type) throws SQLException {
        ensureNonNull("type", type);
        return new Query(LIST_PROVIDERS_FROM_TYPE).with(type.name()).select().getAll(ProviderRecord.class);
    }

    /**
     * Inserts a new provider.
     *
     * @param identifier the provider identifier
     * @param type       the provider type
     * @param impl       the provider implementation (coverage-file, feature-store...)
     * @param config     the provider configuration
     * @param owner      the provider owner
     * @return the inserted {@link ProviderRecord} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    public ProviderRecord writeProvider(final String identifier, final ProviderType type, final String impl, final GeneralParameterValue config, final UserRecord owner) throws SQLException, IOException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("type",       type);
        ensureNonNull("impl",       impl);
        ensureNonNull("config",     config);

        // Prepare insertion.
        final StringReader reader = new StringReader(IOUtilities.writeParameter(config));
        final String login        = owner != null ? owner.getLogin() : null;

        // Proceed to insertion.
        final int id = new Query(WRITE_PROVIDER).with(identifier, type.name(), impl, reader, login).insert();

        // Return inserted line.
        return new ProviderRecord(this, id, identifier, type, impl, login);
    }

    /**
     * Updates the provider with the specified {@code generatedId}.
     *
     * @param generatedId   the provider auto-generated id
     * @param newIdentifier the new provider identifier
     * @param newType       the new provider type
     * @param newImpl       the new provider implementation (coverage-file, feature-store...)
     * @param newOwner      the new provider owner
     * @throws SQLException
     */
    /* internal */ void updateProvider(final int generatedId, final String newIdentifier, final ProviderType newType, final String newImpl, final String newOwner) throws SQLException {
        new Query(UPDATE_PROVIDER).with(newIdentifier, newType.name(), newImpl, newOwner, generatedId).update();
    }

    /**
     * Updates the configuration of the provider with the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @param newConfig   the new provider configuration
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    /* internal */ void updateProviderConfig(final int generatedId, final GeneralParameterValue newConfig) throws SQLException, IOException {
        final StringReader reader = new StringReader(IOUtilities.writeParameter(newConfig));
        new Query(UPDATE_PROVIDER_CONFIG).with(reader, generatedId).update();
    }

    /**
     * Deletes the provider with the specified {@code identifier}.
     *
     * @param identifier the provider identifier
     * @throws SQLException if a database access error occurs
     */
    public void deleteProvider(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        new Query(DELETE_PROVIDER).with(identifier).update();
    }


    /**************************************************************************
     *                         style table queries                            *
     **************************************************************************/

    /**
     * Queries the style with the specified {@code generatedId}.
     *
     * @param generatedId the style auto-generated id
     * @return the {@link StyleRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    /* internal */ StyleRecord readStyle(final int generatedId) throws SQLException {
        return new Query(READ_STYLE_FROM_ID).with(generatedId).select().getFirst(StyleRecord.class);
    }

    /**
     * Queries the style with the specified {@code name} from the provider with the
     * specified {@code providerId}.
     *
     * @param name       the style name
     * @param providerId the style provider identifier
     * @return the {@link StyleRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public StyleRecord readStyle(final String name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        return new Query(READ_STYLE).with(name, providerId).select().getFirst(StyleRecord.class);
    }

    /**
     * Queries the body of the style with the specified {@code generatedId}.
     *
     * @param generatedId the style auto-generated id
     * @return the {@link InputStream} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the body cannot be read
     */
    /* internal */ InputStream readStyleBody(final int generatedId) throws SQLException {
        return new Query(READ_STYLE_BODY).with(generatedId).select().getClob();
    }

    /**
     * Queries the complete list of registered styles.
     *
     * @return a {@link List} of {@link ProviderRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<StyleRecord> readStyles() throws SQLException {
        return new Query(LIST_STYLES).select().getAll(StyleRecord.class);
    }

    /**
     * Queries the list of registered styles related to the specified {@link DataRecord}.
     *
     * @param data the {@link DataRecord} instance
     * @return a {@link List} of {@link StyleRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<StyleRecord> readStyles(final DataRecord data) throws SQLException {
        ensureNonNull("data", data);
        return new Query(LIST_STYLES_FROM_DATA).with(data.id).select().getAll(StyleRecord.class);
    }

    /**
     * Queries the list of registered styles related to the specified {@link ProviderRecord}.
     *
     * @param provider the {@link ProviderRecord} instance
     * @return a {@link List} of {@link StyleRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<StyleRecord> readStyles(final ProviderRecord provider) throws SQLException {
        ensureNonNull("provider", provider);
        return new Query(LIST_STYLES_FROM_PROVIDER).with(provider.id).select().getAll(StyleRecord.class);
    }

    /**
     * Inserts a new style.
     *
     * @param name     the style name
     * @param provider the style type
     * @param type     the style type
     * @param body     the style body
     * @param owner    the style owner
     * @return the inserted {@link ProviderRecord} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the body cannot be written
     */
    public StyleRecord writeStyle(final String name, final ProviderRecord provider, final StyleType type, final MutableStyle body, final UserRecord owner) throws SQLException, IOException {
        ensureNonNull("name",     name);
        ensureNonNull("provider", provider);
        ensureNonNull("type",     type);
        ensureNonNull("body",     body);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final StringReader reader = new StringReader(IOUtilities.writeStyle(body));
        final String login        = owner != null ? owner.getLogin() : null;

        // Proceed to insertion.
        final int id = new Query(WRITE_STYLE).with(name, provider.id, type.name(), date.getTime(), title, description, reader, login).insert();

        // Return inserted line.
        return new StyleRecord(this, id, name, provider.id, type, date, title, description, login);
    }

    /**
     * Updates the style with the specified {@code generatedId}.
     *
     * @param generatedId the style auto-generated id
     * @param newName     the new style identifier
     * @param newProvider the new provider
     * @param newType     the new style type
     * @param newOwner    the new style owner
     * @throws SQLException if a database access error occurs
     */
    /* internal */ void updateStyle(final int generatedId, final String newName, final int newProvider, final StyleType newType, final String newOwner) throws SQLException {
        new Query(UPDATE_STYLE).with(newName, newProvider, newType.name(), newOwner, generatedId).update();
    }

    /**
     * Updates the body of the style with the specified {@code generatedId}.
     *
     * @param generatedId the style auto-generated id
     * @param newBody     the new style body
     * @throws SQLException if a database access error occurs
     * @throws IOException if the body cannot be written
     */
    /* internal */ void updateStyleBody(final int generatedId, final MutableStyle newBody) throws SQLException, IOException {
        final StringReader reader = new StringReader(IOUtilities.writeStyle(newBody));
        new Query(UPDATE_STYLE_BODY).with(reader, generatedId).update();
    }

    /**
     * Deletes the style with the specified {@code name} from the provider with the
     * specified {@code providerId}.
     *
     * @param name       the style name
     * @param providerId the style provider identifier
     * @throws SQLException if a database access error occurs
     */
    public void deleteStyle(final String name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        new Query(DELETE_STYLE).with(name, providerId).update();
    }


    /**************************************************************************
     *                          data table queries                            *
     **************************************************************************/

    /* internal */ DataRecord readData(final int generatedId) throws SQLException {
        return new Query(READ_DATA_FROM_ID).with(generatedId).select().getFirst(DataRecord.class);
    }

    public DataRecord readData(final String name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        return new Query(READ_DATA).with(name, providerId).select().getFirst(DataRecord.class);
    }

    public List<DataRecord> readData() throws SQLException {
        return new Query(LIST_DATA).select().getAll(DataRecord.class);
    }

    public List<DataRecord> readData(final StyleRecord style) throws SQLException {
        ensureNonNull("style", style);
        return new Query(LIST_DATA_FROM_STYLE).with(style.id).select().getAll(DataRecord.class);
    }

    public List<DataRecord> readData(final ProviderRecord provider) throws SQLException {
        ensureNonNull("provider", provider);
        return new Query(LIST_DATA_FROM_PROVIDER).with(provider.id).select().getAll(DataRecord.class);
    }

    public DataRecord writeData(final String name, final ProviderRecord provider, final DataType type, final UserRecord owner) throws SQLException {
        ensureNonNull("name",     name);
        ensureNonNull("provider", provider);
        ensureNonNull("type",     type);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner != null ? owner.getLogin() : null;

        // Proceed to insertion.
        final int id = new Query(WRITE_DATA).with(name, provider.id, type.name(), date.getTime(), title, description, login).insert();

        // Return inserted line.
        return new DataRecord(this, id, name, provider.id, type, date, title, description, login);
    }

    /* internal */ void updateData(final int generatedId, final String newName, final int newProvider, final DataType newType, final String newOwner) throws SQLException {
        new Query(UPDATE_DATA).with(newName, newProvider, newType.name(), newOwner, generatedId).update();
    }

    public void deleteData(final String name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        new Query(DELETE_DATA).with(name, providerId).update();
    }


    /**************************************************************************
     *                      styled-data table queries                         *
     **************************************************************************/

    public void writeStyledData(final StyleRecord style, final DataRecord data) throws SQLException {
        ensureNonNull("style", style);
        ensureNonNull("data",  data);
        new Query(WRITE_STYLED_DATA).with(style.id, data.id).update();
    }

    public void deleteStyledData(final StyleRecord style, final DataRecord data) throws SQLException {
        ensureNonNull("style", style);
        ensureNonNull("data",  data);
        new Query(DELETE_STYLED_DATA).with(style.id, data.id).update();
    }


    /**************************************************************************
     *                          service table queries                         *
     **************************************************************************/

    /* internal */ ServiceRecord readService(final int generatedId) throws SQLException {
        return new Query(READ_SERVICE_FROM_ID).with(generatedId).select().getFirst(ServiceRecord.class);
    }

    public ServiceRecord readService(final String identifier, final Specification spec) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);
        return new Query(READ_SERVICE).with(identifier, spec.name()).select().getFirst(ServiceRecord.class);
    }

    /* internal */ InputStream readServiceConfig(final int generatedId) throws SQLException {
        return new Query(READ_SERVICES_CONFIG).with(generatedId).select().getClob();
    }

    public List<ServiceRecord> readServices() throws SQLException {
        return new Query(LIST_SERVICES).select().getAll(ServiceRecord.class);
    }

    public List<ServiceRecord> readServices(final Specification spec) throws SQLException {
        return new Query(LIST_SERVICES_FROM_TYPE).with(spec.name()).select().getAll(ServiceRecord.class);
    }

    public ServiceRecord writeService(final String identifier, final Specification spec, final Object config, final UserRecord owner) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner != null ? owner.getLogin() : null;

        // Proceed to insertion.
        final int id = new Query(WRITE_SERVICE).with(identifier, spec.name(), date.getTime(), title, description, config, login).insert();

        // Return inserted line.
        return new ServiceRecord(this, id, identifier, spec, date, title, description, login);
    }

    /* internal */ void updateService(final int generatedId, final String newIdentifier, final Specification newType, final String newOwner) throws SQLException {
        new Query(UPDATE_SERVICE).with(newIdentifier, newType.name(), newOwner, generatedId).update();
    }

    /* internal */ void updateServiceConfig(final int generatedId, final StringReader newConfig) throws SQLException {
        new Query(UPDATE_SERVICE_CONFIG).with(newConfig, generatedId).update();
    }

    public void deleteService(final String identifier, final Specification spec) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);
        new Query(DELETE_SERVICE).with(identifier, spec.name()).update();
    }


    /**************************************************************************
     *                           layer table queries                          *
     **************************************************************************/

    /* internal */ LayerRecord readLayer(final int generatedId) throws SQLException {
        return new Query(READ_LAYER_FROM_ID).with(generatedId).select().getFirst(LayerRecord.class);
    }

    public LayerRecord readLayer(final String alias, final ServiceRecord service) throws SQLException {
        ensureNonNull("alias",   alias);
        ensureNonNull("service", service);
        return new Query(READ_LAYER).with(alias, service.id).select().getFirst(LayerRecord.class);
    }

    /* internal */ InputStream readLayerConfig(final int generatedId) throws SQLException {
        return new Query(READ_LAYER_CONFIG).with(generatedId).select().getClob();
    }

    public List<LayerRecord> readLayers() throws SQLException {
        return new Query(LIST_LAYERS).select().getAll(LayerRecord.class);
    }

    public List<LayerRecord> readLayers(final ServiceRecord service) throws SQLException {
        ensureNonNull("service", service);
        return new Query(LIST_LAYERS_FROM_SERVICE).with(service.id).select().getAll(LayerRecord.class);
    }

    public LayerRecord writeLayer(final String alias, final ServiceRecord service, final DataRecord data, final Object config, final UserRecord owner) throws SQLException {
        ensureNonNull("alias",   alias);
        ensureNonNull("service", service);
        ensureNonNull("data",    data);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner != null ? owner.getLogin() : null;

        // Proceed to insertion.
        final int id = new Query(WRITE_LAYER).with(alias, service.id, data.id, date.getTime(), title, description, config, login).insert();

        // Return inserted line.
        return new LayerRecord(this, id, alias, service.id, data.id, date, title, description, login);
    }

    /* internal */ void updateLayer(final int generatedId, final String newAlias, final int newService, final int newData, final String newOwner) throws SQLException {
        new Query(UPDATE_LAYER).with(newAlias, newService, newData, newOwner, generatedId).update();
    }

    /* internal */ void updateLayerConfig(final int generatedId, final StringReader newConfig) throws SQLException {
        new Query(UPDATE_LAYER_CONFIG).with(newConfig).update();
    }

    public void deleteLayer(final String alias, final ServiceRecord service) throws SQLException {
        ensureNonNull("alias",   alias);
        ensureNonNull("service", service);
        new Query(DELETE_LAYER).with(alias, service.id).update();
    }


    /**************************************************************************
     *                          task table queries                            *
     **************************************************************************/

    public TaskRecord readTask(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        return new Query(READ_TASK).with(identifier).select().getFirst(TaskRecord.class);
    }

    public List<TaskRecord> readTasks() throws SQLException {
        return new Query(LIST_TASKS).select().getAll(TaskRecord.class);
    }

    public TaskRecord readTasks(final TaskState state) throws SQLException {
        ensureNonNull("state", state);
        return new Query(LIST_TASKS_FROM_STATE).with(state.name()).select().getFirst(TaskRecord.class);
    }

    public TaskRecord writeTask(final String identifier, final String type, final String owner) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("type",       type);

        // Prepare insertion.
        final TaskState state = TaskState.PENDING;
        final Integer title   = nextIdForI18n();
        final int description = nextIdForI18n();
        final Date start      = new Date();

        // Proceed to insertion.
        new Query(WRITE_TASK).with(identifier, state.name(), type, title, description, start, owner).insert();

        // Return inserted line.
        return new TaskRecord(this, identifier, state, type, title, description, start, null, owner);
    }

    /* internal */ void updateTask(final String identifier, final TaskState newState) throws SQLException {
        new Query(UPDATE_TASK).with(newState.name(), new Date().getTime(), identifier).update();
    }

    public void deleteTask(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        new Query(DELETE_TASK).with(identifier).update();
    }


    /**************************************************************************
     *                                 engine                                 *
     **************************************************************************/

    private final class Query {

        private PreparedStatement stmt;

        Query(final String key) throws SQLException {
            stmt = connect.prepareStatement(QUERIES.getProperty(key), Statement.RETURN_GENERATED_KEYS);
        }

        Query with(final Object... args) throws SQLException {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    stmt.setString(i + 1, (String) args[i]);
                } else if (args[i] instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) args[i]);
                } else if (args[i] instanceof Double) {
                    stmt.setDouble(i + 1, (Double) args[i]);
                } else if (args[i] instanceof Float) {
                    stmt.setFloat(i + 1, (Float) args[i]);
                } else if (args[i] instanceof Long) {
                    stmt.setLong(i + 1, (Long) args[i]);
                } else if (args[i] instanceof StringReader) {
                    stmt.setClob(i + 1, (StringReader) args[i]);
                } else {
                    stmt.setObject(i + 1, args[i]);
                }
            }
            return this;
        }

        Result select() throws SQLException {
            return new Result(stmt);
        }

        void update() throws SQLException {
            try {
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }
        }

        int insert() throws SQLException {
            ResultSet rs = null;
            try {
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } finally {
                if (rs != null) rs.close();
                stmt.close();
            }
            throw new SQLException("There is no generated key to return.");
        }
    }

    private final class Result {

        final Statement stmt;
        final ResultSet rs;

        Result(final PreparedStatement stmt) throws SQLException {
            this.stmt = stmt;
            this.rs   = stmt.executeQuery();
        }

        <T> T getFirst(final Class<? extends Record> type) throws SQLException {
            try {
                if (rs.next()) {
                    try {
                        return createRecord(rs, type);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "An error occurred while creating a " + type.getCanonicalName() + " instance from ResultSet.");
                    }
                }
            } finally {
                rs.close();
                stmt.close();
            }
            return null;
        }

        <T> T getFirstAt(final int columnIndex, final Class<T> type) throws SQLException {
            try {
                if (rs.next()) {
                    return rs.getObject(columnIndex, type);
                }
            } finally {
                rs.close();
                stmt.close();
            }
            return null;
        }

        <T> List<T> getAll(final Class<? extends Record> type) throws SQLException {
            final List<T> list = new ArrayList<>();
            try {
                while (rs.next()) {
                    try {
                        list.add((T) createRecord(rs, type));
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "An error occurred while creating a " + type.getCanonicalName() + " instance from ResultSet.");
                    }
                }
            } finally {
                rs.close();
                stmt.close();
            }
            return list;
        }

        InputStream getClob() throws SQLException {
            try {
                if (rs.next()) {
                    return rs.getClob(1).getAsciiStream();
                }
            } finally {
                rs.close();
                stmt.close();
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createRecord(final ResultSet rs, final Class<? extends Record> type) throws Exception {
        return (T) type.getConstructor(Session.class, ResultSet.class).newInstance(this, rs);
    }
}
