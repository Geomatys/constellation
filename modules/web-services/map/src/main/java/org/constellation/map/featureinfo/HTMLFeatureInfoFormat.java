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
package org.constellation.map.featureinfo;

import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.feature.ComplexAttribute;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.Property;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.opengis.util.InternationalString;

import javax.measure.unit.Unit;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic FeatureInfoFormat that produce HTML output for Features and Coverages.
 * Supported mimeTypes are :
 * <ul>
 *     <li>text/html</li>
 * </ul>
 *
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class HTMLFeatureInfoFormat extends AbstractTextFeatureInfoFormat {

    private static final class LayerResult{
        private String layerName;
        private final List<String> values = new ArrayList<>();
    }
    
    private final Map<String,LayerResult> results = new HashMap<>();
    
    
    public HTMLFeatureInfoFormat() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedMimeTypes() {
        return Collections.singletonList(MimeType.TEXT_HTML);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        
        final FeatureMapLayer layer = graphic.getLayer();
        final String layerName = layer.getName();
        
        final Feature feature = graphic.getCandidate();
        final FeatureType type = feature.getType();
        final Collection<PropertyDescriptor> descs = type.getDescriptors();
        
        LayerResult result = results.get(layerName);
        if(result==null){
            //first feature of this type
            result = new LayerResult();
            result.layerName = layerName;
            results.put(layerName, result);
        }
        
        //the feature values
        final StringBuilder typeBuilder = new StringBuilder();
        final StringBuilder dataBuilder = new StringBuilder();
        
        typeBuilder.append("<h2>").append(feature.getIdentifier().getID()).append("</h2>");
        typeBuilder.append("</br>");
        typeBuilder.append("<div>");
        typeBuilder.append("<div class=\"left-part\">");
        dataBuilder.append("<div class=\"right-part\">");
        recursive(feature, typeBuilder, dataBuilder, 0);
        typeBuilder.append("</div>");
        dataBuilder.append("</div>");
        dataBuilder.append("</div>");
        
        result.values.add(typeBuilder.toString());
        result.values.add(dataBuilder.toString());
    }

    private void recursive(final Property att, final StringBuilder typeBuilder, final StringBuilder dataBuilder, int depth){
        
        if(att instanceof ComplexAttribute){
            if(depth!=0){
                typeBuilder.append("<li>\n");
                typeBuilder.append(att.getDescriptor().getName().tip().toString());
                typeBuilder.append("</li>\n");
                dataBuilder.append("<br/>\n");
            }
            
            final ComplexAttribute ca = (ComplexAttribute) att;
            typeBuilder.append("<ul>\n");
            for(Property property : ca.getProperties()){
                recursive(property, typeBuilder, dataBuilder , depth+1);
            }
            typeBuilder.append("</ul>\n");
            
        }else{
            typeBuilder.append("<li>\n");
            typeBuilder.append(att.getDescriptor().getName().tip().toString());
            typeBuilder.append("</li>\n");
            
            final Object value = att.getValue();
            final String valStr = toString(value);
            dataBuilder.append("<a class=\"values\" title=\"");
            dataBuilder.append(valStr);
            dataBuilder.append("\">");
            dataBuilder.append(valStr);
            dataBuilder.append("</a>");
        }
    }

    private String toString(Object value){
        String str;
        if(value == null){
            str = "null";
        }else if(value.getClass().isArray()){
            //convert to an object array
            final Object[] array = new Object[Array.getLength(value)];
            for(int i=0;i<array.length;i++){
                array[i] = toString(Array.get(value, i));
            }                
            str = Arrays.toString(array);
        }else{
            str = String.valueOf(value);
        }
        return str;
    }
    
    @Override
    protected void nextProjectedCoverage(ProjectedCoverage graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final List<Map.Entry<GridSampleDimension,Object>> covResults = FeatureInfoUtilities.getCoverageValues(graphic, context, queryArea);

        if (covResults == null) {
            return;
        }

        final String layerName = graphic.getLayer().getCoverageReference().getName().tip().toString();
        
        LayerResult result = results.get(layerName);
        if(result==null){
            //first feature of this type
            result = new LayerResult();
            result.layerName = layerName;
            results.put(layerName, result);
        }
        
        final StringBuilder typeBuilder = new StringBuilder();
        final StringBuilder dataBuilder = new StringBuilder();
        
        typeBuilder.append("<div>");
        typeBuilder.append("<div class=\"left-part\">");
        dataBuilder.append("<div class=\"right-part\">");
        typeBuilder.append("<ul>\n");
        for(Map.Entry<GridSampleDimension,Object> entry : covResults){
            typeBuilder.append("<li>\n");
            final GridSampleDimension gsd = entry.getKey();
            final InternationalString title = gsd.getDescription();
            if(title!=null){
                typeBuilder.append(title);
            }
            final Unit unit = gsd.getUnits();
            if(unit!=null){
                typeBuilder.append(unit.toString());
            }
            typeBuilder.append("</li>\n");
            
            dataBuilder.append(String.valueOf(entry.getValue()));
            dataBuilder.append("<br/>\n");
        }
        typeBuilder.append("</ul>\n");
        typeBuilder.append("</div>");
        dataBuilder.append("</div>");
        dataBuilder.append("</div>");
        
        result.values.add(typeBuilder.toString());
        result.values.add(dataBuilder.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeatureInfo(final SceneDef sdef, final ViewDef vdef, final CanvasDef cdef, final Rectangle searchArea,
                                 final GetFeatureInfo getFI) throws PortrayalException {

        //fill coverages and features maps
        getCandidates(sdef, vdef, cdef, searchArea, -1);

        final StringBuilder response = new StringBuilder();
        
        response.append("<html>\n")
                
                .append("    <head>\n")
                .append("        <title>GetFeatureInfo HTML output</title>\n")
                .append("    </head>\n")
                
                .append("    <style>\n")
                .append("ul{\n" +
                "               margin-top: 0;\n" +
                "               margin-bottom: 0px;\n" +
                "           }\n" +
                "           .left-part{\n" +
                "               display:inline-block;\n" +
                "               width:350px;\n" +
                "               overflow:auto;\n" +
                "               white-space:nowrap;\n" +
                "           }\n" +
                "           .right-part{\n" +
                "               display:inline-block;\n" +
                "               width:600px;\n" +
                "               overflow: hidden;\n" +
                "           }\n" +
                "           .values{\n" +
                "               text-overflow: ellipsis;\n" +
                "               white-space:nowrap;\n" +
                "               display:block;\n" +
                "               overflow: hidden;\n" +
                "           }")
                .append("    </style>\n")
                
                .append("    <body>\n");
        
        // optimization move this filter to getCandidates
        Integer maxValue = getFeatureCount(getFI);
        if (maxValue == null) {
            maxValue = 1;
        }

        for(LayerResult result : results.values()){
            response.append("<h2>").append(result.layerName).append("</h2>");
            response.append("<br/>");
            
            int cpt = 0;
            for (final String record : result.values) {
                response.append(record);
                cpt++;
                if (cpt >= maxValue) break;
            }
            response.append("<br/>");
        }
        
        response.append("    </body>\n")
                .append("</html>");

        return response.toString();
    }
}
