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
import java.util.Properties;
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
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.configuration.StringList;
import org.constellation.dto.ObservationFilter;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.Providers;
import org.constellation.provider.observationstore.ObservationStoreProvider;
import org.constellation.sos.configuration.SOSConfigurer;
import org.constellation.sos.configuration.SensorMLGenerator;
import static org.constellation.utils.RESTfulUtilities.ok;
import org.geotoolkit.gml.xml.v321.AbstractGeometryType;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.netcdf.ExtractionResult;
import org.geotoolkit.sos.netcdf.NetCDFExtractor;
import org.opengis.parameter.ParameterValueGroup;

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
    public Response importSensor(final @PathParam("id") String id, final File sensor) throws Exception {
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
    public Response getSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
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
    
    @POST
    @Path("{id}/observations")
    public Response getObservations(final @PathParam("id") String id, final ObservationFilter filter) throws Exception {
        return ok(getConfigurer().getObservationsCsv(id, filter.getSensorID(), filter.getObservedProperty()));
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
    @Path("{id}/sensor/import")
    public Response importSensorFromData(final @PathParam("id") String id, final ParameterValues params) throws Exception {
        final String providerId = params.get("providerId");
        final String dataId = params.get("dataId");
        final String bandName;
        if (dataId != null && dataId.startsWith(providerId + '.')) {
            bandName = dataId.substring(providerId.length() + 1, dataId.length());
        } else {
            bandName = dataId;
        }
        final Provider provider = DataProviders.getInstance().getProvider(providerId);
        final ExtractionResult result;
        if (provider instanceof ObservationStoreProvider) {
            final ObservationStoreProvider omProvider = (ObservationStoreProvider) provider;
            result = ((ObservationStore)omProvider.getMainStore()).getResults();
        } else {
            final ParameterValueGroup config = Providers.getConfigurator().getProviderConfiguration(providerId);
            final ParameterValueGroup choice = ParametersExt.getGroup(config, "choice");
            final ParameterValueGroup type   = (ParameterValueGroup) choice.values().get(0);
            if (type.getDescriptor().getName().getCode().equals("FileCoverageStoreParameters")) {
                final String filePath = type.parameter("path").getValue().toString();
                if (filePath != null) {
                    if (filePath.endsWith(".nc")) {
                        final File ncFile = new File(filePath);
                        result = NetCDFExtractor.getObservationFromNetCDF(ncFile, providerId, bandName);
                        
                    } else {
                        return ok(new AcknowlegementType("Failure", "Only available on netCDF file for now"));
                    }
                } else {
                    return ok(new AcknowlegementType("Failure", "Unable to find a \"path\" parameter in the provider configuration"));
                }
            } else {
                return ok(new AcknowlegementType("Failure", "Available only on FileCoverageStore provider for now"));
            }
        }
        
        // SensorML generation
        final Properties prop = new Properties();
        prop.put("id",         providerId);
        prop.put("beginTime",  result.spatialBound.dateStart);
        prop.put("endTime",    result.spatialBound.dateEnd);
        prop.put("longitude",  result.spatialBound.minx);
        prop.put("latitude",   result.spatialBound.miny);
        prop.put("phenomenon", result.phenomenons);
        final AbstractSensorML sml = SensorMLGenerator.getTemplateSensorML(prop);

        final SOSConfigurer configurer = getConfigurer();
        configurer.importSensor(id, sml, providerId);
        configurer.importObservations(id, result.observations);

        //record location
        final AbstractGeometryType geom = (AbstractGeometryType) result.spatialBound.getGeometry("2.0.0");
        if (geom != null) {
            configurer.updateSensorLocation(id, providerId, geom);
        }
        
        return ok(new AcknowlegementType("Success", "The specified observations have been imported in the SOS"));
    }

    private static SOSConfigurer getConfigurer() throws NotRunningServiceException {
        return (SOSConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.SOS);
    }
}
