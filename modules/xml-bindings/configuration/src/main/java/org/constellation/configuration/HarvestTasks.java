/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="HarvestTasks")
@XmlAccessorType(XmlAccessType.FIELD)
public class HarvestTasks {

    private List<HarvestTask> task;

    public HarvestTasks() {

    }

    public HarvestTasks(List<HarvestTask> task) {
        this.task = task;
    }

    /**
     * @return the tasks
     */
    public List<HarvestTask> getTask() {
        if (task == null) {
            task = new ArrayList<HarvestTask>();
        }
        return task;
    }

    /**
     * @param task the tasks to set
     */
    public void setTask(List<HarvestTask> task) {
        this.task = task;
    }

    /**
     * @param task the tasks to set
     */
    public void addTask(HarvestTask task) {
        if (this.task == null) {
            this.task = new ArrayList<HarvestTask>();
        }
        this.task.add(task);
    }

    /**
     * Return a task from is sourceURL
     */
    public HarvestTask getTaskFromSource(String sourceURL) {
        if (task == null) {
            task = new ArrayList<HarvestTask>();
        }
        for (HarvestTask t: task) {
            if (t.getSourceURL() != null && t.getSourceURL().equals(sourceURL)) {
                return t;
            }
        }
        return null;
    }
}
