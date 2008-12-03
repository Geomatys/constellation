/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.portrayal;

import com.vividsolutions.jts.geom.Geometry;
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
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.map.CoverageMapLayer;
import org.geotools.referencing.CRS;

import org.opengis.display.primitive.Graphic;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
class MapContextInfoExtractor {


    public String getHtmlDescription(final Graphic graphic, final Shape selectedArea ) {
        final GraphicJ2D gra = (GraphicJ2D) graphic;
        final Object userObj = gra.getUserObject();

        if(userObj instanceof Feature){
            final Feature feature = (Feature) userObj;
            final StringBuilder builder = new StringBuilder();

            for(final Property prop : feature.getProperties()){
                if( Geometry.class.isAssignableFrom( prop.getType().getBinding() )){
                    builder.append("<b>").append(prop.getName().toString()).append(" : </b>").append(prop.getType().getBinding().getSimpleName()).append("<br>");
                }else{
                    builder.append("<b>").append(prop.getName().toString()).append(" : </b>").append(prop.getValue().toString()).append("<br>");
                }
            }

            return builder.toString();
        }else if(userObj instanceof CoverageMapLayer){
            final StringBuilder builder  = new StringBuilder();
            final CoverageMapLayer layer = (CoverageMapLayer) userObj;

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
                return "";
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
                    return "";
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
                    return "";
                }catch(TransformException ex){
                    ex.printStackTrace();
                    return "";
                }catch(IOException ex){
                    ex.printStackTrace();
                    return "";
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
                System.out.println("obj : "+dp);
                objToData.transform(dp, dp);
                System.out.println("data : "+dp);

                float[] values = new float[coverage.getNumSampleDimensions()];
                p2d.setLocation(dp.getOrdinate(0), dp.getOrdinate(1));
                values = coverage.evaluate(p2d,values);

                for(int i=0; i<values.length; i++){
                    final float value = values[i];
                    final GridSampleDimension sample = coverage.getSampleDimension(i);
                    final Unit unit = sample.getUnits();
                    builder.append("<b>").append(i).append(" = </b> ").append(value).append((unit == null) ? "" : unit.toString()).append("<br>");
                }

            } catch (FactoryException ex) {
                ex.printStackTrace();
            } catch (TransformException ex) {
                ex.printStackTrace();
            }

            
            return builder.toString();
        }else{
            return null;
        }

        
    }

}
