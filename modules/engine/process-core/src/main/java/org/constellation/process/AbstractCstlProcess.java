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
package org.constellation.process;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ServiceBusiness;
import org.geotoolkit.io.X364;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCstlProcess extends AbstractProcess {
    
    protected static final Logger LOGGER = Logging.getLogger(AbstractCstlProcess.class);
    
    protected static final boolean X364_SUPPORTED = X364.isSupported();
    
    @Autowired
    protected ServiceBusiness serviceBusiness;
    
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
