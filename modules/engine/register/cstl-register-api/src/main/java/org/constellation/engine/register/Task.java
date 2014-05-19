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
package org.constellation.engine.register;

public interface Task {

    public abstract void setOwner(User owner);

    public abstract User getOwner();

    public abstract void setEnd(int end);

    public abstract int getEnd();

    public abstract void setStart(long start);

    public abstract long getStart();

    public abstract void setDescription(int description);

    public abstract int getDescription();

    public abstract void setTitle(int title);

    public abstract int getTitle();

    public abstract void setType(String type);

    public abstract String getType();

    public abstract void setState(String state);

    public abstract String getState();

    public abstract void setIdentifier(String identifier);

    public abstract String getIdentifier();

}