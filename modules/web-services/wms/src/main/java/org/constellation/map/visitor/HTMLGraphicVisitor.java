/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.map.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.measure.unit.Unit;

import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerDetails.TYPE;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.GridSampleDimension;

import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.opengis.feature.ComplexAttribute;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.util.InternationalString;


/**
 * Visit results of a GetFeatureInfo request, and format the output into HTML.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public final class HTMLGraphicVisitor extends TextGraphicVisitor implements GetFeatureInfoVisitor{
    
    private static final class LayerResult{
        private String layerName;
        private final List<String> values = new ArrayList<String>();
    }
    
    private final Map<String,LayerResult> results = new HashMap<String, LayerResult>();
    
    private int index = 0;
    

    public HTMLGraphicVisitor(final GetFeatureInfo gfi, List<LayerDetails> layerDetails) {
        super(gfi);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        final Integer count = gfi.getFeatureCount();
        if (count != null) {
            return (index == count);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        index++;
        
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
        
        typeBuilder.append(feature.getIdentifier().getID());
        typeBuilder.append("<div style=\"float:left;width:400px;overflow:auto\">");
        dataBuilder.append("<div style=\"float:left;width:200px;\">");
        recursive(feature, typeBuilder, dataBuilder, 0);
        typeBuilder.append("</div>");
        dataBuilder.append("</div>");
        
        result.values.add(typeBuilder.toString());
        result.values.add(dataBuilder.toString());
    }
    
    private void recursive(final Property att, final StringBuilder typeBuilder, final StringBuilder dataBuilder, int depth){
        
        if(att instanceof ComplexAttribute){
            if(depth!=0){
                typeBuilder.append("<li>\n");
                typeBuilder.append(att.getDescriptor().getName().getLocalPart());
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
            typeBuilder.append(att.getDescriptor().getName().getLocalPart());
            typeBuilder.append("</li>\n");
            
            dataBuilder.append(String.valueOf(att.getValue()));
            dataBuilder.append("<br/>\n");
        }
    }
    

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D queryArea) {
        index++;
        final List<Entry<GridSampleDimension,Object>> results = getCoverageValues(coverage, context, queryArea);

        if (results == null) {
            return;
        }

        final String layerName = coverage.getLayer().getName();
        List<String> strs = coverages.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            coverages.put(layerName, strs);
        }

        final StringBuilder builder = new StringBuilder();
        for (final Entry<GridSampleDimension,Object> entry : results) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            builder.append(value);
            final Unit unit = entry.getKey().getUnits();
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
            //builder.append(" [").append(i).append(']').append(';');
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length() - 2));

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult() {
        final StringBuilder response = new StringBuilder();
        
        response.append("<html>\n")
                
                .append("    <head>\n")
                .append("        <title>GetFeatureInfo HTML output</title>\n")
                .append("    </head>\n")
                
                .append("    <style>\n")
                .append("       ul{\n")
                .append("           margin-top: 0;\n")
                .append("           margin-bottom: 0;\n")
                .append("       }\n")
                .append("    </style>\n")
                
                .append("    <body>\n");

        for(LayerResult result : results.values()){
            response.append(result.layerName).append("<br/>");
            for (final String record : result.values) {
                response.append(record);
            }
            response.append("<br/>");
        }
        
        response.append("    </body>\n")
                .append("</html>");

        return response.toString();
    }

    @Override
    public String getMimeType() {
        return MimeType.TEXT_HTML;
    }

}
