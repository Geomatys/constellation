
package org.constellation.json.metadata.binding;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author guilhem
 */
@JsonTypeInfo(  
    use = JsonTypeInfo.Id.NAME,  
    include = JsonTypeInfo.As.PROPERTY,  
    property = "type") 
@JsonSubTypes({  
    @Type(value = FieldObj.class, name = "field"),  
    @Type(value = BlockObj.class, name = "block") })  
public abstract class ComponentObj {
    
    abstract void updatePath(final String oldPrefix, final String newPrefix);
}
