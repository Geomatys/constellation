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

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.*;
import org.constellation.dto.Details;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.Automatic;
import org.constellation.json.metadata.Template;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.configuration.CSWConfigurer;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.opengis.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

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
import java.io.File;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.apache.sis.xml.XML;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.metadata.io.MetadataIoException;

import static org.constellation.utils.RESTfulUtilities.ok;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.w3c.dom.Document;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Path("/1/CSW")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class CSWServicesRest {

    @Autowired
    protected IServiceBusiness serviceBusiness;
    
    private static final TimeZone tz = TimeZone.getTimeZone("GMT+2:00");

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(CSWServicesRest.class);
    
    @POST
    @Path("{id}/index/refresh")
    public Response refreshIndex(final @PathParam("id") String id, final ParameterValues values) throws Exception {
        final boolean asynchrone = values.getAsBoolean("ASYNCHRONE");
        final boolean forced     = values.getAsBoolean("FORCED");
        final CSWConfigurer conf = getConfigurer();
        final AcknowlegementType ack = conf.refreshIndex(id, asynchrone, forced);
        if (!asynchrone && ack.getStatus().equals("Success")) {
            serviceBusiness.restart("CSW", id, true);
        }
        return ok(ack);
   }

    @PUT
    @Path("{id}/index/{metaID}")
    public Response AddToIndex(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().addToIndex(id, metaID));
    }

    @DELETE
    @Path("{id}/index/{metaID}")
    public Response removeFromIndex(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().removeFromIndex(id, metaID));
    }

    @POST
    @Path("{id}/index/stop")
    public Response stopIndexation(final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer().stopIndexation(id));
    }

    // TODO change fileName into dataType parameter
    @PUT
    @Path("{id}/records/{fileName}")
    public Response importRecord(final @PathParam("id") String id, final @PathParam("fileName") String fileName, final File record) throws Exception {
        return ok(getConfigurer().importRecords(id, record, fileName));
    }

    @GET
    @Path("{id}/records/{count: \\w+}-{startIndex: \\w+}")
    public Response getMetadataList(final @PathParam("id") String id, final @PathParam("count") int count, final @PathParam("startIndex") int startIndex) throws Exception {
        final List<BriefNode> nodes = getConfigurer().getMetadataList(id, count, startIndex);
        return ok(new BriefNodeList(nodes));
    }

    @DELETE
    @Path("{id}/record/{metaID}")
    public Response removeMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().removeRecords(id, metaID));
    }
    
    @DELETE
    @Path("{id}/records")
    public Response removeAllMetadata(final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer().removeAllRecords(id));
    }

    @GET
    @Path("{id}/record/{metaID}")
    public Response getMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().getMetadata(id, metaID));
    }
    
    @GET
    @Path("{id}/clearCache")
    public Response clearCache(final @PathParam("id") String id) throws Exception {
        final CSWworker worker = (CSWworker) WSEngine.getInstance("CSW", id);
        if (worker != null) {
            worker.refresh();
            return ok(AcknowlegementType.success("The CSW cache has been cleared"));
        }
        return ok(AcknowlegementType.failure("Unable to find a csw service " + id));
    }
    
    @GET
    @Path("{id}/record/exist/{metaID}")
    public Response metadataExist(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        return ok(getConfigurer().metadataExist(id, metaID));
    }

    @GET
    @Path("{id}/record/download/{metaID}")
    @Produces("application/xml")
    public Response downloadMetadata(final @PathParam("id") String id, final @PathParam("metaID") String metaID) throws Exception {
        final Node md = getConfigurer().getMetadata(id, metaID);
        return Response.ok(md, MediaType.APPLICATION_XML_TYPE).header("Content-Disposition", "attachment; filename=\"" + metaID + ".xml\"").build();
    }

    @GET
    @Path("{id}/records/count")
    public Response getMetadataCount(final @PathParam("id") String id) throws Exception {
        return ok(new SimpleValue(getConfigurer().getMetadataCount(id)));
    }

    @GET
    @Path("types")
    public Response getCSWDatasourceType() throws Exception {
        return ok(getConfigurer().getAvailableCSWDataSourceType());
    }
    
    @POST
    @Path("{id}/federatedCatalog")
    public Response setFederatedCatalog(final @PathParam("id") String id, StringList url) throws Exception {
        final Details details = serviceBusiness.getInstanceDetails("csw", id, null);
        final Automatic conf = (Automatic) serviceBusiness.getConfiguration("csw", id);
        final List<String> urls = conf.getParameterList("CSWCascading");
        urls.addAll(url.getList());
        conf.setParameterList("CSWCascading", urls);
        serviceBusiness.configure("csw", id, details, conf);
        return ok(new AcknowlegementType("Success", "federated catalog added"));
    }

    /**
     * Returns applied template for metadata.
     *
     * @param id service identifier.
     * @param metaID given record identifier.
     * @param type type raster or vector.
     * @param prune flag that indicates if template result will clean empty children/block.
     * @return {@code Response}
     */
    @GET
    @Path("{id}/metadataJson/{metaID}/{type}/{prune}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getCSWMetadataJson(final @PathParam("id") String id,
                                       final @PathParam("metaID") String metaID,
                                       final @PathParam("type") String type,
                                       final @PathParam("prune") boolean prune) {
        try{
            final CSWConfigurer configurer = getConfigurer();
            final Node node = configurer.getMetadata(id, metaID);
            if(node!=null){
                final Metadata metadata = configurer.getMetadataFromNode(id, node);
                if(metadata!=null){
                    if(metadata instanceof DefaultMetadata){
                        //prune the metadata
                        ((DefaultMetadata)metadata).prune();
                    }

                    final StringBuilder buffer = new StringBuilder();
                    final String templateName = configurer.getTemplateName(id, metaID, type);
                    final Template template = Template.getInstance(templateName);
                    template.write(metadata,buffer,prune);
                    return Response.ok(buffer.toString()).build();
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error while writing metadata to json.", ex);
            return Response.status(500).entity("failed").build();
        }
        return Response.status(500).entity("Cannot get metadata for id "+metaID).build();
    }

    private static CSWConfigurer getConfigurer() throws NotRunningServiceException {
        return (CSWConfigurer) ServiceConfigurer.newInstance(Specification.CSW);
    }

    /**
     * Proceed to save metadata with given values from metadata editor.
     *
     * @param id service identifier.
     * @param metaID given record identifier.
     * @param type the data type.
     * @param metadataValues the values of metadata editor.
     * @return {@code Response}
     */
    @POST
    @Path("{id}/metadata/save/{metaID}/{type}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveMetadata(final @PathParam("id") String id,
                                 final @PathParam("metaID") String metaID,
                                 final @PathParam("type") String type,
                                 final RootObj metadataValues) {
        try {
            final CSWConfigurer configurer = getConfigurer();
            final Node node = configurer.getMetadata(id, metaID);
            if(node!=null){
                final Metadata metadata = configurer.getMetadataFromNode(id, node);
                if(metadata!=null){
                    //get template name
                    final String templateName = configurer.getTemplateName(id, metaID, type);
                    final Template template = Template.getInstance(templateName);
                    template.read(metadataValues,metadata,false);
                    // Save metadata
                    final Node result = getNodeFromGeotkMetadata(metadata);
                    //@TODO call updateRecord instead
                    getConfigurer().importRecord(id,result);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error while saving metadata", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Convert geotk metadata string xml to w3c document.
     *
     * @param metadata the given metadata xml as string.
     * 
     * @return {@link Node} that represents the metadata in w3c document format.
     * @throws MetadataIoException
     */
    protected Node getNodeFromGeotkMetadata(final Object metadata) throws MetadataIoException {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            final Marshaller marshaller = EBRIMMarshallerPool.getInstance().acquireMarshaller();
            final MarshallWarnings warnings = new MarshallWarnings();
            marshaller.setProperty(XML.CONVERTER, warnings);
            marshaller.setProperty(XML.TIMEZONE, tz);
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, true);
            marshaller.setProperty(XML.GML_VERSION, LegacyNamespaces.VERSION_3_2_1);
            marshaller.marshal(metadata, document);

            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }
}
