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

package org.constellation.ws.rest;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.SensorRecord;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SensorMLTree;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import static org.geotoolkit.sml.xml.SensorMLUtilities.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/sensor/")
public class SensorRest {
    private static final Logger LOGGER = Logging.getLogger(SensorRest.class);
    
    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorList() {
        final List<SensorMLTree> values = new ArrayList<>();
        final List<SensorRecord> sensors = ConfigurationEngine.getSensors();
        for (final SensorRecord sensor : sensors) {
            final SensorMLTree t = new SensorMLTree(sensor.getIdentifier(), sensor.getType());
            final List<SensorMLTree> children = new ArrayList<>();
            final List<SensorRecord> records = ConfigurationEngine.getSensorChildren(sensor.getIdentifier());
            for (SensorRecord record : records) {
                children.add(new SensorMLTree(record.getIdentifier(), record.getType()));
            }
            t.setChildren(children);
            values.add(t);
        }
        final SensorMLTree result = SensorMLTree.buildTree(values);
        return Response.ok(result).build();
    }
    
    @PUT
    @Path("add")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response importSensor(final ParameterValues pv) {
        final String path = pv.get("path");
        final File imported = new File(path);
        try {
            if (imported.isDirectory()) {
                final Map<String, List<String>> parents = new HashMap<>();
                final List<File> files = getFiles(imported);
                for (File f : files) {
                    final AbstractSensorML sml  = unmarshallSensor(f);
                    final String type           = getSensorMLType(sml);
                    final String sensorID       = getSmlID(sml);
                    final List<String> children = getChildrenIdentifiers(sml);
                    ConfigurationEngine.writeSensor(sensorID, type, null);
                    parents.put(sensorID, children);
                }
                // update dependencies
                for (Entry<String, List<String>> entry : parents.entrySet()) {
                    for (String child : entry.getValue()) {
                        final SensorRecord childRecord = ConfigurationEngine.getSensor(child);
                        childRecord.setParentIdentifier(entry.getKey());
                    }
                }
            } else {
                final AbstractSensorML sml = unmarshallSensor(imported);
                final String type          = getSensorMLType(sml);
                final String sensorID      = getSmlID(sml);
                ConfigurationEngine.writeSensor(sensorID, type, null);
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error while reading sensorML file", ex);
            return Response.status(500).entity("fail to read sensorML file").build();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while storing sensor record", ex);
            return Response.status(500).entity("Error while storing sensor record").build();
        }
        return Response.status(200).build();
    }
    
    private List<File> getFiles(final File directory) {
        final List<File> results = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    results.addAll(getFiles(f));
                } else {
                    results.add(f);
                }
            }
        } else {
            results.add(directory);
        }
        return results;
    }
    
    private static AbstractSensorML unmarshallSensor(final File f) throws JAXBException {
        final Unmarshaller um = SensorMLMarshallerPool.getInstance().acquireUnmarshaller();
        Object obj = um.unmarshal(f);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        if (obj instanceof AbstractSensorML) {
            return (AbstractSensorML)obj;
        }
        return null;
    }
}
