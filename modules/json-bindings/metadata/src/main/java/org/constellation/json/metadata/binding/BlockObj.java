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

package org.constellation.json.metadata.binding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.io.Serializable;

/**
 * Pojo class used for Jackson that represents the binding for block object
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class BlockObj extends ComponentObj implements Serializable {

    private Block block;

    public BlockObj() {

    }
    
    public BlockObj(final BlockObj block) {
        this.block = new Block(block.block);
    }
    
    public BlockObj(final Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String getPath() {
        return block.getPath();
    }
    
    public static BlockObj diff(BlockObj originalB, BlockObj modifiedB) {
        final Block b = Block.diff(originalB.block, modifiedB.block);
        if (b != null) {
            return new BlockObj(b);
        }
        return null;
    }

    @Override
    public void updatePath(final String oldPrefix, final String newPrefix) {
        block.updatePath(oldPrefix, newPrefix);
    }
    
    @Override
    public String toString() {
        return "[BlockObj]\nsuperBlock:" + block;
    }

}
