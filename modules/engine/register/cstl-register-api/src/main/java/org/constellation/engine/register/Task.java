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