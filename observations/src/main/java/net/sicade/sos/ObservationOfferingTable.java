/*
 * ObservationOfferingTable.java
 * 
 * Created on 10 oct. 2007, 12:33:53
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sicade.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.gml.BoundingShapeEntry;
import net.sicade.gml.EnvelopeEntry;
import net.sicade.gml.EnvelopeTable;
import net.sicade.observation.PhenomenonEntry;
import net.sicade.observation.ProcessEntry;
import net.sicade.observation.SamplingFeatureEntry;
import net.sicade.observation.TemporalObjectEntry;

/**
 *
 * @author legal
 */
public class ObservationOfferingTable extends SingletonTable<ObservationOfferingEntry>{

    /**
     * Un lien vers la table des procedure offering
     */ 
    private OfferingProcedureTable procedures;
    
     /**
     * Un lien vers la table des phenomene offering
     */ 
    private OfferingPhenomenonTable phenomenons;
    
     /**
     * Un lien vers la table des station offering
     */ 
    private OfferingSamplingFeatureTable stations;
    
      /**
     * Un lien vers la table des station offering
     */ 
    private OfferingResponseModeTable responseModes;
    
    
    /**
     * Un lien vers la table des envelope.
     */
    private EnvelopeTable envelopes;
    
    /**
     * Construit une table des envelope.
     *
     * @param  database Connexion vers la base de données.
     */
    public ObservationOfferingTable(final Database database) {
        this(new ObservationOfferingQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private ObservationOfferingTable(final ObservationOfferingQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }
    
    /**
     * Return the procedure table for offering.
     */
     public OfferingProcedureTable getProcedures() {
        return procedures;
    }

    /**
     * Return the phenomenon table for offering.
     */ 
    public OfferingPhenomenonTable getPhenomenons() {
        return phenomenons;
    }

    /**
     * Return the station table for offering.
     */
    public OfferingSamplingFeatureTable getStations() {
        return stations;
    }
    
    /**
     *  Create a new offering from the database.
     * 
     * @param results a resultSet obtain by a "SELECT" SQL request.
     * 
     * @return A observationOffering object.
     * 
     * @throws net.sicade.catalog.CatalogException
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
         
         if (phenomenons == null) {
            phenomenons =  getDatabase().getTable(OfferingPhenomenonTable.class);
            phenomenons =  new OfferingPhenomenonTable(phenomenons);
         }
         phenomenons.setIdOffering(idOffering);
         Collection<OfferingPhenomenonEntry> entries1 = phenomenons.getEntries();
        
         List<PhenomenonEntry> phenos = new ArrayList<PhenomenonEntry>();
        
         Iterator i = entries1.iterator();
         while(i.hasNext()) {
            OfferingPhenomenonEntry c =(OfferingPhenomenonEntry) i.next();
            phenos.add(c.getComponent());
         }
         
          if (procedures == null) {
            procedures =  getDatabase().getTable(OfferingProcedureTable.class);
            procedures =  new OfferingProcedureTable(procedures);
         }
         procedures.setIdOffering(idOffering);
         Collection<OfferingProcedureEntry> entries2 = procedures.getEntries();
        
         List<ProcessEntry> process = new ArrayList<ProcessEntry>();
        
         i = entries2.iterator();
         while(i.hasNext()) {
            OfferingProcedureEntry c =(OfferingProcedureEntry) i.next();
            process.add(c.getComponent());
         }
         
         if (stations == null) {
            stations =  getDatabase().getTable(OfferingSamplingFeatureTable.class);
            stations =  new OfferingSamplingFeatureTable(stations);
         }
         stations.setIdOffering(idOffering);
         Collection<OfferingSamplingFeatureEntry> entries3 = stations.getEntries();
        
         List<SamplingFeatureEntry> sampling = new ArrayList<SamplingFeatureEntry>();
        
         i = entries3.iterator();
         while(i.hasNext()) {
            OfferingSamplingFeatureEntry c =(OfferingSamplingFeatureEntry) i.next();
            sampling.add(c.getComponent());
         }
         Timestamp begin = null;
         Timestamp end   = null;
         
         if (results.getTimestamp(indexOf(query.eventTimeBegin)) != null) {
            begin =  results.getTimestamp(indexOf(query.eventTimeBegin));
         }
         
         if (results.getTimestamp(indexOf(query.eventTimeEnd)) != null) {
            end =  results.getTimestamp(indexOf(query.eventTimeEnd));
         }
         
         TemporalObjectEntry eventTime = new TemporalObjectEntry(begin, end);
         
         if (responseModes == null) {
            responseModes =  getDatabase().getTable(OfferingResponseModeTable.class);
            responseModes =  new OfferingResponseModeTable(responseModes);
         }
         responseModes.setIdOffering(idOffering);
         Collection<OfferingResponseModeEntry> entries4 = responseModes.getEntries();
         List<ResponseMode> modes = new ArrayList<ResponseMode>();
         i = entries4.iterator();
         
         while(i.hasNext()) {
            OfferingResponseModeEntry c =(OfferingResponseModeEntry) i.next();
            modes.add(c.getMode());
         }
         
         return new ObservationOfferingEntry(idOffering,
                                             results.getString(indexOf(query.name)),
                                             results.getString(indexOf(query.description)),
                                             null,
                                             boundedBy,
                                             results.getString(indexOf(query.srsName)),
                                             eventTime,
                                             process,
                                             phenos,
                                             sampling,
                                             results.getString(indexOf(query.responseFormat)),
                                             results.getString(indexOf(query.resultModel)),
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
        if (off.getName() != null) {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.id), off.getId());
            ResultSet result = statement.executeQuery();
            if(result.next())
                return off.getId();
            else
                id = off.getId();
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
       

        //on insere le srs name
        if (off.getSrsName() != null) {
            statement.setString(indexOf(query.srsName), off.getSrsName());
        } else {
            statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
        }
            
        // on insere le "eventTime""
        if (off.getEventTime() != null && ((TemporalObjectEntry)off.getEventTime()).getBeginTime() != null) {
            Timestamp date = ((TemporalObjectEntry)off.getEventTime()).getBeginTime();
            statement.setTimestamp(indexOf(query.eventTimeBegin), date);
            if (((TemporalObjectEntry)off.getEventTime()).getEndTime() != null) {
                date = ((TemporalObjectEntry)off.getEventTime()).getEndTime();           
                statement.setTimestamp(indexOf(query.eventTimeEnd), date);
            } else {
                statement.setNull(indexOf(query.eventTimeEnd), java.sql.Types.TIMESTAMP);
            }
        } else {
            statement.setNull(indexOf(query.eventTimeBegin), java.sql.Types.TIMESTAMP);
            statement.setNull(indexOf(query.eventTimeEnd),   java.sql.Types.TIMESTAMP);
        }
        
        // on insere l'envellope qui borde l'offering
        if (off.getBoundedBy() != null) {
            if (envelopes == null) {
                envelopes = getDatabase().getTable(EnvelopeTable.class);
            }
            statement.setString(indexOf(query.boundedBy), envelopes.getIdentifier(off.getBoundedBy().getEnvelope()));
        } else {
            statement.setNull(indexOf(query.boundedBy), java.sql.Types.VARCHAR);
        }
        statement.setString(indexOf(query.responseFormat), off.getResponseFormat());
        statement.setString(indexOf(query.resultModel), off.getResultModel());
        
        insertSingleton(statement);
        
         // on insere les modes de reponse
        if (off.getResponseMode() != null && off.getResponseMode().size() != 0){
            for (ResponseMode mode:off.getResponseMode()) {
                if (responseModes == null) {
                    responseModes = getDatabase().getTable(OfferingResponseModeTable.class);
                }
                responseModes.getIdentifier(new OfferingResponseModeEntry(off.getId(), mode));
            } 
        }
         // on insere la liste de station qui a effectué cette observation
         if (off.getFeatureOfInterest() != null && off.getFeatureOfInterest().size() != 0) {
             for (SamplingFeatureEntry station:off.getFeatureOfInterest()) {
                    
                if (stations == null) {
                    stations = getDatabase().getTable(OfferingSamplingFeatureTable.class);
                }
                stations.getIdentifier(new OfferingSamplingFeatureEntry(off.getId(), station));
             }
        }
        
        // on insere les phenomene observé
         if(off.getObservedProperty() != null && off.getObservedProperty().size() != 0){
             
            for (PhenomenonEntry pheno: off.getObservedProperty()){
                if (phenomenons == null) {
                    phenomenons = getDatabase().getTable(OfferingPhenomenonTable.class);
                }
                phenomenons.getIdentifier(new OfferingPhenomenonEntry(off.getId(), pheno));
            }
         } 
        
        //on insere les capteur
        if (off.getProcedure() != null) {
            for (ProcessEntry process:off.getProcedure()){
                if (procedures == null) {
                    procedures = getDatabase().getTable(OfferingProcedureTable.class);
                }
                procedures.getIdentifier(new OfferingProcedureEntry(off.getId(), process));
            }
        } 
        return id;
    }
}
