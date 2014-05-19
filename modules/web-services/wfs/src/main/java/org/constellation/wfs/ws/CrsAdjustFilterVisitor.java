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

    public CrsAdjustFilterVisitor(final CoordinateReferenceSystem baseCrs, final CoordinateReferenceSystem replacementCrs) {
        this.baseCrs = baseCrs;
        this.replacementCrs = replacementCrs;
    }
    
    @Override
    public Object visit(final Literal expression, final Object extraData) {
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
        } catch (FactoryException | TransformException ex) {
            Logger.getLogger(FillCrsVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return getFactory(extraData).literal(obj);
    }
    
}
