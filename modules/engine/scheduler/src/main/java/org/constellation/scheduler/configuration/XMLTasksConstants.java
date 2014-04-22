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
package org.constellation.scheduler.configuration;

/**
 * XML constants for tag and attributes name.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
final class XMLTasksConstants {
    
    public static final String TAG_TASKS = "tasks";
    public static final String TAG_TASK = "task";
    public static final String ATT_ID = "id";
    public static final String ATT_TITLE = "title";
    public static final String ATT_AUTHORITY = "authority";
    public static final String ATT_CODE = "code";
    
    public static final String TAG_TRIGGER = "trigger";
    public static final String ATT_STEP = "step";
    public static final String ATT_START_DATE = "startDate";
    public static final String ATT_START_STEP = "startStep";
    
    public static final String TAG_PARAMETERS = "parameters";
    
    private XMLTasksConstants(){}
    
}
