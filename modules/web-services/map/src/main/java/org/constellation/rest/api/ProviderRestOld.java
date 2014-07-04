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

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.DataBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.map.configuration.DefaultProviderOperationListener;
import org.constellation.map.configuration.ProviderOperationListener;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.CreateProviderDescriptor;
import org.constellation.process.provider.DeleteProviderDescriptor;
import org.constellation.process.provider.GetConfigProviderDescriptor;
import org.constellation.process.provider.RestartProviderDescriptor;
import org.constellation.process.provider.UpdateProviderDescriptor;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderFactory;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.constellation.utils.RESTfulUtilities.ok;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/DP")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ProviderRestOld {

    @Inject
    private DataBusiness dataBusiness;
    
    private static final Logger LOGGER = Logging.getLogger(ProviderRestOld.class);
    
    private final ProviderOperationListener providerListener = new DefaultProviderOperationListener(); // TODO overrding mecanism
    
    private final Map<String, ProviderFactory> services = new HashMap<>();

    public ProviderRestOld() {
        
        final Collection<DataProviderFactory> availableLayerServices = org.constellation.provider.DataProviders.getInstance().getFactories();
        for (DataProviderFactory service: availableLayerServices) {
            this.services.put(service.getName(), service);
        }
        final Collection<StyleProviderFactory> availableStyleServices = org.constellation.provider.StyleProviders.getInstance().getFactories();
        for (StyleProviderFactory service: availableStyleServices) {
            this.services.put(service.getName(), service);
        }
    }
    
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
    
    @DELETE
    @Path("{id}/{deleteData}")
    public Response deleteProvider(final @PathParam("id") String providerId, final @PathParam("deleteData") boolean deleteData) throws ConfigurationException {
        try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(DeleteProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);
            if (deleteData) {
                inputs.parameter(DeleteProviderDescriptor.DELETE_DATA_NAME).setValue(deleteData);
            }

            try {
                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                process.call();

                /*update the layer context using this provider
                for (String specification : CommonConstants.WXS) {
                    final Map<String, Worker> instances = WSEngine.getWorkersMap(specification);
                    if (instances != null) {
                        for (Worker w : instances.values()) {
                            if (w.getConfiguration() instanceof LayerContext) {
                                final LayerContext configuration = (LayerContext) w.getConfiguration();
                                if (configuration.hasSource(providerId)) {
                                    configuration.removeSource(providerId);
                                    // save new Configuration
                                    LOGGER.log(Level.INFO, "Updating service {0}-{1} for deleted provider", new Object[]{specification, w.getId()});
                                    try {
                                        ConfigurationEngine.storeConfiguration(specification, w.getId(), configuration);
                                    } catch (JAXBException ex) {
                                        throw new ConfigurationException(ex);
                                    }
                                }
                            }
                        }
                    }
                }*/
                providerListener.fireProvidedDeleted(providerId);
            } catch (ProcessException ex) {
                return ok(new AcknowlegementType("Failure", ex.getLocalizedMessage()));
            }

            return ok(new AcknowlegementType("Success", "The provider has been deleted"));

        } catch (NoSuchIdentifierException | InvalidParameterValueException ex) {
           throw new ConfigurationException(ex);
        }
    }
    
    @POST
    @Path("{serviceName}")
    public Response createProvider(final @PathParam("serviceName") String serviceName, final InputStream providerConfig) throws ConfigurationException {
        final ProviderFactory service = this.services.get(serviceName);
        if (service != null) {

            final ParameterValueReader reader = new ParameterValueReader(service.getProviderDescriptor());

            try {
                // we read the source parameter to add
                reader.setInput(providerConfig);
                final ParameterValueGroup sourceToAdd = (ParameterValueGroup) reader.read();

                final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);

                final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(serviceName);
                inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(sourceToAdd);

                try {
                    final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                    process.call();
                    providerListener.fireProvidedAdded((String)sourceToAdd.parameter("id").getValue());
                } catch (ProcessException ex) {
                    return ok(new AcknowlegementType("Failure", ex.getLocalizedMessage()));
                }

                reader.dispose();
                return ok(new AcknowlegementType("Success", "The source has been added"));

            } catch (NoSuchIdentifierException | XMLStreamException | InvalidParameterValueException | IOException ex) {
                throw new ConfigurationException(ex);
            }
        } else {
            throw new ConfigurationException("No provider service for: " + serviceName + " has been found");
        }
    }
    
    @POST
    @Path("{serviceName}/{id}")
    public Response updateProvider(final @PathParam("serviceName") String serviceName, final @PathParam("id") String currentId, final Object objectRequest) throws ConfigurationException{
        final ProviderFactory service = services.get(serviceName);
        if (service != null) {

            ParameterDescriptorGroup desc = service.getProviderDescriptor();
            desc = (ParameterDescriptorGroup) desc.descriptor("source");
            final ParameterValueReader reader = new ParameterValueReader(desc);

            try {
                // we read the source parameter to add
                reader.setInput(objectRequest);
                final ParameterValueGroup sourceToModify = (ParameterValueGroup) reader.read();

                final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderDescriptor.NAME);

                final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
                inputs.parameter(UpdateProviderDescriptor.PROVIDER_ID_NAME).setValue(currentId);
                inputs.parameter(UpdateProviderDescriptor.SOURCE_NAME).setValue(sourceToModify);

                try {
                    final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                    process.call();
                    providerListener.fireProvidedModified(currentId);
                } catch (ProcessException ex) {
                    return ok(new AcknowlegementType("Failure", ex.getLocalizedMessage()));
                }

                reader.dispose();
                return ok(new AcknowlegementType("Success", "The source has been updated"));

            } catch (NoSuchIdentifierException | XMLStreamException | IOException | InvalidParameterValueException ex) {
                throw new ConfigurationException(ex);
            } 
        } else {
            throw new ConfigurationException("No descriptor for: " + serviceName + " has been found");
        }
    }
    
    
    @GET
    @Path("{id}/configuration")
    public Object getProviderConfiguration(final @PathParam("id") String id) throws ConfigurationException {
         try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(GetConfigProviderDescriptor.PROVIDER_ID_NAME).setValue(id);
            try {

                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                final ParameterValueGroup outputs = process.call();
                return outputs.parameter(GetConfigProviderDescriptor.CONFIG_NAME).getValue();

            } catch (ProcessException ex) {
                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
            }

        } catch (NoSuchIdentifierException | InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @GET
    @Path("providers")
    public Response listProviderServices(){
        final List<ProviderServiceReport> providerServ = new ArrayList<>();

        final Collection<DataProvider> layerProviders = org.constellation.provider.DataProviders.getInstance().getProviders();
        final Collection<StyleProvider> styleProviders = org.constellation.provider.StyleProviders.getInstance().getProviders();
        for (ProviderFactory service : services.values()) {

            final List<ProviderReport> providerReports = new ArrayList<>();
            for (final DataProvider p : layerProviders) {
                if (p.getFactory().equals(service)) {
                    final List<DataBrief> keys = new ArrayList<>();
                    for(Name n : p.getKeys()){
                        final QName name = new QName(n.getNamespaceURI(), n.getLocalPart());
                        final DataBrief db = dataBusiness.getDataBrief(name,p.getId());
                        keys.add(db);
                    }
                    final Date date = (Date) p.getSource().parameter("date").getValue();
                    final String providerType = (String) p.getSource().parameter("providerType").getValue();
                    providerReports.add(new ProviderReport(p.getId(), service.getName(), keys, date, providerType));
                }
            }
            for (final StyleProvider p : styleProviders) {
                if (p.getFactory().equals(service)) {
                    final List<DataBrief> keys = new ArrayList<>();
                    for(String n : p.getKeys()){
                        final DataBrief db = new DataBrief();
                        db.setName(n);
                        keys.add(db);
                    }
                    final Date date = (Date) p.getSource().parameter("date").getValue();
                    final String providerType = (String) p.getSource().parameter("providerType").getValue();
                    providerReports.add(new ProviderReport(p.getId(), service.getName(), keys, date, providerType));
                }
            }
            providerServ.add(new ProviderServiceReport(service.getName(), service instanceof StyleProviderFactory, providerReports));
        }

        return ok(new ProvidersReport(providerServ));
    }
    
  
    @GET
    @Path("service/descriptor/{serviceName}")
    public Response getServiceDescriptor(final @PathParam("serviceName") String serviceName) throws ConfigurationException {
        final ProviderFactory service = services.get(serviceName);
        if (service != null) {
            return ok(service.getProviderDescriptor());
        }
        throw new ConfigurationException("No provider service for: " + serviceName + " has been found");
    }

    @GET
    @Path("source/descriptor/{serviceName}")
    public Response getSourceDescriptor(final @PathParam("serviceName") String serviceName) throws ConfigurationException {
        final ProviderFactory service = services.get(serviceName);
        if (service != null) {
            return ok(service.getStoreDescriptor());
        }
        throw new ConfigurationException("No provider service for: " + serviceName + " has been found");
    }
}
