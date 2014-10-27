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
package org.constellation.api;

/**
 * List of all property keys that can be stored in
 * {@link org.constellation.engine.register.repository.PropertyRepository}
 *
 * @author Quentin Boileau (Geomatys)
 */
public class PropertyConstants {

    /**
     * Service URL property key
     */
    public static final String SERVICES_URL_KEY = "services.url";

    /**
     * Auto analyse data.
     * This property define a flag that enable/disable data analysing at
     * creation or during application startup. This analyse contains
     * coverage histogram.
     */
    public static final String DATA_ANALYSE_KEY = "data.auto.analyse";

}
