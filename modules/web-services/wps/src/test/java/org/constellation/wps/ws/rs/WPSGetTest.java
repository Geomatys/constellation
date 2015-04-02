/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.wps.ws.rs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sis.util.NullArgumentException;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.wps.xml.v100.DataInputsType;
import org.geotoolkit.wps.xml.v100.DocumentOutputDefinitionType;
import org.geotoolkit.wps.xml.v100.InputReferenceType;
import org.geotoolkit.wps.xml.v100.InputType;
import org.geotoolkit.wps.xml.v100.LiteralDataType;
import org.geotoolkit.wps.xml.v100.ResponseDocumentType;
import org.geotoolkit.wps.xml.v100.ResponseFormType;
import org.junit.Assert;
import org.junit.Test;

public class WPSGetTest {

    private static final String TEST_PROCESS_ID = "urn:ogc:cstl:wps:test:wpstest";
    private static final String TEST_INPUT_LITERAL = "urn:ogc:cstl:wps:test:wpstest:input:literal";
    private static final String TEST_INPUT_BBOX = "urn:ogc:cstl:wps:test:wpstest:input:bbox";
    private static final String TEST_INPUT_REFERENCE = "urn:ogc:cstl:wps:test:wpstest:input:reference";

    private static final String LOCAL_HTTP_RESOURCE_URL = "http://localhost:666/img.gif";
    private static final String SCHEMA_URL = "http://localhost:666/schema.xsd";

    private static final String MIME_TYPE = "mimeType";
    private static final String ENCODING = "encoding";
    private static final String GIF = "image/gif-wf";
    private static final String BASE_64 = "base64";
    private static final String HREF = "href";
    private static final String SCHEMA = "schema";
    private static final String UOM = "uom";
    private static final String DATA_TYPE = "dataType";

    /** Tests the method WPSService.extractInput
     *
     * Test cases :
     *
     * case 1
     *      dataInputs = urn:ogc:cstl:wps:test:wpstest:input:bbox=46,102,47,103,urn:ogc:def:crs:EPSG:6.6:4326,2;urn:ogc:cstl:wps:test:wpstest:input:double=42.24@uom=meters@dataType=float;urn:ogc:cstl:wps:test:wpstest:input:ref=@href=http://localhost:666/img.gif@mimeType=image/gif-wf@encoding=base64@schema=http://localhost:666/schema.xsd
     *      processIdentifier = urn:ogc:cstl:wps:test:wpstest
     *
     * case 2
     *      dataInputs = an empty string
     *      processIdentifier = urn:ogc:cstl:wps:test:wpstest
     *
     * case 3
     *      dataInputs = null
     *      processIdentifier = urn:ogc:cstl:wps:test:wpstest
     *
     * case 4
     *      dataInputs = null
     *      processIdentifier = null
     */
    @Test
    public void extractInputTest() throws CstlServiceException {
        // TEST CASE 1
        // Contains a bbox, a literal, and a reference
        // The bbox crs has 2 dimension
        // The literal and the bbox have all their attributes
        String dataInputs = "urn:ogc:cstl:wps:test:wpstest:input:bbox=46,102,47,103,urn:ogc:def:crs:EPSG:6.6:4326,2;"
                + "urn:ogc:cstl:wps:test:wpstest:input:double=42.24@uom=meters@dataType=float;"
                + "urn:ogc:cstl:wps:test:wpstest:input:ref=@href=http://localhost:666/img.gif@mimeType=image/gif-wf@encoding=base64@schema=http://localhost:666/schema.xsd";
        String processIdentifier = "urn:ogc:cstl:wps:test:wpstest";

        DataInputsType dataInputsType = WPSService.extractInput(processIdentifier, dataInputs);

        Assert.assertTrue(dataInputsType.getInput().size() == 3);

        for (InputType inputType : dataInputsType.getInput()) {
            // Bounding box assertions
            if (inputType.getIdentifier().getValue().equals("urn:ogc:cstl:wps:test:wpstest:input:bbox")) {
                Assert.assertTrue(inputType.getData() != null);
                BoundingBoxType bbox = inputType.getData().getBoundingBoxData();
                Assert.assertTrue(bbox != null);
                Assert.assertTrue(bbox.getCrs().equals("EPSG:4326"));
                Assert.assertTrue(bbox.getDimensions() == 2);

                List<Double> lowerCorner = bbox.getLowerCorner();
                List<Double> upperCorner = bbox.getUpperCorner();

                Assert.assertTrue(lowerCorner.size() == 2);
                Assert.assertTrue(upperCorner.size() == 2);

                Assert.assertTrue(lowerCorner.get(0) == 46);
                Assert.assertTrue(lowerCorner.get(1) == 102);
                Assert.assertTrue(upperCorner.get(0) == 47);
                Assert.assertTrue(upperCorner.get(1) == 103);
            }
            // Literal assertions
            else if (inputType.getIdentifier().getValue().equals("urn:ogc:cstl:wps:test:wpstest:input:double")) {
                Assert.assertTrue(inputType.getData() != null);
                LiteralDataType literal = inputType.getData().getLiteralData();
                Assert.assertTrue(literal != null);
                Assert.assertTrue(literal.getUom().equals("meters"));
                Assert.assertTrue(literal.getDataType().equals("float"));
                Assert.assertTrue(literal.getValue().equals("42.24"));
            }
            // Reference assertions
            else if (inputType.getIdentifier().getValue().equals("urn:ogc:cstl:wps:test:wpstest:input:ref")) {
                InputReferenceType reference = inputType.getReference();
                Assert.assertTrue(reference != null);
                Assert.assertTrue(reference.getEncoding().equals(BASE_64));
                Assert.assertTrue(reference.getMimeType().equals(GIF));
                Assert.assertTrue(reference.getSchema().equals(SCHEMA_URL));
                Assert.assertTrue(reference.getHref().equals(LOCAL_HTTP_RESOURCE_URL));
            }
            // If it is not any of three above types there is an issue with the extracted input
            else
                Assert.fail();
        }

        // TEST CASE 2
        // dataInputs is an empty string
        // It should raise an exception
        try {
            WPSService.extractInput(processIdentifier, "");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {
        }


        // TEST CASE 3
        // dataInputs is null
        // Should raise an exception
        try {
            WPSService.extractInput(processIdentifier, null);
            Assert.fail();
        }
        catch (NullArgumentException ex) {
        }

        // TEST CASE 4
        // dataInputs and processIdentifier are null
        // should raise an exception
        try {
            WPSService.extractInput(null, null);
            Assert.fail();
        }
        catch (NullArgumentException ex) {
        }
    }

    /** Tests the method WPSService.detectReference
     *
     * Test cases :
     *
     * - one with a map containing no reference (expected result : return false)
     * - one with a map containing a reference (expected result : return true)
     * - one with a map containing juste attributes (check for mandatory argument, expected result false)
     * - one with a map containing a reference and attributes (expected result : return true)
     *
     * Missing test : null as attributes map
     */
    @Test
    public void detectReferenceTest() {
        // No reference
        {
        Map<String, String> attrMap = new HashMap<>();

        Assert.assertFalse(WPSService.detectReference(attrMap));
        }

        // A reference with no optional attributes
        {
            Map<String, String> attrMap = new HashMap<>();
            attrMap.put(HREF, LOCAL_HTTP_RESOURCE_URL);
            attrMap.put(TEST_INPUT_REFERENCE, null);

            Assert.assertTrue(WPSService.detectReference(attrMap));
        }


        // Just optional attributes
        {
            Map<String, String> attrMap = new HashMap<>();
            attrMap.put(TEST_INPUT_REFERENCE, null);
            attrMap.put(MIME_TYPE, GIF);
            attrMap.put(ENCODING, BASE_64);

            Assert.assertFalse(WPSService.detectReference(attrMap));
        }


        // Reference plus optional attributes
        {
            Map<String, String> attrMap = new HashMap<>();
            attrMap.put(TEST_INPUT_REFERENCE, null);
            attrMap.put(MIME_TYPE, GIF);
            attrMap.put(ENCODING, BASE_64);
            attrMap.put(HREF, LOCAL_HTTP_RESOURCE_URL);

            Assert.assertTrue(WPSService.detectReference(attrMap));
        }
    }

    /** Tests the method WPSService.detectBoundingBox
     *
     * Test cases :
     *
     * - good inputIdentifier, good processIdentifier (expected : true)
     * - bad inputIdentifier, bad processIdentifier (expected : false)
     * - good processIdentifier, bad inputIdentifier (expected : false)
     * - bad processIdentifier, bad inputIdentifier and a map with an attribute (expected : false)
     *
     * Missing test : null processId, null inputId
     */
    @Test
    public void detectBoundingBoxTest() throws CstlServiceException {
        String processId;
        String inputId;
        Map<String, String> attributesMap = new HashMap<>();

        // Good processId, good inputId
        processId = TEST_PROCESS_ID;
        inputId = TEST_INPUT_BBOX;
        attributesMap.put(inputId, null);

        // In order to detect a bounding box the WPSService class retrieves the
        // the process descriptors using a WPSUtils method, that is why a TestProcess
        // has been added to this test package (see the classes TestProcessingRegistry,
        // TestDescriptor, TestProcess)
        Assert.assertTrue(WPSService.detectBoundingBox(processId, inputId, attributesMap));

        try {
            // Bad processId, bad inputId
            processId = "bad:process:id";
            inputId = "bad:input:id";
            WPSService.detectBoundingBox(processId, inputId, attributesMap);
            Assert.fail();

            // Good processId, bad inputId
            WPSService.detectBoundingBox(processId, inputId, attributesMap);
            Assert.fail();
        }
        catch (CstlServiceException ex) {
        }

        // Bad processId, bad inputId and an attribute
        attributesMap.put(DATA_TYPE, "string");
        Assert.assertFalse(WPSService.detectBoundingBox(processId, inputId, attributesMap));
    }

    /** Tests the method WPSService.readReference
     *
     * Test cases :
     *
     * - a case with one unknown attribute, expects an exception
     * - a case with everything correctly set, expects an InputType correctly filled
     *
     * Missing test :
     *
     * Test not implemented attributes (method, body, bodyReference, header)
     */
    @Test
    public void readReferenceTest() throws CstlServiceException {
       String inputIdentifier = TEST_INPUT_REFERENCE;

       // One unknown argument
       {
            Map<String, String> attr = new HashMap<>();
            attr.put(TEST_INPUT_REFERENCE, null);
            attr.put(HREF, LOCAL_HTTP_RESOURCE_URL);
            attr.put("unknowAttribute", "unkown-value");

            try {
                WPSService.readReference(inputIdentifier, attr);
                Assert.fail("");
            }
            catch (CstlServiceException ex) {
            }
       }

       // Everything correctly set
       {
            Map<String, String> attr = new HashMap<>();
            attr.put(TEST_INPUT_REFERENCE, null);
            attr.put(HREF, LOCAL_HTTP_RESOURCE_URL);
            attr.put(SCHEMA, SCHEMA_URL);
            attr.put(MIME_TYPE, GIF);
            attr.put(ENCODING, BASE_64);

           InputType inputType = WPSService.readReference(inputIdentifier, attr);
           InputReferenceType inputRef = inputType.getReference();

           Assert.assertTrue(inputType.getIdentifier().getValue().equals(TEST_INPUT_REFERENCE));
           Assert.assertTrue(inputRef != null);
           Assert.assertTrue(inputRef.getEncoding().equalsIgnoreCase(BASE_64));
           Assert.assertTrue(inputRef.getHref().equals(LOCAL_HTTP_RESOURCE_URL));
           Assert.assertTrue(inputRef.getMimeType().equalsIgnoreCase(GIF));
       }
    }

    /** Tests the method WPSService.readLiteralData
     *
     * Test cases :
     *
     * - one case with a value and all attributes (expects a correctly filled InputType)
     * - one case with no value and no attributes (expects an exception)
     * - one case with a value and an unknown attribute (expects an exception)
     *
     * Missing test :
     *
     * null values as inputs
     */
    @Test
    public void readLiteralDataTest() throws CstlServiceException {
        String inputIdentifier = TEST_INPUT_LITERAL;

        // A value plus all attributes
        {
            Map<String , String> attr = new HashMap<>();
            attr.put(inputIdentifier, "42.24");
            attr.put(UOM, "meters");
            attr.put(DATA_TYPE, "float");

            InputType inputType = WPSService.readLiteralData(inputIdentifier, attr);
            Assert.assertTrue(inputType.getData() != null && inputType.getData().getLiteralData() != null);
            LiteralDataType literal = inputType.getData().getLiteralData();

            Assert.assertTrue(literal.getDataType().equals("float"));
            Assert.assertTrue(literal.getUom().equals("meters"));
            Assert.assertTrue(literal.getValue().equals("42.24"));
        }

        // No value, no attributes
        {
            Map<String, String> attr = new HashMap<>();
            attr.put(inputIdentifier, null);

            try {
                WPSService.readLiteralData(inputIdentifier, attr);
                Assert.fail();
            }
            catch (CstlServiceException ex) {
            }
        }

        // A value plus an unkown attribute
        {
            Map<String , String> attr = new HashMap<>();
            attr.put(inputIdentifier, "42.24");
            attr.put("unkownAttribute", "unkown-value");

            try {
                WPSService.readLiteralData(inputIdentifier, attr);
                Assert.fail();
            }
            catch (CstlServiceException ex) {
            }
        }
    }

    /** Tests the method WPSService.readBoundingBoxData
     *
     * Test cases with the following BoundingBox Strings :
     *
     * 41,107,42,108 ---> correct input
     * 41,107,42,108,2 --> correct input
     * 41,107,42,108,3 --> bad input, the dimension hint is different from the number of dimension read
     * 41,107,42,108,2,EPSG:4326 --> bad input, the dimension can not be before the crs
     * 41,107,42,108,EPSG:4326,2 --> correct input
     * 41,107,42,108,EPSG:4326 --> correct input
     * 41,107,42,108,EPSG:4326,3 --> bad input, the CRS has 2 dimension while we gave inputs for 2 dimensions
     * 41,107,42,108,10,200,EPSG:4326,3 --> bad input, the CRS has 2 dimension and isn't applicable on a 3D bounding box
     * xx,yy,zz,aa,bb,EPSG:4326,3 --> bad input, this is not a bounding box
     *
     * Missing test case :
     *
     * null values
     */
    @Test
    public void readBoundingBoxDataTest() throws CstlServiceException {
        final String inputIdentifier = TEST_INPUT_BBOX;
        final String DEFAULT_CRS = "EPSG:4326";
        final int DIMENSION = 2;
        final String BASE_BBOX_INPUT = "41,107,42,108";

        // Test with the correct input 41,107,42,108
        testCorrectBoundingBox(inputIdentifier, "41,107,42,108", DIMENSION, DEFAULT_CRS);

        // Test with the correct input 41,107,42,108,2
        testCorrectBoundingBox(inputIdentifier, BASE_BBOX_INPUT + ",2", DIMENSION, DEFAULT_CRS);

        // Test with the correct input 41,107,42,108,EPSG:4326,2
        testCorrectBoundingBox(inputIdentifier, BASE_BBOX_INPUT + "," + DEFAULT_CRS + ",2", DIMENSION, DEFAULT_CRS);

        // Test with the correct input 41,107,42,108,EPSG:4326
        testCorrectBoundingBox(inputIdentifier, BASE_BBOX_INPUT + "," + DEFAULT_CRS, DIMENSION, DEFAULT_CRS);

        // Test with the bad input 41,107,42,108,3
        testBadBoundingBox(inputIdentifier, BASE_BBOX_INPUT + ",3");

        // Test with the bad input 41,107,42,108,EPSG:4326,3
        testBadBoundingBox(inputIdentifier, BASE_BBOX_INPUT + "," + DEFAULT_CRS + ",3");

        // Test with the bad input 41,107,42,108,10,200,EPSG:4326,3
        testBadBoundingBox(inputIdentifier, BASE_BBOX_INPUT + "10,200," + DEFAULT_CRS + ",3");

        // Test with the bad input xx,yy,zz,aa,bb,EPSG:4326,3
        testBadBoundingBox(inputIdentifier, "xx,yy,zz,aa,bb,EPSG:4326,3");

        // Test with the correct input 41,107,42,108,2,EPSG:4326
        testBadBoundingBox(inputIdentifier, BASE_BBOX_INPUT + ",2," + DEFAULT_CRS);
    }

    /**
     * Helper method that tests a bounding box input that should be correct
     *
     * WARNING :
     * The method assumes that the bboxInput always start with the following sub-string
     * for the sake of simplicity...
     *
     * 41,107,42,108
     *
     *
     * @param inputIdentifier
     * @param bboxInput
     * @param expectedDimension
     * @param crsCode
     * @throws CstlServiceException
     */
    public static void testCorrectBoundingBox(final String inputIdentifier, final String bboxInput, int expectedDimension, String crsCode) throws CstlServiceException {
        Map<String, String> attr = new HashMap<>();
        attr.put(inputIdentifier, bboxInput);

        InputType inputType = WPSService.readBoundingBoxData(inputIdentifier, attr);

        Assert.assertTrue(inputType.getIdentifier().getValue().equals(inputIdentifier));
        Assert.assertTrue(inputType.getData() != null && inputType.getData().getBoundingBoxData() != null);

        BoundingBoxType bbox = inputType.getData().getBoundingBoxData();

        Assert.assertTrue(bbox.getDimensions() == expectedDimension);

        Assert.assertTrue(bbox.getLowerCorner().size() == expectedDimension &&
                          bbox.getLowerCorner().get(0) == 41 &&
                          bbox.getLowerCorner().get(1) == 107);

        Assert.assertTrue(bbox.getUpperCorner().size() == expectedDimension &&
                          bbox.getUpperCorner().get(0) == 42 &&
                          bbox.getUpperCorner().get(1) == 108);

        Assert.assertTrue(bbox.getCrs().equals(crsCode));
    }

    /**
     * Helper method that tests a bounding box input that should be incorrect
     * @param inputIdentifier inputIdentifier of the bounding box
     * @param bboxInput incorrect bounding box input
     */
    public static void testBadBoundingBox(final String inputIdentifier, final String bboxInput) {
        Map<String, String> attr = new HashMap<>();
        attr.put(inputIdentifier, bboxInput);

        try {
            WPSService.readBoundingBoxData(inputIdentifier, attr);
            Assert.fail();
        }
        catch (CstlServiceException ex) {
        }
    }

    /**
     * Tests the method WPSService.extractOutputParameter
     *
     * Test cases :
     *
     * The method accepts few inputs : "true", "false", null
     * All cases are tested
     */
    @Test
    public void extractOutputParameterTest() throws CstlServiceException {
        Assert.assertTrue(WPSService.extractOutputParameter("true"));
        Assert.assertFalse(WPSService.extractOutputParameter("false"));
        Assert.assertFalse(WPSService.extractOutputParameter(null));

        try {
            WPSService.extractOutputParameter("should raise an exception on this");
            Assert.fail();
        }
        catch (CstlServiceException ex) {
        }
    }

    /**
     * Tests the method WPSService.extractResponsForm
     *
     * Test cases :
     *
     * - one with isRawData=true and an asReference attribute set (expects an exception)
     * - one with isRawData=false and every attributes set (expects a correctly filled responseForm
     * - one with an unknown attribute (expects an exception)
     * - one with a null input
     */
    @Test
    public void extractResponseFormTest() throws CstlServiceException {

        // isRawData=true and asReference defined
        {
            String input = "urn:ogc:cstl:wps:test:output:result=@" + MIME_TYPE + "=" + GIF +
                            "@" + ENCODING + "=" + BASE_64 +
                            "@" + SCHEMA + "=" + SCHEMA_URL +
                            "@" + UOM + "=meters@asReference=true";
            try {
                WPSService.extractResponseForm(input, true);
                Assert.fail();
            }
            catch (CstlServiceException ex) {
            }
        }

        // isRawData=false and all attributes
        {
            String input = "urn:ogc:cstl:wps:test:output:result=@" + MIME_TYPE + "=" + GIF +
                    "@" + ENCODING + "=" + BASE_64 +
                    "@" + SCHEMA + "=" + SCHEMA_URL +
                    "@" + UOM + "=meters@asReference=true";

            ResponseFormType responseForm = WPSService.extractResponseForm(input, false);

            Assert.assertNotNull(responseForm.getResponseDocument());
            ResponseDocumentType responseDocument = responseForm.getResponseDocument();
            Assert.assertTrue(responseDocument.getOutput().size() == 1);
            DocumentOutputDefinitionType outputDef = responseDocument.getOutput().get(0);
            Assert.assertTrue(outputDef.isAsReference());
            Assert.assertTrue(outputDef.getEncoding().equals(BASE_64));
            Assert.assertTrue(outputDef.getMimeType().equals(GIF));
            Assert.assertTrue(outputDef.getSchema().equals(SCHEMA_URL));
            Assert.assertTrue(outputDef.getUom().equals("meters"));
            Assert.assertTrue(outputDef.getIdentifier().getValue().equals("urn:ogc:cstl:wps:test:output:result"));
        }

        // isRawData=false and an unkown attribute
        {
            String input = "urn:ogc:cstl:wps:test:output:result=@unknownattribute=unknownvalue";
            try {
                WPSService.extractResponseForm(input, false);
                Assert.fail();
            }
            catch (CstlServiceException ex) {
            }
        }

        // null input
        {
            try {
                WPSService.extractResponseForm(null, false);
                Assert.fail();
            }
            catch (NullArgumentException ex) {
            }
        }
    }
}
