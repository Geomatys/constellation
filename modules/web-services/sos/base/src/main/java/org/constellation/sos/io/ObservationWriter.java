/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.sos.io;

import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.gml.xml.AbstractGeometry;

/**
 *
 * @author guilhem
 */
public interface ObservationWriter extends org.geotoolkit.observation.ObservationWriter {
    
    void writeProcedure(final String procedureID, final AbstractGeometry position, final String parent, final String type) throws DataStoreException;
}
