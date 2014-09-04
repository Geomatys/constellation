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

import com.google.common.base.Optional;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.DatasetBusiness;
import org.constellation.admin.MetadataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSetBrief;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.UserRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * RESTful API for dataset metadata.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/domain/{domainId}/metadata/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MetadataRest {

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(MetadataRest.class);

    /**
     * Injected metadata business.
     */
    @Inject
    private MetadataBusiness metadataBusiness;

    /**
     * Injected provider business.
     */
    @Inject
    private ProviderBusiness providerBusiness;

    /**
     * Injected dataset business.
     */
    @Inject
    private DatasetBusiness datasetBusiness;

    /**
     * Injected data business.
     */
    @Inject
    private DataBusiness dataBusiness;

    /**
     * Injected user repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Returns all dataset in brief format with its own list of data in brief format.
     *
     * @return Response for client side as json.
     */
    @GET
    @Path("dataset/all")
    public Response getAllDataset() {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final Integer dataSetId = ds.getId();
                final Provider provider = providerBusiness.getProvider(ds.getProviderId());
                final Optional<CstlUser> optUser = userRepository.findById(provider.getOwner());
                String owner = null;
                if(optUser!=null){
                    final CstlUser user = optUser.get();
                    if(user != null){
                        owner = user.getLogin();
                    }
                }
                final List<DataBrief> dataBriefList = dataBusiness.getDataBriefsFromDatasetId(dataSetId);
                final DataSetBrief dsb = new DataSetBrief(ds.getIdentifier(), provider.getType(), owner, dataBriefList);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

}
