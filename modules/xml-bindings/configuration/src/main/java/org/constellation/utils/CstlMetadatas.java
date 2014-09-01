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

package org.constellation.utils;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.ArgumentChecks;
import org.constellation.dto.Details;
import org.geotoolkit.feature.type.Name;

import java.util.Date;
import java.util.List;

import static org.constellation.utils.CstlMetadataTemplate.DATA;
import static org.constellation.utils.CstlMetadataTemplate.PROVIDER;
import static org.constellation.utils.CstlMetadataTemplate.SERVICE;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlMetadatas {
    
//    /**
//     * Create a default metadata for OGC service.
//     *
//     * @param serviceIdentifier the identifier of the service.
//     * @param serviceType the OGC type of the service (WMS, SOS, WFS, ...)
//     * @param cstlURL the current URL of the service.
//     * @param serviceInfo Information about the service.
//     *
//     * @return DefaultMetadata a new metadata for the service with given identifier.
//     */
//    public static DefaultMetadata defaultServiceMetadata(final String serviceIdentifier, final String serviceType, final String cstlURL, final Details serviceInfo) {
//        final String serviceID = getMetadataIdForService(serviceIdentifier, serviceType);
//        final DefaultMetadata metadata = defaultServiceMetadata(serviceID, serviceInfo);
//
//        // add specific service part
//        final MetadataFeeder feeder = new MetadataFeeder(metadata);
//        final String serviceURL = cstlURL + "/WS/" + serviceType.toLowerCase() + '/' + serviceIdentifier;
//        feeder.feedService(serviceInfo);
//        feeder.addServiceInformation(serviceType, serviceURL);
//        feeder.setServiceInstanceName(serviceIdentifier);
//        return metadata;
//    }
    
    public static void updateServiceMetadataURL(final String serviceIdentifier, final String serviceType, final String cstlURL, final DefaultMetadata metadata) {
        final MetadataFeeder feeder = new MetadataFeeder(metadata);
        final String serviceURL = cstlURL + "/WS/" + serviceType.toLowerCase() + '/' + serviceIdentifier;
        feeder.updateServiceURL(serviceURL);
    }
    
//    public static void updateServiceMetadataLayer(final DefaultMetadata metadata, final List<String> layerIds) {
//        final MetadataFeeder feeder = new MetadataFeeder(metadata);
//        feeder.setServiceMetadataIdForData(layerIds);
//    }
    
//    public static void addServiceMetadataLayer(final DefaultMetadata metadata, final String layerId) {
//        final MetadataFeeder feeder = new MetadataFeeder(metadata);
//        feeder.addServiceMetadataIdForData(layerId);
//    }
    
//    public static String getMetadataIdForService(final String serviceName, final String serviceType){
//        ArgumentChecks.ensureNonNull("serviceName", serviceName);
//        ArgumentChecks.ensureNonNull("serviceType", serviceType);
//        return serviceType + "_" + serviceName;
//    }
    
//    public static String getMetadataIdForProvider(final String providerId){
//        ArgumentChecks.ensureNonNull("providerId", providerId);
//        return PROVIDER.getPrefix()+ '_' + providerId;
//    }
    
    public static String getMetadataIdForData(final String providerId, final Name dataName){
        ArgumentChecks.ensureNonNull("dataName", dataName);
        ArgumentChecks.ensureNonNull("providerId", providerId);
        return  providerId + '_' + dataName.getLocalPart(); // TODO namespace?
    }
    
    public static String getMetadataIdForDataset(final String providerId){
        ArgumentChecks.ensureNonNull("providerId", providerId);
        return  providerId; 
    }
    
//    public static String getMetadataIdForLayer(final String dataName){
//        ArgumentChecks.ensureNonNull("dataName", dataName);
//        return DATA.getPrefix()+ '_' + dataName;
//    }
    
//    /**
//     * Create a default metadata for the given data.
//     *
//     * @param metadataId the identifier of the metadata to create.
//     * @param serviceInfo
//     *
//     * @return A new {@link DefaultMetadata}. ISO_19115 compliant.
//     */
//    private static DefaultMetadata defaultServiceMetadata(final String metadataId, final Details serviceInfo) {
//
//        ArgumentChecks.ensureNonNull("serviceInfo", serviceInfo);
//        ArgumentChecks.ensureNonNull("metadataId", metadataId);
//        final DefaultMetadata metadata = new DefaultMetadata();
//        final MetadataFeeder feeder    = new MetadataFeeder(metadata);
//        final Date creationDate        = new Date();
//
//        // create the Service part
//        feeder.getServiceIdentification();
//        feeder.setTitle(serviceInfo.getName());
//        feeder.setCitationIdentifier(metadataId);
//        feeder.setCreationDate(creationDate);
//
//        // Fills metadata with generic values
//        metadata.setFileIdentifier(metadataId);
//        metadata.setDateStamp(creationDate);
//
//        return metadata;
//    }
}
