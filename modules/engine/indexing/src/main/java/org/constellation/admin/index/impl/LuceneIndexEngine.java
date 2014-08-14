package org.constellation.admin.index.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.util.Version;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.index.IndexEngine;
import org.constellation.utils.MetadataFeeder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by christophe mourette on 14/08/14 for Geomatys.
 */
@Component
public class LuceneIndexEngine implements IndexEngine {
    private static final Logger LOGGER = Logging.getLogger(LuceneIndexEngine.class);

    private IndexWriter indexWriter;

    private IndexSearcher indexSearcher;


    @PostConstruct
    private void init() throws IOException {
        createIndex();
        initIndexSearcher();

    }

    private void createIndex() throws IOException {
        final File dataIndexDirectory = ConfigDirectory.getDataIndexDirectory();
        final Directory directory = FSDirectory.open(dataIndexDirectory, new SimpleFSLockFactory());
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();
    }



    private void initIndexSearcher() throws IOException {

        DirectoryReader directoryReader = DirectoryReader.open(indexWriter.getDirectory());
        indexSearcher = new IndexSearcher(directoryReader);
    }


    @PreDestroy
    private void destroy() throws IOException {
        LOGGER.info("closing metadata index");
        indexWriter.close();
    }


    public void addMetadataToIndex(DefaultMetadata metadata, Integer dataId)  {
        final MetadataFeeder metadataFeeder = new MetadataFeeder(metadata);
        try {

            Document doc = new Document();
            doc.add(new IntField("dataId",dataId, Field.Store.YES));
            final String keywords = StringUtils.join(metadataFeeder.getKeywordsNoType(), " ");
            if (keywords!=null && keywords.length()>0) {
                doc.add(new TextField("keywords", keywords, Field.Store.NO));
            }
            final String title = metadataFeeder.getTitle();
            if (title != null && title.length()>0){
                doc.add(new TextField("title", title, Field.Store.NO));
            }
            final String abstractField = metadataFeeder.getAbstract();
            if (abstractField != null && abstractField.length()>0) {
                doc.add(new TextField("abstract", abstractField, Field.Store.NO));
            }
            final List<String> topicCategories = metadataFeeder.getAllTopicCategory();
            if (topicCategories != null && topicCategories.size()>0) {
                doc.add(new TextField("topic", StringUtils.join(topicCategories, ' '), Field.Store.NO));
            }
            final List<String> sequenceIdentifiers = metadataFeeder.getAllSequenceIdentifier();
            if (sequenceIdentifiers != null && sequenceIdentifiers.size()>0){
                doc.add(new TextField("data",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            final String processingLevel = metadataFeeder.getProcessingLevel();
            if (processingLevel != null && processingLevel.length()>0){
                doc.add(new TextField("level",processingLevel,Field.Store.NO));
            }
            final List<String> geographicIdentifiers = metadataFeeder.getAllGeographicIdentifier();
            if (geographicIdentifiers != null && geographicIdentifiers.size()>0){
                doc.add(new TextField("area",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            indexWriter.addDocument(doc);
            indexWriter.commit();

        } catch (IOException e) {
            throw new ConstellationException(e);
        }
    }


    public List<Integer> searchOnMetadata(String queryString) throws ParseException, IOException {
        List<Integer> result = new ArrayList<>();
        initIndexSearcher();
        TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
        final MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46, new String[]{
                "title", "abstract", "keywords", "topic", "data", "level", "area" }, new StandardAnalyzer(Version.LUCENE_46));
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        final Query q = queryParser.parse(queryString);
        indexSearcher.search(q, collector);
        final ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (ScoreDoc scoreDoc : hits){
            Document doc = indexSearcher.doc(scoreDoc.doc);
            final Integer dataId = Integer.valueOf(doc.get("dataId"));
            result.add(dataId);
        }
        return result;
    }

}
