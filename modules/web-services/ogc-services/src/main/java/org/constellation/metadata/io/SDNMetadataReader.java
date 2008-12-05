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

package org.constellation.metadata.io;


// J2SE dependencies
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.nerc.CodeTableType;
import org.constellation.generic.vocabulary.Vocabulary;
import org.constellation.skos.RDF;
import org.constellation.ws.rs.WebService;

// Geotools dependencies
import org.geotools.metadata.iso.IdentifierImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.identification.KeywordsImpl;
import org.geotools.util.SimpleInternationalString;

// GeoAPI dependencies
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public abstract class SDNMetadataReader extends GenericMetadataReader {
    
    /**
     * A map making the correspondance between parameter code and the real keyword value.
     * this map is fill from a list of configuration file P021.xml, L05.xml, ..
     */
    protected Map<String, Vocabulary> vocabularies;
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param genericConfiguration
     */
    public SDNMetadataReader(Automatic configuration, Connection connection) throws SQLException, JAXBException {
        super(configuration, connection);
        File cswConfigDir      = new File(WebService.getSicadeDirectory(), "csw_configuration");
        vocabularies           = loadVocabulary(new File(cswConfigDir, "vocabulary"), true);
    }
    
    /**
     * Build a new Generic metadata reader and initialize the statement (with a flag for filling the Anchors).
     * @param genericConfiguration
     */
    public SDNMetadataReader(Automatic configuration, Connection connection, boolean fillAnchor) throws SQLException, JAXBException {
        super(configuration, connection, fillAnchor);
        File cswConfigDir      = new File(WebService.getSicadeDirectory(), "csw_configuration");
        vocabularies           = loadVocabulary(new File(cswConfigDir, "vocabulary"), fillAnchor);
    }

    /**
     * Load a Map of vocabulary from the specified directory
     */
    private Map<String, Vocabulary> loadVocabulary(File vocabDirectory, boolean fillAnchor) {
        Map<String, Vocabulary> result = new HashMap<String, Vocabulary>();
        if (vocabDirectory.isDirectory()) {
            if (vocabDirectory.listFiles().length == 0) {
                logger.severe("the vocabulary folder is empty :" + vocabDirectory.getPath());
            }
            for (File f : vocabDirectory.listFiles()) {
                if (f.getName().startsWith("SDN.") && f.getName().endsWith(".xml")) {
                    try {
                        
                        Object obj      = unmarshaller.unmarshal(f);
                        Vocabulary voca = null;
                        if (obj instanceof CodeTableType) {
                            CodeTableType ct = (CodeTableType) obj;
                            voca = new Vocabulary(ct.getListVersion(), ct.getListLongName(), ct.getListLastMod());

                            File skosFile = new File(f.getPath().replace("xml", "rdf"));
                            if (skosFile.exists()) {
                                RDF rdf = (RDF) unmarshaller.unmarshal(skosFile);
                                if (fillAnchor)
                                    rdf.fillAnchors();
                                voca.setMap(rdf.getShortMap());
                            } else {
                                logger.severe("no skos file found for vocabulary file : " + f.getName());
                            }

                        } else if (obj instanceof Vocabulary) {
                            voca = (Vocabulary) obj;
                            voca.fillMap();
                            String vocaName = f.getName();
                            vocaName = vocaName.substring(vocaName.indexOf("SDN.") + 4);
                            vocaName = vocaName.substring(0, vocaName.indexOf('.'));
                        } else {
                            logger.severe("Unexpected vocabulary file type for file: " + f.getName());
                        }
                        
                        if (voca != null) {
                            String vocaName = f.getName();
                            vocaName = vocaName.substring(vocaName.indexOf("SDN.") + 4);
                            vocaName = vocaName.substring(0, vocaName.indexOf('.'));
                            result.put(vocaName, voca);
                             //info part (debug) 
                            String report = "added vocabulary: " + vocaName + " with ";
                            report += voca.getMap().size() + " entries";
                            logger.finer(report);
                        }
                    } catch (JAXBException ex) {
                        logger.severe("Unable to unmarshall the vocabulary configuration file : " + f.getPath());
                        ex.printStackTrace();
                    }
                } else if (!f.getName().endsWith(".rdf")){
                    logger.severe("Vocabulary file : " + f.getPath() + " does not follow the pattern 'SDN.<vocabName>...'");
                }
            }
        } else {
            logger.severe("There is nor vocabulary directory: " + vocabDirectory.getPath());
        }
        return result;
    }
    
    /**
     * 
     */
    protected List<String> getKeywordsValue(List<String> values, String altTitle) {
        
        //we try to get the vocabulary Map.
        Vocabulary voca = vocabularies.get(altTitle);
        Map<String, String> vocaMap = null;
        if (voca == null) {
            logger.info("No vocabulary found for code: " + altTitle);
        } else {
            vocaMap = voca.getMap();
        }
        
        List<String> result = new ArrayList<String>();
        for (String value: values) {
            if (vocaMap != null) {
                String mappedValue = vocaMap.get(value);
                if (mappedValue != null)
                    value = mappedValue;
            }
            if (value != null) {
                result.add(value);
            } else {
                logger.severe("keywords value null");
            }
        }
        return result;
    }
    
    /**
     * 
     * @param values
     * @param keywordType
     * @param altTitle
     * @return
     */
    protected Keywords createKeyword(List<String> values, String keywordType, String altTitle) {

        //we try to get the vocabulary Map.
        Vocabulary voca = vocabularies.get(altTitle);
        Map<String, String> vocaMap = null;
        if (voca == null) {
            logger.info("No vocabulary found for code: " + altTitle);
        } else {
            vocaMap = voca.getMap();
        }
        
        KeywordsImpl keyword = new KeywordsImpl();
        List<InternationalString> kws = new ArrayList<InternationalString>();
        for (String value: values) {
            if (vocaMap != null) {
                String mappedValue = vocaMap.get(value);
                if (mappedValue != null)
                    value = mappedValue;
            }
            if (value != null) {
                kws.add(new SimpleInternationalString(value));
            } else {
                logger.severe("keywords value null");
            }
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf(keywordType));
        
        //we create the citation describing the vocabulary used
        if (voca != null) {
            CitationImpl citation = new CitationImpl();
            citation.setTitle(new SimpleInternationalString(voca.getTitle()));
            citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(altTitle)));
            CitationDate revisionDate;
            if (voca.getDate() != null && !voca.getDate().equals("")) {
                revisionDate = createRevisionDate(voca.getDate());
            } else {
                revisionDate = new CitationDateImpl(null, DateType.REVISION); 
            }
            citation.setDates(Arrays.asList(revisionDate));
            if (voca.getVersion() != null && !voca.getVersion().equals(""))
                citation.setEdition(new SimpleInternationalString(voca.getVersion()));
            citation.setIdentifiers(Arrays.asList(new IdentifierImpl("http://www.seadatanet.org/urnurl/")));
            keyword.setThesaurusName(citation);
        }
        
        return keyword;
    }
    
    public void destroy() {
        super.destroy();
         vocabularies.clear();
    }
}
