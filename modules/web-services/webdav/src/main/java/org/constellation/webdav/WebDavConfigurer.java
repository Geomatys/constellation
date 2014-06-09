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

package org.constellation.webdav;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.WebdavContext;
import org.constellation.ogc.configuration.OGCConfigurer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WebDavConfigurer extends OGCConfigurer {

    /**
     * Create a new {@link CSWConfigurer} instance.
     */
    public WebDavConfigurer() {
        super(Specification.WEBDAV, WebdavContext.class);
    }

}
