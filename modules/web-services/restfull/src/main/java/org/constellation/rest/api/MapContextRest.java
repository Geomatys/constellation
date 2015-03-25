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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.admin.dto.MapContextStyledLayerDTO;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IMapContextBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Mapcontext;
import org.constellation.engine.register.jooq.tables.pojos.MapcontextStyledLayer;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.repository.MapContextRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.provider.Providers;
import org.geotoolkit.georss.xml.v100.WhereType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.owc.xml.v10.MethodCodeType;
import org.geotoolkit.owc.xml.v10.OfferingType;
import org.geotoolkit.owc.xml.v10.OperationType;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.opengis.util.FactoryException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3._2005.atom.CategoryType;
import org.w3._2005.atom.DateTimeType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.IdType;
import org.w3._2005.atom.TextType;

import com.google.common.base.Optional;


/**
 * Map context REST API.
 *
 * @author Cédric Briançon (Geomatys)
 */
@Component
@Path("/1/context/")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class MapContextRest {
    private static final org.w3._2005.atom.ObjectFactory OBJ_ATOM_FACT = new org.w3._2005.atom.ObjectFactory();
    private static final org.geotoolkit.owc.xml.v10.ObjectFactory OBJ_OWC_FACT = new org.geotoolkit.owc.xml.v10.ObjectFactory();
    private static final org.geotoolkit.georss.xml.v100.ObjectFactory OBJ_GEORSS_FACT = new org.geotoolkit.georss.xml.v100.ObjectFactory();

    @Inject
    private IMapContextBusiness contextBusiness;

    @Inject
    private MapContextRepository contextRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IProviderBusiness providerBusiness;

    @GET
    @Path("list")
    public Response findAll() {
        return Response.ok(contextRepository.findAll()).build();
    }

    @GET
    @Path("list/layers")
    public Response findAllMapContextLayers() {
        return Response.ok(contextBusiness.findAllMapContextLayers()).build();
    }

    @PUT
    @Transactional
    public Response create(final MapContextLayersDTO mapContext,@Context HttpServletRequest req) {
        //set owner
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());
        if (!cstlUser.isPresent()) {
            return Response.status(500).entity("operation not allowed without login").build();
        }
        mapContext.setOwner(cstlUser.get().getId());
        final Mapcontext mapContextCreated = contextBusiness.create(mapContext);
        return Response.ok(mapContextCreated).build();
    }

    @POST
    @Transactional
    public Response update(final MapContextLayersDTO mapContext) {
        contextRepository.update(mapContext);
        return Response.ok(mapContext).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") final int contextId) {
        contextRepository.delete(contextId);
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("layers/{id}")
    @Transactional
    public Response setMapItems(@PathParam("id") final int contextId, final List<MapcontextStyledLayer> layers) {
        contextBusiness.setMapItems(contextId, layers);
        return Response.status(201).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Path("{id}/extent")
    public Response getContextExtents(@PathParam("id") final int contextId) {
        final ParameterValues values;
        try {
            values = contextBusiness.getExtent(contextId);
        } catch (FactoryException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract envelope for context "+contextId+". "+ex.getMessage()).status(500).build();
        }
        if (values == null) {
            return Response.status(500).build();
        }
        return Response.ok(values).build();
    }

    @POST
    @Path("extent/layers")
    public Response getContextExtents(final List<MapcontextStyledLayer> layers) {
        final ParameterValues values;
        try {
            values = contextBusiness.getExtentForLayers(layers);
        } catch (FactoryException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract envelope for these layers. "+ex.getMessage()).status(500).build();
        }
        if (values == null) {
            return Response.status(500).entity("Cannot calculate envelope for layers, maybe the layers array sent is empty!").build();
        }
        return Response.ok(values).build();
    }

    @POST
    @Path("external/capabilities/layers/{version}")
    public Response getLayersForWms(@PathParam("version") final String version, final String url) throws IOException, JAXBException {
        final WebMapClient client = (version != null && !version.isEmpty()) ?
                new WebMapClient(new URL(url), version) : new WebMapClient(new URL(url));
        final InputStream response = client.createGetCapabilities().getResponseStream();
        final MarshallerPool pool =  WMSMarshallerPool.getInstance();
        final Unmarshaller unmarsh = pool.acquireUnmarshaller();
        final Object obj = unmarsh.unmarshal(response);
        pool.recycle(unmarsh);

        if (!(obj instanceof AbstractWMSCapabilities)) {
            throw new JAXBException("Unable to parse get capabilities response");
        }

        final List<AbstractLayer> layers = ((AbstractWMSCapabilities)obj).getLayers();
        final List<AbstractLayer> finalList = new ArrayList<>();
        for (final AbstractLayer layer : layers) {
            // Remove layer groups, if any
            if (layer.getLayer() != null && layer.getLayer().size() > 0) {
                continue;
            }
            finalList.add(layer);
        }

        return Response.ok(finalList).build();
    }

    @GET
    @Path("{id}/export")
    @Produces("application/xml")
    public Response export(@Context HttpServletRequest hsr, @PathParam("id") final int id) throws JAXBException {
        final Mapcontext ctxt = contextRepository.findById(id);
        final MapContextLayersDTO ctxtItem = contextBusiness.findMapContextLayers(id);

        // Final object to return
        final FeedType feed = new FeedType();
        final List<Object> entriesToSet = feed.getAuthorOrCategoryOrContributor();
        final IdType idFeed = new IdType();
        idFeed.setValue(String.valueOf(id));
        entriesToSet.add(OBJ_ATOM_FACT.createEntryTypeId(idFeed));
        final TextType title = new TextType();
        title.getContent().add(ctxt.getName());
        entriesToSet.add(OBJ_ATOM_FACT.createEntryTypeTitle(title));

        try {
            final DateTimeType dateTime = new DateTimeType();
            final Date date = new Date();
            final GregorianCalendar gregCal = new GregorianCalendar();
            gregCal.setTime(date);
            dateTime.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal));
            entriesToSet.add(OBJ_ATOM_FACT.createEntryTypeUpdated(dateTime));
        } catch (DatatypeConfigurationException ex) {
            Providers.LOGGER.log(Level.INFO, ex.getMessage(), ex);
        }

        if (ctxt.getWest() != null && ctxt.getNorth() != null && ctxt.getEast() != null && ctxt.getSouth() != null && ctxt.getCrs() != null) {
            final DirectPositionType lowerCorner = new DirectPositionType(ctxt.getWest(), ctxt.getSouth());
            final DirectPositionType upperCorner = new DirectPositionType(ctxt.getEast(), ctxt.getNorth());
            final EnvelopeType envelope = new EnvelopeType(null, lowerCorner, upperCorner, ctxt.getCrs());
            envelope.setSrsDimension(2);
            final WhereType where = new WhereType();
            where.setEnvelope(envelope);
            entriesToSet.add(OBJ_GEORSS_FACT.createWhere(where));
        }

        for (final MapContextStyledLayerDTO styledLayer : ctxtItem.getLayers()) {
            final boolean isExternal = (styledLayer.getExternalLayer() != null);
            final String layerName = (isExternal) ? styledLayer.getExternalLayer() : styledLayer.getName();

            final EntryType newEntry = new EntryType();
            final List<Object> entryThings = newEntry.getAuthorOrCategoryOrContent();
            final IdType idNewEntry = new IdType();
            idNewEntry.setValue("Web Map Service Layer");
            entryThings.add(OBJ_ATOM_FACT.createEntryTypeId(idNewEntry));
            final TextType titleNewEntry = new TextType();
            titleNewEntry.getContent().add(layerName);
            entryThings.add(OBJ_ATOM_FACT.createEntryTypeTitle(titleNewEntry));
            final org.w3._2005.atom.ContentType contentNewEntry = new org.w3._2005.atom.ContentType();
            contentNewEntry.setType("html");
            entryThings.add(OBJ_ATOM_FACT.createEntryTypeContent(contentNewEntry));
            final CategoryType categoryNewEntry = new CategoryType();
            categoryNewEntry.setScheme("http://www.opengis.net/spec/owc/active");
            categoryNewEntry.setTerm("true");
            entryThings.add(OBJ_ATOM_FACT.createEntryTypeCategory(categoryNewEntry));

            final OfferingType offering = new OfferingType();
            offering.setCode("http://www.opengis.net/spec/owc-atom/1.0/req/wms");

            final String defStyle;
            final String urlWms;
            final String layerBBox;
            if (isExternal) {
                urlWms = styledLayer.getExternalServiceUrl();
                defStyle = (styledLayer.getExternalStyle() != null) ? styledLayer.getExternalStyle().split(",")[0] : "";
                layerBBox = styledLayer.getExternalLayerExtent();
            } else {
                final String reqUrl = hsr.getRequestURL().toString();
                if(styledLayer.isIswms()){
                    urlWms = reqUrl.split("/api")[0] +"/WS/wms/"+ styledLayer.getServiceIdentifier();
                }else {
                    urlWms = reqUrl.split("/api")[0] +"/api/1/portrayal/portray/style";
                }
                defStyle = (styledLayer.getExternalStyle() != null) ? styledLayer.getExternalStyle() : "";
                ParameterValues extentValues = null;
                try {
                    extentValues = contextBusiness.getExtentForLayers(Collections.singletonList(styledLayer.getMapcontextStyledLayer()));
                } catch (FactoryException ex) {
                    Providers.LOGGER.log(Level.INFO, ex.getMessage(), ex);
                }
                if (extentValues != null) {
                    layerBBox = extentValues.get("west") +","+ extentValues.get("south") +","+ extentValues.get("east") +","+ extentValues.get("north");
                } else {
                    layerBBox = "-180,-90,180,90";
                }
            }

            if(styledLayer.isIswms()){
                final OperationType opCaps = new OperationType();
                opCaps.setCode("GetCapabilities");
                opCaps.setMethod(MethodCodeType.GET);
                final StringBuilder capsUrl = new StringBuilder();
                capsUrl.append(urlWms).append("?REQUEST=GetCapabilities&SERVICE=WMS");
                opCaps.setHref(capsUrl.toString());
                offering.getOperationOrContentOrStyleSet().add(OBJ_OWC_FACT.createOfferingTypeOperation(opCaps));
            }

            final OperationType opGetMap = new OperationType();
            opGetMap.setCode("GetMap");
            opGetMap.setMethod(MethodCodeType.GET);
            final StringBuilder getMapUrl = new StringBuilder();
            if(styledLayer.isIswms()){
                //external wms or internal wms layer
                getMapUrl.append(urlWms).append("?REQUEST=GetMap&SERVICE=WMS&FORMAT=image/png&TRANSPARENT=true&WIDTH=1024&HEIGHT=768&CRS=CRS:84&BBOX=")
                        .append(layerBBox)
                        .append("&LAYERS=").append(layerName)
                        .append("&STYLES=").append(defStyle)
                        .append("&VERSION=1.3.0");
            }else {
                //internal data
                final Integer dataID = styledLayer.getDataId();
                String layerDataName = layerName;
                String provider="";
                try {
                    final Data data = dataBusiness.findById(dataID);
                    final String namespace = data.getNamespace();
                    final String dataName = data.getName();
                    if(namespace!= null && !namespace.isEmpty()){
                        layerDataName = "{"+namespace+"}"+dataName;
                    }else {
                        layerDataName = dataName;
                    }
                    final Integer providerID = data.getProvider();
                    final Provider p = providerBusiness.getProvider(providerID);
                    provider = p.getIdentifier();
                }catch(ConfigurationException ex){
                    Providers.LOGGER.log(Level.INFO, ex.getMessage(), ex);
                }
                getMapUrl.append(urlWms).append("?REQUEST=GetMap&SERVICE=WMS&FORMAT=image/png&TRANSPARENT=true&WIDTH=1024&HEIGHT=768&CRS=CRS:84&BBOX=")
                        .append(layerBBox)
                        .append("&LAYERS=").append(layerDataName)
                        .append("&STYLES=").append(defStyle)
                        .append("&SLD_VERSION=1.1.0")
                        .append("&PROVIDER=").append(provider)
                        .append("&VERSION=1.3.0");
                if(defStyle!=null && !defStyle.isEmpty()){
                    getMapUrl.append("&SLDID=").append(defStyle)
                             .append("&SLDPROVIDER=sld");
                }
            }
            opGetMap.setHref(getMapUrl.toString());
            offering.getOperationOrContentOrStyleSet().add(OBJ_OWC_FACT.createOfferingTypeOperation(opGetMap));

            entryThings.add(OBJ_OWC_FACT.createOffering(offering));

            entriesToSet.add(OBJ_ATOM_FACT.createEntry(newEntry));
        }
        return Response.ok(feed, MediaType.APPLICATION_XML_TYPE).header("Content-Disposition", "attachment; filename=\"context-" + ctxt.getName() + ".xml\"").build();
    }
}
