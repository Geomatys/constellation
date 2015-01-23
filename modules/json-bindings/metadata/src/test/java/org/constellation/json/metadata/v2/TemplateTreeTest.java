
package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.constellation.json.metadata.binding.RootObj;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author guilhem
 */
public class TemplateTreeTest {
    
    
    @Test
    public void testTreeFromEmptyTemplate() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream = TemplateTreeTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree  = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result   = tree.getRoot();
        
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode dataQuality = new ValueNode("metadata.dataQualityInfo", null, 0, expresult, null, false);
        final ValueNode report      = new ValueNode("metadata.dataQualityInfo.report", "org.opengis.metadata.quality.DomainConsistency", 0, dataQuality, null, false);
        final ValueNode resultN     = new ValueNode("metadata.dataQualityInfo.report.result", "org.opengis.metadata.quality.ConformanceResult", 0, report, null, false);
        final ValueNode spec        = new ValueNode("metadata.dataQualityInfo.report.result.specification", null, 0, resultN, null, false);
        final ValueNode specTitle   = new ValueNode("metadata.dataQualityInfo.report.result.specification.title", null, null, "Specification.codelist", 0, null, spec);
        final ValueNode specDate    = new ValueNode("metadata.dataQualityInfo.report.result.specification.date", null, 0, spec, null, false);
        final ValueNode specDateD   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.date", null, null, "DATE.text", 0, null, specDate);
        final ValueNode specDateT   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, null, specDate);
        
        final ValueNode scope       = new ValueNode("metadata.dataQualityInfo.scope", null, 0, dataQuality, null, false);
        final ValueNode scopeLvl    = new ValueNode("metadata.dataQualityInfo.scope.level", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, null, scope);
        
        final ValueNode pass        = new ValueNode("metadata.dataQualityInfo.report.result.pass", null, "nilReason:unknown", "ResultPass.codelist", 0, null, resultN);
        final ValueNode explanation = new ValueNode("metadata.dataQualityInfo.report.result.explanation", null, "See the referenced specification", "textarea", 0, null, resultN);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword     = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, null, dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, null, false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate);
        
        final ValueNode fid         = new ValueNode("metadata.fileIdentifier", null, null, "readonly", 0, null, expresult);
        final ValueNode date        = new ValueNode("metadata.dateStamp", null, null, "DATE.readonly", 0, null, expresult);
        final ValueNode stanName    = new ValueNode("metadata.metadataStandardName", null, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", "readonly", 0, null, expresult);
        final ValueNode stanVers    = new ValueNode("metadata.metadataStandardVersion", null, "2011.03", "readonly", 0, null, expresult);
        final ValueNode charac      = new ValueNode("metadata.characterSet", null, "UTF-8", "CharacterSet.codelist", 0, null, expresult);
        final ValueNode lang        = new ValueNode("metadata.language", null, "LanguageCode.fra", "Language.codelist", 0, null, expresult);
        final ValueNode hierar      = new ValueNode("metadata.hierarchyLevel", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, null, expresult);
        
        valueNodeEquals(expresult, result);
        
    }
    
    @Test
    public void testTreeFromFilledTemplate() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode dataQuality = new ValueNode("metadata.dataQualityInfo", null, 0, expresult, null, false);
        final ValueNode report      = new ValueNode("metadata.dataQualityInfo.report", "org.opengis.metadata.quality.DomainConsistency", 0, dataQuality, null, false);
        final ValueNode resultN     = new ValueNode("metadata.dataQualityInfo.report.result", "org.opengis.metadata.quality.ConformanceResult", 0, report, null, false);
        final ValueNode spec        = new ValueNode("metadata.dataQualityInfo.report.result.specification", null, 0, resultN, null, false);
        final ValueNode specTitle   = new ValueNode("metadata.dataQualityInfo.report.result.specification.title", null, null, "Specification.codelist", 0, "some title", spec);
        final ValueNode specDate    = new ValueNode("metadata.dataQualityInfo.report.result.specification.date", null, 0, spec, null, false);
        final ValueNode specDateD   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.date", null, null, "DATE.text", 0, "1970-05-10", specDate);
        final ValueNode specDateT   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, "CI_DateTypeCode.creation", specDate);
        
        final ValueNode scope       = new ValueNode("metadata.dataQualityInfo.scope", null, 0, dataQuality, null, false);
        final ValueNode scopeLvl    = new ValueNode("metadata.dataQualityInfo.scope.level", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", scope);
        
        final ValueNode pass        = new ValueNode("metadata.dataQualityInfo.report.result.pass", null, "nilReason:unknown", "ResultPass.codelist", 0, "true", resultN);
        final ValueNode explanation = new ValueNode("metadata.dataQualityInfo.report.result.explanation", null, "See the referenced specification", "textarea", 0, "some explanation", resultN);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, null, false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate);
        
        final ValueNode dkey2        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 1, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword21    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "this", dkey2);
        final ValueNode keyword22    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "is", dkey2);
        final ValueNode thesau2      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey2, null, false);
        final ValueNode thesauTitle2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau2);
        final ValueNode thesauDate2  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau2, null, false);
        final ValueNode thesauDateD2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate2);
        final ValueNode thesauDateT2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate2);
        
        final ValueNode fid         = new ValueNode("metadata.fileIdentifier", null, null, "readonly", 0, "metadata-id-0007", expresult);
        final ValueNode date        = new ValueNode("metadata.dateStamp", null, null, "DATE.readonly", 0, null, expresult);
        final ValueNode stanName    = new ValueNode("metadata.metadataStandardName", null, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", "readonly", 0, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", expresult);
        final ValueNode stanVers    = new ValueNode("metadata.metadataStandardVersion", null, "2011.03", "readonly", 0, "2011.03", expresult);
        final ValueNode charac      = new ValueNode("metadata.characterSet", null, "UTF-8", "CharacterSet.codelist", 0, "UTF-8", expresult);
        final ValueNode lang        = new ValueNode("metadata.language", null, "LanguageCode.fra", "Language.codelist", 0, "LanguageCode.fra", expresult);
        final ValueNode hierar      = new ValueNode("metadata.hierarchyLevel", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", expresult);
        
        valueNodeEquals(expresult, result);
    }
    
    @Test
    public void testTreeFromFilledTemplate2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result2.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode dataQuality = new ValueNode("metadata.dataQualityInfo", null, 0, expresult, null, false);
        final ValueNode report      = new ValueNode("metadata.dataQualityInfo.report", null, 0, dataQuality, null, false);
        final ValueNode resultN     = new ValueNode("metadata.dataQualityInfo.report.result", null, 0, report, null, false);
        final ValueNode spec        = new ValueNode("metadata.dataQualityInfo.report.result.specification", null, 0, resultN, null, false);
        final ValueNode specTitle   = new ValueNode("metadata.dataQualityInfo.report.result.specification.title", null, null, "Specification.codelist", 0, "some title", spec);
        final ValueNode specDate    = new ValueNode("metadata.dataQualityInfo.report.result.specification.date", null, 0, spec, null, false);
        final ValueNode specDateD   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.date", null, null, "DATE.text", 0, "1970-05-10", specDate);
        final ValueNode specDateT   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, "CI_DateTypeCode.creation", specDate);
        
        final ValueNode scope       = new ValueNode("metadata.dataQualityInfo.scope", null, 0, dataQuality, null, false);
        final ValueNode scopeLvl    = new ValueNode("metadata.dataQualityInfo.scope.level", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", scope);
        
        final ValueNode pass        = new ValueNode("metadata.dataQualityInfo.report.result.pass", null, "nilReason:unknown", "ResultPass.codelist", 0, "true", resultN);
        final ValueNode explanation = new ValueNode("metadata.dataQualityInfo.report.result.explanation", null, "See the referenced specification", "textarea", 0, "some explanation", resultN);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, null, false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate);
        
        final ValueNode dkey2        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 1, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword21    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "this", dkey2);
        final ValueNode keyword22    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "is", dkey2);
        final ValueNode thesau2      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey2, null, false);
        final ValueNode thesauTitle2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau2);
        final ValueNode thesauDate2  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau2, null, false);
        final ValueNode thesauDateD2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate2);
        final ValueNode thesauDateT2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate2);
        
        final ValueNode constr       = new ValueNode("metadata.identificationInfo.resourceConstraints", "org.opengis.metadata.constraint.LegalConstraints", 0, ident, "metadata.block.legalconstraints", false);
        final ValueNode useLim       = new ValueNode("metadata.identificationInfo.resourceConstraints.useLimitation", null, null, "textarea", 0, null, constr);
        final ValueNode access       = new ValueNode("metadata.identificationInfo.resourceConstraints.accessConstraints", null, "MD_RestrictionCode.licence", "Restriction.codelist", 0, "MD_RestrictionCode.licence", constr);
        final ValueNode other        = new ValueNode("metadata.identificationInfo.resourceConstraints.otherConstraints", null, null, "textarea", 0, null, constr);
        
        final ValueNode constr2      = new ValueNode("metadata.identificationInfo.resourceConstraints", "org.opengis.metadata.constraint.SecurityConstraints", 1, ident, "metadata.block.securityconstraints", false);
        final ValueNode useLim2      = new ValueNode("metadata.identificationInfo.resourceConstraints.useLimitation", null, null, "textarea", 0, "some limitations", constr2);
        final ValueNode classif      = new ValueNode("metadata.identificationInfo.resourceConstraints.classification", null, "MD_ClassificationCode.unclassified", "Classification.codelist", 0, "MD_ClassificationCode.unclassified", constr2);
        final ValueNode useNote      = new ValueNode("metadata.identificationInfo.resourceConstraints.userNote", null, null, "textarea", 0, null, constr2);
        
        final ValueNode fid         = new ValueNode("metadata.fileIdentifier", null, null, "readonly", 0, "metadata-id-0007", expresult);
        final ValueNode date        = new ValueNode("metadata.dateStamp", null, null, "DATE.readonly", 0, null, expresult);
        final ValueNode stanName    = new ValueNode("metadata.metadataStandardName", null, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", "readonly", 0, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", expresult);
        final ValueNode stanVers    = new ValueNode("metadata.metadataStandardVersion", null, "2011.03", "readonly", 0, "2011.03", expresult);
        final ValueNode charac      = new ValueNode("metadata.characterSet", null, "UTF-8", "CharacterSet.codelist", 0, "UTF-8", expresult);
        final ValueNode lang        = new ValueNode("metadata.language", null, "LanguageCode.fra", "Language.codelist", 0, "LanguageCode.fra", expresult);
        final ValueNode hierar      = new ValueNode("metadata.hierarchyLevel", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", expresult);
        
        valueNodeEquals(expresult, result);
    }
    
    @Test
    public void testTreeFromFilledTemplate3() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result3.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode dataQuality = new ValueNode("metadata.dataQualityInfo", null, 0, expresult, null, false);
        final ValueNode report      = new ValueNode("metadata.dataQualityInfo.report", null, 0, dataQuality, null, false);
        final ValueNode resultN     = new ValueNode("metadata.dataQualityInfo.report.result", null, 0, report, null, false);
        final ValueNode spec        = new ValueNode("metadata.dataQualityInfo.report.result.specification", null, 0, resultN, null, false);
        final ValueNode specTitle   = new ValueNode("metadata.dataQualityInfo.report.result.specification.title", null, null, "Specification.codelist", 0, "some title", spec);
        final ValueNode specDate    = new ValueNode("metadata.dataQualityInfo.report.result.specification.date", null, 0, spec, null, false);
        final ValueNode specDateD   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.date", null, null, "DATE.text", 0, "1970-05-10", specDate);
        final ValueNode specDateT   = new ValueNode("metadata.dataQualityInfo.report.result.specification.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, "CI_DateTypeCode.creation", specDate);
        
        final ValueNode scope       = new ValueNode("metadata.dataQualityInfo.scope", null, 0, dataQuality, null, false);
        final ValueNode scopeLvl    = new ValueNode("metadata.dataQualityInfo.scope.level", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", scope);
        
        final ValueNode pass        = new ValueNode("metadata.dataQualityInfo.report.result.pass", null, "nilReason:unknown", "ResultPass.codelist", 0, "true", resultN);
        final ValueNode explanation = new ValueNode("metadata.dataQualityInfo.report.result.explanation", null, "See the referenced specification", "textarea", 0, "some explanation", resultN);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, null, false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate);
        
        final ValueNode dkey2        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 1, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword21    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "this", dkey2);
        final ValueNode keyword22    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "is", dkey2);
        final ValueNode thesau2      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey2, null, false);
        final ValueNode thesauTitle2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau2);
        final ValueNode thesauDate2  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau2, null, false);
        final ValueNode thesauDateD2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate2);
        final ValueNode thesauDateT2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate2);
        
        final ValueNode constr       = new ValueNode("metadata.identificationInfo.resourceConstraints", "org.opengis.metadata.constraint.LegalConstraints", 0, ident, "metadata.block.legalconstraints", false);
        final ValueNode useLim       = new ValueNode("metadata.identificationInfo.resourceConstraints.useLimitation", null, null, "textarea", 0, "legal limitations", constr);
        final ValueNode access       = new ValueNode("metadata.identificationInfo.resourceConstraints.accessConstraints", null, "MD_RestrictionCode.licence", "Restriction.codelist", 0, "MD_RestrictionCode.licence", constr);
        final ValueNode other        = new ValueNode("metadata.identificationInfo.resourceConstraints.otherConstraints", null, null, "textarea", 0, null, constr);
        
        final ValueNode constr2      = new ValueNode("metadata.identificationInfo.resourceConstraints", "org.opengis.metadata.constraint.SecurityConstraints", 1, ident, "metadata.block.securityconstraints", false);
        final ValueNode useLim2      = new ValueNode("metadata.identificationInfo.resourceConstraints.useLimitation", null, null, "textarea", 0, "some limitations", constr2);
        final ValueNode classif      = new ValueNode("metadata.identificationInfo.resourceConstraints.classification", null, "MD_ClassificationCode.unclassified", "Classification.codelist", 0, "MD_ClassificationCode.unclassified", constr2);
        final ValueNode useNote      = new ValueNode("metadata.identificationInfo.resourceConstraints.userNote", null, null, "textarea", 0, null, constr2);
        
        final ValueNode fid         = new ValueNode("metadata.fileIdentifier", null, null, "readonly", 0, "metadata-id-0007", expresult);
        final ValueNode date        = new ValueNode("metadata.dateStamp", null, null, "DATE.readonly", 0, null, expresult);
        final ValueNode stanName    = new ValueNode("metadata.metadataStandardName", null, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", "readonly", 0, "x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster", expresult);
        final ValueNode stanVers    = new ValueNode("metadata.metadataStandardVersion", null, "2011.03", "readonly", 0, "2011.03", expresult);
        final ValueNode charac      = new ValueNode("metadata.characterSet", null, "UTF-8", "CharacterSet.codelist", 0, "UTF-8", expresult);
        final ValueNode lang        = new ValueNode("metadata.language", null, "LanguageCode.fra", "Language.codelist", 0, "LanguageCode.fra", expresult);
        final ValueNode hierar      = new ValueNode("metadata.hierarchyLevel", null, "MD_ScopeCode.dataset", "CODELIST.readonly", 0, "MD_ScopeCode.dataset", expresult);
        
        valueNodeEquals(expresult, result);
    }
    
    
    @Test
    public void testTreeFromFilledTemplateKeywords() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result_keywords.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        
        final ValueNode dkey2        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 1, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword21    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "this", dkey2);
        final ValueNode keyword22    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "is", dkey2);
        
        valueNodeEquals(expresult, result);
    }
    
    @Test
    public void testTreeFromFilledTemplateKeywords3() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result_keywords3.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword_inspire", true);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, "GEMET", "readonly", 0, "GEMET", thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, null, false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, "2012-01-01", "DATE.text", 0, "2012-01-01", thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, "CI_DateTypeCode.publication", thesauDate);
        
        final ValueNode dkey2        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 1, ident, "metadata.block.descriptiveKeyword_inspire", true);
        final ValueNode keyword21    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "this", dkey2);
        final ValueNode keyword22    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "is", dkey2);
        final ValueNode thesau2      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey2, null, false);
        final ValueNode thesauTitle2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, "GEMET", "readonly", 0, "GEMET", thesau2);
        final ValueNode thesauDate2  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau2, null, false);
        final ValueNode thesauDateD2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, "2012-01-01", "DATE.text", 0, "2012-01-01", thesauDate2);
        final ValueNode thesauDateT2 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, "CI_DateTypeCode.publication", "CODELIST.readonly", 0, "CI_DateTypeCode.publication", thesauDate2);
        
        final ValueNode dkey3        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 2, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword31    = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, null, dkey3);
        final ValueNode thesau3      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey3, null, false);
        final ValueNode thesauTitle3 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau3);
        final ValueNode thesauDate3  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau3, null, false);
        final ValueNode thesauDateD3 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate3);
        final ValueNode thesauDateT3 = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate3);
        
        valueNodeEquals(expresult, result);
    }
    
    @Test
    public void testTreeFromEmptyTemplateMultipleBlock() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("profile_multiple_block.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, null, dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, null, thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, "metadata.block.descriptiveKeyword_thesaurus_date", false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, null, thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, null, thesauDate);
        
        
        
        valueNodeEquals(expresult, result);
    }
    
    @Test
    public void testTreeFromFilledTemplateMultipleBlock() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final InputStream stream  = TemplateTreeTest.class.getResourceAsStream("result_multiple_block.json");
        final RootObj root        =  objectMapper.readValue(stream, RootObj.class);
        final TemplateTree tree   = TemplateTree.getTreeFromRootObj(root);
        final ValueNode result    = tree.getRoot();
                
        final ValueNode expresult   = new ValueNode("metadata", null, 0, null, null, false);
        
        final ValueNode ident       = new ValueNode("metadata.identificationInfo", null, 0, expresult, null, false);
        
        final ValueNode dkey        = new ValueNode("metadata.identificationInfo.descriptiveKeywords", null, 0, ident, "metadata.block.descriptiveKeyword", false);
        final ValueNode keyword11   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 0, "hello", dkey);
        final ValueNode keyword12   = new ValueNode("metadata.identificationInfo.descriptiveKeywords.keyword", null, null, "KEYWORD.text", 1, "world", dkey);
        final ValueNode thesau      = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName", null, 0, dkey, null, false);
        final ValueNode thesauTitle = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.title", null, null, "text", 0, "GEMET", thesau);
        final ValueNode thesauDate  = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date", null, 0, thesau, "metadata.block.descriptiveKeyword_thesaurus_date", false);
        final ValueNode thesauDateD = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.date", null, null, "DATE.text", 0, "2012-01-01", thesauDate);
        final ValueNode thesauDateT = new ValueNode("metadata.identificationInfo.descriptiveKeywords.thesaurusName.date.dateType", null, null, "DATE.codelist", 0, "CI_DateTypeCode.publication", thesauDate);
        
        
        
        valueNodeEquals(expresult, result);
    }
    
    
    private static void valueNodeEquals(final ValueNode expected, final ValueNode result) {
        if (expected.children.size() == result.children.size()) {
            for (int i = 0 ; i < expected.children.size(); i++) {
                ValueNode expChild = expected.children.get(i);
                ValueNode resChild = result.children.get(i);
                valueNodeEquals(expChild, resChild);
            } 
        }
        Assert.assertEquals(expected, result);
    }
}
