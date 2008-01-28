/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.management;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.seagis.coverage.web.ImageProducer;


/**
 *
 * @author Guilhem Legal
 */
public class WebServiceJMXHandler {
    
    private static final Logger LOGGER = Logger.getLogger("net.seagis.management");
    
    private WebServiceManager manager;
    
    public WebServiceJMXHandler(ImageProducer worker){
        // Get the platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // Uniquely identify the MBeans and register them with the platform MBeanServer
        manager = new WebServiceManager(worker);
       
        try {
           // Uniquely identify the MBeans and register them with the platform MBeanServer
           LOGGER.info("registring MBEAN");
           ObjectName managerName = new ObjectName("WebServiceManager:name=WebServiceManager");
           mbs.registerMBean(manager, managerName);
           
        } catch (InstanceAlreadyExistsException ex) {
            LOGGER.severe("The instance of the MBean already exist");
        } catch (MBeanRegistrationException ex) {
            LOGGER.severe("MBeanRegistrationException: " + ex.getMessage());
        } catch (NotCompliantMBeanException ex) {
            LOGGER.severe("NotCompliantMBeanException: " + ex.getMessage());
        } catch (MalformedObjectNameException ex) {
            LOGGER.severe("Malformed ObjectName for MBeans");
        } catch (NullPointerException ex) {
            LOGGER.severe("null pointer Exception in WebServiceManager");
        }
    }
    
    public void addWorker(ImageProducer worker) {
        manager.addWorker(worker);
    }
    
    public void setImageFileTime(long start, long end) {
        manager.setImageFileTime(end - start);
    }
}
