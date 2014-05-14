/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.SensorRecord;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SensorMLTree;
import org.constellation.provider.DataProviders;
import org.constellation.provider.coveragestore.CoverageStoreProvider;
import org.constellation.provider.observationstore.ObservationStoreProvider;
import org.constellation.sos.configuration.SensorMLGenerator;
import static org.constellation.utils.RESTfulUtilities.ok;
import org.geotoolkit.gml.xml.v321.AbstractGeometryType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;

import static org.geotoolkit.sml.xml.SensorMLUtilities.*;
import org.geotoolkit.sos.netcdf.ExtractionResult;
import org.geotoolkit.sos.netcdf.ExtractionResult.ProcedureTree;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
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
    
    @DELETE
    @Path("{sensorid}")
    public Response deleteSensor(@PathParam("sensorid") String sensorid) {
        ConfigurationEngine.deleteSensor(sensorid);
        return Response.status(200).build();
    }
    
    @PUT
    @Path("generate")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response generateSensor(final ParameterValues pv) {
        final String providerId = pv.get("providerId");
        final QName dataId      = QName.valueOf(pv.get("dataId"));
        
        final org.constellation.provider.Provider provider = DataProviders.getInstance().getProvider(providerId);
        final ExtractionResult result;
        if (provider instanceof ObservationStoreProvider) {
            final ObservationStoreProvider omProvider = (ObservationStoreProvider) provider;
            result = omProvider.getObservationStore().getResults();
        } else if (provider instanceof CoverageStoreProvider) {
            final CoverageStoreProvider covProvider = (CoverageStoreProvider) provider;
            if (covProvider.isSensorAffectable()) {
                result = covProvider.getObservationStore().getResults();
            } else {
                return ok(new AcknowlegementType("Failure", "Only available on netCDF file for coverage for now"));
            }
        } else {
            return ok(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"));
        }
        
        // SensorML generation
        try {
            for (ProcedureTree process : result.procedures) {
                generateSensorML(dataId, providerId, process, result, null);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while writng sensorML", ex);
            return ok(new AcknowlegementType("Failure", "SQLException while writing sensorML"));
        }
        return ok(new AcknowlegementType("Success", "The sensors has been succesfully generated"));
    }
    
    private void generateSensorML(final QName dataID, final String providerID, final ProcedureTree process, final ExtractionResult result, final String parentID) throws SQLException {
        final Properties prop = new Properties();
        prop.put("id",         process.id);
        if (process.spatialBound.dateStart != null) {
            prop.put("beginTime",  process.spatialBound.dateStart);
        }
        if (process.spatialBound.dateEnd != null) {
            prop.put("endTime",    process.spatialBound.dateEnd);
        }
        if (process.spatialBound.minx != null) {
            prop.put("longitude",  process.spatialBound.minx);
        }
        if (process.spatialBound.miny != null) {
            prop.put("latitude",   process.spatialBound.miny);
        }
        prop.put("phenomenon", result.fields);
        final List<String> component = new ArrayList<>();
        for (ProcedureTree child : process.children) {
            component.add(child.id);
            generateSensorML(dataID, providerID, child, result, process.id);
        }
        prop.put("component", component);
        final String sml = SensorMLGenerator.getTemplateSensorMLString(prop, process.type);
        
        SensorRecord record = ConfigurationEngine.getSensor(process.id);
        if (record == null) {
            record = ConfigurationEngine.writeSensor(process.id, process.type, parentID);
            record.setMetadata(new StringReader(sml));
        }
        ConfigurationEngine.linkDataToSensor(dataID, providerID, record.getIdentifier());
    }
    
    @PUT
    @Path("add")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response importSensor(final ParameterValues pv) {
        final List<SensorRecord> sensorsImported = new ArrayList<>();
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
                    final SensorRecord sensor = ConfigurationEngine.writeSensor(sensorID, type, null);
                    sensorsImported.add(sensor);
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
                final SensorRecord sensor = ConfigurationEngine.writeSensor(sensorID, type, null);
                sensorsImported.add(sensor);
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error while reading sensorML file", ex);
            return Response.status(500).entity("fail to read sensorML file").build();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while storing sensor record", ex);
            return Response.status(500).entity("Error while storing sensor record").build();
        }
        return Response.ok(sensorsImported).build();
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
