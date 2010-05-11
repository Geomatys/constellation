/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

// J2SE dependencies
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.generic.GenericReader;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import static org.constellation.metadata.io.AbstractMetadataReader.*;

// Geotoolkit dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

//geoAPI dependencies
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericMetadataReader extends GenericReader implements CSWMetadataReader {
    
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
    public GenericMetadataReader(Automatic configuration) throws MetadataIoException {
        super(configuration);
        try {
            marshallerPool   = new MarshallerPool(getJAXBContext());
            contacts         = loadContacts(new File(configuration.getConfigurationDirectory(), "contacts"));
        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader: \n cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * Build a new Generic metadata reader and initialize the statement (with a flag for filling the Anchors).
     * @param configuration
     */
    public GenericMetadataReader(Automatic configuration, boolean fillAnchor) throws MetadataIoException {
        super(configuration);
      try {
            contacts         = new HashMap<String, ResponsibleParty>();
            marshallerPool   = new MarshallerPool(getJAXBContext());
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
    protected GenericMetadataReader(Automatic configuration, Map<String, ResponsibleParty> contacts) throws MetadataIoException {
        super(configuration);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        try {
            this.contacts      = contacts;
            marshallerPool     = new MarshallerPool(getJAXBContext());
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
                    Unmarshaller unmarshaller = null;
                    try {
                        unmarshaller = marshallerPool.acquireUnmarshaller();
                        final Object obj = unmarshaller.unmarshal(f);
                        if (obj instanceof ResponsibleParty) {
                            final ResponsibleParty contact = (ResponsibleParty) obj;
                            String code = f.getName();
                            code = code.substring(code.indexOf("EDMO.") + 5, code.indexOf(".xml"));
                            results.put(code, contact);
                        }
                    } catch (JAXBException ex) {
                        LOGGER.severe("Unable to unmarshall the contact file : " + f.getPath());
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    } finally {
                        if (unmarshaller != null) {
                            marshallerPool.release(unmarshaller);
                        }
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
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    private Values loadData(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
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

    @Override
    public Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, elementName);
    }

    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata)
     * 
     * @throws MetadataIoException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        
        //TODO we verify that the identifier exists
        final Values values = loadData(identifier, mode, type, elementName);

        final Object result;
        if (mode == ISO_19115) {
            result = getISO(identifier, values);
            
        } else if (mode == DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName, values);
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    /**
     * return a metadata in dublin core representation.
     * 
     * @param identifier
     * @param type
     * @param elementName
     * @return
     */
    protected abstract AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName, Values values);
    
    /**
     * return a metadata in ISO representation.
     * 
     * @param identifier
     * @return
     */
    protected abstract DefaultMetadata getISO(String identifier, Values values);
    
    /**
     * Return a list of variables name used for the dublicore representation.
     * @return
     */
    protected abstract List<String> getVariablesForDublinCore(ElementSetType type, List<QName> elementName);

    /**
     * Return a list of variables name used for the ISO representation.
     * @return
     */
    protected abstract List<String> getVariablesForISO();

       
    /**
     * Return a list of contact id used in this database.
     */
    public abstract List<String> getVariablesForContact();

    /**
     * Return a list of package ':' separated use to create JAXBContext for the unmarshaller.
     */
    protected abstract Class[] getJAXBContext();
    
    /**
     * Return all the entries from the database.
     * 
     * @return
     * @throws MetadataIoException
     */
    @Override
    public List<DefaultMetadata> getAllEntries() throws MetadataIoException {
        final List<DefaultMetadata> result = new ArrayList<DefaultMetadata>();
        final List<String> identifiers  = getAllIdentifiers();
        for (String id : identifiers) {
            result.add((DefaultMetadata) getMetadata(id, ISO_19115, ElementSetType.FULL, null));
        }
        return result;
    }
    
    /**
     * Return all the identifiers in this database.
     * 
     * @return
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        return getMainQueryResult();
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
    
    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * close all the statements and clear the maps.
     */
    @Override
    public void destroy() {
        super.destroy();
        contacts.clear();
        LOGGER.info("destroying generic metadata reader");
    }
            
}
