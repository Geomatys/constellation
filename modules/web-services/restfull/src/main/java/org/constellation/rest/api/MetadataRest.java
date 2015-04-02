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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDatasetBusiness;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSetBrief;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import com.google.common.base.Optional;

/**
 * RESTful API for dataset metadata.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Component
@Path("/1/domain/{domainId}/metadata/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MetadataRest {

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(MetadataRest.class);

    /**
     * Injected dataset business.
     */
    @Inject
    private IDatasetBusiness datasetBusiness;

    /**
     * Injected data business.
     */
    @Inject
    private IDataBusiness dataBusiness;

    /**
     * Injected user repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Injected security manager
     */
    @Inject
    private org.constellation.security.SecurityManager securityManager;

    /**
     * Proceed to remove a dataset
     * @param domainId
     * @param datasetIdentifier
     * @return
     */
    @DELETE
    @Path("dataset/{datasetIdentifier}")
    public Response removeDataSet(@PathParam("domainId") final int domainId,
                                  @PathParam("datasetIdentifier") final String datasetIdentifier) {
        try{
            datasetBusiness.removeDataset(datasetIdentifier, domainId);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Failed to remove dataset with identifier "+datasetIdentifier,ex);
            return Response.status(500).entity("failed").build();
        }
    }

    @POST
    @Path("dataset/create")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createDataset(@PathParam("domainId") final int domainId,
                                  final ParameterValues values) {

        final String metafile = values.getValues().get("metadataFilePath");
        
        final String datasetIdentifier = values.getValues().get("datasetIdentifier");
        if (datasetIdentifier != null && !datasetIdentifier.isEmpty()) {
            try {
                Dataset dataset = datasetBusiness.getDataset(datasetIdentifier);
                if (dataset != null) {
                    LOGGER.log(Level.WARNING, "Dataset with identifier " + datasetIdentifier + " already exist");
                    return Response.status(Response.Status.CONFLICT).entity("failed").build();
                }
                
                DefaultMetadata metadataISO = null;
                if (metafile != null) {
                    metadataISO = dataBusiness.unmarshallMetadata(new File(metafile));
                }

                Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
                Dataset dataSet = datasetBusiness.createDataset(datasetIdentifier, metadataISO, user.get().getId());
                return Response.ok().status(Response.Status.CREATED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(dataSet)
                        .build();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to create dataset with identifier " + datasetIdentifier, ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed").build();
            }
        } else {
            LOGGER.log(Level.WARNING, "Can't create dataset with empty identifier");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed").build();
        }
    }

    /**
     * Returns all dataset in brief format with its own list of data in brief format.
     *
     * @return Response for client side as json.
     * @TODO is it the right place? perhaps it should be moved to DatasetRest facade.
     */
    @GET
    @Path("dataset/all")
    public Response getAllDataset(@PathParam("domainId") final int domainId) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final DataSetBrief dsb = buildDatsetBrief(ds,domainId);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    /**
     * Return as an attachment file the metadata of data set in xml format.
     * @param datasetIdentifier given dataset identifier.
     * @return the xml file
     */
    @GET
    @Path("dataset/{datasetIdentifier}")
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadMetadataForDataSet(@PathParam("domainId") final int domainId,
                                               @PathParam("datasetIdentifier") final String datasetIdentifier) {
        try{
            final DefaultMetadata metadata  =  datasetBusiness.getMetadata(datasetIdentifier, domainId);
            if (metadata != null) {
                metadata.prune();
                final String xmlStr = dataBusiness.marshallMetadata(metadata);
                return Response.ok(xmlStr, MediaType.APPLICATION_XML_TYPE)
                        .header("Content-Disposition", "attachment; filename=\"" + datasetIdentifier + ".xml\"").build();
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Failed to get xml metadata for dataset with identifier "+datasetIdentifier,ex);
        }
        return Response.ok("<empty></empty>", MediaType.APPLICATION_XML_TYPE)
                .header("Content-Disposition", "attachment; filename=\"" + datasetIdentifier + ".xml\"").build();
    }

    /**
     * Proceed to search dataset for query.
     * @param values given parameters
     * @return {code Response} that contains all dataset that matches the lucene query.
     */
    @POST
    @Path("dataset/find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findDataset(@PathParam("domainId") final int domainId, final ParameterValues values) {
        final String search = values.getValues().get("search");
        List<DataSetBrief> briefs = new ArrayList<>();
        final List<Dataset> datasetList;
        try {
            datasetList = datasetBusiness.searchOnMetadata(search);
            for (final Dataset ds : datasetList) {
                final DataSetBrief dsb = buildDatsetBrief(ds,domainId);
                briefs.add(dsb);
            }
            return Response.ok(briefs).build();
        } catch (ConstellationException | IOException ex) {
            return Response.ok("Failed to parse query : "+ex.getMessage()).status(500).build();
        }
    }

    /**
     * Build {@link DataSetBrief} instance from {@link Dataset} and domain id.
     * @param dataset given dataset object.
     * @param domainId given domain id.
     * @return {@link DataSetBrief} built from the given dataset.
     */
    private DataSetBrief buildDatsetBrief(final Dataset dataset,final int domainId){
        final Integer dataSetId = dataset.getId();
        final List<DataBrief> dataBriefList = dataBusiness.getDataBriefsFromDatasetId(dataSetId);
        final DataSetBrief dsb = datasetBusiness.getDatasetBrief(dataSetId, dataBriefList);
        return dsb;
    }

}
