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
package org.constellation.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.constellation.gml.v311.BoundingShapeEntry;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.EnvelopeTable;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimeIndeterminateValueType;
import org.constellation.gml.v311.TimeInstantType;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.sos.v100.NamespacePrefixMapperImpl;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.OfferingPhenomenonEntry;
import org.constellation.sos.v100.OfferingProcedureEntry;
import org.constellation.sos.v100.OfferingResponseModeEntry;
import org.constellation.sos.v100.OfferingSamplingFeatureEntry;
import org.constellation.sos.v100.ResponseModeType;

/**
 *
 * @author Guilhem Legal
 */
public class ObservationOfferingTable extends SingletonTable<ObservationOfferingEntry>{

    /**
     * a link to the offering procedure.
     */ 
    private OfferingProcedureTable procedures;
    
    /**
     * a link to the offering phenomenon table.
     */ 
    private OfferingPhenomenonTable phenomenons;
    
    /**
     * a link to the offering station table. 
     */ 
    private OfferingSamplingFeatureTable stations;
    
    /**
     * a link to the offering response mode table
     */ 
    private OfferingResponseModeTable responseModes;
    
    
    /**
     * a link to the envelope table.
     */
    private EnvelopeTable envelopes;
    
    /**
     * A mapper allowing to retrieve the prefix of a namespace.
     */
    NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl(null);
    
    /**
     * Build a new observation offering table.
     *
     * @param  database Connexion to database.
     */
    public ObservationOfferingTable(final Database database) {
        this(new ObservationOfferingQuery(database));
    }

    /**
     * Initialize the table identifier.
     */
    private ObservationOfferingTable(final ObservationOfferingQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }
    
    /**
     * Return the procedure table for offering.
     */
     public OfferingProcedureTable getProcedures() throws NoSuchTableException {
        if (procedures == null) {
            procedures =  getDatabase().getTable(OfferingProcedureTable.class);
            procedures =  new OfferingProcedureTable(procedures);
         }
        return procedures;
    }

    /**
     * Return the phenomenon table for offering.
     */ 
    public OfferingPhenomenonTable getPhenomenons() throws NoSuchTableException {
        if (phenomenons == null) {
            phenomenons =  getDatabase().getTable(OfferingPhenomenonTable.class);
            phenomenons =  new OfferingPhenomenonTable(phenomenons);
        }
        return phenomenons;
    }

    /**
     * Return the station table for offering.
     */
    public OfferingSamplingFeatureTable getStations() throws NoSuchTableException {
        if (stations == null) {
            stations =  getDatabase().getTable(OfferingSamplingFeatureTable.class);
            stations =  new OfferingSamplingFeatureTable(stations);
        }
        return stations;
    }
    
    public OfferingResponseModeTable getResponseModes() throws NoSuchTableException {
        if (responseModes == null) {
            responseModes =  getDatabase().getTable(OfferingResponseModeTable.class);
            responseModes =  new OfferingResponseModeTable(responseModes);
        }
        return responseModes;
    }
    
    /**
     *  Create a new offering from the database.
     * 
     * @param results a resultSet obtain by a "SELECT" SQL request.
     * 
     * @return A observationOffering object.
     * 
     * @throws org.constellation.catalog.CatalogException
     * @throws java.sql.SQLException
     */
    @Override
    protected ObservationOfferingEntry createEntry(ResultSet results) throws CatalogException, SQLException {
         final ObservationOfferingQuery query = (ObservationOfferingQuery) super.query;
         String idOffering = results.getString(indexOf(query.id));
         
         if (envelopes == null) {
             envelopes = getDatabase().getTable(EnvelopeTable.class);
         }
         EnvelopeEntry envelope = envelopes.getEntry(results.getString(indexOf(query.boundedBy)));
         BoundingShapeEntry boundedBy = new  BoundingShapeEntry(envelope);
         
         getPhenomenons().setIdOffering(idOffering);
         //System.out.println("ID OFERRRRRRRRRRRRRING" + idOffering);
         Collection<OfferingPhenomenonEntry> entries1 = getPhenomenons().getEntries();
        
         List<PhenomenonEntry> phenos = new ArrayList<PhenomenonEntry>();
        
         Iterator i = entries1.iterator();
         while(i.hasNext()) {
            OfferingPhenomenonEntry c =(OfferingPhenomenonEntry) i.next();
            phenos.add(c.getComponent());
         }
         
         getProcedures().setIdOffering(idOffering);
         Collection<OfferingProcedureEntry> entries2 = getProcedures().getEntries();
        
         List<ReferenceEntry> process = new ArrayList<ReferenceEntry>();
        
         i = entries2.iterator();
         while(i.hasNext()) {
            OfferingProcedureEntry c =(OfferingProcedureEntry) i.next();
            process.add(c.getComponent());
         }
         
         getStations().setIdOffering(idOffering);
         Collection<OfferingSamplingFeatureEntry> entries3 = stations.getEntries();
        
         List<ReferenceEntry> sampling = new ArrayList<ReferenceEntry>();
        
         i = entries3.iterator();
         while(i.hasNext()) {
            OfferingSamplingFeatureEntry c =(OfferingSamplingFeatureEntry) i.next();
            sampling.add(c.getComponent());
         }
         TimePositionType beginPosition = null;
         TimePositionType endPosition   = null;
         
         if (results.getTimestamp(indexOf(query.eventTimeBegin)) != null) {
            Timestamp begin =  results.getTimestamp(indexOf(query.eventTimeBegin));
            if (begin != null) {
                //we normalize the timeStamp by replacing the space by 'T'
                String normalizedBegin = begin.toString().replace(' ', 'T');
                beginPosition = new TimePositionType(normalizedBegin);
            }
         }
         
         if (results.getTimestamp(indexOf(query.eventTimeEnd)) != null) {
            Timestamp end =  results.getTimestamp(indexOf(query.eventTimeEnd));
            if (end != null){
                 //we normalize the timeStamp by replacing the space by 'T'
                String normalizedEnd = end.toString().replace(' ', 'T');
                endPosition = new TimePositionType(normalizedEnd);
            } else {
                endPosition = new TimePositionType(TimeIndeterminateValueType.NOW);
            }
         } else {
             endPosition = new TimePositionType(TimeIndeterminateValueType.NOW);
         }
         
         TimePeriodType eventTime = new TimePeriodType(beginPosition, endPosition);
         
         getResponseModes().setIdOffering(idOffering);
         Collection<OfferingResponseModeEntry> entries4 = getResponseModes().getEntries();
         List<ResponseModeType> modes = new ArrayList<ResponseModeType>();
         i = entries4.iterator();
         
         while(i.hasNext()) {
            OfferingResponseModeEntry c =(OfferingResponseModeEntry) i.next();
            modes.add(c.getMode());
         }
         List<String> responseFormat = new ArrayList<String>();
         responseFormat.add(results.getString(indexOf(query.responseFormat)));
         
         List<QName> resultModel = new ArrayList<QName>();
         String namespace        = results.getString(indexOf(query.resultModelNamespace));
         resultModel.add(new QName(namespace,
                                   results.getString(indexOf(query.resultModelLocalPart)),
                                   prefixMapper.getPreferredPrefix(namespace, "", false)));
         
         List<String> srsName = new ArrayList<String>();
         srsName.add(results.getString(indexOf(query.srsName)));
         
         return new ObservationOfferingEntry(idOffering,
                                             results.getString(indexOf(query.name)),
                                             results.getString(indexOf(query.description)),
                                             null,
                                             boundedBy,
                                             srsName,
                                             eventTime,
                                             process,
                                             phenos,
                                             sampling,
                                             responseFormat,
                                             resultModel,
                                             modes);
         
    }
    
      /**
     * Retourne un nouvel identifier (ou l'identifier de l'offering passée en parametre si non-null)
     * et enregistre la nouvelle offering dans la base de donnée.
     *
     * @param off l'ofeering a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final ObservationOfferingEntry off) throws SQLException, CatalogException {
        final ObservationOfferingQuery query = (ObservationOfferingQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (off.getName() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.id), off.getId());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return off.getId();
                } else {
                    id = off.getId();
                }
            } else {
                id = searchFreeIdentifier("urn:BRGM:offering:");
            }
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.name), off.getName());
            statement.setString(indexOf(query.id), id);
            if (off.getRemarks() != null) {
                statement.setString(indexOf(query.description),  off.getRemarks());
            } else {
                statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
            }
            if (off.getSrsName() != null && off.getSrsName().size() > 0) {
               statement.setString(indexOf(query.srsName), off.getSrsName().get(0)); 
            } else {
               statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR); 
            }                 
            // on insere le "eventTime""
            if (off.getTime() != null) {
                if (off.getTime() instanceof TimePeriodType) {
                    TimePeriodType time = (TimePeriodType)off.getTime();
                    String s = time.getBeginPosition().getValue();
                    Timestamp date = Timestamp.valueOf(s);
                    statement.setTimestamp(indexOf(query.eventTimeBegin), date);
                    
                    if (time.getEndPosition().getIndeterminatePosition() == null) {
                       
                        time.getEndPosition().getValue();
                        date = Timestamp.valueOf(s);
                        statement.setTimestamp(indexOf(query.eventTimeEnd),  date);
                   
                    } else {
                        statement.setNull(indexOf(query.eventTimeEnd),   java.sql.Types.DATE);
                    }
                } else if (off.getTime() instanceof TimeInstantType) {
                    
                    TimeInstantType time = (TimeInstantType)off.getTime();
                    String s = time.getTimePosition().getValue();
                    Timestamp date = Timestamp.valueOf(s);
                    statement.setTimestamp(indexOf(query.eventTimeBegin),  date);
                    statement.setNull(indexOf(query.eventTimeEnd), java.sql.Types.DATE);
                    
                } else {
                    throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                }
            } else {
                statement.setNull(indexOf(query.eventTimeBegin), java.sql.Types.TIMESTAMP);
                statement.setNull(indexOf(query.eventTimeEnd),   java.sql.Types.TIMESTAMP);
            }
        
            // on insere l'envellope qui borde l'offering
            if (off.getBoundedBy() != null && off.getBoundedBy().getEnvelope() != null) {
                if (envelopes == null) {
                    envelopes = getDatabase().getTable(EnvelopeTable.class);
                }
                statement.setString(indexOf(query.boundedBy), envelopes.getIdentifier(off.getBoundedBy().getEnvelope()));
            } else {
                statement.setNull(indexOf(query.boundedBy), java.sql.Types.VARCHAR);
            }
            // TODO transform in list
            statement.setString(indexOf(query.responseFormat), off.getResponseFormat().get(0));
            statement.setString(indexOf(query.resultModelNamespace), off.getResultModel().get(0).getNamespaceURI());
            statement.setString(indexOf(query.resultModelLocalPart), off.getResultModel().get(0).getLocalPart());
        
            updateSingleton(statement);
        
            // we insert the response mode
            if (off.getResponseMode() != null && off.getResponseMode().size() != 0){
                for (ResponseModeType mode:off.getResponseMode()) {
                    getResponseModes().getIdentifier(new OfferingResponseModeEntry(off.getId(), mode));
                } 
            }
            // on insere la liste de station qui a effectué cette observation
            if (off.getFeatureOfInterest() != null && off.getFeatureOfInterest().size() != 0) {
                for (ReferenceEntry station:off.getFeatureOfInterest()) {
                    getStations().getIdentifier(new OfferingSamplingFeatureEntry(off.getId(), station));
                }
            }
        
            // on insere les phenomene observé
            if(off.getObservedProperty() != null && off.getObservedProperty().size() != 0){
                for (PhenomenonEntry pheno: off.getObservedProperty()){
                    getPhenomenons().getIdentifier(new OfferingPhenomenonEntry(off.getId(), pheno));
                }
            } 
        
            //on insere les capteur
            if (off.getProcedure() != null) {
                for (ReferenceEntry process:off.getProcedure()){
                    getProcedures().getIdentifier(new OfferingProcedureEntry(off.getId(), process));
                }
            }
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
