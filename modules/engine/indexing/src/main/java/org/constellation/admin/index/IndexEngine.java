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

package org.constellation.admin.index;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.admin.exception.ConstellationException;

import java.io.IOException;
import java.util.Set;

/**
 * @author Christophe Mourette (Geomatys).
 */
public interface IndexEngine {
    void addMetadataToIndexForData(DefaultMetadata metadata, Integer id) throws ConstellationException;
    void addMetadataToIndexForDataset(DefaultMetadata metadata, Integer id) throws ConstellationException;
    void removeDatasetMetadataFromIndex(Integer id) throws ConstellationException;
    void removeDataMetadataFromIndex(Integer id) throws ConstellationException;
    Set<Integer> searchOnMetadata(String queryString, String attributeId) throws ParseException, IOException;
}
