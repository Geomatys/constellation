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
package org.constellation.scheduler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;
import org.opengis.util.NoSuchIdentifierException;
import org.quartz.SimpleTrigger;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;

import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.xml.StaxStreamReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;

import static javax.xml.stream.XMLStreamReader.*;
import static org.constellation.scheduler.TasksConstants.*;

/**
 * Reader tasks from an xml file. A task can get a repeat step (in seconds), a start date ( formatted as YYYY/MM/dd HH:mm:ss)
 * and a start step (still in seconds), which is the step available only for the first execution.
 *
 * Ex : If you specify a start Step but no start date, the process will start at current time + start step seconds.
 * If you specify a start date + a start step, you'll start at specified date + start date.
 *
 * If a step is given, the task will be repeated every step seconds. Otherwise, task is executed only once.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @module pending
 */
public class TasksReader extends StaxStreamReader{
    
    public TasksReader(){
        
    }
    
    public List<Task> read() throws XMLStreamException, IOException {
        
        while (reader.hasNext()) {
            final int type = reader.next();
            if (type == START_ELEMENT
                    && reader.getLocalName().equalsIgnoreCase(TAG_TASKS)) {
                return readTasks();
            }
        }

        throw new XMLStreamException("Tasks tag not found");
    }
    
    private List<Task> readTasks() throws XMLStreamException, IOException {
        final List<Task> tasks = new ArrayList<Task>();
        
        while (reader.hasNext()) {
            final int type = reader.next();
            if (type == START_ELEMENT
                    && reader.getLocalName().equalsIgnoreCase(TAG_TASK)) {
                Task t;
                try {
                    t = readTask();
                } catch (NoSuchIdentifierException ex) {
                    t = null;
                    Logging.getLogger(TasksReader.class).log(Level.WARNING, ex.getMessage());
                }
                if(t != null){
                    tasks.add(t);
                }
            }else if(type == END_ELEMENT && reader.getLocalName().equals(TAG_TASKS)){
                break;
            }
        }
        
        return tasks;
    }
    
    private Task readTask() throws XMLStreamException, IOException, NoSuchIdentifierException {
        
        final String id = reader.getAttributeValue(null, ATT_ID);
        final String authority = reader.getAttributeValue(null, ATT_AUTHORITY);
        final String code = reader.getAttributeValue(null, ATT_CODE);
        final String title = reader.getAttributeValue(null, ATT_TITLE);
        final ParameterDescriptorGroup inputDesc = ProcessFinder.getProcessDescriptor(authority, code).getInputDescriptor();
        
        final Task task = new Task(id);
        task.setTitle(title);
        
        ParameterValueGroup params = null;
        SimpleTrigger trigger = null;
        
        
        
        while (reader.hasNext()) {
            final int type = reader.next();
            if (type == START_ELEMENT){
                final String localName = reader.getLocalName();
                
                if(localName.equalsIgnoreCase(TAG_PARAMETERS)) {
                    params = readParameters(inputDesc);
                }else if(localName.equalsIgnoreCase(TAG_TRIGGER)) {
                    trigger = readTrigger();
                }
            }else if(type == END_ELEMENT){
                break;
            }
        }
                
        
        final ProcessJobDetail detail = new ProcessJobDetail(authority, code, params);
        task.setDetail(detail);
        task.setTrigger(trigger);        
        return task;
    }
    
    private ParameterValueGroup readParameters(final ParameterDescriptorGroup desc) throws XMLStreamException, IOException {
        final ParameterValueReader prmreader = new ParameterValueReader(desc);
        prmreader.setInput(reader);
        return (ParameterValueGroup) prmreader.read();
    }
    
    private SimpleTrigger readTrigger() throws XMLStreamException {

        int step = 0, startStep = 0;
        Date startDate;

        final String strStep      = reader.getAttributeValue(null, ATT_STEP);
        final String strStartDate = reader.getAttributeValue(null, ATT_START_DATE);
        final String strStartStep = reader.getAttributeValue(null, ATT_START_STEP);

        if(strStep != null) {
            step = Integer.decode(strStep);
        }

        if(strStartStep != null) {
            startStep = Integer.decode(strStartStep);
        }

        if(strStartDate != null) {
            DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                startDate = format.parse(strStartDate);
            } catch (ParseException e) {
                throw new XMLStreamException("Unable to parse the date given in "+ ATT_START_DATE + "attribute. It must be " +
                        "formatted as yyyy/MM/dd HH:mm:ss", e);
            }
        } else {
            startDate = new Date();
        }

        // Increment start step to ensure that the first iteration date won't be in the past at trigger start.
        if (++startStep > 0) {
            startDate.setTime(startDate.getTime()+1000*startStep);
        }

        toTagEnd(TAG_TRIGGER);
        
        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        tb.startAt(startDate);
        if (step > 0) {
            tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever((int)step));
        }
        return (SimpleTrigger) tb.build();
    }
    
}
