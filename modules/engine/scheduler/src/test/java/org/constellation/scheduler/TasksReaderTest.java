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

import org.constellation.scheduler.configuration.XMLTasksReader;
import java.util.List;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import org.quartz.impl.triggers.SimpleTriggerImpl;

import static org.junit.Assert.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class TasksReaderTest {
    
    public TasksReaderTest() {
    }


    @Test
    public void testReader() throws IOException, XMLStreamException {
        
        final XMLTasksReader reader = new XMLTasksReader();
        reader.setInput(TasksReaderTest.class.getResource("/org/constellation/scheduler/tasks.xml"));
        
        final List<Task> tasks = reader.read();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        
        
        final Task one = tasks.get(0);
        assertEquals("task1", one.getId());
        assertEquals("do something", one.getTitle());
        assertEquals("mymaths", one.getDetail().getFactoryIdentifier());
        assertEquals("add", one.getDetail().getProcessIdentifier());
        assertEquals(new Double(15), one.getDetail().getParameters().parameter("first").getValue());
        assertEquals(new Double(5), one.getDetail().getParameters().parameter("second").getValue());
        assertEquals(150000, ((SimpleTriggerImpl)one.getTrigger()).getRepeatInterval());
                
        final Task two = tasks.get(1);
        assertEquals("task2", two.getId());
        assertEquals("do something else", two.getTitle());
        assertEquals("mymaths", two.getDetail().getFactoryIdentifier());
        assertEquals("add", two.getDetail().getProcessIdentifier());
        assertEquals(new Double(21), two.getDetail().getParameters().parameter("first").getValue());
        assertEquals(new Double(13), two.getDetail().getParameters().parameter("second").getValue());
        assertEquals(60000, ((SimpleTriggerImpl)two.getTrigger()).getRepeatInterval());
        
    }
}
