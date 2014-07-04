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

package org.constellation.rest.api;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.SensorBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.StringList;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SensorMLTree;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Sensor;
import org.constellation.provider.DataProviders;
import org.constellation.provider.coveragestore.CoverageStoreProvider;
import org.constellation.provider.observationstore.ObservationStoreProvider;
import org.constellation.sos.configuration.SensorMLGenerator;
import org.constellation.sos.ws.SOSUtils;
import org.constellation.util.Util;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sos.netcdf.ExtractionResult.ProcedureTree;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.utils.RESTfulUtilities.ok;
import static org.geotoolkit.sml.xml.SensorMLUtilities.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Path("/1/sensor/")
public class SensorRest {
    private static final Logger LOGGER = Logging.getLogger(SensorRest.class);
    
    @Inject
    private SensorBusiness sensorBusiness;
    
    @Inject
    private ProviderBusiness providerBusiness;
    
    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorList() {
        final SensorMLTree result = getFullSensorMLTree();
        return Response.ok(result).build();
    }
    
    private SensorMLTree getFullSensorMLTree() {
        final List<SensorMLTree> values = new ArrayList<>();
        final List<Sensor> sensors = sensorBusiness.getAll();
        for (final Sensor sensor : sensors) {
            final SensorMLTree t = new SensorMLTree(sensor.getIdentifier(), sensor.getType());
            final List<SensorMLTree> children = new ArrayList<>();
            final List<Sensor> records = sensorBusiness.getChildren(sensor);
            for (Sensor record : records) {
                children.add(new SensorMLTree(record.getIdentifier(), record.getType()));
            }
            t.setChildren(children);
            values.add(t);
        }
        return SensorMLTree.buildTree(values);
    }
    
    @DELETE
    @Path("{sensorid}")
    public Response deleteSensor(@PathParam("sensorid") String sensorid) {
        sensorBusiness.delete(sensorid);
        return Response.status(200).build();
    }
    
    @GET
    @Path("{sensorid}")
    public Response getSensorMetadata(@PathParam("sensorid") String sensorid) {
        final Sensor record = sensorBusiness.getSensor(sensorid);
        if (record != null) {
            final AbstractSensorML sml;
            try {
                sml = SOSUtils.unmarshallSensor(record.getMetadata());
                return ok(sml);
            } catch (JAXBException | DataStoreException ex) {
                LOGGER.log(Level.WARNING, "error while unmarshalling SensorML", ex);
                return Response.status(500).entity("failed").build();
            }
        } else {
            return Response.status(404).build();
        }
    }
    
    @PUT
    @Path("generate")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response generateSensor(final ParameterValues pv) {
        final String providerId = pv.get("providerId");
        final QName dataId      = Util.parseQName(pv.get("dataId"));
        
        final org.constellation.provider.Provider provider = DataProviders.getInstance().getProvider(providerId);
        final List<ProcedureTree> procedures;
        try {
            if (provider instanceof ObservationStoreProvider) {
                final ObservationStoreProvider omProvider = (ObservationStoreProvider) provider;
                procedures = omProvider.getObservationStore().getProcedures();
            } else if (provider instanceof CoverageStoreProvider) {
                final CoverageStoreProvider covProvider = (CoverageStoreProvider) provider;
                if (covProvider.isSensorAffectable()) {
                    procedures = covProvider.getObservationStore().getProcedures();
                } else {
                    return ok(new AcknowlegementType("Failure", "Only available on netCDF file for coverage for now"));
                }
            } else {
                return ok(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"));
            }
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, "Error while reading netCDF", ex);
            return ok(new AcknowlegementType("Failure", "Error while reading netCDF"));
        }
        
        // SensorML generation
        try {
            for (ProcedureTree process : procedures) {
                generateSensorML(dataId, providerId, process, null);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while writng sensorML", ex);
            return ok(new AcknowlegementType("Failure", "SQLException while writing sensorML"));
        }
        return ok(new AcknowlegementType("Success", "The sensors has been succesfully generated"));
    }
    
    private void generateSensorML(final QName dataID, final String providerID, final ProcedureTree process, final String parentID) throws SQLException {
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
        prop.put("phenomenon", process.fields);
        final List<String> component = new ArrayList<>();
        for (ProcedureTree child : process.children) {
            component.add(child.id);
            generateSensorML(dataID, providerID, child, process.id);
        }
        prop.put("component", component);
        final String sml = SensorMLGenerator.getTemplateSensorMLString(prop, process.type);
        
        Sensor sensor = sensorBusiness.getSensor(process.id);
        if (sensor == null) {
            sensor = sensorBusiness.create(process.id, process.type, parentID, sml);

        }
        sensorBusiness.linkDataToSensor(dataID, providerID, sensor.getIdentifier());
    }
    
    @PUT
    @Path("add")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response importSensor(final ParameterValues pv) {
        final List<Sensor> sensorsImported = new ArrayList<>();
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

                    final Sensor sensor = sensorBusiness.create(sensorID, type, null, marshallSensor(sml));
                    sensorsImported.add(sensor);
                    parents.put(sensorID, children);
                }
                // update dependencies
                for (Entry<String, List<String>> entry : parents.entrySet()) {
                    for (String child : entry.getValue()) {
                        final Sensor childRecord = sensorBusiness.getSensor(child);//ConfigurationEngine.getSensor(child);
                        childRecord.setParent(entry.getKey());
                        sensorBusiness.update(childRecord);
                    }
                }
            } else {
                final AbstractSensorML sml = unmarshallSensor(imported);
                final String type          = getSensorMLType(sml);
                final String sensorID      = getSmlID(sml);
                final Sensor sensor = sensorBusiness.create(sensorID, type, null, marshallSensor(sml));
                sensorsImported.add(sensor);
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error while reading sensorML file", ex);
            return Response.status(500).entity("fail to read sensorML file").build();
        }
        return Response.ok(sensorsImported).build();
    }
    
    @GET
    @Path("observedProperties/identifiers")
    public Response getObservedPropertiesIds() throws Exception {
        try {
            final Set<String> phenomenons = new HashSet<>();
            final List<Sensor> records = sensorBusiness.getAll();
            for (Sensor record : records) {
                final List<Data> datas = sensorBusiness.getLinkedData(record);
                
                // look for provider ids
                final Set<String> providerIDs = new HashSet<>();
                for (Data data : datas) {
                    final Provider provider = providerBusiness.getProvider(data.getProvider());
                    providerIDs.add(provider.getIdentifier());
                }
                
                for (String providerId : providerIDs) {
                    final ObservationReader reader = getObservationReader(providerId);
                    phenomenons.addAll(reader.getPhenomenonNames());
                }
            }
            return ok(new StringList(phenomenons));
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @GET
    @Path("sensors/identifiers/{observedProperty}")
    public Response getSensorIdsForObservedProperty(final @PathParam("observedProperty") String observedProperty) throws Exception {
        try {
            final Set<String> sensorIDS = new HashSet<>();
            final List<Sensor> records = sensorBusiness.getAll();
            for (Sensor record : records) {
                final List<Data> datas = sensorBusiness.getLinkedData(record);

                // look for provider ids
                final Set<String> providerIDs = new HashSet<>();
                for (Data data : datas) {
                    final Provider provider = providerBusiness.getProvider(data.getProvider());
                    providerIDs.add(provider.getIdentifier());
                }

                for (String providerId : providerIDs) {
                    final ObservationReader reader = getObservationReader(providerId);
                    if (reader.existPhenomenon(observedProperty)) {
                        sensorIDS.addAll(reader.getProcedureNames());
                    }
                }
            }
            return ok(new StringList(sensorIDS));
            
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @GET
    @Path("observedProperty/identifiers/{sensorID}")
    public Response getObservedPropertiesForSensor(final @PathParam("sensorID") String sensorID) throws ConfigurationException {
        try {
            final Sensor sensor = sensorBusiness.getSensor(sensorID);
            if (sensor != null) {
                final Set<String> phenomenons = new HashSet<>();
                final List<Data> datas = sensorBusiness.getLinkedData(sensor);
                
                // look for provider ids
                final Set<String> providerIDs = new HashSet<>();
                for (Data data : datas) {
                    final Provider provider = providerBusiness.getProvider(data.getProvider());
                    providerIDs.add(provider.getIdentifier());
                }
                
                for (String providerId : providerIDs) {
                    final ObservationReader reader = getObservationReader(providerId);
                    phenomenons.addAll(reader.getPhenomenonNames());
                }

                return ok(new StringList(phenomenons));
            } else {
                return Response.status(404).build();
            }
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @GET
    @Path("location/{sensorID}")
    public Response getWKTSensorLocation(final @PathParam("sensorID") String sensorID) throws ConfigurationException {
        try {
            final Sensor sensor = sensorBusiness.getSensor(sensorID);
            if (sensor != null) {
                final List<Data> datas = sensorBusiness.getLinkedData(sensor);

                // look for provider ids
                final Set<String> providerIDs = new HashSet<>();
                for (Data data : datas) {
                    final Provider provider = providerBusiness.getProvider(data.getProvider());
                    providerIDs.add(provider.getIdentifier());
                }

                final List<Geometry> jtsGeometries = new ArrayList<>();
                final SensorMLTree root            = getFullSensorMLTree();
                final SensorMLTree current         = root.find(sensorID);
                for (String providerId : providerIDs) {
                    final ObservationReader reader = getObservationReader(providerId);
                    jtsGeometries.addAll(SOSUtils.getJTSGeometryFromSensor(current, reader));
                }

                if (jtsGeometries.size() == 1) {
                    final WKTWriter writer = new WKTWriter();
                    return ok(new SimpleValue(writer.write(jtsGeometries.get(0))));
                } else if (!jtsGeometries.isEmpty()) {
                    final Geometry[] geometries   = jtsGeometries.toArray(new Geometry[jtsGeometries.size()]);
                    final GeometryCollection coll = new GeometryCollection(geometries, new GeometryFactory());
                    final WKTWriter writer        = new WKTWriter();
                    return ok(new SimpleValue(writer.write(coll)));
                }
                return ok(new SimpleValue(""));
                
            } else {
                return Response.status(404).build();
            }
        } catch (DataStoreException | FactoryException | TransformException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    private ObservationReader getObservationReader(final String providerID) throws ConfigurationException {
        final org.constellation.provider.Provider provider = DataProviders.getInstance().getProvider(providerID);
        if (provider instanceof ObservationStoreProvider) {
            final ObservationStoreProvider omProvider = (ObservationStoreProvider) provider;
            return omProvider.getObservationStore().getReader();
        } else if (provider instanceof CoverageStoreProvider) {
            final CoverageStoreProvider covProvider = (CoverageStoreProvider) provider;
            if (covProvider.isSensorAffectable()) {
                return covProvider.getObservationStore().getReader();
            } else {
                throw new ConfigurationException("Only available on netCDF file for coverage for now");
            }
        } else {
            throw new ConfigurationException("Available only on Observation provider (and netCDF coverage) for now");
        }
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
        SensorMLMarshallerPool.getInstance().recycle(um);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        if (obj instanceof AbstractSensorML) {
            return (AbstractSensorML)obj;
        }
        return null;
    }
    
    private static String marshallSensor(final AbstractSensorML f) throws JAXBException {
        final Marshaller m = SensorMLMarshallerPool.getInstance().acquireMarshaller();
        final StringWriter sw = new StringWriter();
        m.marshal(f, sw);
        SensorMLMarshallerPool.getInstance().recycle(m);
        return sw.toString();
    }
}
