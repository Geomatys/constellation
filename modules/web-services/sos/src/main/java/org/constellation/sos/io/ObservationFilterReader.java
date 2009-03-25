/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.io;

import java.util.List;
import org.constellation.ws.CstlServiceException;
import org.opengis.observation.Observation;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationFilterReader extends ObservationFilter {

    public List<Observation> getObservationTemplates() throws CstlServiceException;

    public List<Observation> getObservations() throws CstlServiceException;

    public String getResults() throws CstlServiceException;

    public String getOutOfBandResults() throws CstlServiceException;

}
