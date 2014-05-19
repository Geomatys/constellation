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
