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
package org.constellation.gui.event;

import java.util.EventListener;


/**
 * Interface des objets interessés à être informés des changements d'images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface CoverageChangeListener extends EventListener {
    /**
     * Prévient qu'une image a changée.
     */
    void coverageChanged(CoverageChangeEvent event);
}
