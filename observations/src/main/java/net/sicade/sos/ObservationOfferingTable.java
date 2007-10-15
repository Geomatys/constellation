/*
 * ObservationOfferingTable.java
 * 
 * Created on 10 oct. 2007, 12:33:53
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sicade.sos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
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
         
         TemporalObjectEntry eventTime = new TemporalObjectEntry(results.getTimestamp(indexOf(query.eventTimeBegin)),
                                                                 results.getTimestamp(indexOf(query.eventTimeEnd)));
                
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
                                             ResponseMode.fromValue(results.getString(indexOf(query.responseMode))));
         
    }

}
