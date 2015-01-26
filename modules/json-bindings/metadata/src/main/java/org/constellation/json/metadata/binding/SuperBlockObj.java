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

import java.io.Serializable;

/**
 * Pojo class used for Jackson that represents the binding for superblock object
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
public class SuperBlockObj implements Serializable {

    private SuperBlock superblock;

    public SuperBlockObj(){

    }
    
    public SuperBlockObj(final SuperBlockObj sbj){
        this.superblock = new SuperBlock(sbj.superblock);
    }

    public SuperBlock getSuperblock() {
        return superblock;
    }

    public void setSuperblock(SuperBlock superblock) {
        this.superblock = superblock;
    }
    
    @Override
    public String toString() {
        return "[SuperBlockObj]\nsuperBlock:" + superblock;
    }
}
