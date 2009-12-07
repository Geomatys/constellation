/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.geotoolkit.display2d.ext.pattern.PatternSymbolizer;
import org.geotoolkit.filter.DefaultFilterFactory2;
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
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Rule;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StyleUtils {

    private static final MutableStyleFactory SF = new DefaultStyleFactory();
    private static final FilterFactory2 FF = new DefaultFilterFactory2();

    private StyleUtils(){}

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

        double from = Double.NaN;
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

}
