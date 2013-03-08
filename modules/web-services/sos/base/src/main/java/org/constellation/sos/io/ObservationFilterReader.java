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
import org.geotoolkit.gml.xml.Envelope;
import org.opengis.observation.Observation;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ObservationFilterReader extends ObservationFilter {

    /**
     * Return a list of Observation templates matching the builded filter.
     *
     * @return A list of Observation templates matching the builded filter.
     * @throws CstlServiceException
     */
    List<Observation> getObservationTemplates(final String version) throws CstlServiceException;

     /**
     * Return a list of Observation matching the builded filter.
     *
     * @return A list of Observation matching the builded filter.
     * @throws CstlServiceException
     */
    List<Observation> getObservations(final String version) throws CstlServiceException;

    /**
     * Return an encoded block of data in a string.
     * The datas are the results of the matching observations.
     *
     * @return An encoded block of data in a string.
     * @throws CstlServiceException
     */
    String getResults() throws CstlServiceException;

    /**
     * Return an encoded block of data in a string.
     * The datas are the results of the matching observations.
     * The datas are usually encoded as CSV (comma separated value) format.
     * @return
     * @throws CstlServiceException
     */
    String getOutOfBandResults() throws CstlServiceException;

    /**
     * MIME type of the data that will be returned as the result of a GetObservation request.
     * This is usually text/xml; subtype="om/1.0.0".
     * In the case  that data is delivered out of band it might be text/xml;subtype="tml/2.0" for TML or some
     * other MIME type.
     *
     * @param responseFormat the MIME type of the response.
     */
    void setResponseFormat(String responseFormat);
    
    /**
     * return true if the filter reader take in charge the calculation of the collection bounding shape.
     * 
     * @return True if the filter compute itself the bounding shape of the collection. 
     */
    boolean computeCollectionBound();
    
    /**
     * If the filter reader caompute itself the bounding shape of the obervation collection.
     * this methode return the current shape.
     * @return 
     */
    Envelope getCollectionBoundingShape();
}
