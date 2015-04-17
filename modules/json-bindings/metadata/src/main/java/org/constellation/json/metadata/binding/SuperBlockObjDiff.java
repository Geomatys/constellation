
package org.constellation.json.metadata.binding;

/**
 *
 * @author guilhem
 */
public class SuperBlockObjDiff extends SuperBlockObj {
    
    private String state;

    public SuperBlockObjDiff() {
        
    }
    
    public SuperBlockObjDiff(SuperBlockObj sbo, final String state) {
        super(sbo);
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
