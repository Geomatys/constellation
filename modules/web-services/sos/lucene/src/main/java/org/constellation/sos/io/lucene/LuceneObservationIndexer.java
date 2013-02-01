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


package org.constellation.sos.io.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.ws.Utils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.AbstractGML;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.observation.xml.v100.MeasurementType;
import org.geotoolkit.observation.xml.Process;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.swe.xml.Phenomenon;
import org.opengis.observation.Observation;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalObject;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationIndexer extends AbstractIndexer<Observation> {

    private File observationDirectory;

    private File observationTemplateDirectory;

    private boolean template = false;

    /**
     * Creates a new SOS indexer for a FileSystem reader.
     *
     * @param configuration A configuration object containing the database informations.Must not be null.
     * @param serviceID  The identifier, if there is one, of the index/service.
     */
    public LuceneObservationIndexer(Automatic configuration, String serviceID) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory(), new WhitespaceAnalyzer(Version.LUCENE_36));
        final File dataDirectory = configuration.getDataDirectory();
        if (dataDirectory != null && dataDirectory.exists()) {
            observationDirectory = new File(dataDirectory, "observations");
            if (!observationDirectory.exists()) {
                observationDirectory.mkdir();
            }
            observationTemplateDirectory = new File(dataDirectory, "observationTemplates");
            if (!observationTemplateDirectory.exists()) {
                observationTemplateDirectory.mkdir();
            }
        } else {
            throw new IndexingException("The data directory does not exist: ");
        }
        if (create) {
            createIndex();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getAllIdentifiers() throws IndexingException {
        throw new UnsupportedOperationException("not used in this implementation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Observation getEntry(final String identifier) throws IndexingException {
        throw new UnsupportedOperationException("not used in this implementation");
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public void createIndex() throws IndexingException {
        LOGGER.info("Creating lucene index for Filesystem observations please wait...");

        final long time = System.currentTimeMillis();
        int nbObservation = 0;
        int nbTemplate    = 0;
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = SOSMarshallerPool.getInstance().acquireUnmarshaller();
            final IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            final IndexWriter writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), conf);

            // getting the objects list and index avery item in the IndexWriter.
            for (File observationFile : observationDirectory.listFiles()) {
                Object observation = unmarshaller.unmarshal(observationFile);
                if (observation instanceof JAXBElement) {
                    observation = ((JAXBElement)observation).getValue();
                }
                if (observation instanceof Observation) {
                    indexDocument(writer, (Observation) observation);
                    nbObservation++;
                } else {
                     LOGGER.info("The observation file " + observationFile.getName() + " does not contains an observation:" + observation);
                }
            }
            template = true;
            for (File observationFile : observationTemplateDirectory.listFiles()) {
                Object observation = unmarshaller.unmarshal(observationFile);
                if (observation instanceof JAXBElement) {
                    observation = ((JAXBElement)observation).getValue();
                }
                if (observation instanceof Observation) {
                    indexDocument(writer, (Observation) observation);
                    nbTemplate++;
                } else {
                     LOGGER.info("The template observation file " + observationFile.getName() + " does not contains an observation:" + observation);
                }
            }
            template = false;
            // writer.optimize(); no longer justified
            writer.close();

        } catch (CorruptIndexException ex) {
            LOGGER.log(Level.SEVERE,CORRUPTED_SINGLE_MSG + "{0}", ex.getMessage());
            throw new IndexingException(CORRUPTED_MULTI_MSG, ex);
        } catch (LockObtainFailedException ex) {
            LOGGER.log(Level.SEVERE,LOCK_SINGLE_MSG + "{0}", ex.getMessage());
            throw new IndexingException(LOCK_MULTI_MSG, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,IO_SINGLE_MSG + "{0}", ex.getMessage());
            throw new IndexingException("IOException while indexing documents.", ex);
        } catch (JAXBException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            LOGGER.log(Level.SEVERE, "JAXB Exception while indexing: {0}", msg);
            throw new IndexingException("JAXBException while indexing documents.", ex);
        } finally {
            if (unmarshaller != null) {
                SOSMarshallerPool.getInstance().release(unmarshaller);
            }
        }

        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms\nObservations indexed: "
                + nbObservation + ". Template indexed:" + nbTemplate + ".");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Document createDocument(final Observation observation, final int docid) {
        // make a new, empty document
        final Document doc = new Document();

        doc.add(new Field("id",      observation.getName(),        Field.Store.YES, Field.Index.ANALYZED));
        if (observation instanceof MeasurementType) {
            doc.add(new Field("type",    "measurement" , Field.Store.YES, Field.Index.ANALYZED));
        } else {
            doc.add(new Field("type",    "observation" , Field.Store.YES, Field.Index.ANALYZED));
        }
        doc.add(new Field("procedure",   ((Process)observation.getProcedure()).getHref(), Field.Store.YES, Field.Index.ANALYZED));

        doc.add(new Field("observed_property",   ((Phenomenon)observation.getObservedProperty()).getName(), Field.Store.YES, Field.Index.ANALYZED));

        doc.add(new Field("feature_of_interest",   ((AbstractGML)observation.getFeatureOfInterest()).getId(), Field.Store.YES, Field.Index.ANALYZED));

        try {
            final TemporalObject time = observation.getSamplingTime();
            if (time instanceof Period) {
                final Period period = (Period) time;
                doc.add(new Field("sampling_time_begin",   Utils.getLuceneTimeValue(period.getBeginning().getPosition()), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("sampling_time_end",   Utils.getLuceneTimeValue(period.getEnding().getPosition()), Field.Store.YES, Field.Index.ANALYZED));

            } else if (time instanceof Instant) {
                final Instant instant = (Instant) time;
                doc.add(new Field("sampling_time_begin",   Utils.getLuceneTimeValue(instant.getPosition()), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("sampling_time_end",    "NULL", Field.Store.YES, Field.Index.ANALYZED));

            } else if (time != null) {
                LOGGER.log(Level.WARNING, "unrecognized sampling time type:{0}", time);
            }
        } catch(CstlServiceException ex) {
            LOGGER.severe("error while indexing sampling time.");
        }
        if (template) {
            doc.add(new Field("template", "TRUE", Field.Store.YES, Field.Index.ANALYZED));
        } else {
            doc.add(new Field("template", "FALSE", Field.Store.YES, Field.Index.ANALYZED));
        }
        // add a default meta field to make searching all documents easy
	doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIdentifier(Observation obj) {
        return obj.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

}
