
package org.constellation.json.metadata.binding;

/**
 *
 * @author guilhem
 */
public class FieldObjDiff extends FieldObj {
    
    private String state;

    public FieldObjDiff() {
        
    }
    
    public FieldObjDiff(FieldObj fo, final String state) {
        super(fo);
        this.state = state;
    }
    
    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
}