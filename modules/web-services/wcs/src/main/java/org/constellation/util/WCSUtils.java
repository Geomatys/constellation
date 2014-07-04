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

package org.constellation.util;

import org.geotoolkit.display2d.ext.pattern.PatternSymbolizer;
import org.geotoolkit.filter.DefaultFilterFactory2;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.function.ThreshholdsBelongTo;
import org.geotoolkit.wcs.xml.RangeSubset;
import org.geotoolkit.wcs.xml.v100.IntervalType;
import org.geotoolkit.wcs.xml.v100.TypedLiteralType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Rule;
import org.opengis.style.Symbolizer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class WCSUtils {

    private static final MutableStyleFactory SF = new DefaultStyleFactory();
    private static final FilterFactory2 FF = new DefaultFilterFactory2();

    /**
     * The date format to match.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final DateFormat FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    static {
        FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    private WCSUtils(){}

    /**
     * Generates a style from a list of categories to highlight.
     *
     * @param incomingStyle The source style.
     * @param categories A list of categories to highlight in the returned style.
     * @return A style that highlights the categories selected.
     */
    public static MutableStyle filterStyle(MutableStyle style, final RangeSubset categories) throws IllegalArgumentException {

        if(style.featureTypeStyles().size() != 1) return style;
        final FeatureTypeStyle fts = style.featureTypeStyles().get(0);

        if(fts.rules().size() != 1) return style;
        final Rule rule = fts.rules().get(0);

        if(rule.symbolizers().size() != 1) return style;
        final Symbolizer symbol = rule.symbolizers().get(0);

        if(!(symbol instanceof PatternSymbolizer)) return style;
        final PatternSymbolizer patterns = (PatternSymbolizer) symbol;

        final Map<Expression,List<Symbolizer>> oldThredholds = patterns.getRanges();
        final LinkedHashMap<Expression,List<Symbolizer>> newThredholds = new LinkedHashMap<Expression, List<Symbolizer>>();
        final List<Double[]> visibles = toRanges(categories);

        double from;
        double to = Double.NaN;
        List<Symbolizer> symbols = null;
        for(final Entry<Expression,List<Symbolizer>> entry : oldThredholds.entrySet()){

            from = to;
            if(entry.getKey() != null){
                to = entry.getKey().evaluate(null,Double.class);
            }else{
                //key is null, means it's negative infinity
                to = Double.NEGATIVE_INFINITY;
            }

            append(newThredholds, visibles, from, to, symbols);
            symbols = entry.getValue();
        }

        from = to;
        to = Double.POSITIVE_INFINITY;
        append(newThredholds, visibles, from, to, symbols);

        return SF.style(new PatternSymbolizer(patterns.getChannel(), newThredholds, ThreshholdsBelongTo.PRECEDING));
    }

    private static void append(Map<Expression,List<Symbolizer>> steps, List<Double[]> visibles,
            double from, double to, List<Symbolizer> symbols){

        if(Double.isNaN(from) || Double.isNaN(to) || symbols == null) return;

        for(final Double[] interval : visibles){

            if(interval[1] < from){
                //interval is before, ignore it
            }else if (interval[0] > to){
                //interval is after, ignore it
            }else if (interval[0] <= from && interval[1] >= to){
                //interval overlaps
                steps.put(toLiteral(from), symbols);
                return;
            }else if (interval[0] >= from && interval[1] <= to){
                //interval is within
                steps.put(toLiteral(interval[0]), symbols);
                from = Math.nextUp(interval[1]);
                continue;
            }else if (interval[0] < from && interval[1] < to){
                //intersect left limit
                steps.put(toLiteral(from), symbols);
                from = Math.nextUp(interval[1]);
                continue;
            }else if (interval[0] > from && interval[1] > to){
                //intersect right limit
                steps.put(toLiteral(to), symbols);
                return;
            }else{
                throw new IllegalArgumentException("should not possibly happen");
                //another case ?

            }
        }

        //fill remaining range
        if(from != to){
            //no style for this range
            steps.put(toLiteral(from), new ArrayList<Symbolizer>());
        }


    }

    private static Literal toLiteral(Double limit){
        if(limit == Double.NEGATIVE_INFINITY){
            return null;
        }else{
            return FF.literal(limit.doubleValue());
        }
    }

    private static List<Double[]> toRanges(final RangeSubset ranges) throws IllegalArgumentException{

        if ( !(ranges instanceof org.geotoolkit.wcs.xml.v100.RangeSubsetType) ) {
            return new ArrayList<Double[]>();
        }

        final List<Double[]> exts = new ArrayList<Double[]>();

        final org.geotoolkit.wcs.xml.v100.RangeSubsetType rangeSubset =
                    (org.geotoolkit.wcs.xml.v100.RangeSubsetType) ranges;

        final List<Object> objects = rangeSubset.getAxisSubset().get(0).getIntervalOrSingleValue();

        for(Object o : objects){
            if(o instanceof IntervalType){
                final IntervalType t = (IntervalType) o;
                final Double d1 = Double.valueOf(t.getMin().getValue());
                final Double d2 = Double.valueOf(t.getMax().getValue());
                exts.add( new Double[]{d1,d2} ) ;
                
            }else if(o instanceof TypedLiteralType){
                final TypedLiteralType t = (TypedLiteralType)o;
                final Double d = Double.valueOf(t.getValue());
                exts.add( new Double[]{d,d} ) ;
            }
        }
        
        Collections.sort(exts, new Comparator<Double[]>(){
            @Override
            public int compare(Double[] t, Double[] t1) {
                double res = t[0] - t1[0];
                if(res < 0){
                    return -1;
                }else if(res > 0){
                    return 1;
                }else{
                    res = t[1] - t1[1];
                    if(res < 0){
                        return -1;
                    }else if(res > 0){
                        return 1;
                    }else{
                        return 0;
                    }
                }
            }
        });

        return exts;

    }

    /**
     * Transform a geographicBoundingBox into a list of direct positions.
     *
     * @param inputGeoBox
     * @param elevations
     * @return
     */
    public static List<DirectPositionType> buildPositions(final GeographicBoundingBox inputGeoBox, final SortedSet<Number> elevations) {
        final List<Double> pos1 = new ArrayList<Double>();
        pos1.add(inputGeoBox.getWestBoundLongitude());
        pos1.add(inputGeoBox.getSouthBoundLatitude());
        final List<Double> pos2 = new ArrayList<Double>();
        pos2.add(inputGeoBox.getEastBoundLongitude());
        pos2.add(inputGeoBox.getNorthBoundLatitude());
        if (elevations != null && elevations.size() >= 2) {
            pos1.add(elevations.first().doubleValue());
            pos2.add(elevations.last().doubleValue());
        }
        final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
        pos.add(new DirectPositionType(pos1));
        pos.add(new DirectPositionType(pos2));
        return pos;
    }
    
    public static List<Object> formatDateList(final SortedSet<Date> dates) {
        final List<Object> times = new ArrayList<Object>();
        synchronized(FORMATTER) {
            for (Date d : dates) {
                times.add(new TimePositionType(FORMATTER.format(d)));
            }
        }
        return times;
    }
}
