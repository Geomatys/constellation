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
import java.util.Properties;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationFilter extends ObservationFilter {

    public LuceneObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        super(observationIdBase, observationTemplateIdBase, map);
    }
    @Override
    public void initFilterObservation(ResponseModeType requestMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initFilterGetResult(String procedure) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFeatureOfInterest(List<String> fois) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeEquals(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeBefore(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeAfter(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeDuring(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> filterObservation() throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
