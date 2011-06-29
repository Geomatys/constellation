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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.process.ProcessFactory;
import org.geotoolkit.process.ProcessFinder;

import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.StaxStreamWriter;
import org.opengis.feature.type.Name;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Constellation can run tasks at regular intervals. 
 * The scheduler used is Quartz, added tasks are stored in a configuration
 * file and are restarted when the server starts.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class CstlScheduler {
    
    private static final Logger LOGGER = Logging.getLogger(CstlScheduler.class);
    private static final String TASK_FILE = "scheduler-tasks.xml";    
    private static CstlScheduler INSTANCE = null;
        
    private final List<Task> tasks = new ArrayList<Task>();
    private Scheduler quartzScheduler;
    
    private CstlScheduler(){
        
        LOGGER.log(Level.WARNING, "=== Starting Constellation Scheduler ===");
        try {
            loadTasks();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to read tasks ===\n"+ex.getLocalizedMessage(), ex); 
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to read tasks ===\n"+ex.getLocalizedMessage(), ex); 
        }
        
        final SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            quartzScheduler = schedFact.getScheduler();
            quartzScheduler.start();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to start quartz scheduler ===\n"+ex.getLocalizedMessage(), ex);            
            return;
        }
        LOGGER.log(Level.WARNING, "=== Constellation Scheduler sucessfully started ===");    
        
        for(Task t : tasks){
            try {
                registerTask(t);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * The returned list is a subset of what can be found with ProcessFinder.
     * But only process with simple types arguments are preserved.
     * 
     * @return List of all available process.
     */
    public List<Name> listProcess(){
        final List<Name> names = new ArrayList<Name>();
        
        final Iterator<ProcessFactory> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessFactory factory = ite.next();            
            final String authorityCode = factory.getIdentification().getCitation()
                              .getIdentifiers().iterator().next().getCode();
            
            for(String processCode : factory.getNames()){
                names.add(new DefaultName(authorityCode, processCode));
            }            
        }
        
        return names;
    }
    
    
    /**
     * @return copied list of all tasks.
     */
    public synchronized List<Task> listTasks(){
        //return a copy, since tasks object are mutable
        final List<Task> copy = new ArrayList<Task>();
        for(Task t : tasks){
            copy.add(new Task(t));
        }
        return copy;
    }
    
    public synchronized Task getTask(final String id){
        for(Task t : listTasks()){
            if(t.getId().equals(id)){
                return t;
            }
        }
        return null;
    }
    
    /**
     * Add a new task.
     */
    public synchronized void addTask(Task task){
        
        tasks.add(new Task(task)); //defense copy
        try {
            registerTask(task);
        } catch (SchedulerException ex) {
            LOGGER.log(Level.WARNING, "Failed to register task :"+task.getId()+","+task.getTitle()+" in scheduler.",ex);
        }
        
        try {
            saveTasks();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to save tasks list", ex);
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.WARNING, "Failed to save tasks list", ex);
        }
    }
    
    /**
     * Update a task.
     */
    public synchronized boolean updateTask(final Task task){
        if(removeTask(task)){
            addTask(task);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a task.
     */
    public synchronized boolean removeTask(Task task){
        return removeTask(task.getId());
    }
    
    /**
     * Remove task for the given id.
     */
    public synchronized boolean removeTask(final String id){
        
        Task task = null;
        for(int i=0,n=tasks.size(); i<n; i++){
            final Task t = tasks.get(i);
            
            if(t.getId().equals(id)){
                task = tasks.remove(i);
                break;
            }
        }
        
        if(task != null){
            try {
                saveTasks();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to save tasks list", ex);
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "Failed to save tasks list", ex);
            }
            try {
                unregisterTask(task);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, "Failed to unregister task:"+task.getId()+","+task.getTitle()+" from scheduler",ex);
            }
        }
        
        return task != null;
    }
    
    /**
     * Load tasks defined in the configuration file.
     */
    private synchronized void loadTasks() throws IOException, XMLStreamException{
        //open configuration file
        final File configDir = ConfigDirectory.getConfigDirectory();
        final File taskFile = new File(configDir, TASK_FILE);
        if(!taskFile.exists()){
            return;
        }
        
        final TasksReader reader = new TasksReader();
        reader.setInput(taskFile);
        final List<Task> candidates = reader.read();
        if(candidates != null){
            tasks.addAll(candidates);
        }
    }
    
    /**
     * save tasks in the configuration file.
     */
    private synchronized void saveTasks() throws IOException, XMLStreamException{
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
            final TasksWriter taskWriter = new TasksWriter();
            taskWriter.setOutput(xmlWriter);
            taskWriter.write(tasks);
            taskWriter.dispose();
        }finally{
            xmlWriter.close();
        }
    }
    
    /**
     * Add the given task in the scheduler.
     */
    private void registerTask(final Task task) throws SchedulerException{
        quartzScheduler.scheduleJob(task.getDetail(), task.getTrigger());
        LOGGER.log(Level.INFO, "Scheduler task added : {0}, {1}   type : {2}.{3}", new Object[]{
            task.getId(),
            task.getTitle(),
            task.getDetail().getFactoryIdentifier(), 
            task.getDetail().getProcessIdentifier()});
    }
    
    /**
     * Add the given task in the scheduler.
     */
    private void unregisterTask(final Task task) throws SchedulerException{
        quartzScheduler.interrupt(task.getDetail().getKey());
        final boolean removed = quartzScheduler.deleteJob(task.getDetail().getKey());
        
        if(removed){
            LOGGER.log(Level.INFO, "Scheduler task removed : {0}, {1}   type : {2}.{3}", new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDetail().getFactoryIdentifier(), 
                task.getDetail().getProcessIdentifier()});
        }else{
            LOGGER.log(Level.WARNING, "Scheduler failed to remove task : {0}, {1}   type : {2}.{3}", new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDetail().getFactoryIdentifier(), 
                task.getDetail().getProcessIdentifier()});
        }
        
    }
    
    
    /**
     * @return Singleton instance of Constellation scheduler.
     */
    public synchronized static CstlScheduler getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new CstlScheduler();
        }
        return INSTANCE;
    }

    public void stop() {        
        LOGGER.log(Level.WARNING, "=== Stopping Scheduler ===");
        try {
            quartzScheduler.shutdown();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to stop quartz scheduler ===");
            return;
        }
        LOGGER.log(Level.WARNING, "=== Scheduler sucessfully stopped ===");    
    }
    
}
