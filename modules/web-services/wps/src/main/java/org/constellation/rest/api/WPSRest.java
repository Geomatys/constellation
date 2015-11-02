/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;
import org.opengis.metadata.Identifier;
import org.constellation.admin.dto.RegistryDTO;
import org.constellation.admin.dto.ProcessDTO;
import org.constellation.admin.dto.RegistryListDTO;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Process;
import org.constellation.wps.utils.WPSUtils;
import org.opengis.util.NoSuchIdentifierException;

/**
 * RESTful API for WPS services configuration.
 *
 * @author Legal Guilhem (Geomatys)
 * @version 1.1
 * @since 1.1
 */
@Path("/1/WPS/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class WPSRest {
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.rest.api");
    
    @Inject
    private IServiceBusiness serviceBusiness;

    /**
     * Returns the list of all supported processes for WPS service.
     * @return {code List} of pojo
     */
    @GET
    @Path("process/all")
    public List<RegistryDTO> getAllProcess() {

        final List<RegistryDTO> results = new ArrayList<>();

        for (Iterator<ProcessingRegistry> it = ProcessFinder.getProcessFactories(); it.hasNext();) {
            final ProcessingRegistry processingRegistry = it.next();
            Iterator<? extends Identifier> iterator = processingRegistry
                    .getIdentification().getCitation().getIdentifiers()
                    .iterator();
            final RegistryDTO registry = new RegistryDTO(iterator.next().getCode());
            
            final List<ProcessDTO> processes = new ArrayList<>();
            for (ProcessDescriptor descriptor : processingRegistry.getDescriptors()) {
                if (WPSUtils.isSupportedProcess(descriptor)) {
                    final ProcessDTO dto = new ProcessDTO(descriptor.getIdentifier().getCode());
                    if (descriptor.getProcedureDescription() != null) {
                        dto.setDescription(descriptor.getProcedureDescription().toString());
                    }
                    processes.add(dto);
                }
            }
            registry.setProcesses(processes);
            if(!processes.isEmpty()) {
                results.add(registry);
            }
        }

        return results;

    }

    /**
     * Returns the list of processes for WPS service
     * @param id the wps instance identifier
     * @return {List} of pojo
     * @throws ConfigurationException
     */
    @GET
    @Path("{id}/process")
    public List<RegistryDTO> getProcess(final @PathParam("id") String id) throws ConfigurationException {
        
        final ProcessContext context = (ProcessContext) serviceBusiness.getConfiguration("WPS", id);
        
        final List<RegistryDTO> results = new ArrayList<>();

        if (Boolean.TRUE.equals(context.getProcesses().getLoadAll()) ) {
            return getAllProcess();
        } else {
            for (ProcessFactory pFacto : context.getProcessFactories()) {
                final RegistryDTO registry = new RegistryDTO(pFacto.getAutorityCode());
                final List<ProcessDTO> processes = new ArrayList<>();

                final ProcessingRegistry processingRegistry = ProcessFinder.getProcessFactory(pFacto.getAutorityCode());
                if (pFacto.getLoadAll()) {
                    for (ProcessDescriptor descriptor : processingRegistry.getDescriptors()) {
                        final ProcessDTO dto = new ProcessDTO(descriptor.getIdentifier().getCode());
                        if (descriptor.getProcedureDescription() != null) {
                            dto.setDescription(descriptor.getProcedureDescription().toString());
                        }
                        processes.add(dto);
                    }
                } else {
                    final List<Process> list = pFacto.getInclude().getProcess();
                    for (Process p : list) {
                        try {
                            final ProcessDescriptor descriptor = processingRegistry.getDescriptor(p.getId());
                            final ProcessDTO dto = new ProcessDTO(descriptor.getIdentifier().getCode());
                            if (descriptor.getProcedureDescription() != null) {
                                dto.setDescription(descriptor.getProcedureDescription().toString());
                            }
                            processes.add(dto);
                        } catch (NoSuchIdentifierException ex) {
                            LOGGER.log(Level.WARNING, "Unable to find a process named:" + p.getId() + " in factory " + pFacto.getAutorityCode(), ex);
                        }
                    }
                }
                registry.setProcesses(processes);
                if(!processes.isEmpty()) {
                    results.add(registry);
                }
            }
            return results;
        }
    }

    /**
     * Add processes list for WPS service
     * @param id the wps instance identifier
     * @param registries pojo
     * @throws ConfigurationException
     */
    @PUT
    @Path("{id}/process")
    public void addProcess(final @PathParam("id") String id, final RegistryListDTO registries) throws ConfigurationException {
        final ProcessContext context = (ProcessContext) serviceBusiness.getConfiguration("WPS", id);
        
        if (Boolean.TRUE.equals(context.getProcesses().getLoadAll())) {
            // WPS already contains all the registries, nothing to do
        } else {
            for (RegistryDTO registry : registries.getRegistries()) {
                ProcessFactory factory = context.getProcessFactory(registry.getName());
                if (factory != null) {
                    if (Boolean.TRUE.equals(factory.getLoadAll())) {
                        //WPS already contains all the process of this registry, nothing to do
                    } else {
                        for (ProcessDTO process : registry.getProcesses()) {
                            if (factory.getInclude().contains(process.getId())) {
                                // WPS already contain the process
                            } else {
                                factory.getInclude().add(process.getId());
                            }
                        }
                    }
                } else {
                    factory = new ProcessFactory(registry.getName(), false);
                    for (ProcessDTO process : registry.getProcesses()) {
                        factory.getInclude().add(process.getId());
                    }
                    context.getProcessFactories().add(factory);
                }
            }
        }
        
        // save context
        serviceBusiness.configure("WPS", id, null, context);
    }

    /**
     * Remove authority for WPS service
     * @param id the wps instance identifier
     * @param code the authority code
     * @throws ConfigurationException
     */
    @DELETE
    @Path("{id}/authority/{code}")
    public void removeAuthority(final @PathParam("id") String id, final @PathParam("code") String code) throws ConfigurationException {
        final ProcessContext context = (ProcessContext) serviceBusiness.getConfiguration("WPS", id);
        
        if (Boolean.TRUE.equals(context.getProcesses().getLoadAll()) ) {
            context.getProcesses().setLoadAll(Boolean.FALSE);
            
            for (Iterator<ProcessingRegistry> it = ProcessFinder.getProcessFactories(); it.hasNext();) {
                final ProcessingRegistry processingRegistry = it.next();
                final String name = processingRegistry
                        .getIdentification().getCitation().getIdentifiers()
                        .iterator().next().getCode();
                if (!name.equals(code)) {
                    context.getProcessFactories().add(new ProcessFactory(name, Boolean.TRUE));
                }
            }
            
        } else {
            context.removeProcessFactory(code);
        }
        
        // save context
        serviceBusiness.configure("WPS", id, null, context);
    }

    /**
     * remove single process for WPS service.
     * @param id the wps instance identifier
     * @param code the authority code
     * @param processId the process identifier
     * @throws ConfigurationException
     */
    @DELETE
    @Path("{id}/process/{code}/{pid}")
    public void removeProcess(final @PathParam("id") String id, final @PathParam("code") String code, final @PathParam("pid") String processId) throws ConfigurationException {
        final ProcessContext context = (ProcessContext) serviceBusiness.getConfiguration("WPS", id);
        
        if (Boolean.TRUE.equals(context.getProcesses().getLoadAll()) ) {
            for (Iterator<ProcessingRegistry> it = ProcessFinder.getProcessFactories(); it.hasNext();) {
                final ProcessingRegistry processingRegistry = it.next();
                final String name = processingRegistry
                        .getIdentification().getCitation().getIdentifiers()
                        .iterator().next().getCode();
                if (!name.equals(code)) {
                    context.getProcessFactories().add(new ProcessFactory(name, Boolean.TRUE));
                } else {
                    final ProcessFactory newFactory = new ProcessFactory(name, Boolean.FALSE);
                    for (ProcessDescriptor descriptor : processingRegistry.getDescriptors()) {
                        final String pid = descriptor.getIdentifier().getCode();
                        if (!pid.equals(processId)) {
                            newFactory.getInclude().add(pid);
                        }
                    }
                }
            }
        } else {
            final ProcessFactory factory = context.getProcessFactory(code);
            if (factory != null) {
                if (Boolean.TRUE.equals(factory.getLoadAll())) {
                    factory.setLoadAll(Boolean.FALSE);
                    final ProcessingRegistry processingRegistry = ProcessFinder.getProcessFactory(code);
                    for (ProcessDescriptor descriptor : processingRegistry.getDescriptors()) {
                        final String pid = descriptor.getIdentifier().getCode();
                        if (!pid.equals(processId)) {
                            factory.getInclude().add(pid);
                        }
                    }
                } else {
                    factory.getInclude().remove(processId);
                }
            }
        }
        
        // save context
        serviceBusiness.configure("WPS", id, null, context);
    }
}
