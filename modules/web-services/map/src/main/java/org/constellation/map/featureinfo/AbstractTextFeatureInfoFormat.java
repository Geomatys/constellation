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

import com.vividsolutions.jts.geom.Geometry;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.Property;
import org.geotoolkit.feature.type.Name;

import javax.measure.unit.Unit;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.logging.Level;
import javax.measure.converter.ConversionException;
import javax.measure.unit.NonSI;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultCompoundCRS;
import  org.apache.sis.util.logging.Logging;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractTextFeatureInfoFormat extends AbstractFeatureInfoFormat {

    /**
     * Contains the values for all coverage layers requested.
     */
    protected final Map<String, List<String>> coverages = new HashMap<String, List<String>>();


    protected final Map<String, List<String>> features = new HashMap<String, List<String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedCoverage(ProjectedCoverage graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final List<Map.Entry<GridSampleDimension,Object>> results =
                FeatureInfoUtilities.getCoverageValues(graphic, context, queryArea);

        if (results == null) {
            return;
        }
        final CoverageReference ref = graphic.getLayer().getCoverageReference();
        final String layerName = ref.getName().getLocalPart();
        List<String> strs = coverages.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            coverages.put(layerName, strs);
        }

        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<GridSampleDimension,Object> entry : results) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            builder.append(value);
            final Unit unit = entry.getKey().getUnits();
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length() - 2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final StringBuilder builder = new StringBuilder();
        final FeatureMapLayer layer = graphic.getLayer();
        final Feature feature = graphic.getCandidate();

        for (final Property prop : feature.getProperties()) {
            if (prop == null) {
                continue;
            }
            final Name propName = prop.getName();
            if (propName == null) {
                continue;
            }

            if (Geometry.class.isAssignableFrom(prop.getType().getBinding())) {
                builder.append(propName.toString()).append(':').append(prop.getType().getBinding().getSimpleName()).append(';');
            } else {
                final Object value = prop.getValue();
                builder.append(propName.toString()).append(':').append(value).append(';');
            }
        }

        final String result = builder.toString();
        if (builder.length() > 0 && result.endsWith(";")) {
            final String layerName = layer.getName();
            List<String> strs = features.get(layerName);
            if (strs == null) {
                strs = new ArrayList<String>();
                features.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length() - 1));
        }
    }

    /**
     * Escapes the characters in a String.
     *
     * @param str
     * @return String
     */
    protected static String encodeXML(String str) {
        if (str != null && !str.isEmpty()) {
            str = str.trim();
            final StringBuffer buf = new StringBuffer(str.length() * 2);
            int i;
            for (i = 0; i < str.length(); ++i) {
                char ch = str.charAt(i);
                int intValue = (int)ch;
                String entityName = null;

                switch (intValue) {
                    case 34 : entityName = "quot"; break;
                    case 39 : entityName = "apos"; break;
                    case 38 : entityName = "amp"; break;
                    case 60 : entityName = "lt"; break;
                    case 62 : entityName = "gt"; break;
                }

                if (entityName == null) {
                    if (ch > 0x7F) {
                        buf.append("&#");
                        buf.append(intValue);
                        buf.append(';');
                    } else {
                        buf.append(ch);
                    }
                } else {
                    buf.append('&');
                    buf.append(entityName);
                    buf.append(';');
                }
            }
            return buf.toString();
        }
        return str;
    }
}
