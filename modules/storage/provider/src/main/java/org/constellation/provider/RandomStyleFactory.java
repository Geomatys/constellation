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
package org.constellation.provider;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.factory.Factory;
import org.geotoolkit.factory.FactoryFinder;
//import org.geotoolkit.data.FeatureSource;

import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.AnchorPoint;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;

/**
 * Random style factory. This is a convini class if you dont need special styles.
 * This class will provide you simple et good looking styles for your maps.
 * 
 * @author Johann Sorel (Puzzle-GIS)
 */
public class RandomStyleFactory extends Factory {

    private MutableStyleFactory sf     = (MutableStyleFactory)FactoryFinder.getStyleFactory(null);
    private FilterFactory ff           = FactoryFinder.getFilterFactory(null);
    private final String[] pointShapes = {"square", "circle", "triangle", "star", "cross", "x"};
    private final int[] sizes          = {8, 10, 12, 14, 16};
    private final int[] widths         = {1, 2};
    private final Color[] colors = {
        Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY,
        Color.GRAY, Color.GREEN.darker(), Color.LIGHT_GRAY,
        Color.ORANGE, Color.RED, Color.YELLOW.darker()
    };

    //------------------duplicates----------------------------------------------
    public MutableStyle duplicate(MutableStyle style) {
        return style;
//        DuplicatingStyleVisitor xerox = new DuplicatingStyleVisitor();
//        style.accept(xerox);
//        return (Style) xerox.getCopy();
    }

    public MutableFeatureTypeStyle duplicate(MutableFeatureTypeStyle fts) {
        return fts;
//        DuplicatingStyleVisitor xerox = new DuplicatingStyleVisitor();
//        fts.accept(xerox);
//        return (FeatureTypeStyle) xerox.getCopy();
    }

    public MutableRule duplicate(MutableRule rule) {
        return rule;
//        DuplicatingStyleVisitor xerox = new DuplicatingStyleVisitor();
//        rule.accept(xerox);
//        return (Rule) xerox.getCopy();
    }

    //----------------------creation--------------------------------------------
    public PointSymbolizer createPointSymbolizer() {
        
        final Unit uom = NonSI.PIXEL;
        final String geom = StyleConstants.DEFAULT_GEOM;
        final String name = null;
        final Description desc = sf.description("title", "abs");
        
        final List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
        
        final Fill fill = sf.fill(sf.literal(randomColor()), ff.literal(0.6f) );
        final Stroke stroke = sf.stroke(randomColor(), 1);
        final Mark mark = sf.mark(ff.literal("square"), stroke, fill);
        symbols.add(mark);
        
        final Expression opa = ff.literal(1);
        final Expression size = ff.literal(randomPointSize());
        final Expression rotation = ff.literal(0);
        final AnchorPoint anchor = sf.anchorPoint(0, 0);
        final Displacement displacement = sf.displacement(0, 0);
        
        final Graphic gra = sf.graphic(symbols,opa,size,rotation,anchor,displacement);
        
        return sf.pointSymbolizer(name,geom,desc,uom,gra);
    }

    public LineSymbolizer createLineSymbolizer() {
        
        final Unit uom = NonSI.PIXEL;
        final String geom = StyleConstants.DEFAULT_GEOM;
        final String name = null;
        final Description desc = sf.description("title", "abs");
        
        final Stroke stroke = sf.stroke(randomColor(), 1);
        final Expression offset = ff.literal(0);
        
        return sf.lineSymbolizer(name,geom,desc,uom,stroke,offset);
    }

    public PolygonSymbolizer createPolygonSymbolizer() {
        
        final Unit uom = NonSI.PIXEL;
        final String geom = StyleConstants.DEFAULT_GEOM;
        final String name = null;
        final Description desc = sf.description("title", "abs");
        
        final Fill fill = sf.fill(sf.literal(randomColor()), ff.literal(0.6f) );
        final Stroke stroke = sf.stroke(randomColor(), 1);
        
        final Displacement displacement = sf.displacement(0, 0);
        final Expression offset = ff.literal(0);
        
        return sf.polygonSymbolizer(name,geom,desc,uom,stroke, fill,displacement,offset);
    }

    public RasterSymbolizer createRasterSymbolizer() {
        return sf.rasterSymbolizer();
    }

    public MutableStyle createPolygonStyle() {
        
        final PolygonSymbolizer ps = createPolygonSymbolizer();
        final MutableStyle style   = sf.style();

        style.featureTypeStyles().add(sf.featureTypeStyle(ps));

        return style;
    }

   public MutableStyle createDefaultVectorStyle(FeatureType featureType){
        MutableStyle style = null;

        Symbolizer ps = sf.polygonSymbolizer();  //createPolygonSymbolizer(randomColor(), randomWidth());

        try {
            final AttributeDescriptor att = featureType.getGeometryDescriptor();
            final AttributeType type = att.getType();

            final Class cla = type.getBinding();

            if (cla.equals(Polygon.class) || cla.equals(MultiPolygon.class)) {
                ps = sf.polygonSymbolizer();
            } else if (cla.equals(LineString.class) || cla.equals(MultiLineString.class)) {
                ps = sf.lineSymbolizer();
            } else if (cla.equals(Point.class) || cla.equals(MultiPoint.class)) {
                ps = sf.pointSymbolizer();
            }

        } catch (Exception ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }

        style = sf.style();
        style.featureTypeStyles().add(sf.featureTypeStyle(ps));

        return style;
    }
    
    public MutableStyle createRandomVectorStyle(FeatureType featureType) {
        MutableStyle style = null;

        Symbolizer ps = sf.polygonSymbolizer();  //createPolygonSymbolizer(randomColor(), randomWidth());

        try {
            final AttributeDescriptor att = featureType.getGeometryDescriptor();
            final AttributeType type = att.getType();

            final Class cla = type.getBinding();

            if (cla.equals(Polygon.class) || cla.equals(MultiPolygon.class)) {
                ps = createPolygonSymbolizer();
            } else if (cla.equals(LineString.class) || cla.equals(MultiLineString.class)) {
                ps = createLineSymbolizer();
            } else if (cla.equals(Point.class) || cla.equals(MultiPoint.class)) {
                ps = createPointSymbolizer();
            }

        } catch (Exception ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }

        style = sf.style();
        style.featureTypeStyles().add(sf.featureTypeStyle(ps));

        return style;
    }

    public MutableStyle createRasterStyle() {
        final RasterSymbolizer raster = sf.rasterSymbolizer();

        return sf.style(new Symbolizer[]{raster});
    }

    //-----------------------random---------------------------------------------
    private int randomPointSize() {
        return sizes[(int) (Math.random() * sizes.length)];
    }

    private int randomWidth() {
        return widths[(int) (Math.random() * widths.length)];
    }

    private String randomPointShape() {
        return pointShapes[(int) (Math.random() * pointShapes.length)];
    }

    private Color randomColor() {
        return colors[(int) (Math.random() * colors.length)];
    }
}
