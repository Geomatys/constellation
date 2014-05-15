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
package org.constellation.rest.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.constellation.ServiceDef;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.SensorRecord;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.configuration.StringList;
import org.constellation.dto.ObservationFilter;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.coveragestore.CoverageStoreProvider;
import org.constellation.provider.observationstore.ObservationStoreProvider;
import org.constellation.sos.configuration.SOSConfigurer;
import org.constellation.sos.configuration.SensorMLGenerator;
import org.constellation.sos.ws.SOSUtils;
import static org.constellation.utils.RESTfulUtilities.ok;
import org.geotoolkit.gml.xml.v321.AbstractGeometryType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.netcdf.ExtractionResult;
import org.geotoolkit.sos.netcdf.ExtractionResult.ProcedureTree;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/SOS")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SOSServices {
    
    
    @PUT
    @Path("{id}/sensors")
    public Response importSensorMetadata(final @PathParam("id") String id, final File sensor) throws Exception {
        return ok(getConfigurer().importSensor(id, sensor, "xml"));
    }
    
    @DELETE
    @Path("{id}/sensor/{sensorID}")
    public Response removeSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(getConfigurer().removeSensor(id, sensorID));
    }
    
    @DELETE
    @Path("{id}/sensors")
    public Response removeAllSensor(final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer().removeAllSensors(id));
    }

    @GET
    @Path("{id}/sensor/{sensorID}")
    public Response getSensorMetadata(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(getConfigurer().getSensor(id, sensorID));
    }
    
    @GET
    @Path("{id}/sensors")
    public Response getSensorTree(final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer().getSensorTree(id));
    }
    
    @GET
    @Path("{id}/sensors/identifiers")
    public Response getSensorIds(final @PathParam("id") String id) throws Exception {
        return ok(new StringList(getConfigurer().getSensorIds(id)));
    }
    
    @GET
    @Path("{id}/sensors/identifiers/{observedProperty}")
    public Response getSensorIdsForObservedProperty(final @PathParam("id") String id, final @PathParam("observedProperty") String observedProperty) throws Exception {
        return ok(new StringList(getConfigurer().getSensorIdsForObservedProperty(id, observedProperty)));
    }
    
    @GET
    @Path("{id}/sensors/count")
    public Response getSensortCount(final @PathParam("id") String id) throws Exception {
        return ok(new SimpleValue(getConfigurer().getSensorCount(id)));
    }
    
    @PUT
    @Path("{id}/sensor/location/{sensorID}")
    public Response updateSensorLocation(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID, final AbstractGeometryType location) throws Exception {
        return ok(getConfigurer().updateSensorLocation(id, sensorID, location));
    }
    
    @GET
    @Path("{id}/sensor/location/{sensorID}")
    public Response getWKTSensorLocation(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(new SimpleValue(getConfigurer().getWKTSensorLocation(id, sensorID)));
    }
    
    @GET
    @Path("{id}/observedProperty/identifiers/{sensorID}")
    public Response getObservedPropertiesForSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(new StringList(getConfigurer().getObservedPropertiesForSensorId(id, sensorID)));
    }
    
    @GET
    @Path("{id}/time/{sensorID}")
    public Response getTimeForSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(getConfigurer().getTimeForSensorId(id, sensorID));
    }
    
    @POST
    @Path("{id}/observations")
    public Response getObservations(final @PathParam("id") String id, final ObservationFilter filter) throws Exception {
        return ok(getConfigurer().getDecimatedObservationsCsv(id, filter.getSensorID(), filter.getObservedProperty(), filter.getStart(), filter.getEnd(), filter.getWidth()));
    }
    
    @PUT
    @Path("{id}/observations")
    public Response importObservation(final @PathParam("id") String id, final File obs) throws Exception {
        return ok(getConfigurer().importObservations(id, obs));
    }
    
    @DELETE
    @Path("{id}/observation/{observationID}")
    public Response removeObservation(final @PathParam("id") String id, final @PathParam("observationID") String observationID) throws Exception {
        return ok(getConfigurer().removeSingleObservation(id, observationID));
    }
    
    @DELETE
    @Path("{id}/observation/procedure/{procedureID}")
    public Response removeObservationForProcedure(final @PathParam("id") String id, final @PathParam("procedureID") String procedureID) throws Exception {
        return ok(getConfigurer().removeObservationForProcedure(id, procedureID));
    }

    @GET
    @Path("{id}/observedProperties/identifiers")
    public Response getObservedPropertiesIds(final @PathParam("id") String id) throws Exception {
        return ok(new StringList(getConfigurer().getObservedPropertiesIds(id)));
    }

    @PUT
    @Path("{id}/data/import")
    public Response importSensorFromData(final @PathParam("id") String id, final ParameterValues params) throws Exception {
        final String providerId = params.get("providerId");
        final String dataId = params.get("dataId");
        
        final Provider provider = DataProviders.getInstance().getProvider(providerId);
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

        final SOSConfigurer configurer = getConfigurer();

        // import in O&M database
        configurer.importObservations(id, result.observations, result.phenomenons);
        
        // SensorML generation
        for (ProcedureTree process : result.procedures) {
            generateSensorML(id, process, result, configurer);
        }
        
        return ok(new AcknowlegementType("Success", "The specified observations have been imported in the SOS"));
    }
    
    private void generateSensorML(final String id, final ProcedureTree process, final ExtractionResult result, final SOSConfigurer configurer) throws ConfigurationException {
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
            generateSensorML(id, child, result, configurer);
        }
        prop.put("component", component);
        final AbstractSensorML sml = SensorMLGenerator.getTemplateSensorML(prop, process.type);

        configurer.importSensor(id, sml, process.id);
        
        //record location
        final AbstractGeometryType geom = (AbstractGeometryType) process.spatialBound.getGeometry("2.0.0");
        if (geom != null) {
            configurer.updateSensorLocation(id, process.id, geom);
        }
    }
    
    private void updateProcedureLocation(final String id, final ProcedureTree process, final ExtractionResult result, final SOSConfigurer configurer) throws ConfigurationException {
        for (ProcedureTree child : process.children) {
            updateProcedureLocation(id, child, result, configurer);
        }
        
        final AbstractGeometryType geom = (AbstractGeometryType) process.spatialBound.getGeometry("2.0.0");
        if (geom != null) {
            configurer.updateSensorLocation(id, process.id, geom);
        }
    }
    
    @PUT
    @Path("{id}/sensor/import")
    public Response importSensor(final @PathParam("id") String id, final ParameterValues params) throws Exception {
        final String sensorID          = params.get("sensorId");
        final SensorRecord sensor      = ConfigurationEngine.getSensor(sensorID);
        final List<DataRecord> datas   = ConfigurationEngine.getDataLinkedSensor(sensorID);
        final SOSConfigurer configurer = getConfigurer();
        
        //import SML
        final AbstractSensorML sml = SOSUtils.unmarshallSensor(sensor.getMetadata());
        configurer.importSensor(id, sml, sensorID);
        
        //import sensor children
        final List<SensorRecord> sensors = ConfigurationEngine.getSensorChildren(sensorID);
        for (SensorRecord child : sensors) {
            final AbstractSensorML smlChild = SOSUtils.unmarshallSensor(child.getMetadata());
            configurer.importSensor(id, smlChild, child.getIdentifier());
            datas.addAll(ConfigurationEngine.getDataLinkedSensor(child.getIdentifier()));
        }
        
        // look for provider ids
        final Set<String> providerIDs = new HashSet<>();
        for (DataRecord data : datas) {
            providerIDs.add(data.getProvider().getIdentifier());
        }
        
        // import observations
        for (String providerId : providerIDs) {
            final Provider provider = DataProviders.getInstance().getProvider(providerId);
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
            // import in O&M database
            configurer.importObservations(id, result.observations, result.phenomenons);
            
            // update sensor location
            for (ProcedureTree process : result.procedures) {
                updateProcedureLocation(id, process, result, configurer);
            }
        }
        
        
        return ok(new AcknowlegementType("Success", "The specified sensor has been imported in the SOS"));
    }
    
    private static SOSConfigurer getConfigurer() throws NotRunningServiceException {
        return (SOSConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.SOS);
    }
}
