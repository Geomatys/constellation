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
package org.constellation.business;

import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IMetadataBusiness {
    
    String searchMetadata(final String metadataId, final boolean includeService);

    boolean existInternalMetadata(final String metadataID, final boolean includeService);

    List<String> getInternalMetadataIds(final boolean includeService);

    List<String> getAllMetadata(final boolean includeService);
    
    boolean updateMetadata(final String metadataId, final String xml);
    
    List<String> getLinkedMetadataIDs(final String cswIdentifier);
    
    void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier);
    
    void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier);
}
