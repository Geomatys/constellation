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

package org.constellation.query;

import net.jcip.annotations.Immutable;
import org.apache.sis.util.Version;

/**
 * Default Implementation of a Query.
 * @author Johann Sorel (Geomatys)
 */
@Immutable
public final class DefaultQuery implements Query{

    private final QueryRequest request;
    private final String service;
    private final Version version;
    private final String exception;

    public DefaultQuery(QueryRequest request, String service, Version version, String exception) {
        this.request = request;
        this.service = service;
        this.version = version;
        this.exception = exception;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QueryRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getService() {
        return service;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Version getVersion() {
        return version;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExceptionFormat() {
        return exception;
    }

}
