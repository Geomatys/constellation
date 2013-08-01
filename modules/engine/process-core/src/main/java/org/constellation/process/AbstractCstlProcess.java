/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.process;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.io.X364;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.apache.sis.util.logging.Logging;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCstlProcess extends AbstractProcess {
    
    protected static final Logger LOGGER = Logging.getLogger(AbstractCstlProcess.class);
    
    protected static final boolean X364_SUPPORTED = X364.isSupported();
    
    public AbstractCstlProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }
    
    // affichage console ///////////////////////////////////////////////////////
    
    protected static void console(Object ... texts){
        final String text;
        if (texts.length == 1) {
            text = String.valueOf(texts[0]);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (Object obj : texts) {
                if (obj instanceof X364) {
                    if (X364_SUPPORTED) {
                        sb.append(((X364) obj).sequence());
                    }
                } else {
                    sb.append(obj);
                }
            }
            text = sb.toString();
        }
        System.out.print(text);
    }

    protected static void log(Throwable ex, Object ... texts){
        final String text;
        if (texts.length == 1) {
            text = String.valueOf(texts[0]);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (Object obj : texts) {
                if (obj instanceof X364) {
                    if (X364_SUPPORTED) {
                        sb.append(((X364) obj).sequence());
                    }
                } else {
                    sb.append(obj);
                }
            }
            text = sb.toString();
        }
        LOGGER.log(Level.WARNING, text, ex);
    }

}
