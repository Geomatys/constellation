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

package org.constellation.configuration.ws.rs;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.generic.edmo.Organisation;
import org.constellation.generic.edmo.Organisations;
import org.constellation.generic.edmo.ws.EdmoWebservice;
import org.constellation.generic.edmo.ws.EdmoWebserviceSoap;
import org.constellation.generic.nerc.CodeTableType;
import org.constellation.generic.nerc.WhatsListsResponse;
import org.constellation.metadata.Utils;
import org.constellation.metadata.io.GenericMetadataReader;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.generic.database.Automatic.*;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;
import static org.constellation.ows.OWSExceptionCode.*;

// geotools dependencies
import org.geotools.metadata.note.Anchors;
import org.geotools.util.SimpleInternationalString;
import org.geotools.metadata.iso.citation.AddressImpl;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;

// GeoAPI dependencies
import org.opengis.metadata.citation.ResponsibleParty;

/**
 *
 * @author Guilhem Legal
 */
public class CSWconfigurer extends AbstractCSWConfigurer {

    public CSWconfigurer(Marshaller marshaller, Unmarshaller unmarshaller, ContainerNotifierImpl cn) throws ConfigurationException {
        super(marshaller, unmarshaller, cn);
    }
    
    /**
     * Update all the vocabularies skos files and the list of contact.
     */
    public AcknowlegementType updateVocabularies() throws WebServiceException {
        File vocabularyDir = new File(serviceDirectory.get("CSW"), "vocabulary");
        if (!vocabularyDir.exists()) {
            vocabularyDir.mkdir();
        }
        //  we get the Skos and description file for each used list
        saveVocabularyFile("P021",   vocabularyDir);
        saveVocabularyFile("L031",   vocabularyDir);
        saveVocabularyFile("L061",   vocabularyDir);
        saveVocabularyFile("C77",    vocabularyDir);
        saveVocabularyFile("EDMERP", vocabularyDir);
        saveVocabularyFile("L05",    vocabularyDir);
        saveVocabularyFile("L021",   vocabularyDir);
        saveVocabularyFile("L081",   vocabularyDir);
        saveVocabularyFile("L241",   vocabularyDir);
        saveVocabularyFile("L231",   vocabularyDir);
        saveVocabularyFile("C381",   vocabularyDir);
        saveVocabularyFile("C320",   vocabularyDir);
        saveVocabularyFile("C174",   vocabularyDir);
        saveVocabularyFile("C16",    vocabularyDir);
        saveVocabularyFile("C371",   vocabularyDir);
        saveVocabularyFile("L181",   vocabularyDir);
        saveVocabularyFile("C16",    vocabularyDir);
        saveVocabularyFile("L101",    vocabularyDir);
        
        return new AcknowlegementType("success", "the vocabularies has been succefully updated");
    }
    
    /**
     * Update all the contact retrieved from files and the list of contact.
     */
    public AcknowlegementType updateContacts() throws WebServiceException {
        File contactDir = new File(serviceDirectory.get("CSW"), "contacts");
        if (reader != null && reader instanceof GenericMetadataReader) {
            List<String> contactsID = ((GenericMetadataReader)reader).getAllContactID();
            for (String contactID : contactsID) {
                ResponsibleParty contact = loadContactFromEDMOWS(contactID);
                if (contact != null)
                    saveContactFile(contactID, contact, contactDir);
            }
        }
        
        return new AcknowlegementType("success", "the EDMO contacts has been succefully updated");
    }
    
    /**
     * 
     * @param listNumber
     */
    private void saveContactFile(String contactID, ResponsibleParty contact, File directory) throws WebServiceException {
        String filePrefix = "EDMO.";
        try {
            File f = new File(directory, filePrefix + contactID + ".xml");
            marshaller.marshal(contact, f);
        
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBException while marshalling the contact: " + contactID);
            throw new WebServiceException("JAXBException while marshalling the contact: " + contactID, NO_APPLICABLE_CODE, version);
        } 
    }
    
    
    /**
     * 
     * @param listNumber
     */
    private void saveVocabularyFile(String listNumber, File directory) throws WebServiceException {
        CodeTableType VocaDescription = getVocabularyDetails(listNumber);
        String filePrefix = "SDN.";
        String url = "http://vocab.ndg.nerc.ac.uk/list/" + listNumber + "/current";
        try {
            if (VocaDescription != null) {
                File f = new File(directory, filePrefix + listNumber + ".xml");
                marshaller.marshal(VocaDescription, f);
            } else {
                LOGGER.severe("no description for vocabulary: " + listNumber + " has been found");
            }
            
            Object vocab = Utils.getUrlContent(url, unmarshaller);
            if (vocab != null) {
                File f = new File(directory, filePrefix + listNumber + ".rdf");
                marshaller.marshal(vocab, f);
            } else {
                LOGGER.severe("no skos file have been found for :" + listNumber);
            }
        
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBException while marshalling the vocabulary: " + url);
            throw new WebServiceException("JAXBException while marshalling the vocabulary: " + url, NO_APPLICABLE_CODE, version);
        } catch (MalformedURLException ex) {
            LOGGER.severe("The url: " + url + " is malformed");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while contacting the URL:" + url);
            throw new WebServiceException("IO exception while contacting the URL:" + url, NO_APPLICABLE_CODE, version);
        }
    }
    
    /**
     * 
     * @param listNumber
     * @param directory
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private CodeTableType getVocabularyDetails(String listNumber) throws WebServiceException {
        String url = "http://vocab.ndg.nerc.ac.uk/axis2/services/vocab/whatLists?categoryKey=http://vocab.ndg.nerc.ac.uk/term/C980/current/CL12";
        CodeTableType result = null;
        try {
            
            Object obj = Utils.getUrlContent(url, unmarshaller);
            if (obj instanceof WhatsListsResponse) {
                WhatsListsResponse listDetails = (WhatsListsResponse) obj;
                result = listDetails.getCodeTableFromKey(listNumber);
            }
        
        } catch (MalformedURLException ex) {
            LOGGER.severe("The url: " + url + " is malformed");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while contacting the URL:" + url);
            throw new WebServiceException("IO exception while contacting the URL:" + url, NO_APPLICABLE_CODE, version);
        }
        return result;
    }
    
    public void destroy() {
        indexer.destroy();
    }
    
     /**
     * Try to get a contact from EDMO WS and add it to the map of contact.
     * 
     * @param contactIdentifiers
     */
    private ResponsibleParty loadContactFromEDMOWS(String contactID) {
        EdmoWebservice service = new EdmoWebservice();
        EdmoWebserviceSoap port = service.getEdmoWebserviceSoap();
        
        // we call the web service EDMO
        String result = port.wsEdmoGetDetail(contactID);
        StringReader sr = new StringReader(result);
        Object obj;
        try {
            obj = unmarshaller.unmarshal(sr);
            if (obj instanceof Organisations) {
                Organisations orgs = (Organisations) obj;
                switch (orgs.getOrganisation().size()) {
                    case 0:
                        LOGGER.severe("There is no organisation for the specified code: " + contactID);
                        break;
                    case 1:
                        LOGGER.info("contact created for contact ID: " + contactID);
                        return createContact(orgs.getOrganisation().get(0));
                    default:
                        LOGGER.severe("There is more than one contact for the specified code: " + contactID);
                        break;
                }
            }
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBException while getting contact from EDMO WS");
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Build a new Responsible party with the specified Organisation object retrieved from EDMO WS.
     * 
     * @param org
     * @return
     * @throws java.sql.SQLException
     */
    private ResponsibleParty createContact(Organisation org) {
        ResponsiblePartyImpl contact = new ResponsiblePartyImpl();
        contact.setOrganisationName(new SimpleInternationalString(org.getName()));
        try {
            URI uri = new URI("SDN:EDMO::" + org.getN_code());
            Anchors.create(org.getName(), uri); 
        } catch (URISyntaxException ex) {
            LOGGER.severe("URI syntax exeption while adding contact code.");
        }
        ContactImpl contactInfo = new ContactImpl();
        TelephoneImpl phone     = new TelephoneImpl();
        AddressImpl address     = new AddressImpl();
        OnLineResourceImpl or   = new OnLineResourceImpl();
                
        phone.setFacsimiles(Arrays.asList(org.getFax()));
        phone.setVoices(Arrays.asList(org.getPhone()));
        contactInfo.setPhone(phone);
        
        address.setDeliveryPoints(Arrays.asList(org.getAddress()));
        address.setCity(new SimpleInternationalString(org.getCity()));
        // TODO address.setAdministrativeArea(new SimpleInternationalString()); 
        address.setPostalCode(org.getZipcode());
        address.setCountry(new SimpleInternationalString(org.getC_country()));
        if (org.getEmail() != null) {
            address.setElectronicMailAddresses(Arrays.asList(org.getEmail()));
        }
        contactInfo.setAddress(address);
        
        try {
            or.setLinkage(new URI(org.getWebsite()));
        } catch (URISyntaxException ex) {
            LOGGER.severe("URI Syntax exception in contact online resource");
        }
        contactInfo.setOnLineResource(or);
        contact.setContactInfo(contactInfo);
        return contact;
    }
    
}
