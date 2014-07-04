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
package org.constellation.swing.action;

import org.constellation.admin.service.ConstellationClient;
import org.constellation.swing.FrameDisplayer;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class Action {

    private final EventListenerList listeners = new EventListenerList();

    private final String name;

    protected ConstellationClient serverV2;
    protected Object target;
    protected FrameDisplayer displayer;

    public Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConstellationClient getServerV2() {
        return serverV2;
    }

    public void setServerV2(ConstellationClient serverV2) {
        this.serverV2 = serverV2;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public FrameDisplayer getDisplayer() {
        return displayer;
    }

    public void setDisplayer(FrameDisplayer displayer) {
        this.displayer = displayer;
    }

    public PropertyChangeListener[] getPropertyListeners(){
        return listeners.getListeners(PropertyChangeListener.class);
    }
    
    protected void addPropertyChangeListener(PropertyChangeListener[] lsts){
        for(PropertyChangeListener pl : lsts){
            listeners.add(PropertyChangeListener.class, pl);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener){
        listeners.add(PropertyChangeListener.class, listener);
    }

    protected void fireUpdate(){
        for(PropertyChangeListener l : listeners.getListeners(PropertyChangeListener.class)){
            l.propertyChange(new PropertyChangeEvent(this, "update", 0, 1));
        }
    }

    public abstract boolean isEnable();

    public abstract String getDisplayName();

    public abstract ImageIcon getIcon();

    public abstract Color getTextColor();

    public abstract Color getBackgroundColor();

    public abstract void actionPerformed();

    public abstract Action clone();

}
