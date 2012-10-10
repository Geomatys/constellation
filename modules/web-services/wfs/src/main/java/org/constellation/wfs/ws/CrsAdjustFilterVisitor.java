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
package org.constellation.wfs.ws;

import com.vividsolutions.jts.geom.Geometry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.geotoolkit.filter.visitor.FillCrsVisitor;
import org.geotoolkit.geometry.DefaultBoundingBox;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CrsAdjustFilterVisitor extends DuplicatingFilterVisitor{
    
    private final CoordinateReferenceSystem baseCrs;
    private final CoordinateReferenceSystem replacementCrs;

    public CrsAdjustFilterVisitor(CoordinateReferenceSystem baseCrs, CoordinateReferenceSystem replacementCrs) {
        this.baseCrs = baseCrs;
        this.replacementCrs = replacementCrs;
    }
    
    @Override
    public Object visit(Literal expression, Object extraData) {
        Object obj = expression.getValue();
        try {
            if(obj instanceof BoundingBox){
                BoundingBox bbox = (BoundingBox) obj;
                if(CRS.equalsIgnoreMetadata(bbox.getCoordinateReferenceSystem(), baseCrs)){
                    final Envelope e = CRS.transform(bbox, replacementCrs);
                    final BoundingBox rbbox = new DefaultBoundingBox(replacementCrs);
                    rbbox.setBounds(new DefaultBoundingBox(e));
                    
                    obj = rbbox;
                }
            }else if(obj instanceof Geometry){

                Geometry geo = (Geometry) obj;
                geo = (Geometry) geo.clone();
                final CoordinateReferenceSystem geoCrs = JTS.findCoordinateReferenceSystem(geo);
                if(geoCrs == null){
                    JTS.setCRS(geo, replacementCrs);
                }else if(CRS.equalsIgnoreMetadata(geoCrs, baseCrs)){
                    geo = JTS.transform(geo, CRS.findMathTransform(baseCrs, replacementCrs));
                    JTS.setCRS(geo, replacementCrs);
                }
                obj = geo;

            }
        } catch (NoSuchAuthorityCodeException ex) {
            Logger.getLogger(FillCrsVisitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(FillCrsVisitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformException ex) {
            Logger.getLogger(FillCrsVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return getFactory(extraData).literal(obj);
    }
    
}
