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
package org.constellation.metadata.io;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.DomainValuesType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal 
 */
public class FileMetadataReader extends MetadataReader {

    private File dataDirectory;
    
    /**
     * A unMarshaller to get object from metadata files.
     */
    private final Unmarshaller unmarshaller;
    
    public FileMetadataReader(File dataDirectory, Unmarshaller unmarshaller) {
        super(true);
        this.dataDirectory = dataDirectory;
        this.unmarshaller  = unmarshaller;
    }
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws java.sql.SQLException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, WebServiceException {
        return getObjectFromFile(identifier);
    }

    private Object getObjectFromFile(String identifier) throws WebServiceException {
        File metadataFile = new File (dataDirectory,  identifier + ".xml");
        if (metadataFile.exists()) {
            try {
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                addInCache(identifier, metadata);
                return metadata;
            } catch (JAXBException ex) {
                throw new WebServiceException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            }
        } else {
            throw new WebServiceException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    @Override
    public List<DomainValuesType> getFieldDomainofValues(String propertyNames) throws WebServiceException {
        throw new WebServiceException("GetDomain operation are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public List<String> executeEbrimSQLQuery(String SQLQuery) throws WebServiceException {
        throw new WebServiceException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    @Override
    public List<? extends Object> getAllEntries() throws WebServiceException {
        List<Object> results = new ArrayList<Object>();
        for (File f : dataDirectory.listFiles()) {
            if (f.getName().endsWith(".xml")) {
                String identifier = f.getName().substring(0, f.getName().indexOf(".xml"));
                try {
                    Object metadata = unmarshaller.unmarshal(f);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    addInCache(identifier, metadata);
                    results.add(metadata);
                } catch (JAXBException ex) {
                    throw new WebServiceException("The metadataFile : " + f.getPath() + " can not be unmarshalled" + "\n" +
                            "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
                }
            } else {
                throw new WebServiceException("The metadataFile : " + f.getPath() + " is not present", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE);
    }

    /**
     * Return the list of Additional queryable element (0 in MDWeb).
     */
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<QName>();
    }

    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<String, List<String>>();
    }
}
