/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.metadata.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;

import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.generic.database.Automatic;
import static org.constellation.generic.database.Automatic.*;

import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.citation.AddressImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.geotools.metadata.iso.spatial.VectorSpatialRepresentationImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;

/**
 *
 * @author Guilhem Legal
 */
public class GenericMetadataReader extends MetadataReader {
    
    /**
     * A configuration object used in Generic database mode.
     */
    private Automatic genericConfiguration;
    
    private DateFormat dateFormat;
    
    
    public GenericMetadataReader(Automatic genericConfiguration) {
        super();
        this.genericConfiguration = genericConfiguration;
    }
    
    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotools metadata)
     * 
     * @throws java.sql.SQLException
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, WebServiceException {
        Object result = null;
        
        if (mode == ISO_19115) {
            
            result = getMetadataObject(identifier, type, elementName);
            
        } else if (mode == DUBLINCORE) {
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    private MetaDataImpl getMetadataObject(String identifier, ElementSetType type, List<QName> elementName) {
        MetaDataImpl result     = new MetaDataImpl();
        
        try {
        
        //TODO we verify that the identifier exists
        
        
        /*
         * static part
         */ 
        result.setFileIdentifier(identifier);
        result.setLanguage(Locale.ENGLISH);
        result.setCharacterSet(CharacterSet.UTF_8);
        
        switch (genericConfiguration.getType()) {
            
            case CDI: 
                result.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
                result.setHierarchyLevelNames(Arrays.asList("Common Data Index record"));
                break;
                
            case CSR: 
                result.setHierarchyLevels(Arrays.asList(ScopeCode.SERIES));
                result.setHierarchyLevelNames(Arrays.asList("Cruise Summary record"));
                break;
                
            case EDMED: 
                result.setHierarchyLevels(Arrays.asList(ScopeCode.SERIES));
                result.setHierarchyLevelNames(Arrays.asList("EDMED record"));
                break;
                
            
        } 
        
        /*
         * contact parts
         */
        ResultSet res = getVariables("var01");
        
        ResponsiblePartyImpl contact   = new ResponsiblePartyImpl();
        contact.setOrganisationName(new SimpleInternationalString(res.getString("TODO")));
        contact.setRole(Role.AUTHOR);
        
        ContactImpl contactInfo = new ContactImpl();
        TelephoneImpl phone     = new TelephoneImpl();
        AddressImpl address     = new AddressImpl();
        OnLineResourceImpl or   = new OnLineResourceImpl();
                
        phone.setFacsimiles(Arrays.asList(res.getString("TODO")));
        phone.setVoices(Arrays.asList(res.getString("TODO")));
        contactInfo.setPhone(phone);
        
        address.setDeliveryPoints(Arrays.asList(res.getString("TODO")));
        address.setCity(new SimpleInternationalString(res.getString("TODO")));
        address.setAdministrativeArea(new SimpleInternationalString(res.getString("TODO")));
        address.setPostalCode(res.getString("TODO"));
        address.setCountry(new SimpleInternationalString(res.getString("TODO")));
        address.setElectronicMailAddresses(Arrays.asList(res.getString("TODO")));
        contactInfo.setAddress(address);
        
        try {
            or.setLinkage(new URI(res.getString("TODO")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        
        contactInfo.setOnLineResource(or);
        
        contact.setContactInfo(contactInfo);

        result.setContacts(Arrays.asList(contact));
        res.close();
        
        /*
         * creation date TODO
         */ 
        
        /*
         * Spatial representation info
         */  
        res = getVariables("var02");
        
        VectorSpatialRepresentationImpl spatialRep = new VectorSpatialRepresentationImpl();
        GeometricObjectsImpl geoObj = new GeometricObjectsImpl();
        geoObj.setGeometricObjectType(GeometricObjectType.valueOf(res.getString("TODO")));
        spatialRep.setGeometricObjects(Arrays.asList(geoObj));
        
        result.setSpatialRepresentationInfo(Arrays.asList(spatialRep));
        
        /*
         * Reference system info TODO (issues referencing unmarshallable)
         */ 
        
        /*
         * extension information
         */
        
        /*
         * Data indentification
         */ 
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();
        
        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(getVariable("var04")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var05"))));
        
        CitationDateImpl revisionDate = new CitationDateImpl();
        try {
            Date d = dateFormat.parse(getVariable("var06"));
            revisionDate.setDate(d);
            revisionDate.setDateType(DateType.REVISION);
            citation.setDates(Arrays.asList(revisionDate));
        } catch (ParseException ex) {
            logger.severe("parse exception while parsing revision date");
        }
        
        res = getVariables("var07");
        
        contact   = new ResponsiblePartyImpl();
        contact.setOrganisationName(new SimpleInternationalString(res.getString("TODO")));
        contact.setRole(Role.ORIGINATOR);
        
        contactInfo = new ContactImpl();
        phone       = new TelephoneImpl();
        address     = new AddressImpl();
        or          = new OnLineResourceImpl();
                
        phone.setFacsimiles(Arrays.asList(res.getString("TODO")));
        phone.setVoices(Arrays.asList(res.getString("TODO")));
        contactInfo.setPhone(phone);
        
        address.setDeliveryPoints(Arrays.asList(res.getString("TODO")));
        address.setCity(new SimpleInternationalString(res.getString("TODO")));
        address.setAdministrativeArea(new SimpleInternationalString(res.getString("TODO")));
        address.setPostalCode(res.getString("TODO"));
        address.setCountry(new SimpleInternationalString(res.getString("TODO")));
        address.setElectronicMailAddresses(Arrays.asList(res.getString("TODO")));
        contactInfo.setAddress(address);
        
        try {
            or.setLinkage(new URI(res.getString("TODO")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        contactInfo.setOnLineResource(or);
        contact.setContactInfo(contactInfo);
        
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(new SimpleInternationalString(getVariable("var08")));
        
        } catch (SQLException ex) {
            logger.info("TODO remove");
        }
        
        return result;
    }
    
    private ResultSet getVariables(String... variables) {
        return null;
    }
    
    private String getVariable(String variable) {
        return null;
    }
}
