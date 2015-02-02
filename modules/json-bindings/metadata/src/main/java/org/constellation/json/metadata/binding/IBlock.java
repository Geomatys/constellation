
package org.constellation.json.metadata.binding;

/**
 *
 * @author guilhem
 */
public interface IBlock {
    
    Block addBlock(int index, Block block);

    void removeBlock(BlockObj blockObj);
    
    boolean childrenEmpty();
}
