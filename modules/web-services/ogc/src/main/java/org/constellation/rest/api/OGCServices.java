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

package org.constellation.rest.api;

import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.*;
import org.constellation.dto.Service;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.geotoolkit.util.FileUtilities;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonUnmarshaller;

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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.utils.RESTfulUtilities.created;
import static org.constellation.utils.RESTfulUtilities.ok;

/**
 * RESTful API for generic OGC services configuration.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/OGC/{spec}")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class OGCServices {
    private static final Logger LOGGER = Logging.getLogger(OGCServices.class);

    /**
     * @see OGCConfigurer#getInstance(String)
     */
    @GET
    @Path("{id}")
    public Response getInstance(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            return ok(getConfigurer(spec).getInstance(id));
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see OGCConfigurer#getInstances()
     */
    @GET
    @Path("all")
    public Response getInstances(final @PathParam("spec") String spec) throws Exception {
        try {
            return ok(new InstanceReport(getConfigurer(spec).getInstances()));
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see OGCConfigurer#createInstance(String, Service, Object)
     */
    @PUT
    @Path("/")
    public Response addInstance(final @PathParam("spec") String spec, final Service metadata) throws Exception {
        try {
            getConfigurer(spec).createInstance(metadata.getIdentifier(), metadata, null);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return created(AcknowlegementType.success(spec.toUpperCase() + " service \"" + metadata.getIdentifier() + "\" successfully created."));
    }

    /**
     * @see OGCConfigurer#startInstance(String)
     */
    @POST
    @Path("{id}/start")
    public Response start(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            getConfigurer(spec).startInstance(id);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully started."));
    }

    /**
     * @see OGCConfigurer#stopInstance(String)
     */
    @POST
    @Path("{id}/stop")
    public Response stop(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            getConfigurer(spec).stopInstance(id);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully stopped."));
    }

    /**
     * @see OGCConfigurer#restartInstance(String, boolean)
     */
    @POST
    @Path("{id}/restart")
    public Response restart(final @PathParam("spec") String spec, final @PathParam("id") String id, final SimpleValue stopFirst) throws Exception {
        try {
            getConfigurer(spec).restartInstance(id, stopFirst.getAsBoolean());
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully restarted."));
    }

    /**
     * @see OGCConfigurer#renameInstance(String, String)
     */
    @POST
    @Path("{id}/rename")
    public Response rename(final @PathParam("spec") String spec, final @PathParam("id") String id, final SimpleValue newId) throws Exception {
        try {
            getConfigurer(spec).renameInstance(id, newId.getValue());
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully renamed."));
    }

    /**
     * @see OGCConfigurer#deleteInstance(String)
     */
    @DELETE
    @Path("{id}")
    public Response delete(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            getConfigurer(spec).deleteInstance(id);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully deleted."));
    }

    /**
     * @see OGCConfigurer#getInstanceConfiguration(String)
     */
    @GET
    @Path("{id}/config")
    public Response getConfiguration(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            return ok(getConfigurer(spec).getInstanceConfiguration(id));
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see OGCConfigurer#setInstanceConfiguration(String, Object)
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{id}/config")
    public Response setConfiguration(final @PathParam("spec") String spec, final @PathParam("id") String id, final InputStream is) throws Exception {
        try {
            final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final Object config = um.unmarshal(is);
            GenericDatabaseMarshallerPool.getInstance().recycle(um);
            getConfigurer(spec).setInstanceConfiguration(id, config);
        } catch (ConfigurationException | JAXBException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" configuration successfully updated."));
    }

    /**
     * @see OGCConfigurer#setInstanceConfiguration(String, Object)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}/config")
    public Response setConfigurationJson(final @PathParam("spec") String spec, final @PathParam("id") String id, final InputStream is) throws Exception {
        final Map<String, String> nSMap = new HashMap<>(0);
        nSMap.put("http://www.constellation.org/config", "constellation-config");
        final JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(nSMap).build();
        try {
            final JettisonJaxbContext cxtx = new JettisonJaxbContext(config, "org.constellation.configuration:" +
                    "org.constellation.generic.database:" +
                    "org.geotoolkit.ogc.xml.v110:" +
                    "org.apache.sis.internal.jaxb.geometry:" +
                    "org.geotoolkit.gml.xml.v311");
            final JettisonUnmarshaller jsonUnmarshaller = cxtx.createJsonUnmarshaller();

            final Class c;
            final String json = FileUtilities.getStringFromStream(is);
            if (json.startsWith("{\"automatic\"")) {
                c = Automatic.class;
            } else if (json.startsWith("{\"layercontext\"")) {
                c = LayerContext.class;
            } else if (json.startsWith("{\"processcontext\"")) {
                c = ProcessContext.class;
            } else if (json.startsWith("{\"constellation-config.SOSConfiguration\"")) {
                c = SOSConfiguration.class;
            } else if (json.startsWith("{\"webdavcontext\"")) {
                c = WebdavContext.class;
            } else {
                return ok(AcknowlegementType.failure("Unknown configuration object given, unable to update service configuration"));
            }
            final Object configObj = jsonUnmarshaller.unmarshalFromJSON(new StringReader(json), c);
            getConfigurer(spec).setInstanceConfiguration(id, configObj);
        } catch (ConfigurationException | JAXBException | IOException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" configuration successfully updated."));
    }

    /**
     * @see OGCConfigurer#getInstanceMetadata(String)
     */
    @GET
    @Path("{id}/metadata")
    public Response getMetadata(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        try {
            return ok(getConfigurer(spec).getInstanceMetadata(id));
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see OGCConfigurer#setInstanceMetadata(String, Service)
     */
    @POST
    @Path("{id}/metadata")
    public Response setMetadata(final @PathParam("spec") String spec, final @PathParam("id") String id, final Service metadata) throws Exception {
        try {
            getConfigurer(spec).setInstanceMetadata(id, metadata);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            throw e;
        }
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" metadata successfully updated."));
    }

    /**
     * Returns the {@link OGCConfigurer} instance from its {@link Specification}.
     *
     * @throws NotRunningServiceException if the service is not activated or if an error
     * occurred during its startup
     */
    private static OGCConfigurer getConfigurer(final String specification) throws NotRunningServiceException {
        final Specification spec = Specification.fromShortName(specification);
        if (!spec.supported()) {
            throw new IllegalArgumentException(specification + " is not a valid OGC service.");
        }
        return (OGCConfigurer) ServiceConfigurer.newInstance(spec);
    }
}
