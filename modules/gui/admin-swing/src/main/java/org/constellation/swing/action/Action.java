/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.swing.action;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.event.EventListenerList;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.swing.FrameDisplayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class Action {

    private final EventListenerList listeners = new EventListenerList();

    private final String name;

    protected ConstellationServer server;
    protected Object target;
    protected FrameDisplayer displayer;

    public Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConstellationServer getServer() {
        return server;
    }

    public void setServer(ConstellationServer server) {
        this.server = server;
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
