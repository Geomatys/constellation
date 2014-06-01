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
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.Property;
import org.geotoolkit.feature.type.Name;

import java.awt.Rectangle;
import java.util.*;
import java.util.List;
import javax.measure.unit.Unit;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.feature.ComplexAttribute;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.opengis.util.InternationalString;

/**
 * A generic FeatureInfoFormat that produce CSV output for Features and Coverages.
 * Supported mimeTypes are :
 * <ul>
 *     <li>text/plain</li>
 * </ul>
 *
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class CSVFeatureInfoFormat extends AbstractTextFeatureInfoFormat {

    private static final class LayerResult{
        private String layerName;
        private String layerType;
        private final List<String> values = new ArrayList<String>();
    }
    
    private final Map<String,LayerResult> results = new HashMap<String, LayerResult>();
        
    public CSVFeatureInfoFormat() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        
        final FeatureMapLayer layer = graphic.getLayer();
        final String layerName = layer.getName();
        
        final Feature feature = graphic.getCandidate();
        final Collection<PropertyDescriptor> descs = feature.getType().getDescriptors();
        
        LayerResult result = results.get(layerName);
        if(result==null){
            //first feature of this type
            result = new LayerResult();
            result.layerName = layerName;
            //feature type
            final StringBuilder typeBuilder = new StringBuilder();
            for(PropertyDescriptor pd : descs){
                final Name propName = pd.getName();
                typeBuilder.append(propName.toString());
                typeBuilder.append(':');
                typeBuilder.append(pd.getType().getBinding().getSimpleName());
                typeBuilder.append(';');
            }
            result.layerType = typeBuilder.toString();
            results.put(layerName, result);
        }
        
        
        //the feature values
        final StringBuilder dataBuilder = new StringBuilder();
        for(PropertyDescriptor pd : descs){
            final Property prop = feature.getProperty(pd.getName());
            if(prop instanceof ComplexAttribute){
                dataBuilder.append("...complex attribute, use GML or HTML output...");
            }else if(prop !=null){
                dataBuilder.append(String.valueOf(prop.getValue()));
            }
            dataBuilder.append(';');
        }
        result.values.add(dataBuilder.toString());
    }

    @Override
    protected void nextProjectedCoverage(ProjectedCoverage graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final List<Map.Entry<GridSampleDimension,Object>> covResults = FeatureInfoUtilities.getCoverageValues(graphic, context, queryArea);

        if (covResults == null) {
            return;
        }

        final String layerName = graphic.getLayer().getCoverageReference().getName().getLocalPart();
        
        LayerResult result = results.get(layerName);
        if(result==null){
            //first feature of this type
            result = new LayerResult();
            result.layerName = layerName;
            //coverage type
            final StringBuilder typeBuilder = new StringBuilder();
            for (final Map.Entry<GridSampleDimension,Object> entry : covResults) {
                final GridSampleDimension gsd = entry.getKey();
                                
                final InternationalString title = gsd.getDescription();
                if(title!=null){
                    typeBuilder.append(title);
                }
                final Unit unit = gsd.getUnits();
                if(unit!=null){
                    typeBuilder.append(unit.toString());
                }
                typeBuilder.append(';');
            }
            result.layerType = typeBuilder.toString();
            results.put(layerName, result);
        }
        
        //the coverage values
        final StringBuilder dataBuilder = new StringBuilder();
        for(Map.Entry<GridSampleDimension,Object> entry : covResults){
            dataBuilder.append(String.valueOf(entry.getValue()));
            dataBuilder.append(';');
        }
        result.values.add(dataBuilder.toString());
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeatureInfo(SceneDef sdef, ViewDef vdef, CanvasDef cdef, Rectangle searchArea, GetFeatureInfo getFI) throws PortrayalException {

        //fill coverages and features maps
        getCandidates(sdef, vdef, cdef, searchArea, -1);

        final StringBuilder builder = new StringBuilder();

        for(LayerResult result : results.values()){
            builder.append(result.layerName).append('\n');
            builder.append(result.layerType).append('\n');
            for (final String record : result.values) {
                builder.append(record).append('\n');
            }
            builder.append('\n');
        }
        
        results.clear();
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedMimeTypes() {
        return Collections.singletonList(MimeType.TEXT_PLAIN);
    }
}
