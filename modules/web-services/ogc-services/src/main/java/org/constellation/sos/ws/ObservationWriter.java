/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sos.ws;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.gml.v311.ReferenceTable;
import org.constellation.observation.MeasurementEntry;
import org.constellation.observation.MeasurementTable;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.ObservationTable;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ObservationOfferingTable;
import org.constellation.sos.OfferingPhenomenonEntry;
import org.constellation.sos.OfferingProcedureEntry;
import org.constellation.sos.OfferingSamplingFeatureEntry;
import org.constellation.ws.WebServiceException;
import org.postgresql.ds.PGSimpleDataSource;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class ObservationWriter {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.constellation.sos.ws");
    
    /**
     * A Database object for the O&M dataBase.
     */
    private final Database OMDatabase;
    
    /**
     * A database table for insert and get observation
     */
    private final ObservationTable obsTable;
    
    /**
     * A database table for insert and get observation offerring.
     */
    private final ObservationOfferingTable offTable;
    
    /**
     * A database table for insert and get reference object.
     */
    private final ReferenceTable refTable;
    
    /**
     * 
     * @param dataSourceOM
     * @param observationIdBase
     * @throws java.io.IOException
     * @throws org.constellation.catalog.NoSuchTableException
     * @throws java.sql.SQLException
     */
    public ObservationWriter(PGSimpleDataSource dataSourceOM) throws IOException, NoSuchTableException, SQLException {
        OMDatabase   = new Database(dataSourceOM);
       
        //we build the database table frequently used.
        obsTable = OMDatabase.getTable(ObservationTable.class);
        offTable = OMDatabase.getTable(ObservationOfferingTable.class);
        refTable = OMDatabase.getTable(ReferenceTable.class);
        
        
    }
   
    public String writeObservation(ObservationEntry observation) throws SQLException, CatalogException {
        if (obsTable != null) {
            return obsTable.getIdentifier(observation);
        }
        return null;
    }
    
    public String writeMeasurement(MeasurementEntry measurement) throws SQLException, CatalogException {
        MeasurementTable measTable = OMDatabase.getTable(MeasurementTable.class);
        return measTable.getIdentifier(measurement);
    }
    
    public String writeOffering(ObservationOfferingEntry offering) throws SQLException, CatalogException {
        return offTable.getIdentifier(offering);
    }
    
    public void writeOfferingProcedure(OfferingProcedureEntry offProc) throws NoSuchTableException, SQLException, CatalogException {
        offTable.getProcedures().getIdentifier(offProc);
    }
    
    public void writeOfferingPhenomenon(OfferingPhenomenonEntry offPheno) throws NoSuchTableException, SQLException, CatalogException {
        offTable.getPhenomenons().getIdentifier(offPheno);
    }
    
    public void writeOfferingSamplingFeature(OfferingSamplingFeatureEntry offSF) throws NoSuchTableException, SQLException, CatalogException {
        offTable.getStations().getIdentifier(offSF);
    }
    
    public void updateOfferings() {
        offTable.flush();
    }
    
    public void recordProcedureLocation(String SRSName, String sensorId, String x, String y) throws SQLException, WebServiceException {
        Statement stmt2    = OMDatabase.getConnection().createStatement();
        final ResultSet result2;
        String request = "SELECT * FROM ";
        boolean insert = true;
        
        if (SRSName.equals("27582")) {
            request = request + " projected_localisations WHERE id='" + sensorId + "'";
            result2 = stmt2.executeQuery(request);
            if (!result2.next()) {
                request = "INSERT INTO projected_localisations VALUES ('" + sensorId + "', GeometryFromText( 'POINT(" + x + ' ' + y + ")', " + SRSName + "))";
            } else {
                insert = false;
                logger.severe("Projected sensor location already registred for " + sensorId + " keeping old location");
            }
        } else if (SRSName.equals("4326")) {
            request = request + " geographic_localisations WHERE id='" + sensorId + "'";
            result2 = stmt2.executeQuery(request);
            if (!result2.next()) {
                request = "INSERT INTO geographic_localisations VALUES ('" + sensorId + "', GeometryFromText( 'POINT(" + x + ' ' + y + ")', " + SRSName + "))";
            } else {
                insert = false;
                logger.severe("Geographic sensor location already registred for " + sensorId + " keeping old location");
            }
        } else {
            throw new WebServiceException("This CRS " + SRSName + " is not supported",
                                          INVALID_PARAMETER_VALUE);
        }
        logger.info(request);
        if (insert) {
            stmt2.executeUpdate(request);
        }
        result2.close();
        stmt2.close();
    }

}
