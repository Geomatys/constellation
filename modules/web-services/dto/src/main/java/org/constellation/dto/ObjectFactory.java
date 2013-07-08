package org.constellation.dto;

import javax.xml.bind.annotation.XmlRegistry;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@XmlRegistry
public class ObjectFactory {

    private static final Logger LOGGER = Logger.getLogger(ObjectFactory.class.getName());

    public ObjectFactory() {
    }

    public Service createService(){
        return new Service();
    }

    public AccessConstraint createAccessConstraint(){
        return new AccessConstraint();
    }

    public Contact getContact(){
        return new Contact();
    }

}
