package org.constellation.admin.index;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.admin.exception.ConstellationException;

import java.io.IOException;
import java.util.Set;

/**
 * Created by christophe mourette on 14/08/14 for Geomatys.
 */
public interface IndexEngine {
    void addMetadataToIndexForData(DefaultMetadata metadata, Integer id) throws ConstellationException;
    void addMetadataToIndexForDataset(DefaultMetadata metadata, Integer id) throws ConstellationException;
    Set<Integer> searchOnMetadata(String queryString, String attributeId) throws ParseException, IOException;
}
