/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.metadata.io.generic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import static org.constellation.metadata.io.AbstractMetadataReader.*;

import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class SDNGenericMetadataReader extends GenericMetadataReader {

    /**
     * An unmarshaller used for getting EDMO data.
     */
    protected final MarshallerPool marshallerPool;

    /**
     * A map of the already retrieved contact from EDMO WS.
     */
    private Map<String, ResponsibleParty> contacts;

    /**
     * A flag mode indicating we are searching the database for contacts.
     */
    private static final int CONTACT = 10;

    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param configuration
     */
    public SDNGenericMetadataReader(Automatic configuration) throws MetadataIoException {
        super(configuration);
        try {
            marshallerPool   = new MarshallerPool(JAXBContext.newInstance(getJAXBContext()), null);
            contacts         = loadContacts(new File(configuration.getConfigurationDirectory(), "contacts"));
        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader: \n cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * Build a new Generic metadata reader for test purpose.
     *
     * it replace the SQL statement by specified datas.
     *
     * @param genericConfiguration
     */
    protected SDNGenericMetadataReader(Automatic configuration, Map<String, ResponsibleParty> contacts) throws MetadataIoException {
        super(configuration);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        try {
            this.contacts      = contacts;
            marshallerPool     = new MarshallerPool(JAXBContext.newInstance(getJAXBContext()), null);
        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader: \n cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * Load a Map of contact from the specified directory
     */
    private Map<String, ResponsibleParty> loadContacts(File contactDirectory) {
        final Map<String, ResponsibleParty> results = new HashMap<String, ResponsibleParty>();
        if (contactDirectory.isDirectory()) {
            if (contactDirectory.listFiles().length == 0) {
                LOGGER.severe("the contacts folder is empty :" + contactDirectory.getPath());
            }
            for (File f : contactDirectory.listFiles()) {
                if (f.getName().startsWith("EDMO.") && f.getName().endsWith(".xml")) {
                    try {
                        final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                        final Object obj = unmarshaller.unmarshal(f);
                        marshallerPool.recycle(unmarshaller);
                        if (obj instanceof ResponsibleParty) {
                            final ResponsibleParty contact = (ResponsibleParty) obj;
                            String code = f.getName();
                            code = code.substring(code.indexOf("EDMO.") + 5, code.indexOf(".xml"));
                            results.put(code, contact);
                        }
                    } catch (JAXBException ex) {
                        LOGGER.severe("Unable to unmarshall the contact file : " + f.getPath());
                        LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *
     * @param contactIdentifier
     * @return
     */
    protected ResponsibleParty getContact(String contactIdentifier, Role role) {
        if (contacts != null) {
            DefaultResponsibleParty result = (DefaultResponsibleParty)contacts.get(contactIdentifier);
            if (result != null) {
                result = new DefaultResponsibleParty(result);
                result.setRole(role);
            }
            return result;
        }
        return null;
    }

    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *
     * @param contactIdentifier
     * @return
     */
    protected ResponsibleParty getContact(String contactIdentifier, Role role, String individualName) {
        DefaultResponsibleParty result = (DefaultResponsibleParty)contacts.get(contactIdentifier);
        if (result != null) {
            result = new DefaultResponsibleParty(result);
            result.setRole(role);
            if (individualName != null)
                result.setIndividualName(individualName);
        }
        return result;
    }

    /**
     * Return all the contact identifiers used in this database
     *
     * @return
     * @throws org.constellation.ws.MetadataIoException
     */
    public List<String> getAllContactID(Values values) throws MetadataIoException {
        final List<String> results = new ArrayList<String>();
        final List<String> identifiers = getAllIdentifiers();
        for (String id : identifiers) {
            loadData(id, CONTACT, null, null);
            for(String var: getVariablesForContact()) {
                final String contactID = values.getVariable(var);
                if (contactID == null) {
                    final List<String> contactIDs = values.getVariables(var);
                    if (contactIDs != null) {
                        for (String cID : contactIDs) {
                            if (!results.contains(cID))
                                results.add(cID);
                        }
                    }
                } else {
                    if (!results.contains(contactID))
                        results.add(contactID);
                }
            }
        }
        return results;
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    @Override
    protected Values loadData(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        LOGGER.finer("loading data for " + identifier);

        final List<String> variables;

        if (mode == ISO_19115) {
            variables = getVariablesForISO();
        } else {
            if (mode == DUBLINCORE) {
                variables = getVariablesForDublinCore(type, elementName);
            } else if (mode == CONTACT) {
                variables = getVariablesForContact();
            } else {
                throw new IllegalArgumentException("unknow mode");
            }
        }
        return loadData(variables, identifier);
    }

     /**
     * Return a list of contact id used in this database.
     */
    public abstract List<String> getVariablesForContact();

    /**
     * Return a list of package ':' separated use to create JAXBContext for the unmarshaller.
     */
    protected abstract Class[] getJAXBContext();

    /**
     * close all the statements and clear the maps.
     */
    @Override
    public void destroy() {
        super.destroy();
        contacts.clear();
        LOGGER.info("destroying SDN generic metadata reader");
    }
}
