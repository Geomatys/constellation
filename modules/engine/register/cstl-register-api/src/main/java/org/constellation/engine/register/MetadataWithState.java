
package org.constellation.engine.register;

import org.constellation.engine.register.jooq.tables.pojos.Metadata;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MetadataWithState extends Metadata {
    
    private boolean previousPublishState;
    
    public MetadataWithState() {
        super(Integer.SIZE, null, null, Integer.SIZE, Integer.MIN_VALUE, Integer.MIN_VALUE, 
              Integer.MIN_VALUE, Integer.SIZE, Long.MIN_VALUE, Long.MIN_VALUE, null, null, 
              Integer.MIN_VALUE, Boolean.TRUE, Boolean.TRUE, null, null, "NONE", null, null);
    }
    
    public MetadataWithState(Metadata metadata, boolean previousPublishState) {
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
        this.previousPublishState = previousPublishState;
        
    }

    /**
     * @return the previousPublishState
     */
    public boolean isPreviousPublishState() {
        return previousPublishState;
    }

    /**
     * @param previousPublishState the previousPublishState to set
     */
    public void setPreviousPublishState(boolean previousPublishState) {
        this.previousPublishState = previousPublishState;
    }
}
