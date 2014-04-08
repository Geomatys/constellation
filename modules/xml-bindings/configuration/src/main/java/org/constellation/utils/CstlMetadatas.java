/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.utils;

import java.util.Date;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.ArgumentChecks;
import org.constellation.ServiceDef;
import org.constellation.dto.Service;
import static org.constellation.utils.CstlMetadataTemplate.DATA;
import static org.constellation.utils.CstlMetadataTemplate.SERVICE;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlMetadatas {
    
    /**
     * Create a default metadata for OGC service.
     *
     * @param serviceIdentifier the identifier of the service.
     * @param serviceType the OGC type of the service (WMS, SOS, WFS, ...)
     * @param cstlURL the current URL of the service.
     * @param serviceInfo Information about the service.
     * 
     * @return DefaultMetadata a new metadata for the service with given identifier.
     */
    public static DefaultMetadata defaultServiceMetadata(final String serviceIdentifier, final String serviceType, final String cstlURL, final Service serviceInfo)
    {
        final String serviceID = getMetadataIdForService(serviceIdentifier, serviceType);
        final DefaultMetadata metadata = defaultServiceMetadata(serviceID, serviceInfo);
        
        // add specific service part
        final MetadataFeeder feeder = new MetadataFeeder(metadata);
        final String serviceURL = cstlURL + "/WS/" + serviceType.toLowerCase() + '/' + serviceIdentifier;
        feeder.feedService(serviceInfo);
        feeder.addServiceInformation(serviceType, serviceURL);
        feeder.setServiceInstanceName(serviceIdentifier);
        return metadata;
    }
    
    
    public static String getMetadataIdForService(final String serviceName, final String serviceType){
        ArgumentChecks.ensureNonNull("serviceName", serviceName);
        ArgumentChecks.ensureNonNull("serviceType", serviceType);
        return SERVICE.getPrefix()+ '_' + serviceType + "_" + serviceName;
    }
    
    public static String getMetadataIdForData(final String dataName){
        ArgumentChecks.ensureNonNull("dataName", dataName);
        return DATA.getPrefix()+ '_' + dataName;
    }
    
    /**
     * Create a default metadata for the given data.
     *
     * @param metadataId the identifier of the metadata to create.
     * @param serviceInfo
     * 
     * @return A new {@link DefaultMetadata}. ISO_19115 compliant.
     */
    private static DefaultMetadata defaultServiceMetadata(final String metadataId, final Service serviceInfo) {

        ArgumentChecks.ensureNonNull("serviceInfo", serviceInfo);
        ArgumentChecks.ensureNonNull("metadataId", metadataId);
        final DefaultMetadata metadata = new DefaultMetadata();
        final MetadataFeeder feeder    = new MetadataFeeder(metadata);
        final Date creationDate        = new Date();

        // create the Service part
        feeder.getServiceIdentification();
        feeder.setTitle(serviceInfo.getName());
        feeder.setCitationIdentifier(metadataId);
        feeder.setCreationDate(creationDate);

        // Fills metadata with generic values
        metadata.setFileIdentifier(metadataId);
        metadata.setDateStamp(creationDate);

        return metadata;
    }
    
    public static String getIdentifier(final CstlMetadataTemplate type, final String metadataID) {
        if (type == DATA) {
            return metadataID.substring(type.getPrefix().length() + 1);
        } else if (type == SERVICE) {
            // remove prefix
            String tmp = metadataID.substring(type.getPrefix().length() + 1);
            // remove specification
            final int index = tmp.indexOf('_');
            tmp = tmp.substring(index + 1);
            return tmp;
        }
        return metadataID;
    }
    
    public static ServiceDef.Specification getSpecification(final String metadataID) {
        // remove prefix
        String tmp = metadataID.substring(SERVICE.getPrefix().length() + 1);
        // remove specification
        final int index = tmp.indexOf('_');
        tmp = tmp.substring(0, index);
        return ServiceDef.Specification.valueOf(tmp);
    }
}
