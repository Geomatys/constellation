package org.constellation.admin.index;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;

import java.io.IOException;
import java.util.List;

/**
 * Created by christophe mourette on 14/08/14 for Geomatys.
 */
public interface IndexEngine {
    void addMetadataToIndex(DefaultMetadata metadata, Integer id);
    List<Integer> searchOnMetadata(String queryString) throws ParseException, IOException;
}
