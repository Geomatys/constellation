/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import static org.constellation.process.service.CreateMapServiceDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.v111.ContactAddress;
import org.geotoolkit.wms.xml.v111.ContactInformation;
import org.geotoolkit.wms.xml.v111.ContactPersonPrimary;
import org.geotoolkit.wms.xml.v111.Keyword;
import org.geotoolkit.wms.xml.v111.KeywordList;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.Capability;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Process that create a new instance configuration from the service name (WMS, WMTS, WCS or WFS) for a specified instance name.
 * If the instance directory is created but no configuration file exist, the process will create one.
 * Execution will throw ProcessExeption if the service name is different from WMS, WMTS of WFS (no matter of case) or if
 * a configuration file already exist fo this instance name.
 * @author Quentin Boileau (Geomatys).
 */
public class CreateMapService extends AbstractProcess {

    public CreateMapService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Create a new instance and configuration for a specified service and instance name.
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS, WCS of WFS (no matter of case)
     * - if a configuration file already exist for this instance name.
     * - if error during file creation or marshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        LayerContext configuration = value(CONFIGURATION, inputParameters);
        File instanceDirectory = value(INSTANCE_DIRECTORY, inputParameters);
        Object theFuttureService = value(CAPABILITIES_CONFIGURATION, inputParameters);

        if (serviceName != null && !serviceName.isEmpty() && ("WMS".equalsIgnoreCase(serviceName) || "WMTS".equalsIgnoreCase(serviceName)
                || "WFS".equalsIgnoreCase(serviceName) || "WCS".equalsIgnoreCase(serviceName))) {
            serviceName = serviceName.toUpperCase();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\").", this, null);
        }

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        if (configuration == null) {
            configuration = new LayerContext();
        }

        //get config directory .constellation if null
        if (instanceDirectory == null) {
            final File configDirectory = ConfigDirectory.getConfigDirectory();


            if (configDirectory != null && configDirectory.isDirectory()) {

                //get service directory ("WMS", "WMTS", "WFS", "WCS")
                final File serviceDir = new File(configDirectory, serviceName);
                if (serviceDir.exists() && serviceDir.isDirectory()) {

                    //create service instance directory
                    instanceDirectory = new File(serviceDir, identifier);

                } else {
                    throw new ProcessException("Service directory can't be found for service name : " + serviceName, this, null);
                }
            } else {
                throw new ProcessException("Configuration directory can't be found.", this, null);
            }
        }


        File configurationFile = null;
        boolean createConfig = true;
        if (instanceDirectory.exists()) {
            configurationFile = new File(instanceDirectory, "layerContext.xml");
            final File portrayalFile = new File(instanceDirectory, "WMSPortrayal.xml");

            //get configuration if aleady exist.
            if (configurationFile.exists()) {
                createConfig = false;
                try {
                    final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    final Object obj = unmarshaller.unmarshal(configurationFile);
                    GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                    if (obj instanceof LayerContext) {
                        configuration = (LayerContext) obj;
                    } else {
                        throw new ProcessException("The layerContext.xml file does not contain a LayerContext object", this, null);
                    }
                } catch (JAXBException ex) {
                    throw new ProcessException(ex.getMessage(), this, ex);
                }
            }

            if (serviceName.equalsIgnoreCase("WMS")) {
                //create default portrayal file if not exist ONLY for WMS services
                if (!portrayalFile.exists()) {
                    try {
                        final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                        marshaller.marshal(new WMSPortrayal(), portrayalFile);
                        GenericDatabaseMarshallerPool.getInstance().release(marshaller);
                    } catch (JAXBException ex) {
                        throw new ProcessException(ex.getMessage(), this, ex);
                    }
                }

//                TODO create capabilities
                if(theFuttureService instanceof Service){
                    Service capabilitiesInformation = (Service) theFuttureService;
                    for (String version : capabilitiesInformation.getVersions()) {
                        if (version.equals("130")) {
                            createV130Capabilities(instanceDirectory, capabilitiesInformation);

                        } else if (version.equals("111")) {
                            createV110Capabilities(instanceDirectory, capabilitiesInformation);

                        } else {
                            createV130Capabilities(instanceDirectory, capabilitiesInformation);
                        }
                    }
                }

            }

        } else if (instanceDirectory.mkdir()) {
            configurationFile = new File(instanceDirectory, "layerContext.xml");
        } else {
            throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
        }

        if (createConfig) {
            //create layerContext.xml file for the default configuration.
            try {
                final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);

            } catch (JAXBException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            }
        }

        getOrCreate(OUT_CONFIGURATION, outputParameters).setValue(configuration);

    }

    /**
     *
     * @param instanceDirectory
     * @param capabilitiesInformation
     */
    private void createV110Capabilities(File instanceDirectory, Service capabilitiesInformation) {
        List<Keyword> keywords = new ArrayList<Keyword>(0);
        for (String keywordString : capabilitiesInformation.getKeywords()) {
            Keyword keyword = new Keyword(keywordString);
            keywords.add(keyword);
        }

        KeywordList keywordList = new KeywordList(keywords);

        Contact currentContact = capabilitiesInformation.getServiceContact();
        ContactPersonPrimary personPrimary = new ContactPersonPrimary(currentContact.getFullname(),
                currentContact.getOrganisation());

        ContactAddress address = new ContactAddress("POSTAL", currentContact.getAddress(),
                currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());

        ContactInformation contact = new ContactInformation(personPrimary, currentContact.getPosition(),
                address, currentContact.getPhone(),
                currentContact.getFax(), currentContact.getEmail());
        org.geotoolkit.wms.xml.v111.Service newService = new org.geotoolkit.wms.xml.v111.Service(capabilitiesInformation.getName(),
                capabilitiesInformation.getIdentifier(), capabilitiesInformation.getDescription(), keywordList, null, contact,
                capabilitiesInformation.getServiceConstraints().getFees(), capabilitiesInformation.getServiceConstraints().getAccessConstraint());

        org.geotoolkit.wms.xml.v111.Capability capability = new org.geotoolkit.wms.xml.v111.Capability(null, null, null, null);
        WMT_MS_Capabilities capabilities = new WMT_MS_Capabilities(newService, capability, "1.1.1", null);
//        TODO marshall new Service
        try {
            final Marshaller marshaller = WMSMarshallerPool.getInstance().acquireMarshaller();
            File capabilitiesDescriptionV111 = new File(instanceDirectory, "WMSCapabilities1.1.1.xml");
            marshaller.marshal(capabilities, capabilitiesDescriptionV111);
            WMSMarshallerPool.getInstance().release(marshaller);
        } catch (JAXBException e) {
//            TODO
            e.printStackTrace();
        }
    }

    /**
     *
     * @param instanceDirectory
     * @param capabilitiesInformation
     */
    private void createV130Capabilities(File instanceDirectory, Service capabilitiesInformation) {
        List<org.geotoolkit.wms.xml.v130.Keyword> keywords = new ArrayList<org.geotoolkit.wms.xml.v130.Keyword>(0);
        for (String keywordString : capabilitiesInformation.getKeywords()) {
            org.geotoolkit.wms.xml.v130.Keyword keyword = new org.geotoolkit.wms.xml.v130.Keyword(keywordString);
            keywords.add(keyword);
        }

        org.geotoolkit.wms.xml.v130.KeywordList keywordsList = new org.geotoolkit.wms.xml.v130.KeywordList(keywords);

        Contact currentContact = capabilitiesInformation.getServiceContact();

        org.geotoolkit.wms.xml.v130.ContactPersonPrimary personPrimary = new org.geotoolkit.wms.xml.v130.ContactPersonPrimary(currentContact.getFullname(), currentContact.getOrganisation());
        org.geotoolkit.wms.xml.v130.ContactAddress address = new org.geotoolkit.wms.xml.v130.ContactAddress("POSTAL",
                currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());
        org.geotoolkit.wms.xml.v130.ContactInformation contact = new org.geotoolkit.wms.xml.v130.ContactInformation(personPrimary, currentContact.getPosition(),
                address, currentContact.getPhone(), currentContact.getFax(), currentContact.getEmail());

        org.geotoolkit.wms.xml.v130.Service newService = new org.geotoolkit.wms.xml.v130.Service(capabilitiesInformation.getName(),
                capabilitiesInformation.getIdentifier(), capabilitiesInformation.getDescription(), keywordsList, null, contact,
                capabilitiesInformation.getServiceConstraints().getFees(), capabilitiesInformation.getServiceConstraints().getAccessConstraint(),
                capabilitiesInformation.getServiceConstraints().getLayerLimit(), capabilitiesInformation.getServiceConstraints().getMaxWidth(),
                capabilitiesInformation.getServiceConstraints().getMaxHeight());

        Capability capability = new Capability(null, null, null, null);
        WMSCapabilities capabilities = new WMSCapabilities(newService, capability, "1.3.0", null);
        try {
            final Marshaller marshaller = WMSMarshallerPool.getInstance().acquireMarshaller();
            File capabilitiesDescriptionV111 = new File(instanceDirectory, "WMSCapabilities1.3.0.xml");
            marshaller.marshal(capabilities, capabilitiesDescriptionV111);
            WMSMarshallerPool.getInstance().release(marshaller);
        } catch (JAXBException e) {
//            TODO
            e.printStackTrace();
        }
//        TODO marshall new Service
    }
}
