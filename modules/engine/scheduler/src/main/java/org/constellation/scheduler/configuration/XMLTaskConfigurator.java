/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.scheduler.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.scheduler.Task;
import org.constellation.scheduler.TaskConfigurator;

/**
 * Task configurator saving configuration in an XML file.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class XMLTaskConfigurator implements TaskConfigurator {
    
    private static final String TASK_FILE = "scheduler-tasks.xml";    

    @Override
    public List<Task> getTasksConfiguration() throws ConfigurationException {
        //open configuration file
        final File configDir = ConfigDirectory.getConfigDirectory();
        final File taskFile = new File(configDir, TASK_FILE);
        if(!taskFile.exists()){
            return Collections.EMPTY_LIST;
        }
        
        final XMLTasksReader reader = new XMLTasksReader();
        try{
            reader.setInput(taskFile);
            final List<Task> candidates = reader.read();
            return candidates;
        }catch(IOException|XMLStreamException ex){
            throw new ConfigurationException(ex);
        }finally{
            try {
                reader.dispose();
            } catch (IOException|XMLStreamException ex) {
                throw new ConfigurationException(ex);
            }
        }
    }

    @Override
    public Task getTaskConfiguration(final String taskId) throws ConfigurationException {
        final List<Task> tasks = getTasksConfiguration();
        for(Task t : tasks){
            if(taskId.equals(t.getId())){
                return t;
            }
        }
        return null;
    }

    @Override
    public void addTaskConfiguration(final Task task) throws ConfigurationException {
        final List<Task> tasks = getTasksConfiguration();
        tasks.add(task);
        try {
            saveTasks(tasks);
        } catch (IOException|XMLStreamException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void updateTaskConfiguration(final Task task) throws ConfigurationException {
        final List<Task> tasks = getTasksConfiguration();
        for(int i=0,n=tasks.size();i<n;i++){
            final Task t = tasks.get(i);
            if(task.getId().equals(t.getId())){
                tasks.set(i, task);
                break;
            }
        }
        try {
            saveTasks(tasks);
        } catch (IOException|XMLStreamException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void removeTaskConfiguration(final Task task) throws ConfigurationException {
        final List<Task> tasks = getTasksConfiguration();
        for(int i=0,n=tasks.size();i<n;i++){
            final Task t = tasks.get(i);
            if(task.getId().equals(t.getId())){
                tasks.remove(i);
                break;
            }
        }
        try {
            saveTasks(tasks);
        } catch (IOException|XMLStreamException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    /**
     * save tasks in the configuration file.
     */
    private synchronized void saveTasks(final List<Task> tasks) throws IOException, XMLStreamException{
        final File configDir = ConfigDirectory.getConfigDirectory();
        if(!configDir.exists()){
            configDir.mkdirs();
        }
        
        final File taskFile = new File(configDir, TASK_FILE);
        
        XMLStreamWriter xmlWriter = null;
        final XMLOutputFactory XMLfactory = XMLOutputFactory.newInstance();
        XMLfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        try {
            xmlWriter = XMLfactory.createXMLStreamWriter(new FileOutputStream(taskFile));
        } catch (FileNotFoundException ex) {
            throw new XMLStreamException(ex.getLocalizedMessage(), ex);
        }
        xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
        
        try{
            final XMLTasksWriter taskWriter = new XMLTasksWriter();
            taskWriter.setOutput(xmlWriter);
            taskWriter.write(tasks);
            taskWriter.dispose();
        }finally{
            xmlWriter.close();
        }
    }
    
}
