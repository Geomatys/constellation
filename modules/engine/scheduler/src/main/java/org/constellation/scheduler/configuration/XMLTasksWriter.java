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
package org.constellation.scheduler.configuration;

import java.io.IOException;
import java.util.Collection;
import javax.xml.stream.XMLStreamException;
import org.constellation.scheduler.Task;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.xml.StaxStreamWriter;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.ParameterValueGroup;
import org.quartz.SimpleTrigger;

import static org.constellation.scheduler.configuration.XMLTasksConstants.*;

/**
 * Write tasks in xml.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class XMLTasksWriter extends StaxStreamWriter{

    public XMLTasksWriter() {
    }
    
    public void write(final Collection<? extends Task> tasks) throws XMLStreamException, IOException{
        writer.setDefaultNamespace("http://www.geotoolkit.org/parameter");
        writer.writeStartDocument();        
        writer.writeStartElement(TAG_TASKS);
        
        for(final Task t : tasks){
            write(t);
        }
        
        writer.writeEndElement();        
        writer.writeEndDocument();        
    }
    
    private void write(final Task task) throws XMLStreamException, IOException{
        final ProcessJobDetail detail = task.getDetail();
        
        writer.writeStartElement(TAG_TASK);
        writer.writeAttribute(ATT_ID, task.getId());
        writer.writeAttribute(ATT_TITLE, task.getTitle());
        writer.writeAttribute(ATT_AUTHORITY, detail.getFactoryIdentifier());
        writer.writeAttribute(ATT_CODE, detail.getProcessIdentifier());
        
        writeTrigger(task.getTrigger());
        writeParameters(detail.getParameters());
        
        writer.writeEndElement();      
    }
    
    private void writeTrigger(final SimpleTrigger trigger) throws XMLStreamException{        
        writer.writeStartElement(TAG_TRIGGER);
        writer.writeAttribute(ATT_STEP, String.valueOf(trigger.getRepeatInterval()/1000));
        writer.writeEndElement();
    }
    
    private void writeParameters(final ParameterValueGroup params) throws XMLStreamException, IOException{        
        writer.writeStartElement(TAG_PARAMETERS);        
        final ParameterValueWriter paramWriter = new ParameterValueWriter();
        paramWriter.setOutput(writer);
        paramWriter.writeForInsertion(params);
        writer.writeEndElement();
    }
    
}
