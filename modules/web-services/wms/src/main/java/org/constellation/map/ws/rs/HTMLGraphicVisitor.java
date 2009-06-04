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
package org.constellation.map.ws.rs;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.measure.unit.Unit;

import org.constellation.query.wms.GetFeatureInfo;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.map.FeatureMapLayer;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class HTMLGraphicVisitor extends TextGraphicVisitor{

    private final Map<String,List<String>> values = new HashMap<String,List<String>>();
    private int index = 0;

    public HTMLGraphicVisitor(final GetFeatureInfo gfi) {
        super(gfi);

        for(String key : gfi.getQueryLayers()){
            values.put(key, new ArrayList<String>());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        Integer count = gfi.getFeatureCount();
        if(count != null){
            return (index == count);
        }else{
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, Shape queryArea) {
        index++;
        final StringBuilder builder = new StringBuilder();
        final FeatureMapLayer layer = graphic.getFeatureLayer();
        final Feature feature       = graphic.getFeature();

        for(final Property prop : feature.getProperties()){
            if(prop == null) continue;
            final Name propName = prop.getName();
            if(propName == null) continue;

            if( Geometry.class.isAssignableFrom( prop.getType().getBinding() )){
                builder.append(propName.toString()).append(':').append(prop.getType().getBinding().getSimpleName()).append(';');
            }else{
                Object value = prop.getValue();
                builder.append(propName.toString()).append(':').append(value).append(';');
            }
        }

        final String result = builder.toString();
        if(builder.length() > 0 && result.endsWith(";")){
            final String layerName = layer.getName();
            List<String> strs = values.get(layerName);
            if(strs == null){
                strs = new ArrayList<String>();
                values.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length()-2));
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedCoverage coverage, Shape queryArea) {
        index++;
        final Object[][] results = getCoverageValues(coverage, queryArea);

        if(results == null) return;

        final String layerName = coverage.getCoverageLayer().getName();
        List<String> strs = values.get(layerName);
        if(strs == null){
            strs = new ArrayList<String>();
            values.put(layerName, strs);
        }

        StringBuilder builder = new StringBuilder();
        for(int i=0;i<results.length;i++){
            final Object value = results[i][0];
            final Unit unit    = (Unit)results[i][1];
            if (value == null) {
                continue;
            }
            builder.append(value);
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
            //builder.append(" [").append(i).append(']').append(';');
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length()-2));

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult(){
        final StringBuilder response = new StringBuilder();
        response.append("<html>\n")
                .append("    <head>\n")
                .append("        <title>GetFeatureInfo output</title>\n")
                .append("    </head>\n")
                .append("    <body>\n")
                .append("    <table>\n");
        for (String layer : values.keySet()) {
            response.append("       <tr>")
                    .append("           <th>").append(layer).append("</th>")
                    .append("       </tr>");
            final List<String> record = values.get(layer);

            if(record.isEmpty()){
                response.append("       <tr>")
                        .append("           <th>")
                        .append("               No values.")
                        .append("           </th>")
                        .append("       </tr>");
            }else{
                for (String value : record) {
                    response.append("       <tr>")
                            .append("           <th>")
                            .append(value)
                            .append("           </th>")
                            .append("       </tr>");
                }
            }
        }
        response.append("    </table>\n")
                .append("    </body>\n")
                .append("</html>");

        values.clear();
        return response.toString();
    }

}
