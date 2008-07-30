/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.lucene.Filter;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author guilhem
 */
public class SpatialFilter extends Filter {
    
    Logger logger = Logger.getLogger("net.seagis.lucene.filter");
    
    /**
     * The envelope were we search results.
     */
    private GeneralEnvelope boundingBox;
    
    /**
     * The directPosition were we search results.
     */
    private GeneralDirectPosition point ;
    
    /**
     * The line were we search results.
     */
    private Line2D line;
    
    /**
     * The Coordinate reference system of the geometry filter.
     */
    private final CoordinateReferenceSystem geometryCRS;
    
     /**
     * The name of the Coordinate reference system
     */
    private final String geometryCRSName;
    
    /**
     * The distance used in a Dwithin or Beyond spatial filter.
     */
    private Double distance;
    
    /**
     * The unit of measure for the distance.
     */
    private String distanceUnit;
    
    /**
     * The current filter type to apply.
     */
    private int filterType;

    
    public final static int CONTAINS  = 0; //ok
    public final static int INTERSECT = 1; //ok
    public final static int EQUALS    = 2; //ok
    public final static int DISJOINT  = 3; //ok
    public final static int BBOX      = 4; //ok
    public final static int BEYOND    = 5; //ok
    public final static int CROSSES   = 6; //ok       
    public final static int DWITHIN   = 7; //ok
    public final static int WITHIN    = 8; //ok
    public final static int TOUCHES   = 9; //ok
    public final static int OVERLAPS  = 10;//todo
    
    private final static List<String> supportedUnits;
    static {
        supportedUnits = new ArrayList<String>();
        supportedUnits.add("kilometers");
        supportedUnits.add("km");
        supportedUnits.add("meters");
        supportedUnits.add("m");
        supportedUnits.add("centimeters");
        supportedUnits.add("cm");
        supportedUnits.add("milimeters");
        supportedUnits.add("mm");
        supportedUnits.add("miles");
        supportedUnits.add("mi");
    }
    
    /**
     * an approximation to apply to the different filter in order to balance the lost of precision by the reprojection.
     */
    private final double precision    = 0.01;
    
    /**
     * initialize the filter with the specified geometry and filterType.
     * 
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D
     * @param filterType a Flag representing the type of spatial filter to apply (EQUALS, BBOX, CONTAINS, ...)
     */
    public SpatialFilter(Object geometry, String crsName, int filterType) throws NoSuchAuthorityCodeException, FactoryException  {
       
        if (geometry instanceof GeneralEnvelope) {
            boundingBox     = (GeneralEnvelope) geometry;
       
       } else if (geometry instanceof GeneralDirectPosition) {
            point           = (GeneralDirectPosition) geometry;
       
       } else if (geometry instanceof Line2D) {
            line           = (Line2D) geometry;
       
       } else {
           String type = "null type"; 
           if (geometry != null) 
               type = geometry.getClass().getSimpleName();
           
           throw new IllegalArgumentException("Unsupported geometry types:" + type + ".Supported ones are: GeneralEnvelope, GeneralDirectPosition, Line2D");
       }
       
       this.filterType = filterType;
       if (filterType > 10  || filterType < 0) {
           throw new IllegalArgumentException("The filterType is not valid.");
       } else if (filterType == DWITHIN || filterType == BEYOND) {
           throw new IllegalArgumentException("This filterType must be specfied with a distance and an unit.");
       }
       
       geometryCRSName = crsName;
       geometryCRS     = CRS.decode(crsName, true);
    }
    
    /**
     * initialize the filter with the specified geometry and filterType.
     * 
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D.
     * @param filterType A flag representing the type of spatial filter to apply restricted to Beyond and Dwithin.
     * @param distance   The distance to applies to this filter.
     * @param units      The unit of measure of the distance.
     */
    public SpatialFilter(Object geometry, String crsName, int filterType, Double distance, String units) throws NoSuchAuthorityCodeException, FactoryException  {
       
       this.distance = distance;
       if (!supportedUnits.contains(units)) {
           String msg = "Unsupported distance units. supported ones are: ";
           for (String s: supportedUnits) {
               msg = msg + s + ',';
           }
           msg = msg.substring(0, msg.length() - 1);
           throw new IllegalArgumentException(msg);
       } 
       this.distanceUnit = units;
       
       if (geometry instanceof GeneralEnvelope) {
            boundingBox     = (GeneralEnvelope) geometry;
       
       } else if (geometry instanceof GeneralDirectPosition) {
            point           = (GeneralDirectPosition) geometry;
       
       } else if (geometry instanceof Line2D) {
            line           = (Line2D) geometry;
       
       } else {
           throw new IllegalArgumentException("Unsupported geometry. supported ones are: GeneralEnvelope, GeneralDirectPosition, Line2D");
       }
       
       this.filterType = filterType;
       if (filterType != 5  && filterType != 7 ) {
           throw new IllegalArgumentException("The filterType is not valid: allowed ones are DWithin, Beyond");
       }
       
       geometryCRSName = crsName;
       geometryCRS     = CRS.decode(crsName, true);
    }
    
    
    @Override
    public BitSet bits(IndexReader reader) throws IOException {
        // we prepare the result
        BitSet bits = new BitSet(reader.maxDoc());
        
        TermDocs termDocs = reader.termDocs();
        
                  
        // we are searching for matching points
        termDocs.seek(new Term("geometry", "point"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralDirectPosition tempPoint = readPoint(reader.document(docNum));
            Line2D pointLine                = new Line2D.Double(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1), 
                                                                tempPoint.getOrdinate(0), tempPoint.getOrdinate(1));
            switch (filterType) {
                
                case BBOX :
                    
                    if (boundingBox != null && boundingBox.contains(tempPoint)) {
                            bits.set(docNum);
                    }
                    break;
            
                case EQUALS :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    }
                    break;
            
                case TOUCHES :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempPoint)) {
                        bits.set(docNum);
                        
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case INTERSECT :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case DISJOINT :
                
                    if (point != null && !point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && !boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && !line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case WITHIN :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                   
                    } else if (boundingBox != null && boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
            
                case CROSSES :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempPoint)) {
                       bits.set(docNum);

                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case DWITHIN :
                    
                    if (getDistance(tempPoint) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    
                    if (getDistance(tempPoint) > distance) 
                        bits.set(docNum);
                    break;
            }
        }
        
        
        //then we search for matching box
        termDocs.seek(new Term("geometry", "boundingbox"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralEnvelope tempBox = readBoundingBox(reader.document(docNum));
            if (tempBox == null)
                continue;
            switch (filterType) {

                case CONTAINS:
            
                    if (boundingBox != null && tempBox.contains(boundingBox, false)) {
                        bits.set(docNum);
                        
                    } else if (line != null && GeometricUtilities.contains(tempBox, line)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempBox.contains(point)) {
                        bits.set(docNum);
                    }
                    break;
                
                case BBOX :
                    
                    if (boundingBox != null && boundingBox.contains(tempBox, false)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case INTERSECT :
                    
                    if (boundingBox != null  && boundingBox.intersects(tempBox, false)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempBox.contains(point)){
                        bits.set(docNum);
                        
                    } else if (line != null  && GeometricUtilities.intersect(tempBox, line)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case EQUALS :
                    
                    if (boundingBox != null && boundingBox.equals(tempBox)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case DISJOINT :
                
                    if (boundingBox != null && !boundingBox.intersects(tempBox, false)) {
                        bits.set(docNum);
                        
                    } else if (point != null && !tempBox.contains(point)) {
                        bits.set(docNum);
                        
                    } else if (line != null && GeometricUtilities.disjoint(tempBox, line)) {
                        bits.set(docNum);
                        
                    }
                    break;
                
                case WITHIN :
                
                    if (boundingBox != null && boundingBox.contains(tempBox, false)) {
                        bits.set(docNum);
                    }
                    break;
                
                case CROSSES :
                
                    if (line != null && GeometricUtilities.crosses(tempBox, line)) {
                        bits.set(docNum);
                
                    } else if (point != null && GeometricUtilities.crosses(tempBox, point)) {
                        bits.set(docNum);
                    }
                    break;
                
                case TOUCHES :
                
                    if (point != null ) {
                        if (GeometricUtilities.touches(tempBox, point))
                            bits.set(docNum);
                
                    } else if (line != null && GeometricUtilities.touches(tempBox, line)) {
                        
                            bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempBox)) {
                       
                        bits.set(docNum);
                        
                    }
                    break;
                    
                case DWITHIN :
                    
                    if (getDistance(tempBox) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    
                    if (getDistance(tempBox) > distance) 
                        bits.set(docNum);
                    break;
                
                case OVERLAPS : 
                    if (boundingBox != null && GeometricUtilities.overlaps(boundingBox, tempBox)) 
                        bits.set(docNum);
                    break;
            }
        }
       
        //then we search for matching line
        termDocs.seek(new Term("geometry", "line"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            
            Line2D tempLine = readLine(reader.document(docNum));
            GeneralDirectPosition tempPoint1 = new GeneralDirectPosition(tempLine.getX1(), tempLine.getY1());
            tempPoint1.setCoordinateReferenceSystem(geometryCRS);
            GeneralDirectPosition tempPoint2 = new GeneralDirectPosition(tempLine.getX2(), tempLine.getY2());
            tempPoint2.setCoordinateReferenceSystem(geometryCRS);
            
            switch (filterType) {
                
                case BBOX :
                    
                    if (boundingBox != null && boundingBox.contains(tempPoint1) && boundingBox.contains(tempPoint2)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case INTERSECT :
                    
                    if (boundingBox != null  && GeometricUtilities.intersect(boundingBox, tempLine)) {
                        bits.set(docNum); 
                        
                    } else if (line != null  && line.intersectsLine(tempLine)){
                        bits.set(docNum);
                        
                    } else if (point != null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    }
                    break;
                
                case EQUALS :
                
                    if (line != null && GeometricUtilities.equalsLine(tempLine, line)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case CROSSES :
                    
                    if (line != null && line.intersectsLine(tempLine)) {
                        bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.crosses(boundingBox, tempLine)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                       bits.set(docNum);
                    }
                    break;
                    
                case TOUCHES :
                    
                    if (line != null && GeometricUtilities.touches(line, tempLine)) {
                    
                        bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempLine)) {
                        
                        bits.set(docNum);
                        
                    } else if (point !=null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    }
                    break;
                    
                case CONTAINS :
                    
                    if (point !=null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) { 
                        bits.set(docNum);
                        
                    } else if (line != null && tempLine.intersectsLine(line.getX1(), line.getY1(), line.getX1(), line.getY1()) && 
                                               tempLine.intersectsLine(line.getX2(), line.getY2(), line.getX2(), line.getY2())) {
                        bits.set(docNum);
                        
                    }
                    break;
                    
                case DISJOINT :
                
                    if (boundingBox != null && GeometricUtilities.disjoint(boundingBox, tempLine)) {
                        bits.set(docNum);
                        
                    } else if (point != null && !tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    
                    } else if (line != null && !line.intersectsLine(tempLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case WITHIN :
                
                    if (line != null && line.intersectsLine(tempLine.getX1(), tempLine.getY1(), tempLine.getX1(), tempLine.getY1())
                                     && line.intersectsLine(tempLine.getX2(), tempLine.getY2(), tempLine.getX2(), tempLine.getY2())) {
                        bits.set(docNum);
                
                    } else if (boundingBox != null && boundingBox.contains(tempPoint1) && boundingBox.contains(tempPoint2) ) {
                        bits.set(docNum);
                    }
                    break;
                
                case DWITHIN :
                    
                    if (getDistance(tempLine) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    if (getDistance(tempLine) > distance) 
                        bits.set(docNum);
                    break;
            }
        }
        
        
        return bits;
    }
    
    
    /**
     * Extract a boundingBox from the specified Document.
     *  
     * @param doc a Document containing a geometry of type bounding box.
     * @return a GeneralEnvelope.
     */
    private GeneralEnvelope readBoundingBox(Document doc) {
        

        double minx = Double.parseDouble(doc.getField("minx").stringValue());
        double miny = Double.parseDouble(doc.getField("miny").stringValue());
        double maxx = Double.parseDouble(doc.getField("maxx").stringValue());
        double maxy = Double.parseDouble(doc.getField("maxy").stringValue());
        String sourceCRSName = doc.getField("CRS").stringValue();
        
        double[] min = {minx, miny};
        double[] max = {maxx, maxy};
        GeneralEnvelope result = null;
        
        try {
            result = new GeneralEnvelope(min, max);
        
        } catch (IllegalArgumentException e) {
            String s = doc.getField("Title").stringValue();
        
            logger.severe("Unable to read the bouding box(minx="+ minx +" miny=" + miny + " maxx=" + maxx + " maxy=" + maxy + ")for the Document:" + s + '\n' +
                          "cause: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            if (result != null) {
                if (sourceCRSName.equals(geometryCRSName)) {
                    result.setCoordinateReferenceSystem(geometryCRS);
                
                } else {
                
                    CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                    result.setCoordinateReferenceSystem(sourceCRS);
                    String boxbefore = result.toString(); 
                    if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)) {
                        logger.finer("sourceCRS:" + sourceCRS + '\n' +
                                    "geometryCRS:" + geometryCRS + '\n' +
                                    "equals? " + CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)); 
                        result = (GeneralEnvelope) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
                        logger.finer("reprojecting from " + sourceCRSName + " to " + geometryCRSName + '\n' +
                                    "bbox before: " + boxbefore + '\n' +
                                    "bbox after : " + result.toString());
                    }
                }
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            logger.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }

        return result;
    }
    
    /**
     * Extract a Line from the specified Document.
     *  
     * @param doc a Document containing a geometry of type line.
     * @return a Line2D.
     */
    private Line2D readLine(Document doc) {
        
        double x1 = Double.parseDouble(doc.getField("x1").stringValue());
        double y1 = Double.parseDouble(doc.getField("y1").stringValue());
        double x2 = Double.parseDouble(doc.getField("x2").stringValue());
        double y2 = Double.parseDouble(doc.getField("y2").stringValue());
        String sourceCRSName = doc.getField("CRS").stringValue();
        Line2D result = new Line2D.Double(x1, y1, x2, y2);
        try {
            if (!sourceCRSName.equals(geometryCRSName)) {
                CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS))
                    result =  (Line2D) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            logger.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }
        
        return result;
    }
    
    /**
     * Extract a Point from the specified Document.
     *  
     * @param doc a Document containing a geometry of type point.
     * @return a GeneralDirectPosition.
     */
    private GeneralDirectPosition readPoint(Document doc) {
        
        double x = Double.parseDouble(doc.getField("x").stringValue());
        double y = Double.parseDouble(doc.getField("y").stringValue());
        String sourceCRSName = doc.getField("CRS").stringValue();
        GeneralDirectPosition result = new GeneralDirectPosition(y, x);
        
        try {
            if (sourceCRSName.equals(geometryCRSName)) {
                
                result.setCoordinateReferenceSystem(geometryCRS);
                
            } else {
                CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                result.setCoordinateReferenceSystem(sourceCRS);
                if (!CRS.equalsIgnoreMetadata(geometryCRS, sourceCRS)) {
                    result = (GeneralDirectPosition) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
                    result.setCoordinateReferenceSystem(geometryCRS); 
                }
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            logger.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }
        
        return result; 
    }
    
    
    /**
     * Return the orthodromic distance between two geometric object on the earth.
     * 
     * @param geometry a geometric object.
     */
    private double getDistance(final Object geometry) {
        if (geometry instanceof GeneralDirectPosition) {

            GeneralDirectPosition tempPoint = (GeneralDirectPosition) geometry;
            if (point != null) {
                return GeometricUtilities.getOrthodromicDistance(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1),
                                                                     point.getOrdinate(0),     point.getOrdinate(1), distanceUnit);

            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToPointDistance(boundingBox, tempPoint, distanceUnit);
                
            } else if (line != null) {
                return GeometricUtilities.lineToPointDistance(line, tempPoint, distanceUnit);
            } else {
                return 0;
            }
        
        } else if (geometry instanceof GeneralEnvelope) {
            
            GeneralEnvelope tempBox = (GeneralEnvelope) geometry;
            if (point != null) {
                return GeometricUtilities.BBoxToPointDistance(tempBox, point, distanceUnit);
            
            } else if (line != null) {
                return GeometricUtilities.lineToBBoxDistance(line, tempBox, distanceUnit);
                
            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToBBoxDistance(tempBox, boundingBox, distanceUnit);
            
            } else {
                return 0;
            }
        
        } else if (geometry instanceof Line2D) {
            
            Line2D tempLine = (Line2D) geometry;
            if (point != null) {
                return GeometricUtilities.lineToPointDistance(tempLine, point, distanceUnit);
            
            } else if (line != null) {
                return GeometricUtilities.lineTolineDistance(tempLine, line, distanceUnit); 
            
            } else if (boundingBox != null) {
                return GeometricUtilities.lineToBBoxDistance(tempLine, boundingBox, distanceUnit);
                
            } else {
                return 0;
            }
        } else {
            return 0;
        }   
    }
    
    /**
     * Return the current filter type.
     * 
     */
    public int getFilterType() {
        return filterType;
    }
    
    /**
     * Return the current geometry object.
     * 
     */
    public Object getGeometry() {
        if (boundingBox != null)
            return boundingBox;
        else if (line != null)
            return line;
        else if (point != null)
            return point;
        return null;
    }
    
    /**
     * Return the distance units (in case of a Distance Spatial filter).
     */
    public String getDistanceUnit() {
        return this.distanceUnit;
    }
    
    /**
     * Return the distance (in case of a Distance Spatial filter).
     */
    public Double getDistance() {
        return this.distance;
    }
            
    /**
     * Return a string description of the filter type.
     */
    public static String valueOf(final int filterType) {
        switch (filterType) {
            case 0: return "CONTAINS";
            
            case 1:  return "INTERSECT";
            case 2:  return "EQUALS";
            case 3:  return "DISJOINT";
            case 4:  return "BBOX";
            case 5:  return "BEYOND";
            case 6:  return "CROSSES";       
            case 7:  return "DWITHIN";
            case 8:  return "WITHIN";
            case 9:  return "TOUCHES";
            case 10: return "OVERLAPS";
            default: return "UNKNOW FILTER TYPE";
        }
    }
    
    /**
     * Return The flag corresponding to the specified spatial operator name.
     * 
     * @param operator The spatial operator name.
     * 
     * @return a flag.
     */
    public static int valueOf(final String operator) {
        
        if (operator.equalsIgnoreCase("Intersects")) {
            return SpatialFilter.INTERSECT;
        } else if (operator.equalsIgnoreCase("Touches")) {
            return SpatialFilter.TOUCHES;
        } else if (operator.equalsIgnoreCase("Disjoint")) {
            return SpatialFilter.DISJOINT;
        } else if (operator.equalsIgnoreCase("Crosses")) {
            return SpatialFilter.CROSSES;
        } else if (operator.equalsIgnoreCase("Contains")) {
            return SpatialFilter.CONTAINS;
        } else if (operator.equalsIgnoreCase("Equals")) {
            return SpatialFilter.EQUALS;
        } else if (operator.equalsIgnoreCase("Overlaps")) {
            return SpatialFilter.OVERLAPS;
        } else if (operator.equalsIgnoreCase("Within")) {
            return SpatialFilter.WITHIN;
        } else {
            return -1;
        }
    }
    
    /**
     * Return a String description of the filter
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[SpatialFilter]: ").append(valueOf(filterType)).append('\n');
        if (boundingBox != null) {
            s.append("geometry types: GeneralEnvelope.").append('\n').append(boundingBox);
        } else if (line != null) {
            s.append("geometry types: Line2D.").append('\n').append(GeometricUtilities.logLine2D(line));
        } else if (point != null) {
            s.append("geometry types: GeneralDirectPosition.").append('\n').append(point);
        }
        s.append("geometry CRS: ").append(geometryCRSName).append('\n');
        s.append("precision: ").append(precision).append('\n');
        if (distance != null) 
            s.append("Distance: ").append(distance).append(" ").append(distanceUnit).append('\n');
        return s.toString();
    }

}
