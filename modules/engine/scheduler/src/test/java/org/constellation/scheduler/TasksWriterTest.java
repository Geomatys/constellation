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
package org.constellation.scheduler;

import org.apache.sis.test.XMLComparator;
import org.constellation.scheduler.configuration.XMLTasksWriter;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.junit.Test;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
