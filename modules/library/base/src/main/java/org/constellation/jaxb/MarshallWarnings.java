/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.jaxb;

import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.xml.ObjectConverters;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MarshallWarnings extends ObjectConverters {

    // The warnings collected during (un)marshalling.
    private final List<String> messages = new ArrayList<String>();

    // Collects the warnings and allows the process to continue.
    @Override
    protected <T> boolean exceptionOccured(T value, Class<T> sourceType, Class<?> targetType, Exception exception) {
        messages.add(exception.getLocalizedMessage() + " value=[" + value + "] sourceType:" + sourceType + " targetType:" + targetType);
        return true;
    }

    /**
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }
    
}
