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
package org.constellation.portrayal;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.measure.unit.Unit;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReadParam;
import org.geotools.coverage.io.CoverageReader;
import org.geotools.display.canvas.GraphicVisitor;
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.primitive.GraphicFeatureJ2D;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.map.CoverageMapLayer;
import org.geotools.referencing.CRS;

import org.opengis.display.primitive.Graphic;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Simple extractor that handle basic graphic types, Feature and Coverage
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractGraphicVisitor implements GraphicVisitor {

    public abstract void visit(GraphicFeatureJ2D feature, Shape queryArea);

    public abstract void visit(GraphicJ2D graphic, CoverageMapLayer coverage, Shape queryArea);

    /**
     * {@inheritDoc }
     */
    @Override
    public void startVisit() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void endVisit() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(Graphic graphic, Shape area) {

        if(graphic == null || !(graphic instanceof GraphicJ2D)) return;

        final GraphicJ2D j2d = (GraphicJ2D) graphic;
        final Object userObj = j2d.getUserObject();

        if(j2d instanceof GraphicFeatureJ2D){
            GraphicFeatureJ2D gfj2d = (GraphicFeatureJ2D) j2d;
            visit(gfj2d, area);
        }else if(userObj instanceof CoverageMapLayer){
            CoverageMapLayer coverage = (CoverageMapLayer) userObj;
            visit(j2d, coverage, area);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        return false;
    }

    /**
     * Returns the datas of the given coverage.
     * if can not calculate values, then returns null.
     *
     * first column is the value : Float
     * second column is the unit : Unit
     */
    protected Object[][] getCoverageValues(final GraphicJ2D gra, final CoverageMapLayer layer, final Shape selectedArea){

        //find center of the selected area
        final Rectangle2D bounds2D   = selectedArea.getBounds2D();
        final double centerX         = bounds2D.getCenterX();
        final double centerY         = bounds2D.getCenterY();

        //find grid coverage
        final ReferencedCanvas2D canvas = gra.getCanvas();
        final GridCoverage2D coverage;

        final AffineTransform dispToObj;

        try{
            dispToObj = canvas.getController().getTransform().createInverse();
        }catch(NoninvertibleTransformException ex){
            ex.printStackTrace();
            return null;
        }

        if(layer.getGridCoverage2D() != null){
            coverage = layer.getGridCoverage2D();
        }else if(layer.getCoverageReader() != null){
            CoverageReader reader = layer.getCoverageReader();
            final Rectangle2D displayRect = canvas.getDisplayBounds().getBounds2D();
            final Rectangle2D objectiveRect;
            final double[] resolution = new double[2];

            try{
                objectiveRect = canvas.getObjectiveBounds().getBounds2D();
            }catch(TransformException ex){
                ex.printStackTrace();
                return null;
            }

            resolution[0] = objectiveRect.getWidth()/displayRect.getWidth();
            resolution[1] = objectiveRect.getHeight()/displayRect.getHeight();

            GeneralEnvelope env = new GeneralEnvelope(objectiveRect);
            env.setCoordinateReferenceSystem(canvas.getObjectiveCRS());

            CoverageReadParam param = new CoverageReadParam(env, resolution);

            try{
                coverage = reader.read(param);
            }catch(FactoryException ex){
                ex.printStackTrace();
                return null;
            }catch(TransformException ex){
                ex.printStackTrace();
                return null;
            }catch(IOException ex){
                ex.printStackTrace();
                return null;
            }

        }else{
            throw new IllegalArgumentException("A coverageLayer without gridcoverage2D nor coverage reader ? should not be possible.");
        }


        try {

            final CoordinateReferenceSystem dataCRS = coverage.getCoordinateReferenceSystem();
            final MathTransform objToData           = CRS.findMathTransform(canvas.getObjectiveCRS(), dataCRS,true);

            final Point2D p2d = new Point2D.Double(centerX, centerY);

            //transform to objective CRS
            dispToObj.transform(p2d, p2d);

            final GeneralDirectPosition dp = new GeneralDirectPosition(p2d);
            dp.setCoordinateReferenceSystem(canvas.getObjectiveCRS());

            //transform to coverage CRS
            objToData.transform(dp, dp);

            float[] values = new float[coverage.getNumSampleDimensions()];
            p2d.setLocation(dp.getOrdinate(0), dp.getOrdinate(1));
            values = coverage.evaluate(p2d,values);

            Object[][] results = new Object[values.length][2];
            for(int i=0; i<values.length; i++){
                final float value = values[i];
                final GridSampleDimension sample = coverage.getSampleDimension(i);
                final Unit unit = sample.getUnits();
                results[i][0] = value;
                results[i][1] = unit;
                return results;
            }

        } catch (FactoryException ex) {
            ex.printStackTrace();
        } catch (TransformException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public abstract String getResult();

}
