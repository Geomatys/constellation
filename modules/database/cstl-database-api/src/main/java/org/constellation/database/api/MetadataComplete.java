/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.database.api;

import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.constellation.database.api.jooq.tables.pojos.MetadataBbox;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MetadataComplete extends Metadata {
    
    private List<MetadataBbox> bboxes = new ArrayList<>();

    public MetadataComplete() {
        super(Integer.SIZE, null, null, Integer.SIZE, Integer.MIN_VALUE, Integer.MIN_VALUE, 
              Integer.MIN_VALUE, Integer.SIZE, Long.MIN_VALUE, Long.MIN_VALUE, null, null, 
              Integer.MIN_VALUE, Boolean.TRUE, Boolean.TRUE, null, null, "NONE", null, null);
    }
    
    public MetadataComplete(Metadata metadata, List<MetadataBbox> bboxes) {
        super(metadata.getId(),
              metadata.getMetadataId(),
              metadata.getMetadataIso(),
              metadata.getDataId(),
              metadata.getDatasetId(),
              metadata.getServiceId(),
              metadata.getMdCompletion(),
              metadata.getOwner(),
              metadata.getDatestamp(),
              metadata.getDateCreation(),
              metadata.getTitle(),
              metadata.getProfile(),
              metadata.getParentIdentifier(),
              metadata.getIsValidated(),
              metadata.getIsPublished(),
              metadata.getLevel(),
              metadata.getResume(),
              metadata.getValidationRequired(),
              metadata.getValidatedState(),
              metadata.getComment());
        this.bboxes = bboxes;
        
    }
    /**
     * @return the bboxes
     */
    public List<MetadataBbox> getBboxes() {
        return bboxes;
    }

    /**
     * @param bboxes the bboxes to set
     */
    public void setBboxes(List<MetadataBbox> bboxes) {
        this.bboxes = bboxes;
    }
}
