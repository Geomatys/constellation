/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.ImageProducer;


/**
 *
 * @author Guilhem Legal
 */
public class WebServiceManager implements WebServiceManagerMBean {
    /**
     * The List of all the web service workers running.
     */
    private final List<ImageProducer> workers;

    /**
     * The last time of the execution of getImageFile method.
     */
    private long imageFileTime;

    /**
     * A debbuging logger.
     */
    private static final Logger LOGGER = Logger.getLogger("net.seagis.management");

    public WebServiceManager(){
       workers = new ArrayList<ImageProducer>();
    }

    /**
     * Add a new worker to the list.
     */
    public void addWorker(ImageProducer worker){
        workers.add(worker);
    }

    /**
     * remove a worker from the list.
     */
    public void removeWorker(ImageProducer worker){
        workers.remove(worker);
    }

    /**
     * Call the method flush on every workers runing.
     */
    public void flush() {
        LOGGER.info("flush in MBean");
        int i = 1;
        for (ImageProducer worker: workers){
            try {
                worker.flush();
                i++;
            } catch (WebServiceException ex) {
                LOGGER.severe("Exception while flushing the workers: " + ex.getMessage());
            }
        }
    }

    /**
     * Return the current number of webService worker.
     */
    public int workerCount() {
        return workers.size();
    }

    /**
     * Return the last time of execution of the method getImageFile.
     */
    public String lastImageCreationDelay() {
        if (imageFileTime == 0) {
            return "undefined";
        } else {
            return String.valueOf(imageFileTime);
        }
    }

    /**
     * Set the last time of execution of the method getImageFile.
     */
    public void setImageFileTime(long time) {
        imageFileTime = time;
    }
}
