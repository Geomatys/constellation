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
