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

import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.RestartProviderDescriptor;
import static org.constellation.utils.RESTfulUtilities.ok;
import org.constellation.ws.CstlServiceException;
import static org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/DP")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class DataProviders {
    
    @GET
    @Path("restart")
    public Response restartLayerProviders() throws Exception {
        org.constellation.provider.DataProviders.getInstance().reload();
        return ok(new AcknowlegementType("Success", "All layer providers have been restarted."));
    }
    
    @GET
    @Path("{id}/restart")
    public Response restartProvider(final @PathParam("id") String id) throws Exception{
         try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue(id);

            try {
                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                process.call();

            } catch (ProcessException ex) {
                return ok(new AcknowlegementType("Failure", ex.getLocalizedMessage()));
            }

            return ok(new AcknowlegementType("Success", "The source has been deleted"));

        } catch (NoSuchIdentifierException | InvalidParameterValueException ex) {
           throw new CstlServiceException(ex);
        }
    }
}
