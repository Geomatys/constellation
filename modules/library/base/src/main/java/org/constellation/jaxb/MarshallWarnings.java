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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sis.xml.MarshalContext;
import org.apache.sis.xml.ValueConverter;
import org.apache.sis.internal.storage.IOUtilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MarshallWarnings extends ValueConverter {

    // The warnings collected during (un)marshalling.
    private final List<String> messages = new ArrayList<String>();

    // Collects the warnings and allows the process to continue.
    @Override
    protected <T> boolean exceptionOccured(final MarshalContext context, final T value, final Class<T> sourceType, final Class<?> targetType, final Exception exception) {
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
    
   @Override
   public URI toURI(final MarshalContext context, String value) throws URISyntaxException {
        if (value != null && !(value = value.trim()).isEmpty()) try {
            value = IOUtilities.encodeURI(value);
            if (value.contains("\\")) {
                value = value.replace("\\", "%5C");
            }
            return new URI(value);
        } catch (URISyntaxException e) {
            if (!exceptionOccured(context, value, String.class, URI.class, e)) {
                throw e;
            }
        }
        return null;
    }
}
