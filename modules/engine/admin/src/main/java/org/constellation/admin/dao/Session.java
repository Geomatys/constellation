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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.constellation.admin.dao.StyleRecord.StyleType;
import org.constellation.admin.dao.TaskRecord.TaskState;
import org.constellation.admin.util.IOUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * Session for administration database operations
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Session implements Closeable {

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
            @Override
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

    private static final String READ_NEXT_I18N_ID           = "i18n_id.read.next";

    private static final String READ_I18N                   = "i18n.read";
    private static final String WRITE_I18N                  = "i18n.write";
    private static final String UPDATE_I18N                 = "i18n.update";
    private static final String DELETE_I18N                 = "i18n.delete";

    private static final String READ_USER                   = "user.read";
    private static final String LIST_USERS                  = "user.list";
    private static final String WRITE_USER                  = "user.write";
    private static final String UPDATE_USER                 = "user.update";

    private static final String READ_PROVIDER               = "provider.read";
    private static final String READ_PROVIDER_FROM_ID       = "provider.read.from.id";
    private static final String READ_PROVIDER_CONFIG        = "provider.read.config";
    private static final String READ_PROVIDER_METADATA      = "provider.read.metadata";
    private static final String EXIST_PROVIDER_METADATA     = "provider.exist.metadata";
    private static final String SEARCH_PROVIDER_METADATA    = "provider.search.metadata";
    private static final String LIST_PROVIDERS              = "provider.list";
    private static final String LIST_PROVIDERS_FROM_TYPE    = "provider.list.from.type";
    private static final String LIST_PROVIDERS_FROM_IMPL    = "provider.list.from.impl";
    private static final String LIST_PROVIDERS_FROM_PARENT  = "provider.list.from.parent";
    private static final String WRITE_PROVIDER              = "provider.write";
    private static final String UPDATE_PROVIDER             = "provider.update";
    private static final String UPDATE_PROVIDER_CONFIG      = "provider.update.config";
    private static final String UPDATE_PROVIDER_METADATA    = "provider.update.metadata";
    private static final String DELETE_PROVIDER             = "provider.delete";
    
    private static final String READ_SENSOR                 = "sensor.read";
    private static final String READ_SENSOR_FROM_ID         = "sensor.read.from.id";
    private static final String READ_SENSOR_METADATA        = "sensor.read.metadata";
    private static final String EXIST_SENSOR_METADATA       = "sensor.exist.metadata";
    private static final String LIST_SENSORS                = "sensor.list";
    private static final String LIST_SENSORS_FROM_PARENT    = "sensor.list.from.parent";
    private static final String WRITE_SENSOR                = "sensor.write";
    private static final String UPDATE_SENSOR               = "sensor.update";
    private static final String UPDATE_SENSOR_METADATA      = "sensor.update.metadata";
    private static final String DELETE_SENSOR               = "sensor.delete";

    private static final String READ_STYLE                  = "style.read";
    private static final String READ_STYLE_FROM_ID          = "style.read";
    private static final String READ_STYLE_BODY             = "style.read.body";
    private static final String LIST_STYLES                 = "style.list";
    private static final String LIST_STYLES_FROM_DATA       = "style.list.from.data";
    private static final String LIST_STYLES_FROM_PROVIDER   = "style.list.from.provider";
    private static final String WRITE_STYLE                 = "style.write";
    private static final String UPDATE_STYLE                = "style.update";
    private static final String UPDATE_STYLE_BODY           = "style.update.body";
    private static final String DELETE_STYLE                = "style.delete";

    private static final String READ_DATA                   = "data.read";
    private static final String READ_DATA_NMSP              = "data.read.nmsp";
    private static final String READ_DATA_METADATA          = "data.read.metadata";
    private static final String READ_DATA_ISO_METADATA      = "data.read.iso_metadata";
    private static final String READ_DATA_FROM_ID           = "data.read.from.id";
    private static final String READ_DATA_FROM_LAYER        = "data.read.from.layer";
    private static final String LIST_DATA                   = "data.list";
    private static final String LIST_DATA_FROM_STYLE        = "data.list.from.style";
    private static final String LIST_DATA_FROM_PROVIDER     = "data.list.from.provider";
    private static final String WRITE_DATA                  = "data.write";
    private static final String UPDATE_DATA                 = "data.update";
    private static final String UPDATE_DATA_METADATA        = "data.update.metadata";
    private static final String UPDATE_DATA_ISO_METADATA    = "data.update.iso_metadata";
    private static final String UPDATE_DATA_VISIBLE         = "data.update.visible";
    private static final String DELETE_DATA                 = "data.delete";
    private static final String DELETE_DATA_NMSP            = "data.delete.nmsp";
    private static final String SEARCH_DATA_ISO_METADATA    = "data.search.iso_metadata";

    private static final String WRITE_STYLED_DATA           = "styled_data.write";
    private static final String DELETE_STYLED_DATA          = "styled_data.delete";

    private static final String READ_SERVICE                = "service.read";
    private static final String READ_SERVICE_FROM_ID        = "service.read.from.id";
    private static final String READ_SERVICES_CONFIG        = "service.read.config";
    private static final String READ_SERVICES_EXTRA_CONFIG  = "service.read.extra.config";
    private static final String READ_SERVICES_METADATA      = "service.read.metadata";
    private static final String READ_SERVICES_ISO_METADATA  = "service.read.iso_metadata";
    private static final String LIST_SERVICES               = "service.list";
    private static final String LIST_SERVICES_FROM_TYPE     = "service.list.from.type";
    private static final String LIST_SERVICES_FROM_DATA     = "service.list.from.data";
    private static final String WRITE_SERVICE               = "service.write";
    private static final String WRITE_SERVICE_EXTRA_CONFIG  = "service.write.extra.config";
    private static final String WRITE_SERVICE_METADATA      = "service.write.metadata";
    private static final String UPDATE_SERVICE              = "service.update";
    private static final String UPDATE_SERVICE_CONFIG       = "service.update.config";
    private static final String UPDATE_SERVICE_EXTRA_CONFIG = "service.update.extra.config";
    private static final String UPDATE_SERVICE_METADATA     = "service.update.metadata";
    private static final String UPDATE_SERVICE_ISO_METADATA = "service.update.iso_metadata";
    private static final String DELETE_SERVICE              = "service.delete";
    private static final String DELETE_SERVICE_METADATA     = "service.delete.metadata";
    private static final String DELETE_SERVICE_EXTRA_CONFIG = "service.delete.extra.config";
    private static final String SEARCH_SERVICE_ISO_METADATA = "service.search.iso_metadata";

    private static final String READ_LAYER                  = "layer.read";
    private static final String READ_LAYER_FROM_ID          = "layer.read.from.id";
    private static final String READ_LAYER_CONFIG           = "layer.read.config";
    private static final String LIST_LAYERS                 = "layer.list";
    private static final String LIST_LAYERS_FROM_SERVICE    = "layer.list.from.service";
    private static final String WRITE_LAYER                 = "layer.write";
    private static final String UPDATE_LAYER                = "layer.update";
    private static final String UPDATE_LAYER_CONFIG         = "layer.update.config";
    private static final String DELETE_LAYER                = "layer.delete";
    private static final String DELETE_LAYER_NMSP           = "layer.delete.nmsp";
    private static final String DELETE_SERVICE_LAYER        = "layer.service.delete";

    private static final String READ_TASK                   = "task.read";
    private static final String LIST_TASKS                  = "task.list";
    private static final String LIST_TASKS_FROM_STATE       = "task.list.from.state";
    private static final String WRITE_TASK                  = "task.write";
    private static final String UPDATE_TASK                 = "task.update";
    private static final String DELETE_TASK                 = "task.delete";

    private static final String READ_PROPERTY               = "properties.read";
    private static final String WRITE_PROPERTY              = "properties.write";
    private static final String UPDATE_PROPERTY             = "properties.update";

    private static final String READ_CRS                    = "crs.read";
    private static final String LIST_CRS                    = "crs.list";
    private static final String WRITE_CRS                   = "crs.write";
    private static final String UPDATE_CRS                  = "crs.update";
    private static final String DELETE_CRS                  = "crs.delete";

    /**
     * Wrapper database {@link Connection} instance.
     */
    private final Connection connect;


    /**
     * Create a new {@link Session} instance.
     *
     * @param connect   the {@link Connection} instance
     * @param userCache a cache for queried users
     */
    public Session(final Connection connect) {
        this.connect   = connect;
    }

    /**
     * Close the session. {@link Session} instance should not be used after this.
     */
    @Override
    public void close() {
        try {
            connect.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while closing database connection.", ex);
        }
    }

    public boolean isClosed() {
        try {
            return connect.isClosed();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while closing database connection.", ex);
        }
        return false;
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
            final InputStream stream = new Query(READ_I18N).with(id, locale.toString()).select().getClob();
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

        new Query(WRITE_I18N).with(id, locale.toString(), new StringReader(value)).update();
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
        new Query(UPDATE_I18N).with(new StringReader(newValue), id, locale.toString()).update();
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

    public Record searchMetadata(final String metadataId) throws SQLException {
        final Result rs = new Query(SEARCH_PROVIDER_METADATA).with(metadataId).select();
        if (rs.rs.next()) {
            final int id = rs.rs.getInt(1);
            return readProvider(id);
        }
        
        final Result rs2 = new Query(SEARCH_DATA_ISO_METADATA).with(metadataId).select();
        if (rs2.rs.next()) {
            final int id = rs2.rs.getInt(1);
            return readData(id);
        }
        
        final Result rs3 = new Query(SEARCH_SERVICE_ISO_METADATA).with(metadataId).select();
        if (rs3.rs.next()) {
            final int id = rs3.rs.getInt(1);
            return readService(id);
        }
        return null;
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
    /* internal */ void updateUser(final String login, final String newPwd, final String newName) throws SQLException {
        new Query(UPDATE_USER).with(newPwd, newName, login).update();
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
    /* internal */ GeneralParameterValue readProviderConfig(final int generatedId, final GeneralParameterDescriptor descriptor) throws SQLException, IOException {
        final InputStream stream = new Query(READ_PROVIDER_CONFIG).with(generatedId).select().getClob();
        return IOUtilities.readParameter(stream, descriptor);
    }
    /**
     * Queries the metadata of the provider with the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @return the {@link ParameterValueGroup} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ InputStream readProviderMetadata(final int generatedId) throws SQLException, IOException {
        final InputStream stream = new Query(READ_PROVIDER_METADATA).with(generatedId).select().getClob();
        return stream;
    }

    /**
     * look for existence of the provider metadata with the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @return the {@link ParameterValueGroup} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ boolean hasProviderMetadata(final int generatedId) throws SQLException, IOException {
        return new Query(EXIST_PROVIDER_METADATA).with(generatedId).select().hasNext();
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
     * Queries the list of registered providers for the specified implementation.
     *
     * @param implementation the provider implementation to query
     * @return a {@link List} of {@link ProviderRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<ProviderRecord> readProviders(final String implementation) throws SQLException {
        ensureNonNull("implementation", implementation);
        return new Query(LIST_PROVIDERS_FROM_IMPL).with(implementation).select().getAll(ProviderRecord.class);
    }
    
    /**
     * Queries the list of registered providers for the specified parent provider.
     *
     * @param parentIdentifier the provider parent identifier
     * @return a {@link List} of {@link ProviderRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<ProviderRecord> readProvidersFromParent(final String parentIdentifier) throws SQLException {
        ensureNonNull("parentIdentifier", parentIdentifier);
        return new Query(LIST_PROVIDERS_FROM_PARENT).with(parentIdentifier).select().getAll(ProviderRecord.class);
    }

    /**
     * Inserts a new provider.
     *
     * @param identifier the provider identifier
     * @param parent the provider parent identifier, can be null
     * @param type       the provider type
     * @param impl       the provider implementation (coverage-file, feature-store...)
     * @param config     the provider configuration
     * @param owner      the provider owner
     * @return the inserted {@link ProviderRecord} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    public ProviderRecord writeProvider(final String identifier, String parent, 
            final ProviderType type, final String impl, final GeneralParameterValue config, 
            final String owner) throws SQLException, IOException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("type",       type);
        ensureNonNull("impl",       impl);
        ensureNonNull("config",     config);
        if(parent==null) parent="";

        // Prepare insertion.
        final StringReader reader = new StringReader(IOUtilities.writeParameter(config));
        final String login        = owner;

        // Proceed to insertion.
        final int id = new Query(WRITE_PROVIDER).with(identifier, parent, type.name(), impl, reader, login).insert();

        // Return inserted line.
        return new ProviderRecord(this, id, identifier, parent, type, impl, login, null);
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
    /* internal */ void updateProvider(final int generatedId, final String newIdentifier, String parent,
            final ProviderType newType, final String newImpl, final String newOwner) throws SQLException {
        if(parent==null) parent="";
        new Query(UPDATE_PROVIDER).with(newIdentifier, parent, newType.name(), newImpl, newOwner, generatedId).update();
    }

    /**
     * Updates an existing provider using the new given one.
     *
     * @param updatedProvider An existing provider which contains updated fields.
     * @throws SQLException
     */
    public void updateProvider(final ProviderRecord updatedProvider) throws SQLException {
        new Query(UPDATE_PROVIDER).with(updatedProvider.getIdentifier(), updatedProvider.getParentIdentifier(), updatedProvider.getType().name(),
                updatedProvider.getImpl(), updatedProvider.getOwnerLogin(), updatedProvider.id).update();
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
     * Updates provider metadata with the specified {@code generatedId}.
     *
     * @param generatedId the provider auto-generated id
     * @param metadata   the metadata provider
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    /* internal */ void updateProviderMetadata(final int generatedId, final String metadataId, final StringReader metadata) throws SQLException, IOException {
        new Query(UPDATE_PROVIDER_METADATA).with(metadataId, metadata, generatedId).update();
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
     *                        sensor table queries                          *
     **************************************************************************/

    /**
     * Queries a sensor for the specified {@code generatedId}.
     *
     * @param generatedId the sensor auto-generated id
     * @return the {@link SensorRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    /* internal */ SensorRecord readSensor(final int generatedId) throws SQLException {
        return new Query(READ_SENSOR_FROM_ID).with(generatedId).select().getFirst(SensorRecord.class);
    }

    /**
     * Queries a sensor for the specified {@code identifier}.
     *
     * @param identifier the sensor identifier
     * @return the {@link SensorRecord} instance or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public SensorRecord readSensor(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        return new Query(READ_SENSOR).with(identifier).select().getFirst(SensorRecord.class);
    }

    /**
     * Queries the metadata of the sensor with the specified {@code generatedId}.
     *
     * @param generatedId the sensor auto-generated id
     * @return the {@link ParameterValueGroup} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ InputStream readSensorMetadata(final int generatedId) throws SQLException, IOException {
        final InputStream stream = new Query(READ_SENSOR_METADATA).with(generatedId).select().getClob();
        return stream;
    }

    /**
     * look for existence of the sensor metadata with the specified {@code generatedId}.
     *
     * @param generatedId the sensor auto-generated id
     * @return the {@link ParameterValueGroup} instance
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ boolean hasSensorMetadata(final int generatedId) throws SQLException, IOException {
        return new Query(EXIST_SENSOR_METADATA).with(generatedId).select().hasNext();
    }
    /**
     * Queries the complete list of registered sensors.
     *
     * @return a {@link List} of {@link SensorRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<SensorRecord> readSensors() throws SQLException {
        return new Query(LIST_SENSORS).select().getAll(SensorRecord.class);
    }

    /**
     * Queries the list of registered providers for the specified parent sensor.
     *
     * @param parentIdentifier the sensor parent identifier
     * @return a {@link List} of {@link SensorRecord}s
     * @throws SQLException if a database access error occurs
     */
    public List<SensorRecord> readSensorsFromParent(final String parentIdentifier) throws SQLException {
        ensureNonNull("parentIdentifier", parentIdentifier);
        return new Query(LIST_SENSORS_FROM_PARENT).with(parentIdentifier).select().getAll(SensorRecord.class);
    }

    /**
     * Inserts a new sensor.
     *
     * @param identifier The sensor identifier.
     * @param parent The sensor parent identifier, can be null.
     * @param owner The sensor owner.
     * @return The inserted {@link SensorRecord} instance.
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    public SensorRecord writeSensor(final String identifier, String type, String parent,
            final String owner) throws SQLException, IOException {
        ensureNonNull("identifier", identifier);
        if(parent==null) parent="";

        // Prepare insertion.
        final String login        = owner;

        // Proceed to insertion.
        final int id = new Query(WRITE_SENSOR).with(identifier, type, parent, login).insert();

        // Return inserted line.
        return new SensorRecord(this, id, identifier, type, parent, login);
    }

    /**
     * Updates the sensor with the specified {@code generatedId}.
     *
     * @param generatedId   the sensor auto-generated id
     * @param newIdentifier the new sensor identifier
     * @param newOwner      the new provider owner
     * @throws SQLException
     */
    /* internal */ void updateSensor(final int generatedId, final String newIdentifier, final String type, 
            String parent, final String newOwner) throws SQLException {
        if(parent==null) parent="";
        new Query(UPDATE_SENSOR).with(newIdentifier, type, parent, newOwner, generatedId).update();
    }

    /**
     * Updates an existing sensor using the new given one.
     *
     * @param updatedSensor An existing sensor which contains updated fields.
     * @throws SQLException
     */
    public void updateSensor(final SensorRecord updatedSensor) throws SQLException {
        new Query(UPDATE_SENSOR).with(updatedSensor.getIdentifier(), updatedSensor.getType(), updatedSensor.getParentIdentifier(),
                updatedSensor.getOwnerLogin(), updatedSensor.id).update();
    }

    /**
     * Updates sensor metadata with the specified {@code generatedId}.
     *
     * @param generatedId the sensor auto-generated id
     * @param metadata   the metadata sensor
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be written
     */
    /* internal */ void updateSensorMetadata(final int generatedId, final StringReader metadata) throws SQLException, IOException {
        new Query(UPDATE_SENSOR_METADATA).with(metadata, generatedId).update();
    }

    /**
     * Deletes the sensor with the specified {@code identifier}.
     *
     * @param identifier the sensor identifier
     * @throws SQLException if a database access error occurs
     */
    public void deleteSensor(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        new Query(DELETE_SENSOR).with(identifier).update();
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
    public StyleRecord writeStyle(final String name, final ProviderRecord provider, final StyleType type, final MutableStyle body, final String owner) throws SQLException, IOException {
        ensureNonNull("name",     name);
        ensureNonNull("provider", provider);
        ensureNonNull("type",     type);
        ensureNonNull("body",     body);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final StringReader reader = new StringReader(IOUtilities.writeStyle(body));
        final String login        = owner;

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

    public DataRecord readDatafromLayer(final String layerAlias, final String providerId) throws SQLException {
        ensureNonNull("layerAlias", layerAlias);
        ensureNonNull("providerId", providerId);
        return new Query(READ_DATA_FROM_LAYER).with(layerAlias, providerId).select().getFirst(DataRecord.class);
    }

    public DataRecord readData(final QName name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        if (name.getNamespaceURI() != null) {
            return new Query(READ_DATA_NMSP).with(name.getLocalPart(), name.getNamespaceURI(), providerId).select().getFirst(DataRecord.class);
        } else {
            return new Query(READ_DATA).with(name.getLocalPart(), providerId).select().getFirst(DataRecord.class);
        }
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

    /* internal */ InputStream readDataMetadata(final int dataid) throws SQLException, IOException {
        final InputStream stream = new Query(READ_DATA_METADATA).with(dataid).select().getClob();
        return stream;
    }
    
    /* internal */ InputStream readDataIsoMetadata(final int dataid) throws SQLException, IOException {
        final InputStream stream = new Query(READ_DATA_ISO_METADATA).with(dataid).select().getClob();
        return stream;
    }

    public DataRecord writeData(final QName name, final ProviderRecord provider, final DataType type, final String owner) throws SQLException {
        ensureNonNull("name",     name);
        ensureNonNull("provider", provider);
        ensureNonNull("type",     type);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner;
        // Proceed to insertion.
        final int id = new Query(WRITE_DATA).with(name.getLocalPart(), name.getNamespaceURI(), provider.id, type.name(), date.getTime(), title, description, login).insert();

        // Return inserted line.
        return new DataRecord(this, id, name.getLocalPart(), name.getNamespaceURI(), provider.id, type, true, date, title, description, login, null);
    }

    /* internal */ void updateData(final int generatedId, final String newName, final String newNamespace, final int newProvider, final DataType newType, final String newOwner) throws SQLException {
        new Query(UPDATE_DATA).with(newName, newNamespace, newProvider, newType.name(), newOwner, generatedId).update();
    }

    /* internal */ void updateDataMetadata(final int dataId, final StringReader metadata) throws SQLException {
        new Query(UPDATE_DATA_METADATA).with(metadata, dataId).update();
    }
    
    /* internal */ void updateDataIsoMetadata(final int dataId, final String metadataId, final StringReader metadata) throws SQLException {
        new Query(UPDATE_DATA_ISO_METADATA).with(metadataId, metadata, dataId).update();
    }

    /* internal */ void updateDataVisibility(final int dataId, final boolean visible) throws SQLException {
        new Query(UPDATE_DATA_VISIBLE).with(visible, dataId).update();
    }

    public void deleteData(final QName name, final String providerId) throws SQLException {
        ensureNonNull("name",       name);
        ensureNonNull("providerId", providerId);
        if (name.getNamespaceURI() != null) {
            new Query(DELETE_DATA_NMSP).with(name.getLocalPart(), name.getNamespaceURI(), providerId).update();
        } else {
            new Query(DELETE_DATA).with(name.getLocalPart(), providerId).update();
        }
    }
    
    /**
     * look for existence of the data metadata with the specified {@code generatedId}.
     *
     * @param generatedId the data auto-generated id
     * @return true if an iso metadata exist for this service.
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ boolean hasDataIsoMetadata(final int generatedId) throws SQLException, IOException {
        return readDataIsoMetadata(generatedId) != null;
    }


    /**************************************************************************
     *                      crs-data table queries                            *
     **************************************************************************/
    public void writeCRSData(final DataRecord record, final String crsCode) throws SQLException {
        ensureNonNull("crscode", crsCode);
        ensureNonNull("data",  record);
        new Query(WRITE_CRS).with(record.id, crsCode).update();
    }

    public void updateCRSData(final DataRecord record) throws SQLException {
        ensureNonNull("data",  record);
        new Query(UPDATE_CRS).with(record.id).update();
    }

    public void deleteCRSData(final DataRecord record) throws SQLException {
        ensureNonNull("data",  record);
        new Query(DELETE_CRS).with(record.id).update();
    }

    public CRSRecord readCRSData(final DataRecord record) throws SQLException{
        ensureNonNull("data",  record);
        return new Query(READ_CRS).with(record.id).select().getFirst(CRSRecord.class);
    }

    public List<CRSRecord> listCRSData()  throws SQLException{
        return new Query(LIST_CRS).select().getAll(CRSRecord.class);
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

    /* internal */ InputStream readExtraServiceConfig(final int generatedId, final String fileName) throws SQLException {
        return new Query(READ_SERVICES_EXTRA_CONFIG).with(generatedId, fileName).select().getClob();
    }

    /* internal */ InputStream readServiceMetadata(final int generatedId, final String lang) throws SQLException {
        return new Query(READ_SERVICES_METADATA).with(generatedId, lang).select().getClob();
    }
    
    /* internal */ InputStream readServiceIsoMetadata(final int generatedId) throws SQLException {
        return new Query(READ_SERVICES_ISO_METADATA).with(generatedId).select().getClob();
    }

    public List<ServiceRecord> readServices() throws SQLException {
        return new Query(LIST_SERVICES).select().getAll(ServiceRecord.class);
    }

    public List<ServiceRecord> readServices(final Specification spec) throws SQLException {
        return new Query(LIST_SERVICES_FROM_TYPE).with(spec.name()).select().getAll(ServiceRecord.class);
    }

    public List<ServiceRecord> readDataServices(final DataRecord record) throws SQLException {
        return new Query(LIST_SERVICES_FROM_DATA).with(record.id).select().getAll(ServiceRecord.class);
    }

    public ServiceRecord writeService(final String identifier, final Specification spec, final StringReader config, final String owner) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner;
        // Proceed to insertion.
        final int id = new Query(WRITE_SERVICE).with(identifier, spec.name(), date.getTime(), title, description, config, login).insert();

        // Return inserted line.
        return new ServiceRecord(this, id, identifier, spec, date, title, description, login, null);
    }

    public void writeServiceExtraConfig(final String identifier, final Specification spec, final StringReader config, final String fileName) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);

        final ServiceRecord record = readService(identifier, spec);

        // Proceed to insertion.
        new Query(WRITE_SERVICE_EXTRA_CONFIG).with(record.id, fileName, config).insert();
    }

    public void writeServiceMetadata(final String identifier, final Specification spec, final StringReader metadata, final String lang) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);

        final ServiceRecord record = readService(identifier, spec);

        // Proceed to insertion.
        new Query(WRITE_SERVICE_METADATA).with(record.id, lang, metadata).insert();
    }
    
    public void writeServiceIsoMetadata(final String identifier, final Specification spec, final String metadataId, final StringReader isoMetadata) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);

        final ServiceRecord record = readService(identifier, spec);

        new Query(UPDATE_SERVICE_ISO_METADATA).with(metadataId, isoMetadata, record.id).update();
    }

    /* internal */ void updateService(final int generatedId, final String newIdentifier, final Specification newType, final String newOwner) throws SQLException {
        new Query(UPDATE_SERVICE).with(newIdentifier, newType.name(), newOwner, generatedId).update();
    }

    /* internal */ void updateServiceConfig(final int generatedId, final StringReader newConfig) throws SQLException {
        new Query(UPDATE_SERVICE_CONFIG).with(newConfig, generatedId).update();
    }

    /* internal */ void updateServiceExtraConfig(final int generatedId, final String fileName, final StringReader newConfig) throws SQLException {
        new Query(UPDATE_SERVICE_EXTRA_CONFIG).with(newConfig, generatedId, fileName).update();
    }

    /* internal */ void updateServiceMetadata(final int generatedId, final String lang, final StringReader newMetadata) throws SQLException {
        new Query(UPDATE_SERVICE_METADATA).with(newMetadata, generatedId, lang).update();
    }

    public void deleteService(final String identifier, final Specification spec) throws SQLException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("spec",       spec);
        final ServiceRecord record = readService(identifier, spec);
        if (record != null) {
            new Query(DELETE_SERVICE_METADATA).with(record.id).update();
            new Query(DELETE_SERVICE_EXTRA_CONFIG).with(record.id).update();
            new Query(DELETE_SERVICE).with(identifier, spec.name()).update();
        }
    }

    /**
     * look for existence of the service metadata with the specified {@code generatedId}.
     *
     * @param generatedId the service auto-generated id
     * @return true if an iso metadata exist for this service.
     * @throws SQLException if a database access error occurs
     * @throws IOException if the configuration cannot be read
     */
    /* internal */ boolean hasServiceIsoMetadata(final int generatedId) throws SQLException, IOException {
        return readServiceIsoMetadata(generatedId) != null;
    }

    /**************************************************************************
     *                           layer table queries                          *
     **************************************************************************/

    /* internal */ LayerRecord readLayer(final int generatedId) throws SQLException {
        return new Query(READ_LAYER_FROM_ID).with(generatedId).select().getFirst(LayerRecord.class);
    }

    public LayerRecord readLayer(final String name, final ServiceRecord service) throws SQLException {
        ensureNonNull("name",   name);
        ensureNonNull("service", service);
        return new Query(READ_LAYER).with(name, service.id).select().getFirst(LayerRecord.class);
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

    public LayerRecord writeLayer(final QName name, final String alias, final ServiceRecord service, final DataRecord data, final Object config, final String owner) throws SQLException {
        ensureNonNull("name",   name);
        ensureNonNull("service", service);
        ensureNonNull("data",    data);

        // Prepare insertion.
        final Date date           = new Date();
        final Integer title       = nextIdForI18n();
        final int description     = nextIdForI18n();
        final String login        = owner;
        // Proceed to insertion.
        final int id = new Query(WRITE_LAYER).with(name.getLocalPart(), name.getNamespaceURI(), alias, service.id, data.id, date.getTime(), title, description, config, login).insert();

        // Return inserted line.
        return new LayerRecord(this, id, name.getLocalPart(), name.getNamespaceURI(), alias, service.id, data.id, date, title, description, login);
    }

    /* internal */ void updateLayer(final int generatedId, final String newName, final String newNamespace, final String newAlias, final int newService, final int newData, final String newOwner) throws SQLException {
        new Query(UPDATE_LAYER).with(newName, newNamespace, newAlias, newService, newData, newOwner, generatedId).update();
    }

    /* internal */ void updateLayerConfig(final int generatedId, final StringReader newConfig) throws SQLException {
        new Query(UPDATE_LAYER_CONFIG).with(newConfig).update();
    }

    public void deleteLayer(final QName name, final ServiceRecord service) throws SQLException {
        ensureNonNull("name",   name);
        ensureNonNull("service", service);
        if (name.getNamespaceURI() != null) {
            new Query(DELETE_LAYER_NMSP).with(name.getLocalPart(), name.getNamespaceURI(), service.id).update();
        } else {
            new Query(DELETE_LAYER).with(name.getLocalPart(), service.id).update();
        }
    }

    public void deleteServiceLayer(final ServiceRecord service) throws SQLException {
        ensureNonNull("service", service);
        new Query(DELETE_SERVICE_LAYER).with(service.id).update();
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
        new Query(UPDATE_TASK).with(newState.name(), new Date(), identifier).update();
    }

    public void deleteTask(final String identifier) throws SQLException {
        ensureNonNull("identifier", identifier);
        new Query(DELETE_TASK).with(identifier).update();
    }

    /**************************************************************************
     *                          properties table queries                      *
     **************************************************************************/

    public String readProperty(final String key) throws SQLException {
        ensureNonNull("key", key);
        return new Query(READ_PROPERTY).with(key).select().getFirstAt(1, String.class);
    }

    public void writeProperty(final String key, final String value) throws SQLException {
        ensureNonNull("key", key);

        // Proceed to insertion.
        new Query(WRITE_PROPERTY).with(key, value).insert();
    }
    
    public void updateProperty(final String key, final String value) throws SQLException {
        ensureNonNull("key", key);

        // Proceed to insertion.
        new Query(UPDATE_PROPERTY).with(value, key).update();
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
                } else if (args[i] instanceof Date) {
                    stmt.setLong(i + 1, ((Date)args[i]).getTime());
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
                        LOGGER.log(Level.WARNING, "An error occurred while creating a " + type.getCanonicalName() + " instance from ResultSet.", ex);
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
                        LOGGER.log(Level.WARNING, "An error occurred while creating a " + type.getCanonicalName() + " instance from ResultSet.", ex);
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
                    final Clob clob = rs.getClob(1);
                    if (clob != null) {
                        final Reader stream = clob.getCharacterStream();
                        // copy the stream into a new one
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
                        char[] buffer = new char[1024];
                        int len;
                        while ((len = stream.read(buffer)) > -1 ) {
                            osw.append(new String(buffer), 0, len);
                        }
                        osw.flush();
                        baos.flush();
                        return new ByteArrayInputStream(baos.toByteArray());
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while copying clob into new Stream", ex);
            } finally {
                rs.close();
                stmt.close();
            }
            return null;
        }

        boolean hasNext() throws SQLException {
            try {
                return rs.next();
            } finally {
                rs.close();
                stmt.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createRecord(final ResultSet rs, final Class<? extends Record> type) throws Exception {
        return (T) type.getConstructor(Session.class, ResultSet.class).newInstance(this, rs);
    }
}
