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

package org.constellation.sos.io.lucene;

import java.util.List;
import java.util.Properties;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;

/**
 * TODO
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationFilter implements ObservationFilter {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    private String observationIdBase;
    private String observationTemplateIdBase;
    private Properties map;
    private Automatic configuration;

    public LuceneObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        this.configuration             = configuration;
        this.map                       = map;
        this.observationIdBase         = observationIdBase;
        this.observationTemplateIdBase = observationTemplateIdBase;
    }
    
    @Override
    public void initFilterObservation(ResponseModeType requestMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initFilterGetResult(String procedure) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setFeatureOfInterest(List<String> fois) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setTimeEquals(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setTimeBefore(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setTimeAfter(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void setTimeDuring(Object time) throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public List<String> filterObservation() throws CstlServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    public String getInfos() {
        return "Constellation Lucene O&M Filter 0.3";
    }

    public boolean isBoundedObservation() {
        return false;
    }

    public void setBoundingBox(EnvelopeEntry e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

}
