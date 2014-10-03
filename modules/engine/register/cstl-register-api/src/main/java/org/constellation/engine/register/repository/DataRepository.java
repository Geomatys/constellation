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
package org.constellation.engine.register.repository;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.i18n.DataWithI18N;
import org.constellation.engine.register.DataXCsw;

import java.util.List;
import java.util.Map;

public interface DataRepository {

    List<Data> findAll();
    
    Data fromLayer(String layerAlias, String providerId);
    
    Data findById(int dataId);
    
    Data create(Data data);

    int delete(int id);

    int delete(String namespaceURI, String localPart, int providerId);

    Data findDataFromProvider(String namespaceURI, String localPart, String providerId);

    Data findByMetadataId(String metadataId);

    List<Data> findByProviderId(Integer id);

    List<Data> findByDatasetId(Integer id);
    
    List<Data> findAllByDatasetId(Integer id);
    
    DataWithI18N getDescription(Data data);


    Data findByNameAndNamespaceAndProviderId(String localPart, String namespaceURI, Integer providerId);

    void update(Data data);

    Map<Domain, Boolean> getLinkedDomains(int dataId);
    
    Data findByIdentifierWithEmptyMetadata(String localPart);

    List<Data> getCswLinkedData(final int cswId);
    
    DataXCsw addDataToCSW(final int serviceID, final int dataID);
    
    void removeDataFromCSW(final int serviceID, final int dataID);
}
