
package org.constellation.json.metadata.binding;

/**
 *
 * @author guilhem
 */
public class BlockObjDiff extends BlockObj {
    
    private String state;

    public BlockObjDiff() {
        
    }
    
    public BlockObjDiff(BlockObj bo, final String state) {
        super(bo);
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