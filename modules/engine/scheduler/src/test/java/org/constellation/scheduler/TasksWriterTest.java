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

import org.constellation.scheduler.configuration.XMLTasksWriter;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.sis.test.XMLComparator;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;

import org.junit.Test;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;

import org.xml.sax.SAXException;

/**
 * Test task writer.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class TasksWriterTest {
    
    public TasksWriterTest() {
    }


    @Test
    public void testWriter() throws IOException, XMLStreamException, ParserConfigurationException, SAXException, NoSuchIdentifierException {
        final String authority = "mymaths";
        final String code = "add";
        
        final ParameterDescriptorGroup desc = ProcessFinder.getProcessDescriptor(authority, code).getInputDescriptor();
        
        final Collection<Task> tasks = new ArrayList<Task>();
        
        Task task = new Task("task1");
        task.setTitle("do something");
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(150))
                .build());
        ParameterValueGroup values = desc.createValue();
        values.parameter("first").setValue(15);
        values.parameter("second").setValue(5);
        ProcessJobDetail detail = new ProcessJobDetail(authority, code, values);
        task.setDetail(detail);
        tasks.add(task);
        
        task = new Task("task2");
        task.setTitle("do something else");
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(60))
                .build());
        values = desc.createValue();
        values.parameter("first").setValue(21);
        values.parameter("second").setValue(13);
        detail = new ProcessJobDetail(authority, code, values);
        task.setDetail(detail);
        tasks.add(task);
        
        
        
        final File tempfile = File.createTempFile("tasks", ".xml");
        tempfile.deleteOnExit();
        final XMLTasksWriter writer = new XMLTasksWriter();
        writer.setOutput(tempfile);
        writer.write(tasks);
        writer.dispose();
        
        final XMLComparator comparator = new XMLComparator(
                TasksReaderTest.class.getResource("/org/constellation/scheduler/tasks.xml"), 
                tempfile);
        comparator.tolerance = 0.0000001;
        comparator.compare();
        
        
    }
}
