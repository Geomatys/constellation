/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.admin.dao;

import org.constellation.admin.EmbeddedDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class DataRecord extends Record {

    public static enum DataType {
        VECTOR,
        COVERAGE
    }

    private Session session;

    final int id;
    private String name;
    private String namespace;
    private int provider;
    private DataType type;
    private String subtype;
    private boolean visible;
    private boolean sensorable;
    private final Date date;
    private String owner;
    private String metadataId;

    DataRecord(final Session session, final int id, final String name, final String namespace, final int provider, final DataType type, final String subtype,
               final boolean visible, final boolean sensorable, final Date date, final String owner, final String metadataId) {
        this.session     = session;
        this.id          = id;
        this.name        = name;
        this.namespace   = namespace;
        this.provider    = provider;
        this.type        = type;
        this.subtype     = subtype;
        this.visible     = visible;
        this.sensorable  = sensorable;
        this.date        = date;
        this.owner       = owner;
        this.metadataId  = metadataId;
    }

    public DataRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                DataType.valueOf(rs.getString(5)),
                rs.getString(6),
                rs.getBoolean(7),
                rs.getBoolean(8),
                new Date(rs.getLong(9)),
                rs.getString(10),
                rs.getString(11));
    }

    /**
     * {@inheritDoc}
     */
    public void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
    }

    public QName getCompleteName() {
        return new QName(namespace, name);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) throws SQLException {
        this.name = name;
        ensureConnectionNotClosed();
        session.updateData(id, name, namespace, provider, type, subtype, owner);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) throws SQLException {
        this.namespace = namespace;
        ensureConnectionNotClosed();
        session.updateData(id, name, namespace, provider, type, subtype, owner);
    }

    public ProviderRecord getProvider() throws SQLException {
        ensureConnectionNotClosed();
        return session.readProvider(provider);
    }

    public void setProvider(final ProviderRecord provider) throws SQLException {
        this.provider = provider.id;
        ensureConnectionNotClosed();
        session.updateData(id, name, namespace, provider.id, type, subtype, owner);
    }

    public DataType getType() {
        return type;
    }

    public void setType(final DataType type) throws SQLException {
        this.type = type;
        ensureConnectionNotClosed();
        session.updateData(id, name, namespace, provider, type, subtype, owner);
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(final String subtype) throws SQLException {
        this.subtype = subtype;
        ensureConnectionNotClosed();
        session.updateData(id, name, namespace, provider, type, subtype, owner);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) throws SQLException {
        this.visible = visible;
        ensureConnectionNotClosed();
        session.updateDataVisibility(id, visible);
    }

    public boolean isSensorable() {
        return sensorable;
    }

    public void setSensorable(boolean sensorable) throws SQLException {
        this.sensorable = sensorable;
        ensureConnectionNotClosed();
        session.updateDataSensorable(id, sensorable);
    }

    public List<String> getLinkedSensors() throws SQLException {
        ensureConnectionNotClosed();
        final List<SensorRecord> sensors = session.readSensoredDataFromData(this);
        final List<String> ret = new ArrayList<>();
        for (final SensorRecord sensor : sensors) {
            ret.add(sensor.getIdentifier());
        }
        return ret;
    }

    public Date getDate() {
        return date;
    }



    public String getOwnerLogin() {
        return owner;
    }

  

//    public List<StyleRecord> getLinkedStyles() throws SQLException {
//        ensureConnectionNotClosed();
//        return session.readStyles(this);
//    }

//    public void linkToStyle(final StyleRecord style) throws SQLException {
//        ensureConnectionNotClosed();
//        session.writeStyledData(style, this);
//    }

    public InputStream getMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.readDataMetadata(id);
    }

    public void setMetadata(final StringReader metadata) throws IOException, SQLException {
        ensureConnectionNotClosed();
        session.updateDataMetadata(id, metadata);
    }
    
    public InputStream getIsoMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.readDataIsoMetadata(id);
    }

    public void setIsoMetadata(final String metadataId, final StringReader metadata) throws IOException, SQLException {
        ensureConnectionNotClosed();
        this.metadataId = metadataId;
        session.updateDataIsoMetadata(id, metadataId, metadata);
    }
    
    public boolean hasIsoMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.hasDataIsoMetadata(id);
    }
    
    public String getMetadataId() {
        return metadataId;
    }
}
